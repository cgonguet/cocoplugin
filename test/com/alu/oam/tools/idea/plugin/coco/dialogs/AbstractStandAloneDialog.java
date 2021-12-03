package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public abstract class AbstractStandAloneDialog extends JDialog implements ActionListener {
  protected JComponent centerPane;

  private JButton okButton = null;

  protected AbstractStandAloneDialog() throws HeadlessException {
    super(new JFrame(), true);
  }

  public void execute() throws Exception {
    setTitle("TEST DIALOG");
    UIManager.setLookAndFeel(WindowsLookAndFeel.class.getName());
    setUserPane();
    setButtonPane();
    pack();
    setLocationRelativeTo(null);
    setVisible(true);
  }

  private void setButtonPane() {
    final JPanel buttonPanel = new JPanel();
    okButton = new JButton("OK");
    okButton.addActionListener(this);
    buttonPanel.add(okButton, BorderLayout.WEST);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
  }

  public void actionPerformed(ActionEvent e) {
    if (okButton == e.getSource()) {
      try {
        proceed();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      setVisible(false);
    }
  }


  protected void setUserPane() throws Exception {
    centerPane = getUserPane();
    getContentPane().add(centerPane, BorderLayout.CENTER);
  }

  protected void proceed() {
  }

  ;

  protected abstract JComponent getUserPane();
}


