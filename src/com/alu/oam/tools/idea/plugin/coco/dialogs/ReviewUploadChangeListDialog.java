package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.util.ui.BlockBorder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 21 déc. 2007
 * Time: 11:15:16
 * To change this template use File | Settings | File Templates.
 */
public class ReviewUploadChangeListDialog extends DialogWrapper {
  protected JPanel userPane;
  private Collection<LocalChangeList> changeLists;
  private LocalChangeList defaultChangeList;
  private JComboBox changeListComboBox;
  private JTextArea changeListInfo;

  public ReviewUploadChangeListDialog(java.util.List<LocalChangeList> changeLists, LocalChangeList defaultChangeList) {
    super(false);
    this.changeLists = changeLists;
    this.defaultChangeList = defaultChangeList;
  }

  @Nullable
  protected JComponent createCenterPanel() {
    setTitle("Upload change list");
    userPane = new JPanel(new BorderLayout());
    userPane.add(new JLabel("Select the change list to upload:"), BorderLayout.NORTH);
    changeListComboBox = new JComboBox();
    userPane.add(changeListComboBox, BorderLayout.CENTER);
    changeListInfo = new JTextArea(3, 40);
    changeListInfo.setBorder(new BlockBorder());
    changeListInfo.setEditable(false);
    userPane.add(changeListInfo, BorderLayout.SOUTH);

    changeListComboBox.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent actionEvent) {
        LocalChangeList selected = getChangeListSelected();
        changeListInfo.setEditable(true);
        changeListInfo.removeAll();
        changeListInfo.setText("Name: " + selected.getName() + "\n" +
                               "Description: " + selected.getComment() + "\n" +
                               "Nb changes: " + selected.getChanges().size());
        changeListInfo.setEditable(false);
      }
    });

    for (ChangeList changeList : changeLists) {
      changeListComboBox.addItem(changeList.getName());
    }
    changeListComboBox.setSelectedItem(defaultChangeList.getName());

    return userPane;
  }

  public void execute() throws Exception {
    init();
    show();
  }

  public LocalChangeList getChangeListSelected() {
    String name = (String) changeListComboBox.getSelectedItem();
    for (LocalChangeList changeList : changeLists) {
      if (changeList.getName().equals(name)) {
        return changeList;
      }
    }
    return null;
  }


}
