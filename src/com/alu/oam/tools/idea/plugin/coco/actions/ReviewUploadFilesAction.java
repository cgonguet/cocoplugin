package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.ConsoleManager;
import com.alu.oam.tools.idea.plugin.coco.ProgressCommandManager;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.alu.oam.tools.idea.plugin.coco.dialogs.UploadFileListDialog;
import com.intellij.openapi.project.Project;
import com.smartbear.ccollab.datamodel.Review;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 13 déc. 2007
 * Time: 16:22:07
 * To change this template use File | Settings | File Templates.
 */
public class ReviewUploadFilesAction extends AbstractAction {


  public ReviewUploadFilesAction() {
  }

  public ReviewUploadFilesAction(Project project) {
    super(project);
  }

  public void proceed() {
    try {

      Review currentReview = CodeCollaboratorManager.getInstance().getCurrentReview();
      if (currentReview == null) {
        MessagesUtils.error(MessagesUtils.NO_CURRENT_REVIEW);
        return;
      }

      UploadFileListDialog dialog = new UploadFileListDialog(project);
      dialog.execute();
      ArrayList<String> fileList = dialog.getFileList();
      if (!dialog.isOK() || fileList.isEmpty()) {
        return;
      }

      uploadFileList(project, "Upload file list", fileList);
      CodeCollaboratorManager.getInstance().clearBufferedFiles();

      MessagesUtils.done(currentReview, "uploaded with file list");
      ConsoleManager.getInstance(project).refresh();
    } catch (Exception ex) {
      MessagesUtils.error(ex);
    }
  }

  public static void uploadFileList(final Project project, String title, final List<String> fileList) throws Exception {
    ProgressCommandManager commandManager = new ProgressCommandManager(project);
    commandManager.execute(title, new ProgressCommandManager.ProgressRunnable() {

      public boolean run(IProgressMonitor progressMintor) throws Exception {
        CodeCollaboratorManager.getInstance().attachFileList(project, fileList, progressMintor);
        return true;

      }
    });
  }

}
