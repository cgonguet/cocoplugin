package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.alu.oam.tools.idea.plugin.coco.ConsoleManager;
import com.alu.oam.tools.idea.plugin.coco.DefectHighlighter;
import com.alu.oam.tools.idea.plugin.coco.DefectsManager;
import com.alu.oam.tools.idea.plugin.coco.actions.DefectUpdateAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.smartbear.ccollab.datamodel.Defect;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 19 févr. 2008
 * Time: 10:28:09
 * To change this template use File | Settings | File Templates.
 */
public class DefectsTable extends JXTable {


  DefectsTableModel model = new DefectsTableModel();
  HashMap<Integer, String> fileByDefectId = new HashMap<Integer, String>();
  private Project project;
  private ChangesTable changesTable;

  public DefectsTable(Project project, ChangesTable changesTable) {
    this.project = project;
    this.changesTable = changesTable;

    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    setModel(model);
    createDefaultColumnsFromModel();

    setColumnDescription(DefectsTableModel.ColumnHeader.Status.ordinal(), 30)
            .setCellRenderer(new LabelRenderer() {
              public void setUserRenderer(Object value, int row) {
                Boolean isFixed = (Boolean) value;
                if (isFixed) {
                  label.setIcon(IconLoader.getIcon("bug_green.gif"));
                } else {
                  label.setIcon(IconLoader.getIcon("bug_red.gif"));
                }
                setToolTipText("Double-click to open the defect");
              }
            });

    setColumnDescription(DefectsTableModel.ColumnHeader.Defect.ordinal(), 50);
    setColumnDescription(DefectsTableModel.ColumnHeader.Creator.ordinal(), 50);
    setColumnDescription(DefectsTableModel.ColumnHeader.Severity.ordinal(), 80);
    setColumnDescription(DefectsTableModel.ColumnHeader.Type.ordinal(), 100);
    setColumnDescription(DefectsTableModel.ColumnHeader.Text.ordinal(), 350);

    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        int row = getSelectedRow();
        if (row < 0 || row >= model.getRowCount()) {
          return;
        }
        setRowSelectionInterval(row, row);
        if (e.getClickCount() == 2) {
          Defect defect = model.getDefect(convertRowIndexToModel(row));
          openDefect(defect);
        }
      }
    });
  }

  private TableColumn setColumnDescription(int col, int size) {
    TableColumn descCol = getColumnModel().getColumn(col);
    descCol.setPreferredWidth(size);
    return descCol;
  }

  private void openDefect(Defect defect) {
    String relativeFilePath = fileByDefectId.get(defect.getId());

    if (!DefectsManager.OVERALL.equals(relativeFilePath)) {
      ChangesTable.openFile(project, relativeFilePath, DefectHighlighter.getDefectLine(defect));
    }

    DefectUpdateAction action = new DefectUpdateAction(relativeFilePath, defect);
    action.updateDefectWithDialog(project);
    if (action.defectDeleted()) {
      fileByDefectId.remove(defect.getId());
      model.removeDefect(defect);
    }
    if (changesTable != null) {
      changesTable.update(defect);
    }
    ConsoleManager.getInstance(project).refresh();
  }

  public void reset() {
    model.reset();
    fileByDefectId.clear();
  }

  public DefectsTable add(String relativeFilePath, Defect defect) {
    model.addDefect(defect);
    fileByDefectId.put(defect.getId(), relativeFilePath);
    return this;
  }

  public static class DefectsTableModel extends AbstractTableModel {
    TreeSet<Defect> defects = new TreeSet<Defect>();

    enum ColumnHeader {
      Status,
      Defect,
      Creator,
      Severity,
      Type,
      Text,
    }


    public String getColumnName(int column) {
      return ColumnHeader.values()[column].toString();
    }

    public int getRowCount() {
      return defects.size();
    }

    public int getColumnCount() {
      return ColumnHeader.values().length;
    }

    public Object getValueAt(int row, int col) {
      Defect defect = getDefect(row);
      if (defect != null) {
        switch (ColumnHeader.values()[col]) {
          case Status:
            return defect.isMarkedFixed();
          case Defect:
            return defect.getName();
          case Creator:
            return defect.getCreator().getLogin();
          case Severity:
            return defect.getUserDefinedFields().getSelectItem("Severity").getDisplayName();
          case Type:
            return defect.getUserDefinedFields().getSelectItem("Type").getDisplayName();
          case Text:
            return defect.getText();
        }
      }
      return null;
    }

    public boolean isCellEditable(int i, int i1) {
      return false;
    }

    public void addDefect(Defect newDefect) {
      defects.add(newDefect);
      fireTableDataChanged();
    }

    public void removeDefect(Defect defect) {
      defects.remove(defect);
      fireTableDataChanged();
    }

    public void reset() {
      defects.clear();
      fireTableDataChanged();
    }

    public Defect getDefect(int index) {
      int i = 0;
      for (Defect defect : defects) {
        if (i++ == index) {
          return defect;
        }
      }
      return null;
    }
  }
}
