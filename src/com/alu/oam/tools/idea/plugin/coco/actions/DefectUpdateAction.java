package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.ConsoleManager;
import com.alu.oam.tools.idea.plugin.coco.DefectHighlighter;
import com.alu.oam.tools.idea.plugin.coco.dialogs.DefectDialog;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.smartbear.ccollab.datamodel.Defect;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 24 janv. 2008
 * Time: 10:31:24
 * To change this template use File | Settings | File Templates.
 */
public class DefectUpdateAction extends AnAction {
  private final String filePath;
  private final Defect defect;
  private boolean hasBeenDeleted = false;

  public DefectUpdateAction(String filePath, Defect defect) {
    this.filePath = filePath;
    this.defect = defect;
  }

  public void actionPerformed(AnActionEvent e) {
    Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
    updateDefectWithDialog(project);
    ConsoleManager.getInstance(project).refresh();
  }

  public void updateDefectWithDialog(Project project) {
    try {
      DefectDialog dialog = new DefectDialog(filePath, defect);
      dialog.execute();
      if (!dialog.isOK()) {
        if (dialog.shouldBeDeleted()) {
          if (MessagesUtils.proceed("Delete " + defect)) {
            CodeCollaboratorManager.getInstance().removeDefect(filePath, defect);
            hasBeenDeleted = true;
            new DefectHighlighter(project).unregister();
            new DefectHighlighter(project).register();
          }
        }
        return;
      }

      String comment = dialog.getText();
      String severity = dialog.getSeverity();
      String type = dialog.getType();
      boolean markAsFixed = dialog.getMarkAsFixed();
      CodeCollaboratorManager.getInstance().updateDefect(filePath, defect, comment, severity, type, markAsFixed);
    } catch (Exception ex) {
      MessagesUtils.error(ex);
    }
  }

  public boolean defectDeleted() {
    return hasBeenDeleted;
  }
}
