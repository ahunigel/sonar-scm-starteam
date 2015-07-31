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

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.SonarPlugin;

import com.google.common.collect.ImmutableList;

public class StarteamPlugin extends SonarPlugin {

	@Override
	public List getExtensions() {
		List result = new ArrayList();
		result.addAll(
			ImmutableList.of(
				StarteamScmProvider.class,
				StarteamConfiguration.class, 
				StarteamBlameCommand.class));
		return result;
	}	

}