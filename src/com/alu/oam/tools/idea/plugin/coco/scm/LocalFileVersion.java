package com.alu.oam.tools.idea.plugin.coco.scm;

import com.alu.oam.tools.idea.plugin.coco.FilePathUtils;
import com.intellij.openapi.project.Project;
import com.smartbear.ccollab.datamodel.User;
import com.smartbear.cmdline.ScmCommunicationException;
import com.smartbear.scm.IScmClientConfiguration;
import com.smartbear.scm.IScmCommitInfo;
import com.smartbear.scm.IScmVersion;
import com.smartbear.scm.impl.local.LocalConfiguration;
import com.smartbear.scm.impl.none.NoneSystem;
import com.smartbear.scm.impl.none.ScmUncontrolledLocalCheckout;
import com.smartbear.util.Streams;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LocalFileVersion implements IScmVersion {
  private Project project;
  private File file;

  private LocalCommitInfo commitInfo;

  public LocalFileVersion(Project project, File file, User user) {
    this.project = project;
    this.file = file;

    commitInfo = new LocalCommitInfo(null, user);

  }

  public File getLocalPath() {
    return FilePathUtils.getRelativePath(project, file);
  }

  public boolean isLocalVersion() {
    return true;
  }

  public IScmClientConfiguration getClientConfiguration() {
    return new LocalConfiguration(null, file, false);
  }

  public String getDepotPath() {
    try {
      return new ScmUncontrolledLocalCheckout(NoneSystem.INSTANCE, getLocalPath())
              .getDepotPath(new NullProgressMonitor());
    } catch (ScmCommunicationException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getVersion() {
    return "local";
  }

  public String getFullyQualifiedVersion() {
    return getVersion();
  }

  public IScmCommitInfo getCommitInfo(IProgressMonitor iProgressMonitor) throws ScmCommunicationException {
    return commitInfo;
  }

  public byte[] getContent(int maxBytes, IProgressMonitor iProgressMonitor) throws ScmCommunicationException {
    iProgressMonitor.beginTask("Getting content of local file revision", 100);
    iProgressMonitor.subTask("Checking if file exists");
    if (!file.exists() || !file.isFile()) {
      return null;
    }
    byte contents[];

    try {
      contents = Streams.readFile(file, maxBytes, new SubProgressMonitor(iProgressMonitor, 100));
    }
    catch (IOException e) {

      throw new ScmCommunicationException(e);
    }
    iProgressMonitor.done();
    return contents;
  }

  public List<IScmVersion> getHistory(int i, IProgressMonitor iProgressMonitor) throws ScmCommunicationException {
    return null;
  }

}
