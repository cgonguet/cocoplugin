package com.alu.oam.tools.idea.plugin.coco.scm;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.smartbear.ccollab.datamodel.User;
import com.smartbear.cmdline.ScmCommunicationException;
import com.smartbear.scm.IScmClientConfiguration;
import com.smartbear.scm.IScmCommitInfo;
import com.smartbear.scm.IScmLocalCheckout;
import com.smartbear.scm.IScmVersion;
import org.eclipse.core.runtime.IProgressMonitor;

import java.io.File;

public class LocalCheckout implements IScmLocalCheckout {

  private IScmVersion scmVersion;

  public LocalCheckout(Project project, Change change, User user) {
    scmVersion = new LocalChangeVersion(project, change, user);
  }

  public LocalCheckout(Project project, File file, User user) {
    scmVersion = new LocalFileVersion(project, file, user);
  }

  public IScmClientConfiguration getClientConfiguration() {
    return scmVersion.getClientConfiguration();
  }

  public File getLocalPath() {
    return scmVersion.getLocalPath();
  }

  public byte[] getLocalContent(int i, IProgressMonitor iProgressMonitor) throws ScmCommunicationException {
    return scmVersion.getContent(i, iProgressMonitor);
  }

  public String getDepotPath(IProgressMonitor iProgressMonitor) throws ScmCommunicationException {
    return scmVersion.getDepotPath();
  }

  public String getLocalVersionName(IProgressMonitor iProgressMonitor) throws ScmCommunicationException {
    return scmVersion.getVersion();
  }

  public IScmVersion getBaseVersion(IProgressMonitor iProgressMonitor) throws ScmCommunicationException {
    return scmVersion;
  }

  public IScmCommitInfo.State getState(IProgressMonitor iProgressMonitor) throws ScmCommunicationException {
    return scmVersion.getCommitInfo(iProgressMonitor).getState();
  }

  public boolean isLocallyModified(IProgressMonitor iProgressMonitor) throws ScmCommunicationException {
    return true;
  }


}
