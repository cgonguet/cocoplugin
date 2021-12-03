package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.ConsoleManager;
import com.alu.oam.tools.idea.plugin.coco.ProgressCommandManager;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.alu.oam.tools.idea.plugin.coco.dialogs.ReviewUploadChangeListDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.smartbear.ccollab.datamodel.Review;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 13 déc. 2007
 * Time: 16:22:07
 * To change this template use File | Settings | File Templates.
 */
public class ReviewUploadChangesAction extends AbstractAction {

  public ReviewUploadChangesAction() {
  }

  public ReviewUploadChangesAction(Project project) {
    super(project);
  }

  public void proceed() {
    try {
      Review currentReview = CodeCollaboratorManager.getInstance().getCurrentReview();
      if (currentReview == null) {
        MessagesUtils.error(MessagesUtils.NO_CURRENT_REVIEW);
        return;
      }

      if (uploadChangeList(project, "Upload change list")) {
        MessagesUtils.done(currentReview, "uploaded with change list");
        ConsoleManager.getInstance(project).refresh();
      }

    } catch (Exception ex) {
      MessagesUtils.error(ex);
    }
  }

  public static boolean uploadChangeList(final Project project, String title) throws Exception {
    ReviewUploadChangeListDialog dialog =
            new ReviewUploadChangeListDialog(ChangeListManager.getInstance(project).getChangeLists(),
                                             ChangeListManager.getInstance(project).getDefaultChangeList());
    dialog.execute();
    final LocalChangeList selectedchangeList = dialog.getChangeListSelected();
    if (!dialog.isOK() || selectedchangeList == null) {
      return false;
    }

    ProgressCommandManager commandManager = new ProgressCommandManager(project);
    boolean status = commandManager.execute(title, new ProgressCommandManager.ProgressRunnable() {

      public boolean run(IProgressMonitor progressMintor) throws Exception {
        CodeCollaboratorManager.getInstance().attachChangeList(project,
                                                               selectedchangeList.getChanges(),
                                                               selectedchangeList.getComment(),
                                                               progressMintor);
        return true;

      }
    });
    return status;
  }

}
