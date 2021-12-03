package com.alu.oam.tools.idea.plugin.coco.dialogs;

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

public class ReviewCreateDialogAdapterTest extends TestCase {

  public void testPrintDateFormat() throws Exception {
    String timestamp = new ReviewCreateDialogAdapter().createTitleManager().getTimestamp();
    System.out.println("timestamp = " + timestamp);
  }

  public void testReviewCreateDialog() throws Exception {
    new ReviewCreateStandAloneDialog().execute();
  }

  public class ReviewCreateStandAloneDialog extends AbstractStandAloneDialog {
    private ReviewCreateDialogAdapter realDialog;

    protected JComponent getUserPane() {
      realDialog = new ReviewCreateDialogAdapter();
      JComponent userComp = realDialog.getUserPanel();
      realDialog.titleManager.computeLabel();
      realDialog.authorLabel.setText("the author login");
      ArrayList<String> members = new ArrayList<String>();
      members.add("user1");
      members.add("user2");
      members.add("user3");
      realDialog.setTeamMembers(members);

      return userComp;
    }

    protected void proceed() {
      System.out.println("Review Title = " + realDialog.getReviewTitle());
      System.out.println("Moderator    = " + realDialog.getModerator());
      System.out.println("Reviewer     = " + realDialog.getReviewer());
    }


  }


  public void testDialogWithToolbar() throws Exception {
    new ToolbarDialog().execute();
  }

  public class ToolbarDialog extends AbstractStandAloneDialog {
    JPopupMenu moreActionsMenu;
    JButton moreActionsButton;

    protected JComponent getUserPane() {
      JPanel userPane = new JPanel(new BorderLayout());
      JToolBar toolBar = new JToolBar();
      userPane.add(toolBar, BorderLayout.NORTH);


      moreActionsMenu = new JPopupMenu();

      JMenuItem uploadListItem = new JMenuItem("Upload list");
      uploadListItem.setToolTipText("Upload changes from a list of files");
      uploadListItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent actionEvent) {
          System.out.println("Upload list done");
        }
      });
      moreActionsMenu.add(uploadListItem);

      JMenuItem uploadChangesItem = new JMenuItem("Upload changeset");
      uploadChangesItem.setToolTipText("Upload changes from default change list");
      uploadChangesItem.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent actionEvent) {
          System.out.println("Upload changeset done");
        }
      });
      moreActionsMenu.add(uploadChangesItem);
      userPane.addMouseListener(new MouseAdapter() {

      });

      moreActionsButton = new JButton(">>");
      moreActionsButton.setToolTipText("More actions ...");
      moreActionsButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent actionEvent) {
          moreActionsMenu.show(moreActionsButton, 10, 10);
        }
      });

//      userPane.setComponentPopupMenu(moreActionsMenu);

      toolBar.add(moreActionsButton);

      userPane.add(new JLabel("CONTENT"), BorderLayout.CENTER);

      return userPane;
    }

    protected void proceed() {
      System.out.println("the end");
    }


  }


  public void testComboBoxEditor() throws Exception {
    new ComboBoxEditorTestDialog().execute();
  }

  public class ComboBoxEditorTestDialog extends AbstractStandAloneDialog {

    JComboBox cb = new JComboBox();
    ArrayList<String> items = new ArrayList<String>() {
    };
    private JButton editButton = new JButton("Edit");

    public ComboBoxEditorTestDialog() throws HeadlessException {
      items.add("aaa");
      items.add("bbb");
      items.add("ccc");
    }

    protected JComponent getUserPane() {

      JPanel userPane = new JPanel();
      userPane.add(cb);
      userPane.add(editButton);

      cbRefresh();
//      cb.setEditable(true);
//      cb.setEditor(new MyComboBoxEditor());
      editButton.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent actionEvent) {
          try {
            new ComboBoxEditorDialog().execute();
          } catch (Exception e) {
            e.printStackTrace();
          }

        }
      });

      return userPane;
    }


    protected void proceed() {
      System.out.println("RESULT = " + cb.getSelectedItem());
    }

    private void cbRefresh() {
      cb.removeAllItems();
      for (String item : items) {
        cb.addItem(item);
      }
      cb.setSelectedIndex(0);
      cb.repaint();
    }


    public class ComboBoxEditorDialog extends AbstractStandAloneDialog {
      JTextArea editor = new JTextArea(10, 10);

      protected JComponent getUserPane() {
        editor.setEditable(true);
        for (String item : items) {
          editor.append(item + '\n');
        }
        return editor;
      }


      protected void proceed() {
        items.clear();
        String[] editItems = editor.getText().split("\n");
        for (int i = 0; i < editItems.length; i++) {
          String editItem = editItems[i];
          if (!editItem.equals("")) {
            items.add(editItem);
          }
        }
        cbRefresh();
      }
    }

//    class MyComboBoxEditor implements ComboBoxEditor {
//
//      JTextField jtf = new JTextField();
//
//      public MyComboBoxEditor() {
//        jtf.addActionListener(new java.awt.event.ActionListener() {
//          public void actionPerformed(ActionEvent e) {
//            boolean found = false;
//            String inputStr = jtf.getText().trim();
//            for (int i = 0; i < items.size(); i++) {
//              String item = items.get(i);
//
//              if (inputStr.equals(item)) {
//                setItem(item);
//                cb.setSelectedIndex(i);
//                found = true;
//              }
//            }
//            if (!found && !inputStr.equals("")) {
//              items.add(inputStr);
//              cb.addItem(inputStr);
//              cb.setSelectedIndex(items.size()-1);
//            }
//            selectAll();
//          }
//        });
//
//        jtf.addMouseListener(new MouseListener() {
//          public void mouseClicked(MouseEvent e) {
//            selectAll();
//            cb.setPopupVisible(false);
//          }
//
//          public void mouseEntered(MouseEvent e) {
//          }
//
//          public void mouseExited(MouseEvent e) {
//          }
//
//          public void mousePressed(MouseEvent e) {
//            selectAll();
//            cb.setPopupVisible(false);
//          }
//
//          public void mouseReleased(MouseEvent e) {
//            selectAll();
//          }
//        });
//      }
//
//      public void addActionListener(ActionListener l) {
//      }
//
//      public JTextField getEditorComponent() {
//        return jtf;
//      }
//
//      public Object getItem() {
//        return jtf.getText();
//      }
//
//      public void removeActionListener(ActionListener l) {
//      }
//
//      public void selectAll() {
//        jtf.selectAll();
//        jtf.requestFocus();
//
//      }
//
//      public void setItem(Object anObject) {
//        jtf.setText(anObject.toString());
//      }
//    }
  }

}
