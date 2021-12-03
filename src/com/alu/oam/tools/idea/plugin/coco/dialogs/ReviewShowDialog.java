package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Key;
import com.smartbear.ccollab.datamodel.*;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 21 déc. 2007
 * Time: 11:15:16
 * To change this template use File | Settings | File Templates.
 */
public class ReviewShowDialog extends DialogWrapper {
  private Project project;
  private Review review;
  protected ButtonGroup buttonGroup;
  protected JComponent userPane;
  ChangesTable changesTable;
  DefectsTable defectsTable;
  public Key<Object> COCO_DEFECT_KEY;

  public ReviewShowDialog(Project project, Review review) {
    super(false);
    this.project = project;
    this.review = review;
  }

  protected Action[] createActions() {
    return (new Action[]{getOKAction()});
  }

  @Nullable
  protected JComponent createCenterPanel() {
    setTitle("Show review " + review.getId());
    userPane = new JTabbedPane();

    JPanel infoPane = new JPanel(new BorderLayout());
    InfoPanel northPane = new InfoPanel();
    northPane.title("Description");
    northPane.add("Id", review.getId())
            .add("Title", review.getTitle())
            .add("Date", review.getCreationDate())
            .add("Creator", review.getCreator().getLogin())
            .add("Phase", review.getPhase().getName());

    northPane.title("Participants");
    for (Assignment user : review.getAssignments()) {
      northPane.add(user.getRole().getDisplayName(), user.getUser().getRealName());
    }
    infoPane.add(northPane, BorderLayout.NORTH);
    userPane.add("Info", infoPane);

    changesTable = new ChangesTable(project);
    defectsTable = new DefectsTable(project, changesTable);

    JPanel changesPane = new JPanel(new BorderLayout());
    changesPane.add(new JScrollPane(changesTable), BorderLayout.CENTER);
    userPane.add("Changes", changesPane);

    JPanel defectsPane = new JPanel(new BorderLayout());
    defectsPane.add(new JScrollPane(defectsTable), BorderLayout.CENTER);
    userPane.add("Defects", defectsPane);

    updateTables();

    return userPane;
  }

  private void updateTables() {
    List<Changelist> changelists = review.getChangelistsActive(null);
    for (Changelist changelist : changelists) {
      List<Version> versions = changelist.getVersions();
      for (Version version : versions) {
        try {
          String relativeFilePath = version.getFilePath();
          HashSet<Defect> defects = CodeCollaboratorManager.getInstance().getDefects(relativeFilePath);
          changesTable.add(relativeFilePath, defects);
          if (defects != null) {
            for (Defect defect : defects) {
              defectsTable.add(relativeFilePath, defect);
            }
          }
        } catch (Exception e) {
          CodeCollaboratorManager.LOGGER.error(e);
        }
      }
    }
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

  class InfoPanel extends JPanel {
    GridBagConstraints constraints = new GridBagConstraints();

    public InfoPanel() {
      super(new GridBagLayout());
      initConstraints(-1);
    }

    private void initConstraints(int starty) {
      constraints.ipadx = 5;
      constraints.ipady = 5;
      constraints.gridx = 0;
      constraints.gridy = starty;
      constraints.gridwidth = 1;
      constraints.gridheight = 1;
      constraints.anchor = GridBagConstraints.WEST;
    }

    public InfoPanel add(String key, Object value) {
      constraints.gridy++;
      constraints.gridx = 0;
      constraints.gridwidth = 1;
      add(new JLabel(key), constraints);
      constraints.ipadx = 30;
      constraints.gridx = 1;
      constraints.gridwidth = 3;
      add(new JLabel(value.toString()), constraints);
      return this;
    }

    public InfoPanel title(String title) {
      constraints.ipadx = 0;
      constraints.ipady = 30;
      constraints.gridy++;
      constraints.gridx = 0;
      constraints.gridwidth = 4;
      constraints.anchor = GridBagConstraints.CENTER;
      add(new JLabel(title), constraints);
      initConstraints(constraints.gridy + 1);
      return this;
    }
  }

}
