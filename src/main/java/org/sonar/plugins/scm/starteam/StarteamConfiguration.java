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
  private static final int DEFAULT_PORT = 49201;
  private static final Logger LOG = Loggers.get(StarteamConfiguration.class);
  public static final String STARTEAM_URL_FORMAT =
      "[username[:password]@]hostname:port/projectName/viewName";

  private static final String CATEGORY_STARTEAM = "STARTEAM";
  public static final String HOST_PROP_KEY = "scm.starteam.host";
  public static final String PORT_PROP_KEY = "scm.starteam.port";
  public static final String PROJECT_PROP_KEY = "scm.starteam.project";
  public static final String VIEW_PROP_KEY = "scm.starteam.view";
  public static final String FOLDER_PROP_KEY = "scm.starteam.folder";
  public static final String USER_PROP_KEY = "scm.starteam.user";
  public static final String PASSWORD_PROP_KEY = "scm.starteam.password";
  public static final String AGENT_HOST_PROP_KEY = "scm.starteam.agent.host";
  public static final String AGENT_PORT_PROP_KEY = "scm.starteam.agent.port";
  public static final String BLAME_CACHE_FOLDER_PROP_KEY = "scm.blame.cache.folder";

  private final Settings settings;

  private boolean init = false;

  private String user = null;
  private String password = null;
  private String host;
  private int port;
  private String agentHost;
  private int agentPort;
  private String project;
  private String view;
  private String folder;
  private String cacheFolder;
  private String projectBaseFolder;

  public StarteamConfiguration(Settings settings) {
    this.settings = settings;
    init();
  }

  public static List<PropertyDefinition> getProperties() {
    return Arrays.asList(
        PropertyDefinition.builder(HOST_PROP_KEY)
            .name("Hostname")
            .description("StarTeam server hostname")
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
      host = settings.getString(HOST_PROP_KEY);
      port = settings.getInt(PORT_PROP_KEY);
      agentHost = settings.getString(AGENT_HOST_PROP_KEY);
      agentPort = settings.getInt(AGENT_PORT_PROP_KEY);
      user = settings.getString(USER_PROP_KEY);
      password = settings.getString(PASSWORD_PROP_KEY);
      project = settings.getString(PROJECT_PROP_KEY);
      view = settings.getString(VIEW_PROP_KEY);
      folder = settings.getString(FOLDER_PROP_KEY);
      cacheFolder = settings.getString(BLAME_CACHE_FOLDER_PROP_KEY);
      projectBaseFolder = settings.getString("sonar.projectBaseDir");
      if (cacheFolder != null) {
        LOG.info("Setting cache folder to :" + cacheFolder);
        StarteamFunctions.setBlameCacheBaseFolder(cacheFolder);
      }
      init = true;
    }
  }

  public String getUserName() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getHostName() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public String getProject() {
    return project;
  }

  public String getView() {
    return view;
  }

  public String getFolder() {
    return folder;
  }

  public String getCacheFolder() {
    return cacheFolder;
  }

  public String getProjectBaseFolder() {
    return projectBaseFolder;
  }

  public String getAgentHost() {
    return agentHost;
  }

  public int getAgentPort() {
    return agentPort;
  }


}
