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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sonar.api.batch.scm.BlameLine;

public class BlameData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7129003666026086885L;
	private int revision;
	private transient List<BlameLine> blameLines;
	private List<BlameLineSerializable> serializableBlameLines;
	public int getRevision() {
		return revision;
	}
	public void setRevision(int revision) {
		this.revision = revision;
	}	
	
	public List<BlameLine> getBlameLines() {
		if(blameLines==null&&serializableBlameLines!=null){
			blameLines=new ArrayList<BlameLine>();
			for(BlameLineSerializable bls:serializableBlameLines){
				blameLines.add(bls.toBlameLine());
			}
				
		}
		return blameLines;
	}
	public void setBlameLines(List<BlameLine> blameLines) {
		this.blameLines = blameLines;
		serializableBlameLines= new ArrayList<BlameLineSerializable>();
		for(BlameLine bl:blameLines){
			serializableBlameLines.add(new BlameLineSerializable(bl));
		}
	}
	public List<BlameLineSerializable> getSerializableBlameLines() {
		return serializableBlameLines;
	}
	public void setSerializableBlameLines(
			List<BlameLineSerializable> serializableBlameLines) {
		this.serializableBlameLines = serializableBlameLines;
	}



	static class BlameLineSerializable implements Serializable{
		private String author;
		private Date date;
		private String revision;
		
		public BlameLineSerializable(){
			
		}
		public BlameLine toBlameLine() {
			BlameLine bl=new BlameLine();
			bl.author(author);
			bl.date(date);
			bl.revision(revision);
			return bl;
			
		}
		public BlameLineSerializable(BlameLine blameLine){
			this.author=blameLine.author();
			this.date=blameLine.date();
			this.revision=blameLine.revision();
		}
		
		public String getAuthor() {
			return author;
		}
		public void setAuthor(String author) {
			this.author = author;
		}
		public Date getDate() {
			return date;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		public String getRevision() {
			return revision;
		}
		public void setRevision(String revision) {
			this.revision = revision;
		}
		
		
	}
}
