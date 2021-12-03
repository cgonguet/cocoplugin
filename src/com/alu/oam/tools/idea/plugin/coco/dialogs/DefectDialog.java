package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.alu.oam.tools.idea.plugin.coco.CoCoComponent;
import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.DefectHighlighter;
import com.alu.oam.tools.idea.plugin.coco.UserDefinedFieldsManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.smartbear.ccollab.datamodel.Assignment;
import com.smartbear.ccollab.datamodel.Defect;
import com.smartbear.ccollab.datamodel.Role;
import org.jetbrains.annotations.Nullable;
import sun.awt.VerticalBagLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 21 déc. 2007
 * Time: 11:15:16
 * To change this template use File | Settings | File Templates.
 */
public class DefectDialog extends DialogWrapper {
  protected JPanel userPane;
  private String filePath;
  private String lineNumber;
  private String comment;
  protected JTextArea textArea;
  private JComboBox creatorComboBox;
  protected JComboBox severityComboBox;
  protected JComboBox typeComboBox;
  protected JCheckBox fixedCheckBox;
  String defectSeverity;
  String defectType;
  boolean defectFixed;
  private boolean updateMode = false;
  private Integer defectId;
  private boolean shouldBeDeleted = false;
  private String defectCreator;

  public DefectDialog(String filePath, String lineNumber, String comment) {
    super(false);
    this.comment = comment;
    this.lineNumber = lineNumber;
    this.filePath = filePath;
    this.defectSeverity = CoCoComponent.getInstance().getDefectSeverity();
    this.defectType = CoCoComponent.getInstance().getDefectType();
    this.defectFixed = CoCoComponent.getInstance().getDefectFixed();
  }

  public DefectDialog(String filePath, Defect defect) {
    super(false);
    this.comment = defect.getText();
    this.lineNumber = String.valueOf(DefectHighlighter.getDefectLine(defect));
    this.filePath = filePath;
    try {
      this.defectSeverity = defect.getUserDefinedFields().getSelectItem("Severity").getDisplayName();
      this.defectType = defect.getUserDefinedFields().getSelectItem("Type").getDisplayName();
    } catch (Throwable e) {
      CodeCollaboratorManager.LOGGER.error(e);
    }
    this.defectFixed = defect.isMarkedFixed();
    this.updateMode = true;
    this.defectId = defect.getId();
    this.defectCreator = defect.getCreator().getLogin();
  }

  @Nullable
  protected JComponent createCenterPanel() {

    if (updateMode) {
      setTitle("Update defect ID: " + defectId);
    } else {
      setTitle("Create defect");
    }

    userPane = new JPanel(new BorderLayout());

    JPanel northPane = new JPanel(new VerticalBagLayout());
    JPanel filenamePane = new JPanel(new BorderLayout());
    filenamePane.add(new JLabel("File: " + filePath));
    northPane.add(filenamePane);

    JPanel linePane = new JPanel(new BorderLayout());
    linePane.add(new JLabel("Line: " + lineNumber));
    northPane.add(linePane);


    JPanel creatorPane = new JPanel(new FlowLayout());
    creatorPane.add(new JLabel("Creator"));
    creatorComboBox = new JComboBox();
    try {
      if (defectCreator == null) {
        String defaultReviewer = null;
        for (Assignment user : CodeCollaboratorManager.getInstance().getCurrentReview().getAssignments()) {
          creatorComboBox.addItem(user.getUser().getLogin());
          if (defaultReviewer == null && user.getRole().getId().equals(Role.REVIEWER_ID)) {
            defaultReviewer = user.getUser().getLogin();
          }
        }
//        creatorComboBox.setSelectedItem(CodeCollaboratorManager.getInstance().getUser().getLogin());
        creatorComboBox.setSelectedItem(defaultReviewer);
      } else {
        creatorComboBox.addItem(defectCreator);
        creatorComboBox.setSelectedItem(defectCreator);
      }
    } catch (Exception e) {
      CodeCollaboratorManager.LOGGER.error(e);
    }
    creatorPane.add(creatorComboBox);
    northPane.add(creatorPane);

    userPane.add(northPane, BorderLayout.NORTH);

    textArea = new JTextArea(comment, 30, 80);
    userPane.add(new JScrollPane(textArea), BorderLayout.CENTER);

    JPanel southPane = new JPanel(new GridBagLayout());
    MyGridBagConstraints c = new MyGridBagConstraints();

    severityComboBox = new JComboBox(UserDefinedFieldsManager.getInstance().getKeys("Defect-Severity"));
    typeComboBox = new JComboBox(UserDefinedFieldsManager.getInstance().getKeys("Defect-Type"));
    severityComboBox.setSelectedItem(defectSeverity);
    typeComboBox.setSelectedItem(defectType);

    southPane.add(new JLabel("Severity"), c.get(0, 0, 1, 1));
    southPane.add(severityComboBox, c.get(1, 0, 2, 1));
    southPane.add(new JLabel("Type"), c.get(0, 1, 1, 1));
    southPane.add(typeComboBox, c.get(1, 1, 2, 1));

    JPanel fixedPane = new JPanel(new BorderLayout());
    fixedCheckBox = new JCheckBox("Mark as fixed", defectFixed);
    fixedPane.add(fixedCheckBox, BorderLayout.WEST);
    southPane.add(fixedCheckBox, c.get(0, 2, 2, 1));
    userPane.add(southPane, BorderLayout.SOUTH);

    return userPane;
  }


  protected Action[] createLeftSideActions() {
    if (updateMode) {
      return new Action[]{new DeleteAction()};
    }
    return new Action[0];
  }

  class DeleteAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      shouldBeDeleted = true;
      close(NEXT_USER_EXIT_CODE);
    }

    public DeleteAction() {
      super();
      putValue("Name", "Delete");
      putValue("DefaultAction", Boolean.FALSE);
    }
  }

  class MyGridBagConstraints extends GridBagConstraints {
    public GridBagConstraints get(int gx, int gy, int gw, int gh) {
      this.gridx = gx;
      this.gridy = gy;
      this.gridwidth = gw;
      this.gridheight = gh;
      return this;
    }
  }

  public void execute() throws Exception {
    init();
    show();
  }

  public String getText() {
    return textArea.getText();
  }

  public String getSeverity() {
    return (String) severityComboBox.getSelectedItem();
  }

  public boolean getMarkAsFixed() {
    return fixedCheckBox.isSelected();
  }

  public String getType() {
    return (String) typeComboBox.getSelectedItem();
  }

  public boolean shouldBeDeleted() {
    return shouldBeDeleted;
  }

  public String getCreator() {
    return (String) creatorComboBox.getSelectedItem();
  }
}
