package adf.core.debug;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.varia.NullAppender;
import rescuecore2.standard.entities.Building;
import rescuecore2.standard.entities.StandardEntity;

import java.io.File;
import java.util.HashMap;

public class DefaultLogger {

    private static HashMap<String, Logger> name_loggers = new HashMap<>();
    private static final String LOG_FOLDER = "logs";

    public static Logger getLogger(StandardEntity entity) {
        if (entity == null) {
            return getLogger("");
        }

    if (entity instanceof Building) {
      return getLogger(entity.toString());
    }

    String folder = entity.getClass().getSimpleName() + "/";
    return getLogger(folder + entity.getID());
  }


  public static Logger getLogger(String fileName) {
    if (fileName == null) {
      fileName = "";
    }

    if (name_loggers.containsKey(fileName)) {
      return name_loggers.get(fileName);
    }

    if (name_loggers.isEmpty()) {
      reset();
    }

    Logger log = (fileName.equals("") ? Logger.getRootLogger()
        : Logger.getLogger(fileName));
    name_loggers.put(fileName, log);

    String PATTERN = "%d [%p|%c|%C{1}] %m%n";

    FileAppender fa = new FileAppender();
    fa.setName("FileLogger");
    fa.setFile(LOG_FOLDER + "/" + fileName + ".log");
    fa.setLayout(new PatternLayout(PATTERN));
    fa.setThreshold(Level.DEBUG);
    fa.setAppend(false);
    fa.activateOptions();
    log.addAppender(fa);
    return log;
  }


  private static void reset() {

    deleteDirectory(new File(LOG_FOLDER));

    Logger.getRootLogger().getLoggerRepository().resetConfiguration();
    Logger log = Logger.getLogger("com.infomatiq.jsi.rtree.RTree-delete");
    log.addAppender(new NullAppender());
    log = Logger.getLogger("com.infomatiq.jsi.rtree.RTree");
    log.addAppender(new NullAppender());
    log = Logger.getLogger("adf.core.agent.platoon.PlatoonAmbulance");
    log.addAppender(new NullAppender());
    log = Logger.getLogger("adf.core.agent.platoon.PlatoonPolice");
    log.addAppender(new NullAppender());
    log = Logger.getLogger("adf.core.agent.platoon.PlatoonFire");
    log.addAppender(new NullAppender());
    log = Logger.getLogger("adf.core.agent.office.OfficeFire");
    log.addAppender(new NullAppender());
    log = Logger.getLogger("adf.core.agent.office.OfficeAmbulance");
    log.addAppender(new NullAppender());
    log = Logger.getLogger("adf.core.agent.office.OfficePolice");
    log.addAppender(new NullAppender());
  }


  public static boolean deleteDirectory(File directory) {
    if (directory.exists()) {
      File[] files = directory.listFiles();
      if (null != files) {
        for (int i = 0; i < files.length; i++) {
          if (files[i].isDirectory()) {
            deleteDirectory(files[i]);
          } else {
            files[i].delete();
          }
        }
      }
    }
    return (directory.delete());
  }
}