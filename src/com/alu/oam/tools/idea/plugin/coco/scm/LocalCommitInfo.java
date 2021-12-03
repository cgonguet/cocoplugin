package com.alu.oam.tools.idea.plugin.coco.scm;

import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.Change;
import com.smartbear.ccollab.datamodel.User;
import com.smartbear.scm.IScmCommitInfo;

import java.util.Date;

public class LocalCommitInfo implements IScmCommitInfo {
  private Change change;
  private User user;


  public LocalCommitInfo(Change change, User user) {
    this.change = change;
    this.user = user;
  }

  public State getState() {
    if (change != null) {
      return convertFileStatus(change.getFileStatus());
    }
    return IScmCommitInfo.State.UNKNOWN;
  }

  public String getAuthor() {
    return user.getLogin();
  }

  public Date getDate() {
    return new Date();
  }

  public String getComment() {
    return "";
  }

  public Integer getAtomicTransactionId() {
    return 0;
  }

  private static IScmCommitInfo.State convertFileStatus(FileStatus fileStatus) {
    if (FileStatus.ADDED.equals(fileStatus)) {
      return IScmCommitInfo.State.ADDING;
    }
    if (FileStatus.DELETED.equals(fileStatus)) {
      return IScmCommitInfo.State.DELETING;
    }
    return IScmCommitInfo.State.CONTROLLED;
  }


}
