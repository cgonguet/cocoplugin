package com.alu.oam.tools.idea.plugin.coco;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class UserDefinedFieldsManager {

  public static final String REVIEW_PREFIX = "Review-";
  public static final String DEFECT_PREFIX = "Defect-";

  static final String CONFIG_FILE = "config/user_defined_fields.properties";
  static final String COUPLE_SEPARATOR = ",";
  static final String KV_SEPARATOR = "=";

  private Properties properties = new Properties();
  private static UserDefinedFieldsManager theInstance;

  public static UserDefinedFieldsManager getInstance() {
    if (theInstance == null) {
      theInstance = new UserDefinedFieldsManager();
    }
    return theInstance;
  }

  private UserDefinedFieldsManager() {
    load();
  }

  public HashMap<String, Integer> getKVMap(String fieldName) {

    HashMap<String, Integer> kvMap = new HashMap<String, Integer>();

    String str = (String) properties.get(fieldName);
    if (str == null) {
      return kvMap;
    }
    for (String kv : str.split(COUPLE_SEPARATOR)) {
      String[] pair = kv.split(KV_SEPARATOR);
      if (pair.length != 2) {
        throw new RuntimeException("Error while reading property: " + fieldName);
      }
      kvMap.put(pair[0], Integer.valueOf(pair[1]));
    }
    return kvMap;
  }

  public String[] getKeys(String fieldName) {
    String str = (String) properties.get(fieldName);
    if (str == null) {
      return new String[0];
    }
    String[] kvs = str.split(COUPLE_SEPARATOR);
    String[] keys = new String[kvs.length];
    for (int i = 0; i < kvs.length; i++) {
      String kv = kvs[i];
      String[] pair = kv.split(KV_SEPARATOR);
      if (pair.length != 2) {
        throw new RuntimeException("Error while reading property: " + fieldName);
      }
      keys[i] = pair[0];
    }
    return keys;
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
