package com.alu.oam.tools.idea.plugin.coco.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: cgonguet
 * Date: 7 janv. 2008
 * Time: 13:45:00
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractAction extends AnAction implements ActionListener {
  protected Project project;
  protected Module module;

  public AbstractAction() {
    super();
  }

  public AbstractAction(Project project) {
    super();
    this.project = project;
  }

  public void actionPerformed(AnActionEvent e) {
    project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
    module = (Module) e.getDataContext().getData(DataConstants.MODULE);
    proceed();
  }

  public void actionPerformed(ActionEvent actionEvent) {
    proceed();
  }

  public abstract void proceed();

}
