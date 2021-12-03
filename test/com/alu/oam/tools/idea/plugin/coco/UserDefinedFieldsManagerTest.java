package com.alu.oam.tools.idea.plugin.coco;

import com.smartbear.beans.ConfigUtils;
import com.smartbear.beans.GlobalOptions;
import com.smartbear.beans.ISettableGlobalOptions;
import com.smartbear.ccollab.client.NullClient;
import com.smartbear.ccollab.datamodel.*;
import com.smartbear.ssl.IUserConfirmation;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;


public class UserDefinedFieldsManagerTest extends TestCase {


  public void testDebug_LoadClassWithPrivateClassLoader() throws Exception {
    Class<?> localClass = this.getClass().getClassLoader().loadClass("org.apache.xmlrpc.client.XmlRpcClient");
//    System.out.println("KO newInstance for " + localClass + " in " + localClass.getPackage());
//    localClass.newInstance();

    System.out.println("name = " + localClass.getName());
    System.out.println("pkg = " + localClass.getPackage());
    System.out.println("constructors = " + localClass.getConstructors());
  }

  private URLClassLoader getPrivateClassLoader() throws IOException, FileNotFoundException {

    Map<String, String> classByJar = new HashMap<String, String>();

    for (File lib : new File("C:\\Program Files\\IntelliJ IDEA 6.0\\lib").listFiles()) {
      System.out.println("check idea lib: " + lib.getName());

      if (lib.getName().endsWith(".jar")) {

        JarInputStream jarFile = new JarInputStream
                (new FileInputStream(lib));
        JarEntry jarEntry;

        while (true) {
          jarEntry = jarFile.getNextJarEntry();
          if (jarEntry == null) {
            break;
          }

          if (jarEntry.getName().endsWith(".class")) {

            String foundInJar = classByJar.get(jarEntry.getName());
            if (foundInJar != null) {
//            System.out.println("############## WARNING : " + jarEntry.getName() + " added by " + lib + " already present in  = " + foundInJar);
            } else {
              classByJar.put(jarEntry.getName(), lib.getAbsolutePath());
            }
          }
//             System.out.println("  " + jarEntry.getName());
//          if (jarEntry.getName().replaceAll("/", ".").endsWith("org.apache.commons.logging.Log.class")) {
//            System.out.println("+++++++ BINGO " + jarEntry.getName() + " in " + lib);
//          }
          if (jarEntry.getName().replaceAll("/", ".").endsWith("XmlRpcClient.class")) {
            System.out.println("+++++++ BINGO " + jarEntry.getName() + " in " + lib);
          }
          if (jarEntry.getName().replaceAll("/", ".").endsWith("XmlRpcRequest.class")) {
            System.out.println("+++++++ BINGO " + jarEntry.getName() + " in " + lib);
          }
        }
      }
    }


    File librariesDir = new File("lib");
    librariesDir.listFiles();
    ArrayList<URL> libs = new ArrayList<URL>();
    boolean commonsLoggingAdded = false;
    for (File lib : librariesDir.listFiles()) {

      if (lib.getName().endsWith(".jar")) {

        System.out.println("check codecollab lib: " + lib.getName());
        JarInputStream jarFile = new JarInputStream
                (new FileInputStream(lib));
        JarEntry jarEntry;

        while (true) {
          jarEntry = jarFile.getNextJarEntry();
          if (jarEntry == null) {
            break;
          }
          if (jarEntry.getName().endsWith(".class")) {

            String foundInJar = classByJar.get(jarEntry.getName());
            if (foundInJar != null) {
              System.out.println("############## WARNING : " + jarEntry.getName() + " added by " + lib +
                                 " already present in  = " + foundInJar);
            } else {
              classByJar.put(jarEntry.getName(), lib.getAbsolutePath());
            }
          }

//             System.out.println("  " + jarEntry.getName());
//          if (jarEntry.getName().replaceAll("/", ".").endsWith("org.apache.commons.logging.Log.class")) {
//            System.out.println("+++++++ BINGO " + jarEntry.getName() + " in " + lib);
//          }
          if (jarEntry.getName().replaceAll("/", ".").endsWith("XmlRpcClient.class")) {
            System.out.println("+++++++ BINGO " + jarEntry.getName() + " in " + lib);
          }
          if (jarEntry.getName().replaceAll("/", ".").endsWith("XmlRpcRequest.class")) {
            System.out.println("+++++++ BINGO " + jarEntry.getName() + " in " + lib);
          }
        }
      }

//      if(lib.getName().equals("commons-logging.jar")){
//        if(commonsLoggingAdded){
//          System.out.println("continue");
//          continue;
//        }
//        commonsLoggingAdded = true;
//      }
      libs.add(lib.toURL());
    }
    URL[] libsUrls = (URL[]) libs.toArray(new URL[0]);

    URLClassLoader codeCollabLoader = URLClassLoader.newInstance(libsUrls, null);
    return codeCollabLoader;
  }


  public void testLogin() throws Exception {
    ISettableGlobalOptions options = GlobalOptions.copy(ConfigUtils.loadConfigFiles().getKey());

    URLClassLoader myLoader = getPrivateClassLoader();

    Class<?> aClass = Class.forName("org.apache.commons.logging.Log", true, myLoader);
    System.out.println("aClass = " + aClass);
    System.out.println("aClass = " + aClass.getPackage());


    Class<?> logFactoryClass = Class.forName("org.apache.commons.logging.LogFactory", true, myLoader);
    Method getLogMethod = logFactoryClass.getMethod("getLog", String.class);
    Log log = (Log) getLogMethod.invoke(null, "com/smartbear/ccollab/client/LoginUtils");
    log.warn("tracer ok");
//    Log log = LogFactory.getLog("com/smartbear/ccollab/client/LoginUtils");

    Class<?> loginClass = Class.forName("com.smartbear.ccollab.client.LoginUtils", true, myLoader);

//    Method loginMethod = null;
//    loginMethod = loginClass.getMethod("login", IGlobalOptions.class, IUserConfirmation.class);
//    User user = (User) loginMethod.invoke(null, options, new NullClient());
//    System.out.println("user = " + user);


    Method connectionMethod = loginClass.getMethod("establishEngineConnection",
                                                   String.class, String.class, String.class,
                                                   String.class, String.class,
                                                   IUserConfirmation.class);
    Engine engine = (Engine) connectionMethod.invoke(null,
                                                     options.getUrl(), options.getUser(), options.getPassword(),
                                                     options.getServerProxyHost(), options.getServerProxyPort(),
                                                     new NullClient());

//    Engine engine = LoginUtils.establishEngineConnection(options.getUrl(), options.getUser(), options.getPassword(),
//                                                         options.getServerProxyHost(), options.getServerProxyPort(),
//                                                         new NullClient());
    User userlog = engine.userByLogin(options.getUser());
    System.out.println("userlog = " + userlog);
  }

  public void testOptionsAndConnection() throws Exception {

//    ISettableGlobalOptions options = GlobalOptions.copy(ConfigUtils.loadConfigFiles().getKey());
//    System.out.println("options = " + options);

    CodeCollaboratorManager.getInstance().init();

//   XmlRpcClientEngineImplementation xmlRpcClient = new XmlRpcClientEngineImplementation("ccollab4", new URL("http://mobility.ih.lucent.com:8081"),
//                                        "cgonguet",
//                                        "QA1qaqaqa!", null, null, new IUserConfirmation(){
//
//                                          public boolean confirmCertificateChain(X509Certificate[] x509Certificates,
//                                                                                 String string)
//                                                  throws CertificateException {
//                                            return true;  //To change body of implemented methods use File | Settings | File Templates.
//                                          }
//                                        });
//
//
//   int serverVersion = xmlRpcClient.userCreate("toto");
//   System.out.println("serverVersion = " + serverVersion);
//   Engine engine = new Engine(xmlRpcClient, xmlRpcClient.getLargeContentStore());

//   LoginUtils.establishEngineConnection(new URL("http://mobility.ih.lucent.com:8081"),
//                                        "cgonguet",
//                                        "QA1qaqaqa!",
//                                        "","",
//                                        new IUserConfirmation(){
//
//                                          public boolean confirmCertificateChain(X509Certificate[] x509Certificates,
//                                                                                 String string)
//                                                  throws CertificateException {
//                                            return true;  //To change body of implemented methods use File | Settings | File Templates.
//                                          }
//                                        });


  }

  public void testGenerateConfigFile() throws Exception {
    CodeCollaboratorManager.getInstance().init();
    User user = CodeCollaboratorManager.getInstance().getUser();
    ReviewTemplate reviewTemplate = user.getDefaultTemplate();

    FileWriter writer = new FileWriter("src" + File.separator + UserDefinedFieldsManager.CONFIG_FILE);
    writer.write("# Generated by UserDefinedFieldsManagerTest::testGenerateConfigFile()\n");
    writer.write("# At " + new SimpleDateFormat("MM'/'dd'/'yyyy").format(new Date()) + "\n");
    writer.write("# By " + user.getLogin() + "\n");


    System.out.println("--- Review ---\n");
    printUserDefinedFields(writer, UserDefinedFieldsManager.REVIEW_PREFIX,
                           reviewTemplate.getReviewFieldDescriptions(true));
    System.out.println("--- Defect ---\n");
    printUserDefinedFields(writer, UserDefinedFieldsManager.DEFECT_PREFIX,
                           reviewTemplate.getDefectFieldDescriptions(true));
    writer.close();
    System.out.println("Config file: " + UserDefinedFieldsManager.CONFIG_FILE);
  }

  private void printUserDefinedFields(Writer writer, String prefix, List<MetaDataDescription> fields)
          throws IOException {
    for (MetaDataDescription field : fields) {

      System.out.println(field.getTitle() + " : " + field.getDescription());

      List<? extends IDropDownItem> items = field.getDropDownItems(true);
      if (items.size() > 0) {
        writer.write(prefix + field.getTitle() + " = ");
      }
      int j = 0;
      for (IDropDownItem item : items) {
        System.out.println("  " + item.getDisplayName() + " = " + item.getValue());
        writer.write(item.getDisplayName() + UserDefinedFieldsManager.KV_SEPARATOR + item.getValue());
        if (j < items.size() - 1) {
          writer.write(UserDefinedFieldsManager.COUPLE_SEPARATOR);
        }
      }

      if (items.size() > 0) {
        writer.write("\n");
      }
      j++;
      System.out.println("");
    }
  }

  public void testReadConfig() throws Exception {
    displayValues("Review-Product");
    displayValues("Review-Release");
    displayValues("Review-Type");
    displayValues("Defect-Severity");
    displayValues("Defect-Type");
  }

  private void displayValues(String fieldName) {
    Map<String, Integer> map = UserDefinedFieldsManager.getInstance().getKVMap(fieldName);
    System.out.println(fieldName + " = " + map);
  }

}
