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
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.scm.BlameCommand.BlameOutput;
import org.sonar.api.batch.scm.BlameLine;
import org.sonar.api.batch.fs.InputFile;

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

	@Test
	public void testBlame(){
		
		StarteamConnection conn=new StarteamConnection("starteamserver.ers.na.emersonclimate.org", 49201,"CNXA1ER-STARTEA",5201, "sonar_hudson_tool","13m3rson", "JARU", ".16.3.0 from Merged 16.2.0B09 Plugins 16.2.0B11","d:/blame");
		try {
			conn.initialize();
			File file;
			File baseFolder=new File("Software");
			file=new File(baseFolder,"Scripts/version.properties");
			//Jaru\Software\Source\JavaCode\com\cpcus\jaru\biz\impl\estimation\estimator\ActivityExecutionEstimator.java
			Folder folder=conn.findFolder("JARU/Software/JaruPlugins/ProActRMS/Source/JavaCode/com/cpcus/jaru/dataAccess/impl/");
			InputFile inputFile= new DefaultInputFile("", "RefrigerantTypeDAOImpl.java");
			BlameOutput output=new BlameOutput(){

        @Override
        public void blameResult(InputFile inpuptFile, List<BlameLine> result)
        {      
          int i=0;
          System.out.println("Blame Restul for:"+inpuptFile.relativePath()+" lines:"+result.size());
//          for(BlameLine bl:result){
//            System.out.println((++i)+":"+bl);
//          }
        }
			  
			  
			};
      conn.setOutput(output);
			conn.blame(folder, "RefrigerantTypeDAOImpl.java",101,inputFile);
			inputFile= new DefaultInputFile("", "RefrigerantServiceRecordDAOImpl");
			conn.blame(folder, "RefrigerantServiceRecordDAOImpl.java",2255,inputFile);
			inputFile= new DefaultInputFile("", "RmsAdvisoryDAOImpl.java");
	    conn.blame(folder, "RmsAdvisoryDAOImpl.java",2255,inputFile);
			
			conn.startBlame();
//			for(BlameLine bl:result){
//				System.out.println((++i)+":"+bl);
//			}
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
