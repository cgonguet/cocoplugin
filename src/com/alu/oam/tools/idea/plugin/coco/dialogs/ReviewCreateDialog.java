package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.alu.oam.tools.idea.plugin.coco.CoCoComponent;
import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.smartbear.ccollab.datamodel.User;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 21 déc. 2007
 * Time: 11:15:16
 * To change this template use File | Settings | File Templates.
 */
public class ReviewCreateDialog extends DialogWrapper {
  private Project project;
  private ReviewCreateDialogAdapter dialogAdapter;
  private String authorLogin;
  User moderator;
  User reviewer;

  public ReviewCreateDialog(Project project) {
    super(false);
    this.project = project;
    this.dialogAdapter = new ReviewCreateDialogAdapter();
  }

  @Nullable
  public JComponent createCenterPanel() {
    setTitle("Create review");
    JComponent component = dialogAdapter.getUserPanel();
    if (!initData()) {
      return new JLabel("-- ERROR --");
    }
    return component;
  }


  protected void doOKAction() {
    try {
      if (validateData()) {
        super.doOKAction();
      }
    } catch (Exception e) {
      MessagesUtils.error(e);
    }
  }

  private boolean initData() {
    dialogAdapter.productComboBox.setSelectedItem(CoCoComponent.getInstance().getReviewProduct());
    dialogAdapter.releaseComboBox.setSelectedItem(CoCoComponent.getInstance().getReviewRelease());
    dialogAdapter.moduleComboBox.setSelectedItem(CoCoComponent.getInstance().getReviewModule());
    dialogAdapter.typeComboBox.setSelectedItem(CoCoComponent.getInstance().getReviewType());
    dialogAdapter.titleManager.computeLabel();

    CodeCollaboratorManager mgr = null;
    try {
      mgr = CodeCollaboratorManager.getInstance();
    } catch (Exception e) {
      MessagesUtils.error(e.getMessage());
      return false;
    }

    authorLogin = mgr.getUser().getLogin();
    dialogAdapter.authorLabel.setText(authorLogin);

    String membersCSV = CoCoComponent.getInstance().getTeamMembers();
    if (membersCSV == null || membersCSV.equals("")) {
      MessagesUtils.warning("The team members list is not set");
      return true;
    }

    String[] members = membersCSV.split(",");
    Collection<String> membersKO = new ArrayList<String>();
    Collection<String> membersOK = new ArrayList<String>();
    for (String member : members) {
      if (mgr.getUserByLogin(member) == null) {
        membersKO.add(member);
      } else {
        if (!membersOK.contains(member)) {
          membersOK.add(member);
        }
      }
    }
    if (!membersKO.isEmpty()) {
      MessagesUtils.warning("Some team members are not valid: " + membersKO.toString());
    }
    dialogAdapter.setTeamMembers(membersOK);
    return true;
  }

  public boolean validateData() throws Exception {
    String moderatorName = dialogAdapter.getModerator();
    moderator = CodeCollaboratorManager.getInstance().getUserByLogin(moderatorName);
    if (moderator == null) {
      Messages.showErrorDialog("The moderator login is not valid", "Error");
      return false;
    }
    String reviewerName = dialogAdapter.getReviewer();
    reviewer = CodeCollaboratorManager.getInstance().getUserByLogin(reviewerName);
    if (reviewer == null) {
      Messages.showErrorDialog("The reviewer login is not valid", "Error");
      return false;
    }
    if (moderator.getLogin().equals(authorLogin)) {
      Messages.showErrorDialog("The moderator and the author should be different", "Error");
      return false;
    }
    if (reviewer.getLogin().equals(authorLogin)) {
      Messages.showErrorDialog("The reviewer and the author should be different", "Error");
      return false;
    }
    if (reviewer.getLogin().equals(moderator.getLogin())) {
      Messages.showErrorDialog("The moderator and the reviewer should be different", "Error");
      return false;
    }
    return true;
  }

  public void execute() throws Exception {
    init();
    show();
  }

  public User getModerator() {
    return moderator;
  }

  public User getReviewer() {
    return reviewer;
  }

  public String getReviewTitle() {
    return dialogAdapter.getReviewTitle();
  }

  public String getReviewActivity() {
    return dialogAdapter.getReviewActivity();
  }

  public String getReviewProduct() {
    return dialogAdapter.getReviewProduct();
  }

  public String getReviewRelease() {
    return dialogAdapter.getReviewRelease();
  }

  public String getReviewType() {
    return dialogAdapter.getReviewType();
  }

  public String getReviewModule() {
    return dialogAdapter.getReviewModule();
  }

}
