package com.alu.oam.tools.idea.plugin.coco;

import com.alu.oam.tools.idea.plugin.coco.scm.LocalCheckout;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.smartbear.beans.ConfigUtils;
import com.smartbear.beans.GlobalOptions;
import com.smartbear.beans.ISettableGlobalOptions;
import com.smartbear.ccollab.client.LoginUtils;
import com.smartbear.ccollab.client.NullClient;
import com.smartbear.ccollab.datamodel.*;
import com.smartbear.ccollab.datamodel.displaymodel.ChatThread;
import com.smartbear.cmdline.ScmCommunicationException;
import com.smartbear.scm.ScmChangeset;
import com.smartbear.util.Hashing;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CodeCollaboratorManager {
  public static final Logger LOGGER = Logger.getInstance("CoCoPlugin");

  private static CodeCollaboratorManager theInstance = null;
  public static boolean reviewInProgress = false;

  private static boolean firstCall;
  private Engine engine = null;
  private User localUser = null;
  protected Review review = null;
  private DefectsManager defectsMgr = new DefectsManager();
  private ChangesManager changesMgr = new ChangesManager();
  private Set<String> bufferedFiles = new TreeSet<String>();

  private CodeCollaboratorManager() throws Exception {
    init();
    firstCall = true;
  }

  public static CodeCollaboratorManager getInstance() throws Exception {
    if (theInstance == null) {
      theInstance = new CodeCollaboratorManager();
      return theInstance;
    }
    firstCall = false;
    return theInstance;
  }

  public static boolean isFirstCall() {
    return firstCall;
  }

  public static void reset() {
    theInstance = null;
    reviewInProgress = false;
  }

  public void init() throws Exception {
    ISettableGlobalOptions options = GlobalOptions.copy(ConfigUtils.loadConfigFiles().getKey());
    localUser = LoginUtils.login(options, new NullClient());
    engine = localUser.getEngine();
    LOGGER.info("init done");
  }

  public void finished() {
    review.save();
    engine.close(true);
    defectsMgr.reset();
    changesMgr.reset();
    clearBufferedFiles();
    reset();
  }

  public User getUser() {
    return localUser;
  }

  public Review getCurrentReview() {
    return review;
  }

  public Review createReview(String title, String activity, String product, String release, String type, String module)
          throws DataModelException, IOException {
    review = engine.reviewCreate(localUser, title);

    review.getUserDefinedFields().setSelect("Product",
                                            UserDefinedFieldsManager.getInstance().getKVMap("Review-Product").get(
                                                    product));
    review.getUserDefinedFields().setSelect("Release",
                                            UserDefinedFieldsManager.getInstance().getKVMap("Review-Release").get(
                                                    release));
    review.getUserDefinedFields().setSelect("Type",
                                            UserDefinedFieldsManager.getInstance().getKVMap("Review-Type").get(type));
    review.getUserDefinedFields().setString("Activity", activity);
    review.getUserDefinedFields().setString("Team", module);

    review.save();
    defectsMgr.reset();
    changesMgr.reset();
    clearBufferedFiles();
    LOGGER.info("Create review: " + review);
    reviewInProgress = true;
    return review;
  }

  public Review switchReview(Review newReview) throws DataModelException, IOException {
    review = newReview;
    if (review == null) {
      return null;
    }
    reviewInProgress = true;
    loadDefects();
    loadChanges();
    clearBufferedFiles();
    LOGGER.info("Switch review: " + review);
    return review;
  }

  public User getUserByLogin(String login) {
    return engine.userByLogin(login);
  }

  public void addParticipant(User moderator, User reviewer) {
    review.addParticipants(new ReviewParticipant(moderator, getRoleById(Role.MODERATOR_ID)),
                           new ReviewParticipant(reviewer, getRoleById(Role.REVIEWER_ID))
    );
  }

  private Role getRoleById(int id) {
    List<Role> roleList = engine.rolesFind(null, true);
    for (Role role : roleList) {
      if (role.getId().equals(id)) {
        return role;
      }
    }
    return null;
  }


  public void attachFileList(Project project, Collection<String> fileList, IProgressMonitor progressMonitor)
          throws Exception {

    ScmChangeset changeset = new ScmChangeset();

    for (String filePath : fileList) {
      File file = new File(FilePathUtils.getProjectPath(project) + File.separator + filePath);
      if (!file.exists() || !file.isFile()) {
        LOGGER.warn("Could not attach this ghost file: " + file.getAbsolutePath());
        continue;
      }

      Change change = ChangeListManager.getInstance(project).getChange(FilePathUtils.getVirtualFile(project, filePath));
      if (change == null) {
        LOGGER.info("Attach uncontrolled file: " + file);
        changeset.addLocalCheckout(new LocalCheckout(project, file, localUser), false, new NullProgressMonitor());
      } else {
        LOGGER.info("Attach change file: " + file);
        addChangeToChangeSet(project, changeset, change, progressMonitor);
      }
    }


    Scm scm = engine.scmForDiffs(null, null);
    Changelist changelist = scm.uploadChangeset(false, changeset, Hashing.getGuid(),
                                                null, new Date(),
                                                localUser.getLogin(), "Local files",
                                                null, progressMonitor);
    scm.save();
    review.addChangelist(changelist, localUser);

    review.save();
    engine.saveAll();

    for (Version version : changelist.getVersions()) {
      changesMgr.add(FilePathUtils.getRelativePath(project, version.getFilePath()));
    }

  }

  public void attachChangeList(Project project,
                               Collection<Change> changes,
                               String comment,
                               IProgressMonitor progressMonitor)
          throws Exception {

    if (review == null) {
      LOGGER.error("attachChangeList - review is not selected");
      return;
    }

    ScmChangeset changeset = new ScmChangeset();

    for (Change change : changes) {
      addChangeToChangeSet(project, changeset, change, progressMonitor);
    }

    LOGGER.debug("Uploading SCM Changeset...");
    Scm scm = engine.scmForDiffs(null, null);
    Changelist changelist = scm.uploadChangeset(false, changeset, Hashing.getGuid(),
                                                null, new Date(),
                                                localUser.getLogin(), comment,
                                                null, progressMonitor);
    scm.save();
    LOGGER.debug("Add Changeset to review");
    review.addChangelist(changelist, localUser);

    review.save();
    engine.saveAll();

    for (Version version : changelist.getVersions()) {
      changesMgr.add(version.getFilePath());
    }
  }

  private void addChangeToChangeSet(Project project, ScmChangeset changeset, Change change,
                                    IProgressMonitor progressMonitor)
          throws ScmCommunicationException {
    if (change.getFileStatus().equals(FileStatus.DELETED)) {
      LOGGER.debug("Attach deleted file: " + change.getBeforeRevision().getFile().getPath());
      changeset.addLocalCheckout(new LocalCheckout(project, change, localUser), false, new NullProgressMonitor());
      return;
    }

    if (change.getAfterRevision().getFile().isDirectory()) {
      LOGGER.debug("Attach directory (not upload): " + change.getAfterRevision().getFile().getPath());
      return;
    }

    LOGGER.debug("Attach file with status: " + change.getFileStatus());
    changeset.addLocalCheckout(new LocalCheckout(project, change, localUser), false, new NullProgressMonitor());
  }

  public List<Review> getReviews(LocalChangeList vcsChangeList) {
    if (localUser == null) {
      return null;
    }
    return localUser.getReviewsCanUploadChangelists(vcsChangeList.getName());
  }

  private void loadChanges() {
    changesMgr.reset();
    List<Changelist> changelists = review.getChangelists();
    for (Changelist changelist : changelists) {
      for (Version version : changelist.getVersions()) {
        changesMgr.add(version.getFilePath());
      }
    }
  }

  public boolean isAllowedFile(String filePath) {
    return changesMgr.isManaged(filePath);
  }

  public Defect createDefect(Project project, String filePath, int line, String creator, String comment,
                             boolean markAsFixed,
                             String severity,
                             String type) {
    User creatorUser = getUserByLogin(creator);
    Defect defect = SmartbearPackageAccessPoint.createDefect(engine, creatorUser, review,
                                                             filePath, line, comment, markAsFixed, severity,
                                                             type);
    defectsMgr.addDefect(filePath, defect);
    engine.saveAll();
    return defect;
  }


  public void removeDefect(String filePath, Defect defect) {
    Integer defectId = defect.getId();
    defect.delete();
    review.save();
    engine.saveAll();
    defectsMgr.removeDefect(filePath, defectId);
  }


  public void updateDefect(String filePath, Defect defect, String text, String severity, String type,
                           boolean markAsFixed) {
    SmartbearPackageAccessPoint.updateDefect(defect, text, severity, type, markAsFixed);
    defectsMgr.updateDefect(filePath, defect);
    review.save();
    engine.saveAll();
  }


  public void loadDefects() {
    defectsMgr.reset();
    List<ChatThread> conversations = review.getConversations().getAllThreads(false);
    for (ChatThread conversation : conversations) {
      addDefectsByFile(conversation);
    }
  }

  private void addDefectsByFile(ChatThread conversation) {
    String filePath;
    Version version = conversation.getVersion();
    if (version == null) {
      filePath = DefectsManager.OVERALL;
    } else {
      filePath = version.getFilePath();
    }
    List<Defect> defects = conversation.getDefects();
    for (Defect defect : defects) {
      LOGGER.debug("Add defects: " + defect.getName() + " for " + filePath);
      defectsMgr.addDefect(filePath, defect);
    }
  }

  public HashSet<Defect> getDefects(String filePath) {
    return defectsMgr.getDefects(filePath);
  }

  public void completeReview() {
    LOGGER.info("Complete review: " + review);
    SmartbearPackageAccessPoint.completeReview(engine, review);
    finished();
  }

  public String getBufferedFiles() {
    String out = "";
    for (String file : bufferedFiles) {
      out += file + "\n";
    }
    return out;
  }

  public void clearBufferedFiles() {
    bufferedFiles.clear();
  }

  public void addBufferedFile(Project project, String path) {
    bufferedFiles.add(FilePathUtils.getRelativePathToProject(project, path).replaceFirst("/", ""));
  }


}
