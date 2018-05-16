/*
 * SonarQube :: Plugins :: SCM :: STARTEAM
 * Copyright (C) 2015 Emerson Retail Solutions
 * matthew.zhu@emerson.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.scm.starteam;

import org.sonar.api.CoreProperties;
import org.sonar.api.PropertyType;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.Arrays;
import java.util.List;

@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
@BatchSide
public class StarteamConfiguration {
  private static final Logger LOG = Loggers.get(StarteamConfiguration.class);
  public static final String STARTEAM_URL_FORMAT =
      "[username[:password]@]host:port/projectName/viewName";

  private static final String CATEGORY_STARTEAM = "STARTEAM";
  public static final String HOST_PROP_KEY = "sonar.starteam.host";
  public static final String PORT_PROP_KEY = "sonar.starteam.port";
  public static final String PROJECT_PROP_KEY = "sonar.starteam.project";
  public static final String VIEW_PROP_KEY = "sonar.starteam.view";
  public static final String FOLDER_PROP_KEY = "sonar.starteam.folder";
  public static final String USER_PROP_KEY = "sonar.starteam.user";
  public static final String PASSWORD_PROP_KEY = "sonar.starteam.password";
  public static final String AGENT_HOST_PROP_KEY = "sonar.starteam.agent.host";
  public static final String AGENT_PORT_PROP_KEY = "sonar.starteam.agent.port";
  public static final String BLAME_CACHE_FOLDER_PROP_KEY = "sonar.blame.cache.folder";
  public static final String DEFAULT_BLAME_FOLDER = ".starteam";

  private final Settings settings;

  private boolean init = false;

  private String cacheFolder;

  public StarteamConfiguration(Settings settings) {
    this.settings = settings;
    init();
  }

  public static List<PropertyDefinition> getProperties() {
    return Arrays.asList(
        PropertyDefinition.builder(HOST_PROP_KEY)
            .name("Hostname")
            .description("StarTeam server host")
            .type(PropertyType.STRING)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_STARTEAM)
            .index(0)
            .build(),
        PropertyDefinition.builder(PORT_PROP_KEY)
            .name("Port")
            .description("StarTeam server port")
            .type(PropertyType.INTEGER)
            .defaultValue("49201")
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_STARTEAM)
            .index(1)
            .build(),
        PropertyDefinition.builder(PROJECT_PROP_KEY)
            .name("Project")
            .description("Project to be used on StarTeam")
            .type(PropertyType.STRING)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_STARTEAM)
            .index(2)
            .build(),
        PropertyDefinition.builder(VIEW_PROP_KEY)
            .name("View")
            .description("View to be used on StarTeam")
            .type(PropertyType.STRING)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_STARTEAM)
            .index(3)
            .build(),
        PropertyDefinition.builder(FOLDER_PROP_KEY)
            .name("Folder")
            .description("Folder to be used on StarTeam")
            .type(PropertyType.STRING)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_STARTEAM)
            .index(4)
            .build(),
        PropertyDefinition.builder(USER_PROP_KEY)
            .name("Username")
            .description("Username to be used for StarTeam server authentication")
            .type(PropertyType.STRING)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_STARTEAM)
            .index(5)
            .build(),
        PropertyDefinition.builder(PASSWORD_PROP_KEY)
            .name("Password")
            .description("Password to be used for StarTeam server authentication")
            .type(PropertyType.PASSWORD)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_STARTEAM)
            .index(6)
            .build(),
        PropertyDefinition.builder(AGENT_HOST_PROP_KEY)
            .name("Agent Hostname")
            .description("StarTeam cache agent host name")
            .type(PropertyType.STRING)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_STARTEAM)
            .index(7)
            .build(),
        PropertyDefinition.builder(AGENT_PORT_PROP_KEY)
            .name("Agent Port")
            .description("StarTeam cache agent port")
            .type(PropertyType.INTEGER)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_STARTEAM)
            .index(8)
            .build(),
        PropertyDefinition.builder(BLAME_CACHE_FOLDER_PROP_KEY)
            .name("Blame Cache Folder")
            .description("Blame Cache Folder for scanner side")
            .type(PropertyType.STRING)
            .onQualifiers(Qualifiers.PROJECT)
            .category(CoreProperties.CATEGORY_SCM)
            .subCategory(CATEGORY_STARTEAM)
            .index(9)
            .build());
  }

  private synchronized void init() {
    if (!init) {
      cacheFolder = settings.getString(BLAME_CACHE_FOLDER_PROP_KEY);
      if (cacheFolder != null) {
        LOG.info("Configured blame cache folder is: " + cacheFolder);
        StarteamFunctions.setBlameCacheBaseFolder(cacheFolder);
      }
      init = true;
    }
  }

  public String user() {
    return settings.getString(USER_PROP_KEY);
  }

  public String password() {
    return settings.getString(PASSWORD_PROP_KEY);
  }

  public String host() {
    return settings.getString(HOST_PROP_KEY);
  }

  public int port() {
    return settings.getInt(PORT_PROP_KEY);
  }

  public String project() {
    return settings.getString(PROJECT_PROP_KEY);
  }

  public String view() {
    return settings.getString(VIEW_PROP_KEY);
  }

  public String folder() {
    return settings.getString(FOLDER_PROP_KEY);
  }

  public String cacheFolder() {
    return cacheFolder;
  }

  public String agentHost() {
    return settings.getString(AGENT_HOST_PROP_KEY);
  }

  public int agentPort() {
    return settings.getInt(AGENT_PORT_PROP_KEY);
  }

  public Settings settings() {
    return settings;
  }


}
