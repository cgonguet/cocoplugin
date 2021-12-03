package com.alu.oam.tools.idea.plugin.coco.scm;

import com.alu.oam.tools.idea.plugin.coco.FilePathUtils;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
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

public class LocalChangeVersion implements IScmVersion {
  private Project project;
  private Change change;
  private File beforeFile;
  private File afterFile;
  private LocalCommitInfo commitInfo;

  public LocalChangeVersion(Project project, Change change, User user) {
    this.project = project;
    this.change = change;

    commitInfo = new LocalCommitInfo(change, user);

    if (change.getBeforeRevision() != null) {
      beforeFile = change.getBeforeRevision().getFile().getIOFile();
    }
    if (change.getAfterRevision() != null) {
      afterFile = change.getAfterRevision().getFile().getIOFile();
    }

  }

  public File getLocalPath() {
    if (afterFile != null) {
      return FilePathUtils.getRelativePath(project, afterFile);
    }
    return FilePathUtils.getRelativePath(project, beforeFile);
  }

  public boolean isLocalVersion() {
    return true;
  }

  public IScmClientConfiguration getClientConfiguration() {
    return new LocalConfiguration(beforeFile, afterFile, false);
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
    if (change != null) {
      return change.getBeforeRevision() != null ? change.getBeforeRevision().getRevisionNumber().asString() : "added";
    }
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
    File file;
    if (afterFile != null) {
      file = afterFile;
    } else {
      file = beforeFile;
    }
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
