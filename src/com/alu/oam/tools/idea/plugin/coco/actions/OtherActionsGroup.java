package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class OtherActionsGroup extends DefaultActionGroup {


  public void beforeActionPerformedUpdate(AnActionEvent e) {
    update(e);
  }

  public void update(AnActionEvent e) {
    e.getPresentation().setEnabled(CodeCollaboratorManager.reviewInProgress);
  }
}
