package com.alu.oam.tools.idea.plugin.coco;

import com.alu.oam.tools.idea.plugin.coco.actions.DefectUpdateAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.smartbear.ccollab.datamodel.Defect;
import com.smartbear.ccollab.datamodel.LineLocator;
import com.smartbear.ccollab.datamodel.LocatorType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 21 janv. 2008
 * Time: 10:46:58
 * To change this template use File | Settings | File Templates.
 */
public class DefectHighlighter implements FileEditorManagerListener {
  private Project project;


  public DefectHighlighter(Project project) {
    this.project = project;
  }

  public void fileOpened(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
    FileEditor fileEditor = fileEditorManager.getSelectedEditor(virtualFile);
    if (fileEditor instanceof TextEditor) {
      highlightFile(((TextEditor) fileEditor).getEditor(), virtualFile);
    }
  }

  public void fileClosed(FileEditorManager fileEditorManager, VirtualFile virtualFile) {
    FileEditor fileEditor = fileEditorManager.getSelectedEditor(virtualFile);
    if (fileEditor instanceof TextEditor) {
      removeHighlight(((TextEditor) fileEditor).getEditor());
    }
  }

  public void selectionChanged(FileEditorManagerEvent fileEditorManagerEvent) {
  }

  private void removeHighlight(Editor editor) {
    MarkupModel markupModel = editor.getMarkupModel();
    RangeHighlighter[] highlighters = markupModel.getAllHighlighters();
    for (RangeHighlighter highlighter : highlighters) {
      if (isDefectHighlighter(highlighter)) {
        markupModel.removeHighlighter(highlighter);
      }
    }
  }

  private boolean isDefectHighlighter(RangeHighlighter rangeHighlighter) {
    return rangeHighlighter.getGutterIconRenderer() != null
           && rangeHighlighter.getGutterIconRenderer().getTooltipText() != null
           && rangeHighlighter.getGutterIconRenderer().getTooltipText().startsWith("Defect");
  }


  private void highlightFile(Editor editor, VirtualFile virtualFile) {
    try {
      String filePath = virtualFile.getPath();
      String relFilePath = FilePathUtils.getRelativePath(project, filePath);
      HashSet<Defect> defects = CodeCollaboratorManager.getInstance().getDefects(relFilePath);
      if (defects == null || defects.isEmpty()) {
        return;
      }
      removeHighlight(editor);
      MarkupModel markupModel = editor.getMarkupModel();
      for (Defect defect : defects) {
        highlightDefect(defect, markupModel, filePath);
      }

    } catch (Exception e) {
      CodeCollaboratorManager.LOGGER.error(e);
    }
  }

  public void highlightDefect(Defect defect, MarkupModel markupModel, String filePath) throws Exception {
    int line = getDefectLine(defect);
    int maxLine = markupModel.getDocument().getLineCount();
    if (line >= maxLine) {
      line = maxLine - 1;
    }
    RangeHighlighter rangeHighlighter =
            markupModel.addLineHighlighter(line, HighlighterLayer.FIRST, null);
    rangeHighlighter.setErrorStripeMarkColor(Color.ORANGE);
    rangeHighlighter.setErrorStripeTooltip(defect);
    addGutterIcon(rangeHighlighter, filePath, defect);
  }

  public static int getDefectLine(Defect defect) {
    int line = 0;
    if (defect.getLocatorType().equals(LocatorType.LINE)) {
      LineLocator lineLocator = (LineLocator) defect.getLocator();
      if (lineLocator.getLineNumber() > 0) {
        line = lineLocator.getLineNumber() - 1;
      }
    }
    return line;
  }

  public void addGutterIcon(RangeHighlighter rangeHighlighter, final String filePath, final Defect defect) {
    rangeHighlighter.setGutterIconRenderer(new GutterIconRenderer() {
      @NotNull
      public Icon getIcon() {
        if (defect.isMarkedFixed()) {
          return IconLoader.getIcon("icon/bug_green.gif");
        }
        return IconLoader.getIcon("icon/bug_red.gif");
      }

      public String getTooltipText() {
        return "Defect " + defect.getName();
      }

      public boolean isNavigateAction() {
        return true;
      }

      public AnAction getClickAction() {
        return new DefectUpdateAction(FilePathUtils.getRelativePath(project, filePath), defect);
      }
    });
  }

  public void register() {
    FileEditorManager editorManager = FileEditorManager.getInstance(project);
    editorManager.addFileEditorManagerListener(this);
    for (VirtualFile file : editorManager.getOpenFiles()) {
      fileOpened(editorManager, file);
    }
  }

  public void unregister() {
    FileEditorManager editorManager = FileEditorManager.getInstance(project);
    editorManager.removeFileEditorManagerListener(this);
    for (VirtualFile file : editorManager.getOpenFiles()) {
      fileClosed(editorManager, file);
    }
  }

}
