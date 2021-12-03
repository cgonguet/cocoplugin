package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.intellij.openapi.ui.DialogWrapper;
import com.smartbear.ccollab.datamodel.Review;
import org.jetbrains.annotations.Nullable;
import sun.awt.VerticalBagLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 21 déc. 2007
 * Time: 11:15:16
 * To change this template use File | Settings | File Templates.
 */
public class ReviewOpenDialog extends DialogWrapper {
  private List<Review> reviews;
  protected ButtonGroup buttonGroup;
  protected JPanel userPane;

  public ReviewOpenDialog(List<Review> reviews) {
    super(false);
    this.reviews = reviews;
  }

  @Nullable
  protected JComponent createCenterPanel() {
    setTitle("Choose review");
    userPane = new JPanel(new VerticalBagLayout());
    buttonGroup = new ButtonGroup();
    if (reviews == null || reviews.size() == 0) {
      JPanel jPanel = new JPanel(new BorderLayout());
      jPanel.add(new JLabel("No review in progress"));
      userPane.add(jPanel);
      return userPane;
    }
    for (Review review : reviews) {
      addReview(review);
    }
    return userPane;
  }

  private void addReview(Review review) {
    JPanel pane = new JPanel(new BorderLayout());
    JRadioButton jRadioButton = new JRadioButton(review.getDisplayText(true));
//    jRadioButton.setToolTipText(title);
    jRadioButton.setActionCommand(review.getId().toString());
    jRadioButton.setSelected(true);
    pane.add(jRadioButton, BorderLayout.WEST);
    buttonGroup.add(jRadioButton);
    userPane.add(pane);
  }

  public void execute() throws Exception {
    init();
    show();
  }

  public String getReviewSelected() {
    if (buttonGroup.getSelection() == null) {
      return null;
    }
    return buttonGroup.getSelection().getActionCommand();
  }

}
