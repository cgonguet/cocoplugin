package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.ConsoleManager;
import com.alu.oam.tools.idea.plugin.coco.DefectHighlighter;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.alu.oam.tools.idea.plugin.coco.dialogs.ReviewCreateDialog;
import com.intellij.openapi.project.Project;
import com.smartbear.ccollab.datamodel.Review;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 13 déc. 2007
 * Time: 16:22:07
 * To change this template use File | Settings | File Templates.
 */
public class ReviewCreateAction extends AbstractAction {

  public ReviewCreateAction() {
  }

  public ReviewCreateAction(Project project) {
    super(project);
  }

  public void proceed() {
    new Runner(project, new UploadActionRunner() {

      public void run(Project project) throws Exception {
        ReviewUploadChangesAction.uploadChangeList(project, "Create review");
      }
    }).run();
  }

  public static class Runner implements Runnable {

    Project project;
    private UploadActionRunner uploadActionRunner;

    public Runner(Project project, UploadActionRunner uploadActionRunner) {
      this.project = project;
      this.uploadActionRunner = uploadActionRunner;
    }

    public void run() {
      try {
        CodeCollaboratorManager.getInstance();
      } catch (Exception e) {
        MessagesUtils.error("Server error: " + e.getMessage());
        return;
      }


      try {
        ReviewCreateDialog dialog = new ReviewCreateDialog(project);
        dialog.execute();
        if (!dialog.isOK()) {
          return;
        }

        new DefectHighlighter(project).unregister();
        ConsoleManager.getInstance(project).close();

        Review review =
                CodeCollaboratorManager.getInstance().createReview(dialog.getReviewTitle(),
                                                                   dialog.getReviewActivity(),
                                                                   dialog.getReviewProduct(),
                                                                   dialog.getReviewRelease(),
                                                                   dialog.getReviewType(),
                                                                   dialog.getReviewModule());
        CodeCollaboratorManager.getInstance().addParticipant(dialog.getModerator(), dialog.getReviewer());

        try {
          uploadActionRunner.run(project);
        } catch (Exception e1) {
          CodeCollaboratorManager.LOGGER.error(e1);
          MessagesUtils.warning("Review " + review.getId() + " is now created but the changeset upload has failed");
          new DefectHighlighter(project).register();
          ConsoleManager.getInstance(project).open();
          return;
        }

        MessagesUtils.done(review, "created");
        new DefectHighlighter(project).register();
        ConsoleManager.getInstance(project).open();
      } catch (Exception ex) {
        MessagesUtils.error(ex);
      }
    }
  }

  public interface UploadActionRunner {
    void run(Project project) throws Exception;
  }
}
