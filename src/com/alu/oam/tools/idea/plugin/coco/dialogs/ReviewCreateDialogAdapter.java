package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.alu.oam.tools.idea.plugin.coco.CoCoComponent;
import com.alu.oam.tools.idea.plugin.coco.UserDefinedFieldsManager;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 21 déc. 2007
 * Time: 11:15:16
 * To change this template use File | Settings | File Templates.
 */
public class ReviewCreateDialogAdapter implements DialogAdapter {

  protected JPanel userPane;
  JTextField reasonTextField;
  JLabel authorLabel;
  JComboBox productComboBox;
  JComboBox releaseComboBox;
  JComboBox moduleComboBox;
  JComboBox typeComboBox;
  TitleManager titleManager;
  JComboBox moderatorUsersList;
  JComboBox reviewerUsersList;

  public JComponent getUserPanel() {
    userPane = new JPanel(new GridBagLayout());
    MyGridBagConstraints mainConstraints = new MyGridBagConstraints();

    titleManager = createTitleManager();
    userPane.add(new JLabel("Title"), mainConstraints.addLine(1, 2));
    userPane.add(titleManager.getLabel(), mainConstraints.addCol(2));

    userPane.add(new JLabel("Product"), mainConstraints.addLine(1, 1));
    productComboBox = new JComboBox(UserDefinedFieldsManager.getInstance().getKeys("Review-Product"));
    productComboBox.addActionListener(titleManager);
    userPane.add(productComboBox, mainConstraints.addCol(2));

    userPane.add(new JLabel("Release"), mainConstraints.addLine(1, 1));
    releaseComboBox = new JComboBox(UserDefinedFieldsManager.getInstance().getKeys("Review-Release"));
    releaseComboBox.addActionListener(titleManager);
    userPane.add(releaseComboBox, mainConstraints.addCol(2));

    userPane.add(new JLabel("Module"), mainConstraints.addLine(1, 1));
    moduleComboBox = new JComboBox();
    for (String item : CoCoComponent.getInstance().getReviewModuleList()) {
      moduleComboBox.addItem(item);
    }

    moduleComboBox.addActionListener(titleManager);
    userPane.add(moduleComboBox, mainConstraints.addCol(2));

    userPane.add(new JLabel("Type"), mainConstraints.addLine(1, 1));
    typeComboBox = new JComboBox(UserDefinedFieldsManager.getInstance().getKeys("Review-Type"));
    userPane.add(typeComboBox, mainConstraints.addCol(2));

    userPane.add(new JLabel("ReasonId(s)"), mainConstraints.addLine(1, 1));
    reasonTextField = new JTextField(60);
    reasonTextField.addCaretListener(titleManager);
    reasonTextField.setToolTipText("ReasonId1(-ReasonId2- ...)");
    userPane.add(reasonTextField, mainConstraints.addCol(2));

    userPane.add(new JSeparator(), mainConstraints.addLine(3, 2));

    userPane.add(new JLabel("Author"), mainConstraints.addLine(1, 1));
    authorLabel = new JLabel();
    userPane.add(authorLabel, mainConstraints.addCol(2));

    userPane.add(new JLabel("Moderator"), mainConstraints.addLine(1, 1));
    moderatorUsersList = new JComboBox();
    moderatorUsersList.setEditable(true);
    userPane.add(moderatorUsersList, mainConstraints.addCol(2));

    userPane.add(new JLabel("Reviewer"), mainConstraints.addLine(1, 1));
    reviewerUsersList = new JComboBox();
    reviewerUsersList.setEditable(true);
    userPane.add(reviewerUsersList, mainConstraints.addCol(2));

    return userPane;
  }

  TitleManager createTitleManager() {
    return new TitleManager();
  }

  public void setTeamMembers(Collection<String> members) {
    for (String member : members) {
      moderatorUsersList.addItem(member);
      reviewerUsersList.addItem(member);
    }
    if (members.size() > 1) {
      reviewerUsersList.setSelectedIndex(1);
    }
  }

  public class TitleManager implements ActionListener, CaretListener {
    private String timestamp;
    private JLabel label;

    public TitleManager() {
      this.timestamp = getTimestamp();
      this.label = new JLabel(getReviewTitle());
      label.setFont(new Font("Serif", Font.BOLD, 18));
      label.setMaximumSize(new Dimension(60, 20));
    }

    private String getReviewTitle() {
      return getReviewProduct() + " " + getReviewRelease() + " " +
             getReviewActivity() + " " + getReviewModule() + " " + timestamp;
    }

    String getTimestamp() {
      Date today = new Date();
      DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
      dateFormatSymbols.setShortWeekdays(new String[]{
              "6", "7", "1", "2", "3", "4", "5"
      });
      return new SimpleDateFormat("yyyy'w'ww'.'E", dateFormatSymbols).format(today);
    }

    public void actionPerformed(ActionEvent actionEvent) {
      computeLabel();
    }

    public void caretUpdate(CaretEvent caretEvent) {
      computeLabel();
    }

    public void computeLabel() {
      label.setText(getReviewTitle());
    }

    public JLabel getLabel() {
      return label;
    }

  }

  class MyGridBagConstraints extends GridBagConstraints {


    public MyGridBagConstraints() {
      this.gridx = 0;
      this.gridy = 0;
      this.gridwidth = 0;
      this.gridheight = 0;
      this.fill = 1;
      this.ipady = 4;
    }

    public GridBagConstraints get(int gx, int gy, int gw, int gh) {
      this.gridx = gx;
      this.gridy = gy;
      this.gridwidth = gw;
      this.gridheight = gh;
      this.fill = 1;
      return this;
    }

    public GridBagConstraints addLine(int gw, int gh) {
      return get(0, gridy + gridheight, gw, gh);
    }

    public GridBagConstraints addCol(int gw) {
      return get(gridx + gridwidth, gridy, gw, gridheight);
    }
  }

  public String getReviewTitle() {
    return titleManager.getLabel().getText();
  }

  public String getReviewActivity() {
    return reasonTextField == null ? "" : reasonTextField.getText();
  }

  public String getReviewProduct() {
    return (String) (productComboBox == null ? "" : productComboBox.getSelectedItem());
  }

  public String getReviewRelease() {
    return (String) (releaseComboBox == null ? "" : releaseComboBox.getSelectedItem());
  }

  public String getReviewType() {
    return (String) (typeComboBox == null ? "" : typeComboBox.getSelectedItem());
  }

  public String getReviewModule() {
    return (String) (moduleComboBox == null ? "" : moduleComboBox.getSelectedItem());
  }

  public String getModerator() {
    return (String) (moderatorUsersList == null ? "" : moderatorUsersList.getSelectedItem());
  }

  public String getReviewer() {
    return (String) (reviewerUsersList == null ? "" : reviewerUsersList.getSelectedItem());
  }

}
