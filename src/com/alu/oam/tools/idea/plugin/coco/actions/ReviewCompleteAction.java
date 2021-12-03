package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.ConsoleManager;
import com.alu.oam.tools.idea.plugin.coco.DefectHighlighter;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.intellij.openapi.project.Project;
import com.smartbear.ccollab.datamodel.Review;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 10 janv. 2008
 * Time: 11:47:18
 * To change this template use File | Settings | File Templates.
 */
public class ReviewCompleteAction extends AbstractAction {

  public ReviewCompleteAction() {
  }

  public ReviewCompleteAction(Project project) {
    super(project);
  }

  public void proceed() {
    try {
      Review currentReview = CodeCollaboratorManager.getInstance().getCurrentReview();
      if (currentReview == null) {
        MessagesUtils.error(MessagesUtils.NO_CURRENT_REVIEW);
        return;
      }

      if (!MessagesUtils.proceed("Terminate " + currentReview.getDisplayText(true))) {
        return;
      }

      CodeCollaboratorManager.getInstance().completeReview();
      new DefectHighlighter(project).unregister();
      ConsoleManager.getInstance(project).close();
      MessagesUtils.done(currentReview, "completed");

    } catch (Exception ex) {
      MessagesUtils.error(ex);
    }
  }
}
