package com.alu.oam.tools.idea.plugin.coco.dialogs;

import com.intellij.openapi.ui.Messages;
import com.smartbear.ccollab.datamodel.Review;
import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;

public class MessagesUtils {
  private static final String TITLE = "Code Collaborator";
  private static final String TITLE_ERROR = TITLE + " Error";

  public static final String NO_CURRENT_REVIEW = "No current review selected";

  public static void error(String msg){
    CodeCollaboratorManager.LOGGER.warn(msg);
    Messages.showErrorDialog(msg, TITLE_ERROR);
  }

  public static void error(Throwable ex){
    CodeCollaboratorManager.LOGGER.error(ex);
    Messages.showErrorDialog(ex.toString(), TITLE_ERROR);
  }

  public static void warning(String msg){
    CodeCollaboratorManager.LOGGER.warn(msg);
    Messages.showWarningDialog(msg, TITLE + " Warning");
  }

  public static boolean proceed(String msg){
    int yesNo = Messages.showYesNoDialog(msg + "\nDo you wish to continue ?", TITLE, null);
    return yesNo != 1;
  }
  
  public static void done(Review review, String action){
    CodeCollaboratorManager.LOGGER.info("Review " + review.getId() + " is now " + action);
    Messages.showInfoMessage("Review " + review.getId() + " is now " + action, TITLE);
  }

  public static void info(String info){
    CodeCollaboratorManager.LOGGER.info(info);
    Messages.showInfoMessage(info, TITLE);
  }
}
