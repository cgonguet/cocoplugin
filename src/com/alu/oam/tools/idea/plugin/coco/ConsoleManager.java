package com.alu.oam.tools.idea.plugin.coco;

import com.alu.oam.tools.idea.plugin.coco.actions.*;
import com.alu.oam.tools.idea.plugin.coco.dialogs.ChangesTable;
import com.alu.oam.tools.idea.plugin.coco.dialogs.DefectsTable;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.smartbear.ccollab.datamodel.*;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTree;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 18 févr. 2008
 * Time: 17:16:54
 * To change this template use File | Settings | File Templates.
 */
public class ConsoleManager extends JPanel {
  private static ConsoleManager theInstance = null;
  private Logger logger = CodeCollaboratorManager.LOGGER;
  private static Project project;
  private static final String COCO_CONSOLE_ID = "CoCo";
  private MyJTree changesTree;
  private DefectsTable defectsTable;
  private JTabbedPane tabPane;
  private DefaultTableModel infoTableModel;
  private DefaultTableModel participantsTableModel;
  private JLabel reviewLabel;

  enum Tabs {
    Info,
    Changes,
    Defects,
  }

  private ConsoleManager() {
    init();
  }

  public static ConsoleManager getInstance(Project project) {
    ConsoleManager.project = project;
    if (theInstance == null) {
      theInstance = new ConsoleManager();
    }
    return theInstance;
  }

  private void init() {
    ToolWindowManager manager = ToolWindowManager.getInstance(project);
    ToolWindow cocoWindow = manager.getToolWindow(COCO_CONSOLE_ID);
    if (cocoWindow != null) {
      manager.unregisterToolWindow(COCO_CONSOLE_ID);
      cocoWindow = null;
    }
    if (cocoWindow == null) {
      cocoWindow = manager.registerToolWindow(COCO_CONSOLE_ID, this, ToolWindowAnchor.BOTTOM);
      cocoWindow.setIcon(IconLoader.getIcon("icon/smartbear_16x16.png"));
      cocoWindow.setTitle(COCO_CONSOLE_ID);
    }
  }

  public void open() throws Exception {
    tabPane = new JTabbedPane();

    changesTree = new MyJTree();

    try {
      setLayout(new BorderLayout());

      JToolBar tb = new JToolBar(JToolBar.VERTICAL);
      tb.setFloatable(false);
      JButton refreshButton = new JButton(IconLoader.getIcon("icon/refresh.png"));
      refreshButton.setToolTipText("Reload from server");
      refreshButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          reloadFromServer();
        }
      });
      tb.add(refreshButton);

      JButton closeButton = new JButton(IconLoader.getIcon("icon/close.png"));
      closeButton.setToolTipText("Close review");
      closeButton.addActionListener(new ReviewCloseAction(project));
      tb.add(closeButton);

      final JButton actionsButton = new JButton(IconLoader.getIcon("icon/actions.png"));
      tb.add(actionsButton);
      actionsButton.setToolTipText("Actions ...");

      final JPopupMenu moreActionsMenu = new JPopupMenu();

      JMenuItem uploadListItem = new JMenuItem("Upload list", IconLoader.getIcon("icon/uploadList.png"));
      uploadListItem.setToolTipText("Upload changes from a list of files");
      uploadListItem.addActionListener(new ReviewUploadFilesAction(project));
      moreActionsMenu.add(uploadListItem);

      JMenuItem uploadChangesItem = new JMenuItem("Upload changeset", IconLoader.getIcon("icon/uploadChanges.png"));
      uploadChangesItem.setToolTipText("Upload changes from default change list");
      uploadChangesItem.addActionListener(new ReviewUploadChangesAction(project));
      moreActionsMenu.add(uploadChangesItem);

      JMenuItem validateItem = new JMenuItem("Validate review", IconLoader.getIcon("icon/validate.png"));
      validateItem.setToolTipText("Validate review");
      validateItem.addActionListener(new ReviewCompleteAction(project));
      moreActionsMenu.add(validateItem);

      JMenuItem viewOnServerItem = new JMenuItem("View on server", IconLoader.getIcon("icon/navigate.png"));
      viewOnServerItem.setToolTipText("Launch browser to access the review");
      viewOnServerItem.addActionListener(new ViewOnServerAction(project));
      moreActionsMenu.add(viewOnServerItem);

      actionsButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent actionEvent) {
          moreActionsMenu.show(actionsButton, 0, 0);
        }
      });

      JButton defectButton = new JButton(IconLoader.getIcon("icon/bug_red_24.gif"));
      defectButton.setToolTipText("Create defect");
      defectButton.addActionListener(new DefectCreateAction(project));
      tb.add(defectButton);

      add(tb, BorderLayout.WEST);


      reviewLabel = new JLabel();
      reviewLabel.setFont(new Font("Serif", Font.BOLD, 18));
      reviewLabel.setMaximumSize(new Dimension(60, 20));

      add(reviewLabel, BorderLayout.NORTH);

      add(tabPane, BorderLayout.CENTER);

      JPanel infoPane = new JPanel(new BorderLayout());
      infoTableModel = new DefaultTableModel();
      infoTableModel.addColumn("");
      infoTableModel.addColumn("");
      JXTable infoTable = new JXTable(infoTableModel);
      infoTable.setEditable(false);
      infoTable.setRowSelectionAllowed(false);
      infoTable.setColumnSelectionAllowed(false);
      infoTable.setCellSelectionEnabled(false);
      infoTable.getColumnModel().getColumn(0).setMaxWidth(70);
      infoPane.add(infoTable, BorderLayout.NORTH);

      participantsTableModel = new DefaultTableModel();
      participantsTableModel.addColumn("Role");
      participantsTableModel.addColumn("Name");
      JXTable participantsTable = new JXTable(participantsTableModel);
      participantsTable.setEditable(false);
      participantsTable.setRowSelectionAllowed(false);
      participantsTable.setColumnSelectionAllowed(false);
      participantsTable.setCellSelectionEnabled(false);
      participantsTable.getColumnModel().getColumn(0).setMaxWidth(70);
      infoPane.add(new JScrollPane(participantsTable), BorderLayout.CENTER);
      tabPane.add(Tabs.Info.name(), new JScrollPane(infoPane));

      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weightx = 1.0;
      gbc.weighty = 1.0;
      gbc.fill = GridBagConstraints.BOTH;

      JPanel changesPane = new JPanel(new BorderLayout());

      JToolBar changesTb = new JToolBar(JToolBar.VERTICAL);
      changesTb.setFloatable(false);
      JButton expandAllButton = new JButton(IconLoader.getIcon("icon/expandAll.png"));
      expandAllButton.setToolTipText("Expand All");
      expandAllButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          changesTree.expandAll();
        }
      });
      changesTb.add(expandAllButton);
      JButton collapseAllButton = new JButton(IconLoader.getIcon("icon/collapseAll.png"));
      collapseAllButton.setToolTipText("Collapse All");
      collapseAllButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
          changesTree.collapseAll();
        }
      });
      changesTb.add(collapseAllButton);
      changesPane.add(changesTb, BorderLayout.WEST);


      JPanel treePane = new JPanel(new GridBagLayout());
      JScrollPane treeScrollPane = new JScrollPane(changesTree);
      treeScrollPane.setViewportView(changesTree);
      treePane.add(treeScrollPane, gbc);
      changesPane.add(treePane, BorderLayout.CENTER);

      tabPane.add(Tabs.Changes.name(), changesPane);

      defectsTable = new DefectsTable(project, null);
      JPanel defectsPane = new JPanel(new GridBagLayout());
      defectsPane.add(new JScrollPane(defectsTable), gbc);
      tabPane.add(Tabs.Defects.name(), defectsPane);

      refresh();
    } catch (Exception e) {
      logger.error("Console error: ", e);
    }

  }

  private void updateInfo(Review review) throws Exception {
    infoTableModel.setRowCount(0);
    infoTableModel.addRow(new String[]{"Id", review.getId().toString()});
    infoTableModel.addRow(new String[]{"Title", review.getTitle()});
    infoTableModel.addRow(new String[]{"Date", review.getCreationDate().toString()});
    String creatorName =
            CodeCollaboratorManager.getInstance().getUserByLogin(review.getCreator().getLogin()).getRealName();
    infoTableModel.addRow(new String[]{"Creator", creatorName});
    infoTableModel.addRow(new String[]{"Phase", review.getPhase().getName()});
    infoTableModel.fireTableDataChanged();

    participantsTableModel.setRowCount(0);
    for (Assignment user : review.getAssignments()) {
      participantsTableModel.addRow(new String[]{user.getRole().getDisplayName(), user.getUser().getRealName()});
    }
    participantsTableModel.fireTableDataChanged();
  }

  public void refresh() {
    Review review = null;
    try {
      review = CodeCollaboratorManager.getInstance().getCurrentReview();
      if (review == null) {
        reviewLabel.setText("No review in progress");
      }
      reviewLabel.setText(review.getDisplayText(true));
      updateInfo(review);
    } catch (Exception e) {
      logger.error("Console error: ", e);
    }

    MyFileTree fileTree = new MyFileTree();
    defectsTable.reset();

    try {
      HashSet<Defect> defects = CodeCollaboratorManager.getInstance().getDefects(DefectsManager.OVERALL);
      if (defects != null) {
        for (Defect defect : defects) {
          defectsTable.add(DefectsManager.OVERALL, defect);
        }
      }
    } catch (Exception e) {
      CodeCollaboratorManager.LOGGER.error(e);
    }

    List<Changelist> changelists = review.getChangelists();
    for (Changelist changelist : changelists) {
      List<Version> versions = changelist.getVersions();
      for (Version version : versions) {
        try {
          String relativeFilePath = version.getFilePath();
          HashSet<Defect> defects = CodeCollaboratorManager.getInstance().getDefects(relativeFilePath);
          if (defects != null) {
            for (Defect defect : defects) {
              defectsTable.add(relativeFilePath, defect);
            }
          }

          fileTree.store(relativeFilePath);
        } catch (Exception e) {
          CodeCollaboratorManager.LOGGER.error(e);
        }
      }
    }
    changesTree.removeAll();
    fileTree.updateModel();

    changesTree.expandAll();
    changesTree.repaint();
  }

  public void reloadFromServer() {
    try {
      Review review = CodeCollaboratorManager.getInstance().getCurrentReview();

      new DefectHighlighter(project).unregister();
      CodeCollaboratorManager.getInstance().finished();

      Review refreshedReview = CodeCollaboratorManager.getInstance().switchReview(review);
      if (refreshedReview == null) {
        MessagesUtils.error("Review not found on the server");
        ConsoleManager.getInstance(project).close();
        CodeCollaboratorManager.reset();
        return;
      }

      new DefectHighlighter(project).register();

      refresh();

    } catch (Exception e) {
      logger.error("Console error: ", e);
    }

  }

  public void close() {
    ToolWindowManager manager = ToolWindowManager.getInstance(project);
    ToolWindow cocoWindow = manager.getToolWindow(COCO_CONSOLE_ID);
    if (cocoWindow != null) {
      manager.unregisterToolWindow(COCO_CONSOLE_ID);
    }
    theInstance = null;
  }


  class InfoTableModel extends DefaultTableModel {

    public InfoTableModel() {
      super(new Object[0][0], new Object[]{"Property", "Value"});
    }

    public boolean isCellEditable(int row, int col) {
      return false;
    }

    public void addInfo(String key, String value) {
      super.addRow(new Object[]{key, value});
    }

  }

  class MyJTree extends JXTree {

    public MyJTree() {
      super();

      DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
      DefaultTreeModel model = new DefaultTreeModel(rootTreeNode);
      setModel(model);
      setRootVisible(false);
      setCellRenderer(new DefaultTreeCellRenderer() {
        public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean selected,
                boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
          DefaultTreeCellRenderer cellRenderer =
                  (DefaultTreeCellRenderer) super
                          .getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

          try {
            if (leaf) {
              DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
              MyFileNodeDescriptor desc = (MyFileNodeDescriptor) node.getUserObject();

              HashSet<Defect> defects = CodeCollaboratorManager.getInstance().getDefects(desc.getPath());
              if (defects == null || defects.isEmpty()) {
                return cellRenderer;
              }
              String details = "";
              int i = 0;
              for (Defect defect : defects) {
                details += defect.getName();
                if (i++ < defects.size() - 1) {
                  details += ",";
                }
              }
              cellRenderer.setToolTipText(details);
              for (Defect defect : defects) {
                if (!defect.isMarkedFixed()) {
                  cellRenderer.setIcon(IconLoader.getIcon("icon/bug_red.gif"));
                  return cellRenderer;
                }
              }
              cellRenderer.setIcon(IconLoader.getIcon("icon/bug_green.gif"));
              return cellRenderer;
            }

            return cellRenderer;

          } catch (Exception e) {
            CodeCollaboratorManager.LOGGER.error(e);
            return cellRenderer;
          }
        }
      });

      addMouseListener(new MouseAdapter() {

        public void mousePressed(MouseEvent mouseEvent) {
          if (mouseEvent.getClickCount() == 2) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
            if (node != null && node.isLeaf()) {
              ChangesTable.openFile(project, ((MyFileNodeDescriptor) node.getUserObject()).getPath(), 0);
            }
          }
        }
      });

    }

  }


  class MyFileTree {
    MyFileNode root = new MyFileNode("P:");
    private String path;

    public void store(String path) {
      this.path = path;
      add(root, path.split("/"), 1);
    }

    private void add(MyFileNode parent, String[] elems, int level) {
      if (level >= elems.length) {
        return;
      }
      String current = elems[level];
      add(parent.add(current, path), elems, level + 1);
    }

    public DefaultTreeModel updateModel() {
      DefaultTreeModel model = (DefaultTreeModel) changesTree.getModel();
      DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode();
      root.addToTree(rootTreeNode);
      model.setRoot(rootTreeNode);
      return model;
    }
  }

  class MyFileNode implements Comparable {
    String node;
    private String filePath;
    TreeSet<MyFileNode> children = new TreeSet<MyFileNode>();

    public MyFileNode(String node) {
      this.node = node;
    }

    public MyFileNode(String node, String filePath) {
      this.node = node;
      this.filePath = filePath;
    }

    public MyFileNode add(String newChild, String path) {
      for (MyFileNode childNode : children) {
        if (childNode.node.equals(newChild)) {
          return childNode;
        }
      }
      MyFileNode newNode = new MyFileNode(newChild, path);
      children.add(newNode);
      return newNode;
    }

    public void addToTree(DefaultMutableTreeNode parent) {
      MyFileNodeDescriptor nodeDescriptor = new MyFileNodeDescriptor(node, filePath, children.isEmpty());
      DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(nodeDescriptor, true);
      parent.add(treeNode);
      for (MyFileNode child : children) {
        child.addToTree(treeNode);
      }
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      MyFileNode that = (MyFileNode) o;

      if (filePath != null ? !filePath.equals(that.filePath) : that.filePath != null) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      return (filePath != null ? filePath.hashCode() : 0);
    }


    public int compareTo(Object o) {
      if (o instanceof MyFileNode) {
        return filePath.compareTo(((MyFileNode) o).filePath);
      }
      return 0;
    }
  }

  class MyFileNodeDescriptor {
    String name;
    String path;
    boolean isFile;

    public MyFileNodeDescriptor(String name, String path, boolean isFile) {
      this.name = name;
      this.path = path;
      this.isFile = isFile;
    }

    public String toString() {
      return getName();
    }


    public String getName() {
      return name;
    }

    public String getPath() {
      return path;
    }

    public boolean isFile() {
      return isFile;
    }
  }
}
