package com.alu.oam.tools.idea.plugin.coco;

import com.alu.oam.tools.idea.plugin.coco.actions.ReviewCreateAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.CommitSession;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public class CoCoCommitExecutorComponent implements ProjectComponent {
  private Project project;

  public CoCoCommitExecutorComponent(Project project) {
    this.project = project;
  }

  public void initComponent() {
    ChangeListManager.getInstance(project).registerCommitExecutor(new CodeReviewCommitExecutor());

  }

  public void disposeComponent() {
  }

  @NotNull
  public String getComponentName() {
    return "CoCoCommitExecutorComponent";
  }

  public void projectOpened() {
  }

  public void projectClosed() {
    project = null;
  }


  class CodeReviewCommitExecutor implements CommitExecutor {

    @NotNull
    public Icon getActionIcon() {
      return IconLoader.getIcon("icon/smartbear_16x16.png");
    }

    @Nls
    public String getActionText() {
      return "Code Review";
    }

    @Nls
    public String getActionDescription() {
      return "Create a new Code Collaborator review and upload the selected files";
    }

    @NotNull
    public CommitSession createCommitSession() {
      return new CodeReviewCommitSession();
    }
  }

  class CodeReviewCommitSession implements CommitSession {

    @Nullable
    public JComponent getAdditionalConfigurationUI() {
      return null;
    }

    public JComponent getAdditionalConfigurationUI(Collection<Change> changes, String s) {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean canExecute(Collection<Change> collection, String string) {
      return true;
    }

    public void execute(final Collection<Change> changes, final String comment) {

      // IDEABKL-2756
      // Current threading architecture requires any write action to be run in Swing dispatch thread.
      // This makes impossible to provide a progress indicator to any lengthy operations.
      ApplicationManager.getApplication()
              .invokeAndWait(new ReviewCreateAction.Runner(project, new ReviewCreateAction.UploadActionRunner() {

                // The upload action should be launch without the progress bar
                public void run(Project project) throws Exception {
                  CodeCollaboratorManager.getInstance()
                          .attachChangeList(project,
                                            changes,
                                            comment,
                                            new NullProgressMonitor());
                }
              }),
                             ModalityState.defaultModalityState());

    }

    public void executionCanceled() {
    }

  }
}
