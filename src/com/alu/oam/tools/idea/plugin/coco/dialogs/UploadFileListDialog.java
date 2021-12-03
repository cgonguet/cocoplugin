package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.FilePathUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 21 déc. 2007
 * Time: 11:15:16
 * To change this template use File | Settings | File Templates.
 */
public class UploadFileListDialog extends DialogWrapper {
  protected JTextArea textArea;
  protected JPanel userPane;
  private Project project;

  public UploadFileListDialog(Project project) {
    super(false);
    this.project = project;
  }

  @Nullable
  protected JComponent createCenterPanel() {
    setTitle("Upload a list of files");
    userPane = new JPanel(new BorderLayout(1, 1));
    userPane.add(new JLabel("Enter list of files to upload (one by line with a path relative to: '" +
                            FilePathUtils.getProjectPath(project) + "')"), BorderLayout.NORTH);
    try {
      String bufferedFiles = CodeCollaboratorManager.getInstance().getBufferedFiles();
      textArea = new JTextArea(bufferedFiles, 30, 70);
    } catch (Exception e) {
      CodeCollaboratorManager.LOGGER.warn(e);
    }

    textArea.setEditable(true);
    userPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
    return userPane;
  }

  public void execute() throws Exception {
    init();
    show();
  }

  public ArrayList<String> getFileList() {
    ArrayList<String> fileList = new ArrayList<String>();
    for (String line : textArea.getText().split("\n")) {
      fileList.add(line);
    }
    return fileList;
  }

}
