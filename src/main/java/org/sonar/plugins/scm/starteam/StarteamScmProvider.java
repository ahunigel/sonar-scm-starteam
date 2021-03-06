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

import org.sonar.api.batch.scm.BlameCommand;
import org.sonar.api.batch.scm.ScmProvider;

import java.io.File;

public class StarteamScmProvider extends ScmProvider {

  private final StarteamBlameCommand command;

  public StarteamScmProvider(StarteamBlameCommand command) {
    this.command = command;
  }

  @Override
  public String key() {
    return "starteam";
  }

  @Override
  public BlameCommand blameCommand() {
    return command;
  }

  @Override
  public boolean supports(File baseDir) {
    return command.isSupported() || StarteamProperties.INSTANCE.load(new File(baseDir, ".starteam"));
  }


}
