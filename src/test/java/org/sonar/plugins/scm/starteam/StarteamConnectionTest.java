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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.sonar.api.batch.scm.BlameLine;

import com.starteam.Folder;

public class StarteamConnectionTest {

//	@Test
//	public void testInitialize() {
//		StarteamConnection conn=new StarteamConnection("starteamserver.ers.na.emersonclimate.org", 49201, "automated_build","auto", "JARU", "JARU", "JARU/Software");
//		try {
//			conn.initialize();
//			conn.close();
//		} catch (StarteamSCMException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

//	@Test
	public void testBlame(){
		
		StarteamConnection conn=new StarteamConnection("starteamserver.ers.na.emersonclimate.org", 49201, "automated_build","auto", "JARU", "JARU","d:/blame");
		try {
			conn.initialize();
			File file;
			File baseFolder=new File("Software");
			file=new File(baseFolder,"Scripts/version.properties");
			Folder folder=conn.findFolder("JARU/Software/JaruPlugins/G4/Source/JavaCode/com/cpcus/jaru/protocol/impl/g4/operation");
			List<BlameLine> result=(List<BlameLine>) conn.blame(folder, "G4GetApplicationDataOperation.java");
			int i=0;
			for(BlameLine bl:result){
				System.out.println((++i)+":"+bl);
			}
		} catch(IOException e){
			e.printStackTrace();
			
		}catch (StarteamSCMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			
			conn.close();
			
		}
	}
}
