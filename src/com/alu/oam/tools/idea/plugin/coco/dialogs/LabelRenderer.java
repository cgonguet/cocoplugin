package com.alu.oam.tools.idea.plugin.coco.dialogs;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 19 févr. 2008
 * Time: 10:25:46
 * To change this template use File | Settings | File Templates.
 */
abstract class LabelRenderer implements TableCellRenderer {

  protected JLabel label;

  public LabelRenderer() {
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int col) {
    label = new JLabel();

    if (isSelected) {
      label.setForeground(Color.BLUE);
    } else {
      label.setForeground(Color.BLACK);
    }

    setUserRenderer(value, row);
    return label;
  }

  public abstract void setUserRenderer(Object value, int row);

  ;
}
