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

import com.starteam.Folder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.scm.BlameCommand;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.IOException;

public class StarteamBlameCommand extends BlameCommand {

  private static final Logger LOG = Loggers.get(StarteamBlameCommand.class);
  private StarteamConfiguration configuration;

  public StarteamBlameCommand(StarteamConfiguration configuration) {
    this.configuration = configuration;
  }

  public boolean isSupported() {
    return configuration.project() != null && configuration.host() != null;
  }

  @Override
  public void blame(BlameInput input, BlameOutput output) {
    File projectBaseFolder = new File(configuration.settings().getString("sonar.projectBaseDir"));
    LOG.info("****  start blaming\nprojectBaseFolder: " + projectBaseFolder.getPath() + "\nbaseDir: "
        + input.fileSystem().baseDir().getPath() + "\nworking dir: " + input.fileSystem().workDir().getPath());
    StarteamConnection conn = new StarteamConnection(configuration);
    conn.setOutput(output);
    try {
      conn.initialize();
      for (InputFile inputFile : input.filesToBlame()) {
        Folder baseFolder = conn.findFolder(configuration.folder()
            + getFolderPath(projectBaseFolder, inputFile.absolutePath()));
        //LOG.info("set alternatePathFragment:"+inputFile.file().getParent());
        //baseFolder.setAlt//ernatePathFragment(inputFile.file().getParent());
        blame(conn, output, baseFolder, inputFile);
      }
      conn.startBlame();
//			ExecutorService executorService = Executors.newFixedThreadPool(1);
//			List<Future<Void>> tasks = submitTasks(conn,input, output, executorService);
//			waitForTaskToComplete(executorService, tasks);			
    } catch (StarteamSCMException e) {
      LOG.error("Fail to init StarTeam connection.", e);
    } catch (Exception e) {
      LOG.error("IOException", e);
    } finally {
      conn.close();
    }


  }

  private String getFolderPath(File folder, String path) {
    String folderPath = folder.getAbsolutePath().toLowerCase().replaceAll("\\\\", "/");
    String pathLower = path.toLowerCase();
    int endIndex = path.lastIndexOf("/");
    int beginIndex = 0;
    LOG.info(pathLower.startsWith(folderPath) + " pathLower:" + pathLower + " folder:" + folderPath);
    if (pathLower.startsWith(folderPath)) {
      beginIndex = folderPath.length();
    }
    String result = path.substring(beginIndex, endIndex);
    result = result.startsWith("/") ? result : "/" + result;
    return result;
  }


  private void blame(StarteamConnection conn, BlameOutput output, final Folder baseDir,
                     InputFile inputFile) throws IOException {
    conn.blame(baseDir, inputFile.file().getName(), inputFile.lines(), inputFile);
  }


}
