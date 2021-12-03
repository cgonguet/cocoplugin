package com.alu.oam.tools.idea.plugin.coco;

import com.alu.oam.tools.idea.plugin.coco.dialogs.MessagesUtils;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.*;
import com.smartbear.beans.ConfigUtils;
import com.smartbear.beans.GlobalOptions;
import com.smartbear.beans.ISettableGlobalOptions;
import com.smartbear.beans.ValidateBean;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 13 déc. 2007
 * Time: 16:20:01
 * To change this template use File | Settings | File Templates.
 */


public class CoCoComponent implements ApplicationComponent, Configurable, JDOMExternalizable {
  private CoCoComponentConfigurationForm form;
  boolean shouldResetServer;
  public String reviewProduct;
  public String reviewRelease;
  public String reviewType;
  public String reviewModule;
  public ArrayList<String> reviewModuleList;
  public String defectSeverity;
  public String defectType;
  public boolean defectFixed;
  ISettableGlobalOptions options;
  public String teamMembersCSV = "";

  public CoCoComponent() {
    shouldResetServer = false;
  }

  public static CoCoComponent getInstance() {
    Application application = ApplicationManager.getApplication();
    return application.getComponent(CoCoComponent.class);
  }

  public void initComponent() {
    try {
      options = GlobalOptions.copy(ConfigUtils.loadConfigFiles().getKey());
      if (reviewModuleList == null) {
        reviewModuleList = new ArrayList<String>();
        String[] configModules = ProjectPropertiesManager.getInstance().getValues("Review-Module");
        for (int i = 0; i < configModules.length; i++) {
          String configModule = configModules[i];
          reviewModuleList.add(configModule);
        }
      }
    } catch (IOException e) {
      CodeCollaboratorManager.LOGGER.error(e);
    }
  }

  public void disposeComponent() {
    // TODO: insert component disposal logic here
  }

  public String getComponentName() {
    return "CoCoPlugin";
  }

  @Nls
  public String getDisplayName() {
    return "Code Collaborator";
  }

  public Icon getIcon() {
    return IconLoader.getIcon("icon/smartbear_32x32.png");
  }

  @Nullable
  @NonNls
  public String getHelpTopic() {
    return null;
  }

  public JComponent createComponent() {
    if (form == null) {
      form = new CoCoComponentConfigurationForm();

    }
    return form.getRootComponent();
  }

  public boolean isModified() {
    return form != null && form.isModified(this);
  }

  public void apply() throws ConfigurationException {
    if (form != null) {
      try {
        form.getData(this);
        if (shouldResetServer) {
          CodeCollaboratorManager.reset();
          shouldResetServer = false;
        }
      } catch (IOException e) {
        MessagesUtils.error("Config error: " + e.toString());
      } catch (ValidateBean.ValidationException e) {
        MessagesUtils.error("Validation Error: " + e.toString());
      } catch (GeneralSecurityException e) {
        MessagesUtils.error("Security Error: " + e.toString());
      }
    }
  }

  public void reset() {
    if (form != null) {
      form.setData(this);
    }
  }

  public void disposeUIResources() {
    form = null;
  }

  public String getServerUrl() {
    if (options.getUrl() == null) {
      return "";
    }
    return options.getUrl().toString();
  }

  public void setServerUrl(final String serverUrl)
          throws IOException, ValidateBean.ValidationException, GeneralSecurityException {
    if (serverUrl != null ? !serverUrl.equals(getServerUrl()) : getServerUrl() != null) {
      shouldResetServer = true;
      options.setUrl(new URL(serverUrl));
      ConfigUtils.saveConfig("url", getServerUrl());
    }
  }

  public String getServerUserLogin() {
    return options.getUser();
  }

  public void setServerUserLogin(final String serverUserLogin)
          throws IOException, ValidateBean.ValidationException, GeneralSecurityException {
    if (serverUserLogin != null ? !serverUserLogin.equals(getServerUserLogin()) : getServerUserLogin() != null) {
      shouldResetServer = true;
      options.setUser(serverUserLogin);
      ConfigUtils.saveConfig("user", getServerUserLogin());
    }
  }

  public String getServerUserPassword() {
    return options.getPassword();
  }

  public void setServerUserPassword(final char[] serverUserPassword)
          throws IOException, ValidateBean.ValidationException, GeneralSecurityException {
    if (serverUserPassword != null ? !String.valueOf(serverUserPassword).equals(getServerUserPassword()) :
        getServerUserPassword() != null) {
      shouldResetServer = true;
      options.setPassword(String.valueOf(serverUserPassword));
      ConfigUtils.saveConfig("password", getServerUserPassword());
    }
  }

  public String getDefectSeverity() {
    return defectSeverity;
  }

  public String getDefectType() {
    return defectType;
  }

  public boolean getDefectFixed() {
    return defectFixed;
  }

  public void setDefectSeverity(String s) {
    defectSeverity = s;
  }

  public void setDefectType(String s) {
    defectType = s;
  }

  public void setDefectFixed(boolean b) {
    defectFixed = b;
  }


  public String getReviewProduct() {
    return reviewProduct;
  }

  public void setReviewProduct(String reviewProduct) {
    this.reviewProduct = reviewProduct;
  }

  public String getReviewRelease() {
    return reviewRelease;
  }

  public void setReviewRelease(String reviewRelease) {
    this.reviewRelease = reviewRelease;
  }

  public String getReviewType() {
    return reviewType;
  }

  public void setReviewType(String reviewType) {
    this.reviewType = reviewType;
  }

  public String getReviewModule() {
    return reviewModule;
  }

  public void setReviewModule(String s) {
    reviewModule = s;
  }

  public ArrayList<String> getReviewModuleList() {
    return reviewModuleList;
  }

  public void setReviewModuleList(Collection<String> reviewModules) {
    this.reviewModuleList.clear();
    this.reviewModuleList.addAll(reviewModules);
  }

  public String getTeamMembers() {
    return teamMembersCSV;
  }

  public void setTeamMembers(String csv) {
    teamMembersCSV = csv;
  }

  static final String ELEMENT_REVIEWMODULELIST = "ELEMENT_REVIEWMODULELIST";

  public void readExternal(Element element) throws InvalidDataException {
    DefaultJDOMExternalizer.readExternal(this, element);

    final Element parentElement = element.getChild(ELEMENT_REVIEWMODULELIST);
    if (parentElement == null) {
      return;
    }

    reviewModuleList = new ArrayList<String>();
    for (Object child : parentElement.getChildren()) {
      try {
        Element entryElement = (Element) child;
        JDOMExternalizableString item = new JDOMExternalizableString();
        DefaultJDOMExternalizer.readExternal(item, entryElement);
        reviewModuleList.add(item.val);
      } catch (NumberFormatException ex) {
        Logger.getInstance(CoCoComponent.class.getName()).error("Read saved config failure", ex);
      } catch (ArrayIndexOutOfBoundsException ex) {
        Logger.getInstance(CoCoComponent.class.getName()).error("Read saved config failure", ex);
      }
    }
  }

  public void writeExternal(Element element) throws WriteExternalException {

    DefaultJDOMExternalizer.writeExternal(this, element);

    if (reviewModuleList == null || reviewModuleList.isEmpty()) {
      return;
    }

    Element parentElement = new Element(ELEMENT_REVIEWMODULELIST);
    int i = 0;
    for (String reviewModuleItem : reviewModuleList) {
      Element entryElement = new Element(ELEMENT_REVIEWMODULELIST + i++);
      DefaultJDOMExternalizer.writeExternal(JDOMExternalizableString.convert(reviewModuleItem), entryElement);
      parentElement.addContent(entryElement);
    }
    element.addContent(parentElement);
  }

  static class JDOMExternalizableString implements JDOMExternalizable {
    public String val;

    public static JDOMExternalizableString convert(String s) {
      return new JDOMExternalizableString(s);
    }

    public JDOMExternalizableString() {
    }

    private JDOMExternalizableString(String s) {
      this.val = s;
    }

    public void readExternal(Element element) throws InvalidDataException {
      DefaultJDOMExternalizer.readExternal(this, element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
      DefaultJDOMExternalizer.writeExternal(this, element);
    }
  }

}
