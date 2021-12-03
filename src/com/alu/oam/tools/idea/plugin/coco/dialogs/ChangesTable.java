package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.FilePathUtils;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.smartbear.ccollab.datamodel.Defect;
import org.jdesktop.swingx.JXTable;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 19 févr. 2008
 * Time: 10:23:18
 * To change this template use File | Settings | File Templates.
 */
public class ChangesTable extends JXTable {
  private static final int FILEPATH_LENGTH = 100;

  private Project project;
  ChangesTableModel model = new ChangesTableModel();

  public ChangesTable(Project proj) {
    this.project = proj;
    setModel(model);
    createDefaultColumnsFromModel();

    setColumnDescription(ChangesTableModel.ColumnHeader.Status.ordinal(), 40)
            .setCellRenderer(new LabelRenderer() {

              public void setUserRenderer(Object value, int row) {
                HashSet<Defect> defects = (HashSet<Defect>) value;
                if (defects == null || defects.isEmpty()) {
                  return;
                }
                label.setText(String.valueOf(defects.size()));
                String details = "";
                int i = 0;
                for (Defect defect : defects) {
                  details += defect.getName();
                  if (i++ < defects.size() - 1) {
                    details += ",";
                  }
                }
                setToolTipText(details);
                for (Defect defect : defects) {
                  if (!defect.isMarkedFixed()) {
                    label.setIcon(IconLoader.getIcon("bug_red.gif"));
                    return;
                  }
                }
                label.setIcon(IconLoader.getIcon("bug_green.gif"));
              }
            });

    TableColumn fileCol = setColumnDescription(ChangesTableModel.ColumnHeader.File.ordinal(), 600);
    fileCol.sizeWidthToFit();
    fileCol.setCellRenderer(new LabelRenderer() {

      public void setUserRenderer(Object value, int row) {
        String filePath = (String) value;
        int length = filePath.length();
        if (length > FILEPATH_LENGTH) {
          filePath = "..." + filePath.substring(length - FILEPATH_LENGTH, length);
        }
        label.setText(filePath);
      }
    });


    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        int row = getSelectedRow();
        setRowSelectionInterval(row, row);
        if (e.getClickCount() == 2) {
          String filePath = (String) getValueAt(row, ChangesTableModel.ColumnHeader.File.ordinal());
          openFile(project, filePath, 0);
        }
      }
    });

  }

  private TableColumn setColumnDescription(int col, int size) {
    TableColumn descCol = getColumnModel().getColumn(col);
    descCol.setPreferredWidth(size);
    return descCol;
  }

  public static void openFile(Project project, String relativeFilePath, int line) {

    if (relativeFilePath == null) {
      return;
    }

    VirtualFile vFile = FilePathUtils.getVirtualFile(project, relativeFilePath);
    if (vFile == null) {
      MessagesUtils.error("Could not open this file");
      return;
    }

    OpenFileDescriptor ofd = new OpenFileDescriptor(project, vFile, line, 0);
    FileEditorManager.getInstance(project).openEditor(ofd, true);
  }

  public ChangesTable add(String relativeFilePath, HashSet<Defect> defects) {
    model.addChangeFile(relativeFilePath, defects);
    return this;
  }


  public void update(Defect defect) {
    try {
      model.updateRow(defect);
    } catch (Exception e) {
      CodeCollaboratorManager.LOGGER.error(e);
    }
  }

  public static class ChangesTableModel extends AbstractTableModel {
    TreeMap<String, HashSet<Defect>> defectsByFile = new TreeMap<String, HashSet<Defect>>();

    enum ColumnHeader {
      Status,
      File,
    }


    public String getColumnName(int column) {
      return ColumnHeader.values()[column].toString();
    }

    public int getRowCount() {
      return defectsByFile.keySet().size();
    }

    public int getColumnCount() {
      return ColumnHeader.values().length;
    }

    public Object getValueAt(int row, int col) {
      String file = getFile(row);
      if (file != null) {
        switch (ColumnHeader.values()[col]) {
          case Status:
            return defectsByFile.get(file);
          case File:
            return file;
        }
      }
      return null;
    }

    public boolean isCellEditable(int i, int i1) {
      return false;
    }

    public void addChangeFile(String relativeFilePath, HashSet<Defect> defects) {
      defectsByFile.put(relativeFilePath, defects);
      fireTableDataChanged();
    }

    public void updateRow(Defect defect) throws Exception {
      for (int row = 0; row < getRowCount(); row++) {
        String file = getFile(row);
        HashSet<Defect> defects = defectsByFile.get(file);
        if (defects != null && defects.contains(defect)) {
          HashSet<Defect> reloadDefects = CodeCollaboratorManager.getInstance().getDefects(file);
          defectsByFile.put(file, reloadDefects);
          fireTableDataChanged();
          return;
        }
      }
    }

    public String getFile(int index) {
      int i = 0;
      for (String file : defectsByFile.keySet()) {
        if (i++ == index) {
          return file;
        }
      }
      return null;
    }

  }

}
