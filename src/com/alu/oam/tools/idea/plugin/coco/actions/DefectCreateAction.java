package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.*;
import com.alu.oam.tools.idea.plugin.coco.dialogs.DefectDialog;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.smartbear.ccollab.datamodel.Defect;
import org.eclipse.core.runtime.IProgressMonitor;

import java.io.File;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 7 janv. 2008
 * Time: 13:45:00
 * To change this template use File | Settings | File Templates.
 */
public class DefectCreateAction extends AbstractAction {

  public DefectCreateAction() {
  }

  public DefectCreateAction(Project project) {
    super(project);
  }

  public void proceed() {
    try {
      if (CodeCollaboratorManager.getInstance().getCurrentReview() == null) {
        MessagesUtils.error(MessagesUtils.NO_CURRENT_REVIEW);
        return;
      }

      FileEditorManager manager = FileEditorManager.getInstance(project);
      Editor editor = manager.getSelectedTextEditor();
      VirtualFile files[] = manager.getSelectedFiles();
      VirtualFile vAbsFile = null;
      if (files != null && files.length != 0) {
        vAbsFile = files[0];
      }

      if (vAbsFile == null || editor == null) {
        MessagesUtils.error("Please select a file before creating a defect.");
        return;
      }

      final String absFilePath = vAbsFile.getPath();
      final String filePath = FilePathUtils.getRelativePath(project, absFilePath);

      if (!CodeCollaboratorManager.getInstance().isAllowedFile(filePath)) {

        if (!MessagesUtils.proceed("File " + vAbsFile.getName() + " is not part of the review.\n"
                                   + "It should be uploaded.")) {
          return;
        }

        ProgressCommandManager commandManager = new ProgressCommandManager(project);
        commandManager.execute("Upload file", new ProgressCommandManager.ProgressRunnable() {
          public boolean run(IProgressMonitor progressMonitor) throws Exception {

            CodeCollaboratorManager.getInstance().attachFileList(project,
                                                                 Collections.singleton(
                                                                         FilePathUtils.getRelativePathToProject(project,
                                                                                                                absFilePath)),
                                                                 progressMonitor);
            return true;
          }
        });
        ConsoleManager.getInstance(project).refresh();
        MessagesUtils.info("The file has been uploaded");
      }

      int line = 0;
      String selectedText = null;
      if (editor != null && editor.getSelectionModel() != null) {
        line = getCurrentLine(editor);
        selectedText = editor.getSelectionModel().getSelectedText();
      }
      String description = "";
      if (selectedText != null) {
        description += selectedText + "\n";
      }

      DefectDialog dialog = new DefectDialog(filePath,
                                             String.valueOf(line),
                                             description);
      dialog.execute();
      if (!dialog.isOK()) {
        return;
      }

      String creator = dialog.getCreator();
      String comment = dialog.getText();
      String severity = dialog.getSeverity();
      String type = dialog.getType();
      boolean markAsFixed = dialog.getMarkAsFixed();
      Defect defect =
              CodeCollaboratorManager.getInstance()
                      .createDefect(project, filePath, line, creator, comment, markAsFixed, severity, type);
      MessagesUtils.info("Defect ID: " + defect.getName());

      new DefectHighlighter(project).highlightDefect(defect, editor.getMarkupModel(), vAbsFile.getPath());
      ConsoleManager.getInstance(project).refresh();

    } catch (Exception ex) {
      MessagesUtils.error(ex);
    }
  }

  private int getCurrentLine(Editor editor) {
    int startOffset = editor.getSelectionModel().getSelectionStart();
    return editor.getDocument().getLineNumber(startOffset) + 1;
  }

  private Change getCurrentFileChange(Project project, File currentFile) {
    LocalChangeList changeList = ChangeListManager.getInstance(project).getDefaultChangeList();
    for (Change change : changeList.getChanges()) {
      if (change.affectsFile(currentFile)) {
        return change;
      }
    }
    return null;
  }

}
