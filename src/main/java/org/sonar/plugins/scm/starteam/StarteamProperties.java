package org.sonar.plugins.scm.starteam;

import org.sonar.api.internal.apachecommons.lang.StringUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Nigel.Zheng on 6/27/2018.
 */
public enum StarteamProperties {
  INSTANCE;

  private static final Logger LOG = Loggers.get(StarteamProperties.class);

  private static final String DEFAULT_STARTEAM_PROPERTIES = "starteam.properties";

  private Properties properties = new Properties();

  public String getProperty(String key, String defaultValue) {
    if (key.startsWith("sonar.")) {
      key = key.substring(0, 6);
    }
    String configuredValue = properties.getProperty(key);
    if (StringUtils.isBlank(configuredValue)) {
      return defaultValue;
    }
    return configuredValue;
  }

  public boolean load(File file) {
    try {
      if (file.exists()) {
        properties.load(new FileInputStream(new File(file, DEFAULT_STARTEAM_PROPERTIES)));
      }
    } catch (IOException e) {
      LOG.warn("Cannot load starteam properties");
    }
    return !properties.isEmpty();
  }
}
