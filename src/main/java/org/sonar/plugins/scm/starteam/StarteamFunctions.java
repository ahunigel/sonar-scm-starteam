/*
 * SonarQube :: Plugins :: SCM :: STARTEAM
 * Copyright (C) 2015 Emerson Retail Solutions
 * matthew.zhu@emerson.com
 *
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

import com.starteam.File;
import com.starteam.Folder;
import com.starteam.View;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

import static org.sonar.plugins.scm.starteam.StarteamConfiguration.BLAME_CACHE_FOLDER_PROP_KEY;
import static org.sonar.plugins.scm.starteam.StarteamConfiguration.DEFAULT_BLAME_FOLDER;

public class StarteamFunctions {
  private static final Logger LOG = Loggers.get(StarteamFunctions.class);

  private static java.io.File blameCacheBaseFolder;

  public static Folder findFolderInView(final View view,
                                        final String folderName) throws StarteamSCMException {
    // Check the root folder of the view
    if (view.getName().equalsIgnoreCase(folderName)) {
      return view.getRootFolder();
    }

    // Create a File object with the folder name for system-
    // independent matching
    java.io.File ioFolder = new java.io.File(folderName.toLowerCase());

    // Search for the folder in subfolders
    Folder result = findFolderInView(view.getRootFolder(), ioFolder);
    if (result == null) {
      throw new StarteamSCMException("Couldn't find folder " + folderName
          + " in view " + view.getName());
    }
    return result;
  }

  private static Folder findFolderInView(Folder folder, java.io.File ioFolder) {
    Collection<Folder> checkLater = new ArrayList<>();
    for (Folder f : folder.getSubFolders()) {
      if (f.getFolderHierarchy().equalsIgnoreCase(
          ioFolder.getPath() + java.io.File.separator)) {
        return f;
      } else {
        // add to list of folders whose children will be checked
        checkLater.add(f);
      }
    }
    // recurse unto children
    for (Folder f : checkLater) {
      Folder result = findFolderInView(f, ioFolder);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  public static void saveResultToDisk(String prefixFolder,
                                      File file, BlameData result) {
    java.io.File data = getDataFile(prefixFolder, file);
    try (
        FileOutputStream fos = new FileOutputStream(data);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
    ) {
      oos.writeObject(result);
      oos.flush();
    } catch (FileNotFoundException e) {
      LOG.error("Error saving data", e);
    } catch (IOException e) {
      LOG.error("Error saving data", e);
    }
  }

  public static BlameData getResultFromDisk(
      String prefixFolder, File file) {
    java.io.File data = getDataFile(prefixFolder, file);
    BlameData result = null;
    if (data.exists()) {
      try (
          FileInputStream fis = new FileInputStream(data);
          ObjectInputStream ois = new ObjectInputStream(fis);
      ) {
        result = (BlameData) ois.readObject();
      } catch (FileNotFoundException e) {
        LOG.error("Error reading data", e);
      } catch (IOException e) {
        LOG.error("Error reading data", e);
      } catch (ClassNotFoundException e) {
        LOG.error("Error reading data", e);
      }
    }
    return result;
  }

  public static java.io.File getDataFile(
      String prefixFolder, File file) {
    java.io.File folder = findOrCreateFolder(prefixFolder, file);
    return new java.io.File(folder, file.getName() + ".dat");
  }

  public static java.io.File getTempFile(
      String prefixFolder, File file, String suffex) {
    java.io.File folder = findOrCreateFolder(prefixFolder, file);
    return new java.io.File(folder, file.getName() + suffex);
  }

  private static java.io.File findOrCreateFolder(String prefixFolder,
                                                 File file) {
    String folderPath = file.getParentFolderHierarchy();
    String folderS;

    if (!prefixFolder.endsWith("/") && !folderPath.startsWith("/")) {
      folderS = prefixFolder + "/" + folderPath;
    } else {
      folderS = prefixFolder + folderPath;
    }
    java.io.File folder = new java.io.File(getBlameCacheBaseFolder(),
        folderS);
    folder.mkdirs();
    return folder;
  }

  public static void setBlameCacheBaseFolder(String cacheFolder) {
    blameCacheBaseFolder = new java.io.File(cacheFolder);

    if (!blameCacheBaseFolder.exists()) {
      boolean result = blameCacheBaseFolder.mkdirs();
      if (!result) {
        LOG.warn("invalid configuration for blame cache folder: {}, use default");
        blameCacheBaseFolder = null;
      }
    } else {
      LOG.info("Setting blame cache folder to: " + blameCacheBaseFolder.getAbsolutePath());
    }
  }

  public static java.io.File getBlameCacheBaseFolder() {

    if (blameCacheBaseFolder == null) {
      LOG.info("blameCacheBaseFolder not set, try to figure out the blame cache folder.");
      String tmp = System.getProperty(BLAME_CACHE_FOLDER_PROP_KEY);
      boolean usingUserHome = false;
      if (tmp == null || tmp.length() == 0) {
        usingUserHome = true;
        tmp = System.getProperty("user.home") + "/" + DEFAULT_BLAME_FOLDER;
      }

      blameCacheBaseFolder = new java.io.File(tmp);
      blameCacheBaseFolder.mkdirs();
      if (!blameCacheBaseFolder.exists()) {
        if (!usingUserHome) {
          tmp = System.getProperty("user.home") + "/" + DEFAULT_BLAME_FOLDER;
          blameCacheBaseFolder = new java.io.File(tmp);
          blameCacheBaseFolder.mkdirs();
        }
      }
    }
    LOG.info("blameCacheBaseFolder: {}", blameCacheBaseFolder);
    return blameCacheBaseFolder;
  }
}
