package com.alu.oam.tools.idea.plugin.coco.actions;

import com.alu.oam.tools.idea.plugin.coco.CodeCollaboratorManager;
import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.smartbear.beans.ConfigUtils;
import com.smartbear.beans.GlobalOptions;
import com.smartbear.beans.ISettableGlobalOptions;
import com.smartbear.ccollab.datamodel.Review;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 13 déc. 2007
 * Time: 16:22:07
 * To change this template use File | Settings | File Templates.
 */
public class ViewOnServerAction extends AbstractAction {


  public ViewOnServerAction() {
  }

  public ViewOnServerAction(Project project) {
    super(project);
  }

  public void proceed() {
    try {

      Review currentReview = CodeCollaboratorManager.getInstance().getCurrentReview();
      if (currentReview == null) {
        MessagesUtils.error(MessagesUtils.NO_CURRENT_REVIEW);
        return;
      }

      ISettableGlobalOptions options = GlobalOptions.copy(ConfigUtils.loadConfigFiles().getKey());
      BrowserUtil.launchBrowser(
              options.getUrl().toString() + "/index.jsp?page=ReviewDisplay&reviewid=" + currentReview.getId());

    } catch (Exception ex) {
      MessagesUtils.error(ex);
    }
  }


}
