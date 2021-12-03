package com.alu.oam.tools.idea.plugin.coco;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 14 janv. 2008
 * Time: 10:37:51
 * To change this template use File | Settings | File Templates.
 */
public class ProgressCommandManager {
  private ProgressIndicator progress;
  private Project project;
  public Logger logger;
  private Exception caughtException;
  private boolean hasError = false;
  private boolean status;

  public ProgressCommandManager(Project project) {
    this.project = project;
  }

  public boolean execute(final String title, final ProgressRunnable cmd) throws Exception {
    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        try {
          logger = Logger.getInstance("ProgressCommandManager");
          logger.info("Run task: " + title);
          progress = ProgressManager.getInstance().getProgressIndicator();
          progress.setIndeterminate(true);
          progress.startNonCancelableSection();
          status = cmd.run(new IProgressMonitor() {
            public void beginTask(String name, int i) {
              progress.setText(name);
              logger.debug("beginTask: " + name);
            }

            public void done() {
              progress.cancel();
            }

            public void internalWorked(double d) {
            }

            public boolean isCanceled() {
              return false;
            }

            public void setCanceled(boolean flag) {
            }

            public void setTaskName(String s) {
            }

            public void subTask(String s) {
              progress.setText(s);
              logger.debug("subtask: " + s);
            }

            public void worked(int i) {
            }
          });
        }
        catch (Exception ex) {
          hasError = true;
          logger.error("Error while executing command: " + title, ex);
          if (progress == null) {
            caughtException = ex;
          } else {
            caughtException = new Exception(progress.getText() + " - " + progress.getText2(), ex);
          }
        }
      }
    }, title, false, project);

    if (hasError) {
      throw caughtException;
    }
    return status;
  }

  public static interface ProgressRunnable {
    boolean run(IProgressMonitor progressMonitor) throws Exception;
  }
}
