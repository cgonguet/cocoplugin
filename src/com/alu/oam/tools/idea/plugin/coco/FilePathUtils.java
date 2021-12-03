package com.alu.oam.tools.idea.plugin.coco;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 15 févr. 2008
 * Time: 15:13:22
 * To change this template use File | Settings | File Templates.
 */
public class FilePathUtils {
  private static final String PROJECT_ROOT_HACK = "P:";

  public static String getRelativePath(Project project, String path) {
    return PROJECT_ROOT_HACK + getRelativePathToProject(project, path);
  }

  public static File getRelativePath(Project project, File file) {
    return new File(getRelativePath(project, file.getPath()));
  }

  public static String getAbsolutePath(Project project, String path) {
    return getProjectPath(project) + path.replaceFirst(PROJECT_ROOT_HACK, "").replace('\\', '/');
  }

  public static String getProjectPath(Project project) {
    String projectFilePath = project.getProjectFilePath();
    String projectPath = projectFilePath.replaceFirst(project.getProjectFile().getName(), "");
    return projectPath.replace('\\', '/');
  }

  public static String getRelativePathToProject(Project project, String path) {
    return path.replace('\\', '/').replaceFirst(getProjectPath(project), "/");
  }

  public static VirtualFile getVirtualFile(Project project, String relativeFilePath) {
    String absFilePath = FilePathUtils.getAbsolutePath(project, relativeFilePath);
    VirtualFile vFile = VirtualFileManager.getInstance().findFileByUrl("file://" + absFilePath);
    if (vFile != null) {
      return vFile;
    }
    // The given file was not relative.
    // Try to open it as it was already absolute.
    return VirtualFileManager.getInstance().findFileByUrl("file://" + relativeFilePath);
  }

}
