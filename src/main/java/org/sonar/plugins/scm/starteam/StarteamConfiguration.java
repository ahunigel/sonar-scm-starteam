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

import org.sonar.api.BatchComponent;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class StarteamConfiguration implements BatchComponent {
	private static final int DEFAULT_PORT = 49201;
	private static final Logger LOG = Loggers.get(StarteamConfiguration.class);
	private final Settings settings;
	public static final String STARTEAM_URL_FORMAT =
		        "[username[:password]@]hostname:port/projectName/viewName";
	
	private boolean init = false;
	private String user = null;

	private String password = null;
	
	private String host;

	private int port;
	
	 private String agenthost;

	  private int agentport;

    private String project;
    
    private String view;
    
    private String folder;
    private  String cacheFolder;
    
    private String projectBaseFolder;
	
	public StarteamConfiguration(Settings settings) {
		this.settings = settings;
		init() ;
	}

	private synchronized void init() {
		if (!init) {
			host = settings.getString("scm.starteam.host");
			port = settings.getInt("scm.starteam.port");
			agenthost = settings.getString("scm.starteam.agent.host");
      agentport = settings.getInt("scm.starteam.agent.port");
			user = settings.getString("scm.starteam.user");
			password = settings.getString("scm.starteam.password");
			project = settings.getString("scm.starteam.project");
			view = settings.getString("scm.starteam.view");
			folder= settings.getString("scm.starteam.folder");		
			cacheFolder=settings.getString("scm.blame.cache.folder");	
			projectBaseFolder=settings.getString("sonar.projectBaseDir");
			if(cacheFolder!=null){
				LOG.info("Setting cache folder to :"+cacheFolder);
				StarteamFunctions.setBlameCacheBaseFolder(cacheFolder);
			}
			init=true;
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

  public String getAgenthost()
  {
    return agenthost;
  }

  public int getAgentport()
  {
    return agentport;
  }
	
	

}
