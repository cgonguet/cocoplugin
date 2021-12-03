package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ReviewAddFile extends AnAction {

  public void actionPerformed(AnActionEvent e) {
    Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
    try {
      VirtualFile[] selectedFiles = (VirtualFile[]) e.getDataContext().getData(DataConstants.VIRTUAL_FILE_ARRAY);
      for (VirtualFile file : selectedFiles) {
        if (!file.isDirectory()) {
          CodeCollaboratorManager.getInstance().addBufferedFile(project, file.getPath());
        }
      }

      MessagesUtils.info("Files are buffered.\nYou can upload them with the 'Upload files' action.");
    } catch (Exception ex) {
      MessagesUtils.error(ex);
    }
  }

  public void update(AnActionEvent e) {
    e.getPresentation().setEnabled(CodeCollaboratorManager.reviewInProgress);
  }
}
