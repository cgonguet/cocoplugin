package com.alu.oam.tools.idea.plugin.coco;

import com.smartbear.beans.ValidateBean;

import javax.swing.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 21 déc. 2007
 * Time: 10:36:45
 * To change this template use File | Settings | File Templates.
 */
public class CoCoComponentConfigurationForm {
  private JTextField urlField;
  private JTextField userLoginField;
  private JPasswordField userPasswordField;
  private JPanel mainPaneField;
  private JComboBox defectSeverityComboBox;
  private JComboBox defectTypeComboBox;
  private JCheckBox defectFixedCheckBox;
  private JComboBox reviewReleaseComboBox;
  private JComboBox reviewProductComboBox;
  private JComboBox reviewTypeComboBox;
  private JTextField teamMembersField;
  private EditableComboBox reviewModuleNameCbEdit;

  public JComponent getRootComponent() {

    setComboBoxValues(reviewProductComboBox, UserDefinedFieldsManager.getInstance().getKeys("Review-Product"));
    setComboBoxValues(reviewReleaseComboBox, UserDefinedFieldsManager.getInstance().getKeys("Review-Release"));
    setComboBoxValues(reviewTypeComboBox, UserDefinedFieldsManager.getInstance().getKeys("Review-Type"));

    setComboBoxValues(defectSeverityComboBox, UserDefinedFieldsManager.getInstance().getKeys("Defect-Severity"));
    setComboBoxValues(defectTypeComboBox, UserDefinedFieldsManager.getInstance().getKeys("Defect-Type"));

    reviewModuleNameCbEdit.init(new EditableComboBox.ComboBoxEditorValuesMgr() {

      public void set(Collection<String> values) {
        CoCoComponent.getInstance().setReviewModuleList(values);
      }

      public Collection<String> get() {
        return CoCoComponent.getInstance().getReviewModuleList();
      }
    });
    return mainPaneField;
  }

  private void setComboBoxValues(JComboBox comboBox, String[] values) {
    for (String value : values) {
      comboBox.addItem(value);
    }
  }

  public void setData(CoCoComponent data) {
    urlField.setText(data.getServerUrl());
    userLoginField.setText(data.getServerUserLogin());
    userPasswordField.setText(data.getServerUserPassword());
    reviewProductComboBox.setSelectedItem(data.getReviewProduct());
    reviewReleaseComboBox.setSelectedItem(data.getReviewRelease());
    reviewTypeComboBox.setSelectedItem(data.getReviewType());
    reviewModuleNameCbEdit.setSelectedItem(data.getReviewModule());
    defectSeverityComboBox.setSelectedItem(data.getDefectSeverity());
    defectTypeComboBox.setSelectedItem(data.getDefectType());
    defectFixedCheckBox.setSelected(data.getDefectFixed());
    teamMembersField.setText(data.getTeamMembers());

  }

  public void getData(CoCoComponent data)
          throws IOException, ValidateBean.ValidationException, GeneralSecurityException {
    data.setServerUrl(urlField.getText());
    data.setServerUserLogin(userLoginField.getText());
    data.setServerUserPassword(userPasswordField.getPassword());
    data.setDefectSeverity((String) defectSeverityComboBox.getSelectedItem());
    data.setDefectType((String) defectTypeComboBox.getSelectedItem());
    data.setDefectFixed(defectFixedCheckBox.isSelected());
    data.setReviewModule((String) reviewModuleNameCbEdit.getSelectedItem());
    data.setReviewProduct((String) reviewProductComboBox.getSelectedItem());
    data.setReviewRelease((String) reviewReleaseComboBox.getSelectedItem());
    data.setReviewType((String) reviewTypeComboBox.getSelectedItem());
    data.setTeamMembers(teamMembersField.getText());
  }

  public boolean isModified(CoCoComponent data) {
    if (urlField.getText() != null ? !urlField.getText().equals(data.getServerUrl()) : data.getServerUrl() != null) {
      return true;
    }
    if (userLoginField.getText() != null ? !userLoginField.getText().equals(data.getServerUserLogin()) :
        data.getServerUserLogin() != null) {
      return true;
    }
    if (userPasswordField.getText() != null ? !userPasswordField.getText().equals(data.getServerUserPassword()) :
        data.getServerUserPassword() != null) {
      return true;
    }
    if (defectSeverityComboBox.getSelectedItem() != null ?
        !((String) defectSeverityComboBox.getSelectedItem()).equals(data.getDefectSeverity()) :
        data.getDefectSeverity() != null) {
      return true;
    }
    if (defectTypeComboBox.getSelectedItem() != null ?
        !((String) defectTypeComboBox.getSelectedItem()).equals(data.getDefectType()) :
        data.getDefectType() != null) {
      return true;
    }
    if (defectFixedCheckBox.isSelected() != data.getDefectFixed()) {
      return true;
    }
    if (reviewModuleNameCbEdit.getSelectedItem() != null ?
        !((String) reviewModuleNameCbEdit.getSelectedItem()).equals(data.getReviewModule()) :
        data.getReviewModule() != null) {
      return true;
    }
    if (reviewProductComboBox.getSelectedItem() != null ?
        !((String) reviewProductComboBox.getSelectedItem()).equals(data.getReviewProduct()) :
        data.getReviewProduct() != null) {
      return true;
    }
    if (reviewReleaseComboBox.getSelectedItem() != null ?
        !((String) reviewReleaseComboBox.getSelectedItem()).equals(data.getReviewRelease()) :
        data.getReviewRelease() != null) {
      return true;
    }
    if (reviewTypeComboBox.getSelectedItem() != null ?
        !((String) reviewTypeComboBox.getSelectedItem()).equals(data.getReviewType()) :
        data.getReviewType() != null) {
      return true;
    }
    if (teamMembersField.getText() != null ? !teamMembersField.getText().equals(data.getTeamMembers()) :
        data.getTeamMembers() != null) {
      return true;
    }

    return false;
  }

}
