package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.ConsoleManager;
import com.alu.oam.tools.idea.plugin.coco.DefectHighlighter;
import com.alu.oam.tools.idea.plugin.coco.ProgressCommandManager;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.alu.oam.tools.idea.plugin.coco.dialogs.ReviewOpenDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.smartbear.ccollab.datamodel.Review;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 7 janv. 2008
 * Time: 13:53:16
 * To change this template use File | Settings | File Templates.
 */
public class ReviewOpenAction extends AbstractAction {
  private boolean confirm;
  private List<Review> reviews;

  public ReviewOpenAction() {
    confirm = true;
  }

  public ReviewOpenAction(Project project) {
    super(project);
    this.confirm = false;
  }

  public void proceed() {
    try {
      ReviewOpenDialog dialog = new ReviewOpenDialog(getReviews());
      dialog.execute();
      String reviewId = dialog.getReviewSelected();
      if (!dialog.isOK() || reviewId == null) {
        return;
      }

      new DefectHighlighter(project).unregister();
      ConsoleManager.getInstance(project).close();

      Review review = CodeCollaboratorManager.getInstance().switchReview(getReviewById(Integer.parseInt(reviewId)));
      if (confirm) {
        MessagesUtils.done(review, "opened");
      }
      new DefectHighlighter(project).register();

      ConsoleManager.getInstance(project).open();

    } catch (Exception ex) {
      MessagesUtils.error(ex);
    }
  }

  private List<Review> getReviews() throws Exception {

    ProgressCommandManager commandManager = new ProgressCommandManager(project);

    commandManager.execute("Open review", new ProgressCommandManager.ProgressRunnable() {

      public boolean run(IProgressMonitor monitor) throws Exception {
        monitor.beginTask("Open review", 0);
        monitor.subTask("Connect to server");
        CodeCollaboratorManager mgr = CodeCollaboratorManager.getInstance();
        monitor.subTask("Get reviews");
        reviews = mgr.getReviews(
                ChangeListManager.getInstance(project).getDefaultChangeList());
        return true;
      }
    });
    return reviews;
  }

  private Review getReviewById(Integer id) {
    for (Review review : reviews) {
      if (review.getId().equals(id)) {
        return review;
      }
    }
    return null;
  }
}