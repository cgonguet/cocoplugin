package com.alu.oam.tools.idea.plugin.coco;

import java.util.HashSet;
import java.util.Set;

public class ChangesManager {
  private Set<String> changeFiles = new HashSet<String>();

  public void reset() {
    changeFiles = new HashSet<String>();
  }

  public void add(String filePath) {
    changeFiles.add(filePath);
  }

  public boolean isManaged(String filePath) {
    return changeFiles.contains(filePath);
  }
}
