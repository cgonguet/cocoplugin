package com.alu.oam.tools.idea.plugin.coco;

import java.io.IOException;
import java.util.Properties;

public class ProjectPropertiesManager {

  static final String CONFIG_FILE = "config/oam_project.properties";
  static final String LIST_SEPARATOR = ",";

  private Properties properties = new Properties();
  private static ProjectPropertiesManager theInstance;

  public static ProjectPropertiesManager getInstance() {
    if (theInstance == null) {
      theInstance = new ProjectPropertiesManager();
    }
    return theInstance;
  }

  private ProjectPropertiesManager() {
    load();
  }

  public String[] getValues(String property) {
    String str = (String) properties.get(property);
    if (str == null) {
      return new String[0];
    }
    return str.split(LIST_SEPARATOR);
  }

  public String getValue(String property) {
    return (String) properties.get(property);
  }

  private void load() {
    properties = new Properties();
    try {
      properties.load(getClass().getClassLoader().getResourceAsStream(CONFIG_FILE));
    } catch (IOException e) {
      System.out.println("Could not read properties file: " + CONFIG_FILE);
    }
  }
}
