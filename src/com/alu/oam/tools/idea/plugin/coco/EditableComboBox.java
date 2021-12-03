package com.alu.oam.tools.idea.plugin.coco;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

public class EditableComboBox extends JPanel implements ActionListener {
  private JComboBox comboBox;
  private ComboBoxEditorValuesMgr mgr;

  public EditableComboBox() {
    setLayout(new BorderLayout());
    comboBox = new JComboBox();
    add(comboBox, BorderLayout.CENTER);

    JButton button = new JButton("Edit");
    button.addActionListener(this);
    add(button, BorderLayout.EAST);
  }


  public void setSelectedItem(Object item) {
    comboBox.setSelectedItem(item);
  }

  public Object getSelectedItem() {
    return comboBox.getSelectedItem();
  }

  public void init(ComboBoxEditorValuesMgr mgr) {
    this.mgr = mgr;
    setValues(mgr.get());
  }

  private void setValues(Collection<String> values) {
    comboBox.removeAllItems();
    for (String value : values) {
      comboBox.addItem(value);
    }
  }

  public void actionPerformed(ActionEvent actionEvent) {
    new ComboBoxValuesEditorDialog().execute();
  }


  public class ComboBoxValuesEditorDialog extends DialogWrapper {
    JTextArea editor = new JTextArea(10, 30);

    protected ComboBoxValuesEditorDialog() {
      super(true);
    }

    @Nullable
    protected JComponent createCenterPanel() {
      setTitle("Items Editor");
      JPanel userPane = new JPanel(new BorderLayout());
      userPane.add(new JLabel("Add or remove items (one by line):"), BorderLayout.NORTH);
      editor.setEditable(true);
      comboBox.getItemCount();
      for (int i = 0; i < comboBox.getItemCount(); i++) {
        String item = (String) comboBox.getItemAt(i);
        editor.append(item + '\n');
      }

      userPane.add(new JScrollPane(editor), BorderLayout.CENTER);
      return userPane;
    }

    public void execute() {
      init();
      show();
      if (!isOK()) {
        return;
      }
      proceed();
    }

    private void proceed() {
      ArrayList<String> values = new ArrayList<String>() {
      };
      String[] editItems = editor.getText().split("\n");
      for (int i = 0; i < editItems.length; i++) {
        String editItem = editItems[i];
        if (!editItem.equals("")) {
          values.add(editItem);
        }
      }

      if (mgr != null) {
        mgr.set(values);
      }
      setValues(values);
    }

  }

  public interface ComboBoxEditorValuesMgr {
    void set(Collection<String> values);

    Collection<String> get();
  }
}
