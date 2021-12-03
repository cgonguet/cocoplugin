package com.alu.oam.tools.idea.plugin.coco;

import com.smartbear.ccollab.datamodel.Defect;

import java.util.HashMap;
import java.util.HashSet;

public class DefectsManager {
  public static final String OVERALL = "__OVERALL__";


  private HashMap<String, HashSet<Defect>> defectsByFile = new HashMap<String, HashSet<Defect>>();

  public void reset() {
    defectsByFile = new HashMap<String, HashSet<Defect>>();
  }

  public void addDefect(String relativeFilePath, Defect defect) {
    if (defectsByFile.get(relativeFilePath) == null) {
      HashSet<Defect> defects = new HashSet<Defect>();
      defects.add(defect);
      defectsByFile.put(relativeFilePath, defects);
    } else {
      defectsByFile.get(relativeFilePath).add(defect);
    }
  }

  public void removeDefect(String filePath, Integer defectId) {
    HashSet<Defect> defects = defectsByFile.get(filePath);
    HashSet<Defect> newDefects = new HashSet<Defect>();
    for (Defect curDefect : defects) {
      if (curDefect.getId() != defectId) {
        newDefects.add(curDefect);
      }
    }
    defectsByFile.remove(filePath);
    defectsByFile.put(filePath, newDefects);
  }

  public void updateDefect(String filePath, Defect defect) {
    HashSet<Defect> defects = defectsByFile.get(filePath);
    HashSet<Defect> newDefects = new HashSet<Defect>();
    for (Defect curDefect : defects) {
      if (curDefect.getId() == defect.getId()) {
        newDefects.add(defect);
      } else {
        newDefects.add(curDefect);
      }
    }
    defectsByFile.remove(filePath);
    defectsByFile.put(filePath, newDefects);
  }

  public HashSet<Defect> getDefects(String filePath) {
    return defectsByFile.get(filePath);
  }
}
