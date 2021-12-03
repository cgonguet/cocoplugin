package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.ConsoleManager;
import com.alu.oam.tools.idea.plugin.coco.DefectHighlighter;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.intellij.openapi.project.Project;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 7 janv. 2008
 * Time: 13:45:00
 * To change this template use File | Settings | File Templates.
 */
public class ReviewCloseAction extends AbstractAction {

  public ReviewCloseAction() {
  }

  public ReviewCloseAction(Project project) {
    super(project);
  }

  public void proceed() {
    try {
      if (CodeCollaboratorManager.getInstance().getCurrentReview() == null) {
        MessagesUtils.error(MessagesUtils.NO_CURRENT_REVIEW);
        return;
      }

      new DefectHighlighter(project).unregister();
      ConsoleManager.getInstance(project).close();
      CodeCollaboratorManager.getInstance().finished();
    } catch (Exception ex) {
      MessagesUtils.error(ex);
    }
  }

}
