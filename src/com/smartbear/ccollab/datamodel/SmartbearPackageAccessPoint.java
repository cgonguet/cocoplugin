package com.smartbear.ccollab.datamodel;

import com.alu.oam.tools.idea.plugin.coco.UserDefinedFieldsManager;

import java.util.List;

public class SmartbearPackageAccessPoint {

  public static Defect createDefect(Engine engine, User user, Review review, String filePath, Integer line,
                                    String comment, boolean markAsFixed,
                                    String severity, String type) {
    Version version = findVersion(review, filePath);
    Defect defect = engine.defectCreate(review, user, version, new LineLocator(line), comment);
    updateDefect(defect, comment, severity, type, markAsFixed);
    review.save();
    return defect;
  }

  public static void updateDefect(Defect defect, String text, String severity, String type, boolean markAsFixed) {
    defect.setText(text);
    // TODO - CG - 2009// - check defect fields
    defect.getUserDefinedFields()
            .setSelect("Severity", UserDefinedFieldsManager.getInstance().getKVMap("Defect-Severity").get(severity));
    defect.getUserDefinedFields()
            .setSelect("Type", UserDefinedFieldsManager.getInstance().getKVMap("Defect-Type").get(type));
    if (markAsFixed) {
      defect.setMarkedFixed();
    } else {
      defect.setMarkedOpen();
    }
    defect.save();
  }

  private static Version findVersion(Review review, String filePath) {
    List<Changelist> changelists = review.getChangelists();
    for (Changelist changelist : changelists) {
      Version version = Version.findVersionByFilePath(changelist, filePath, false);
      if (version != null) {
        return version;
      }
    }
    return null;
  }

  public static void completeReview(Engine engine, Review review) {
    review.setPhase(Phase.COMPLETED);
    review.save();
  }

//  severity: 52 = Major
//  severity: 53 = Minor
//  severity: 54 = Algorithm
//  severity: 55 = Build
//  severity: 56 = Data Access
//  severity: 57 = Documentation
//  severity: 58 = Error-Handling
//  severity: 59 = Interface
//  severity: 60 = Maintainability
//  severity: 61 = Performance
//  severity: 62 = Robustness
//  severity: 63 = Style
//  severity: 64 = Testing
//  severity: 65 = Text
}
