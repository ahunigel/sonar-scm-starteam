/*
 * MyLanguage Plugin
 * Copyright (C) MyYear MyCompany
 * dev@sonar.codehaus.org
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.sonar.api.batch.scm.BlameLine;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.starteam.CheckoutManager;
import com.starteam.CheckoutOptions;
import com.starteam.File;
import com.starteam.Folder;
import com.starteam.Item;
import com.starteam.Project;
import com.starteam.Server;
import com.starteam.ServerAdministration;
import com.starteam.ServerConfiguration;
import com.starteam.ServerInfo;
import com.starteam.User;
import com.starteam.View;
import com.starteam.ViewMember;
import com.starteam.ViewMemberCollection;
import com.starteam.diff.BasicCompare;
import com.starteam.diff.Edit;
import com.starteam.diff.EditCollection;
import com.starteam.diff.ParsedCharSequence;
import com.starteam.diff.StarTeamDiff;
import com.starteam.events.CheckoutEvent;
import com.starteam.events.CheckoutListener;
import com.starteam.exceptions.DuplicateServerListEntryException;

public class StarteamConnection {
	private static final Logger LOG = Loggers.get(StarteamConnection.class);

	private final String hostName;
	private final int port;
	private final String userName;
	private final String password;
	private final String projectName;
	private final String viewName;
	// private final String folderName;
	private final String cacheFolder;

	private transient Server server;
	private transient View view;
	private transient Folder rootFolder;
	private transient Project project;
	private transient ServerAdministration srvAdmin;
	
	private boolean canReadUserAccts = true;
	
	private User[] userAccts = null;

	private transient CheckoutManager checkoutManager;

	public StarteamConnection(String hostName, int port, String userName,
			String password, String projectName, String viewName,String cacheFolder
		) {
		checkParameters(hostName, port, userName, password, projectName,
				viewName);
		this.hostName = hostName;
		this.port = port;
		this.userName = userName;
		this.password = password;
		this.projectName = projectName;
		this.viewName = viewName;
		this.cacheFolder=cacheFolder;
		// this.folderName = folderName;
	}
	public StarteamConnection(StarteamConfiguration configuration) {
		this(configuration.getHostName(),configuration.getPort(),configuration.getUserName(),configuration.getPassword(),configuration.getProject(),configuration.getView(),configuration.getCacheFolder());
	}

	private ServerInfo createServerInfo() {
		ServerInfo serverInfo = new ServerInfo();
		serverInfo
				.setConnectionType(ServerConfiguration.PROTOCOL_TCP_IP_SOCKETS);
		serverInfo.setHost(this.hostName);
		serverInfo.setPort(this.port);

		populateDescription(serverInfo);

		return serverInfo;
	}

	public void initialize() throws StarteamSCMException {
		server = new Server(createServerInfo());
		server.connect();
		LOG.info("Connect to Server.");
		server.logOn(userName, password);
		LOG.info("log On to Server with User:" + userName + ".");

		project = findProjectOnServer(server, projectName);
		LOG.info("Find out the project:" + projectName);
		view = findViewInProject(project, viewName);
		LOG.info("Find out the view:" + viewName);
		// rootFolder = StarteamFunctions.findFolderInView(view, folderName);
		initCheckoutManager();
		LOG.info("init Checkout Manager.");
		//srvAdmin = server.getAdministration();
	}

	public Folder findFolder(String folderPath) throws StarteamSCMException {
		return StarteamFunctions.findFolderInView(view, folderPath);
	}

	public List<BlameLine> blame(Folder folder, String fileName)
			throws IOException {
		List<BlameLine> blameLines = null;
		// get revision from local
		try {
			File stFile = (File) folder.getItems(server.getTypes().FILE).find(
					fileName, true);
			LOG.info("start blame file " + stFile.getFullName());
			ViewMemberCollection history = stFile.getHistory();

			Iterator<ViewMember> it = history.iterator();
			// read privious
			BlameData prevoiusBlameData = StarteamFunctions.getResultFromDisk(
					getPathForConn(), stFile);
			int startRevision = -1;
			if (prevoiusBlameData != null) {
				startRevision = prevoiusBlameData.getRevision();
				blameLines = prevoiusBlameData.getBlameLines();
			}
			Stack<ViewMember> fileHistories = getHistories(it, startRevision);

			if(fileHistories.size()==1&&blameLines!=null){
				LOG.info("No change since last build, return cached blame lines.");
				return blameLines;
			}
			
			LOG.info("Start from revision======:" + startRevision);

			ViewMember previous = null;
			ViewMember current = null;
//
//			java.io.File previousFile = StarteamFunctions.getTempFile(
//					getPathForConn(), stFile, ".previous");
//			java.io.File currentFile = StarteamFunctions.getTempFile(
//					getPathForConn(), stFile, ".current");
			LOG.info("blame cache folder:"+StarteamFunctions.getBlameCacheBaseFolder().getPath());
			java.io.File tmpFolder=new java.io.File(StarteamFunctions.getBlameCacheBaseFolder(),"temp");
			tmpFolder.mkdirs();
			java.io.File previousFile=new java.io.File(tmpFolder,"previous.tmp");
			java.io.File currentFile=new java.io.File(tmpFolder,"current.tmp");
			java.io.File temp;
			previousFile.createNewFile();
			currentFile.createNewFile();
			StarTeamDiff stDiff = new StarTeamDiff();

			int count = 0;
			while (!fileHistories.isEmpty()) {
				previous = current;
				current = fileHistories.pop();
				
				// move current to previous
				temp = previousFile;
				previousFile = currentFile;
				currentFile = temp;
				// check out current;
				LOG.info("Start check out revision:"
						+ current.getRevisionNumber());
				checkout(current, currentFile);
				// generate blame lines
				LOG.info("generate blame lines for revision:"
						+ current.getRevisionNumber());
				blameLines = generateBlameLine(blameLines, previous, current,
						previousFile, currentFile, stDiff);

			}
			previousFile.delete();
			currentFile.delete();
			saveData(blameLines, stFile, current);
		} catch (Exception e) {
			LOG.error("Can't blame file "+fileName, e);
		}
		return blameLines;
	}

	private void saveData(List<BlameLine> blameLines, File stFile,
			ViewMember current) {
		if (current != null) {
			BlameData data = new BlameData();
			data.setRevision(current.getRevisionNumber());
			data.setBlameLines(blameLines);
			StarteamFunctions.saveResultToDisk(getPathForConn(), stFile, data);
		}
	}

	private List<BlameLine> generateBlameLine(
			List<BlameLine> blameLines, ViewMember previous,
			ViewMember current, java.io.File previousFile,
			java.io.File currentFile, StarTeamDiff stDiff)
			throws FileNotFoundException, IOException {
		String username;
		Date modifyTime;
		String revision;
		if (previous == null) {
			if (blameLines == null) {
				revision = current.getRevisionNumber() + "";
				username = getUserName(current.getModifiedBy());
				modifyTime = current.getModifiedTime().toJavaDate();
				blameLines = new ArrayList<BlameLine>();
				FileReader fr = new FileReader(currentFile);
				LineNumberReader lnr = new LineNumberReader(fr);
				while (lnr.readLine() != null) {
					BlameLine bl = new BlameLine();
					bl.author(username);
					bl.date(modifyTime);
					bl.revision(revision);
					blameLines.add(bl);
				}
				lnr.close();
			}
		} else {
			LOG.debug("Different From " + previous.getRevisionNumber()
					+ " to " + current.getRevisionNumber());
			EditCollection editCollection = stDiff.diff(new ParsedCharSequence(
					previousFile), new ParsedCharSequence(currentFile),
					new BasicCompare());
			Iterator<Edit> editIt = editCollection.iterator();
			Edit ed;
			int position = 0;
			List<BlameLine> newBlameLine = new ArrayList<BlameLine>();
			revision = current.getRevisionNumber() + "";
			username = getUserName(current.getModifiedBy());
			modifyTime = current.getModifiedTime().toJavaDate();

			while (editIt.hasNext()) {
				ed = editIt.next();
				LOG.debug(ed.toString());

				switch (ed.getAction()) {
				case Edit.DELETE:
					for (; position < ed.getStartSource() - 1; position++) {
						newBlameLine.add(blameLines.get(position));
					}				
					break;
				case Edit.INSERT:
					for (; position < ed.getStartSource(); position++) {
						newBlameLine.add(blameLines.get(position));
					}				
					for (int i = ed.getStartTarget() - 1; i < ed.getEndTarget(); i++) {
						BlameLine bl = new BlameLine();
						bl.author(username);
						bl.date(modifyTime);
						bl.revision(revision);
						newBlameLine.add(bl);
					}
					break;
				case Edit.REPLACE:
					for (; position < ed.getStartSource() - 1; position++) {
						newBlameLine.add(blameLines.get(position));
					}
					for (int i = ed.getStartTarget() - 1; i < ed.getEndTarget(); i++) {
						BlameLine bl = new BlameLine();
						bl.author(username);
						bl.date(modifyTime);
						bl.revision(revision);
						newBlameLine.add(bl);
					}					
					break;
				case Edit.UNCHANGED:
					break;
				}
				position = ed.getEndSource();
			}
			for (; position < blameLines.size(); position++) {
				newBlameLine.add(blameLines.get(position));
			}
			blameLines = newBlameLine;
		}
		return blameLines;
	}

	private String getUserName(User user) {
		if(userAccts == null && canReadUserAccts){
	  		try {
	  			srvAdmin = server.getAdministration();
	  			userAccts = srvAdmin.getUsers();
	  		} catch (Exception e) {	  		  
	  		    LOG.info("WARNING: Looks like this user does not have the permission to access UserAccounts on the StarTeam Server!");
	  		    LOG.info("WARNING: Please contact your administrator and ask to be given the permission \"Administer User Accounts\" on the server.");
	  		    LOG.info("WARNING: Defaulting to just using User Full Names which breaks the ability to send email to the individuals who break the build in Hudson!");
	  		    canReadUserAccts = false;
	  		}
		}
		if (canReadUserAccts) {
			User ua = null;
			for (int i=0; i<userAccts.length; i++) {
				ua = userAccts[i];
				if (ua.getID()==user.getID()) {
					LOG.info("INFO: From \'" + user.getID() + "\' found existing user LogonName = " +
							ua.getLogOnName() + " with ID \'" + ua.getID() + "\' and email \'" + ua.getEmailAddress() +"\'");
					int length=ua.getEmailAddress().indexOf('@');
					if(length>-1){
					  return ua.getEmailAddress().substring(0,length);
					}else{
						LOG.info("user "+ua.getLogOnName()+" email["+ua.getEmailAddress()+"] is not correct.");
					  return ua.getLogOnName();
					}
				}
			}
		} else {
			// Since the user account running the build does not have user admin perms
			// Build the base email name from the User Full Name		  
			String shortname = user.getName();
			if (shortname.indexOf(",")>0) {
				// check for a space and assume "lastname, firstname"
				shortname = shortname.charAt((shortname.indexOf(" ")+1))+ shortname.substring(0, shortname.indexOf(","));
			} else {
				// check for a space and assume "firstname lastname"
				if (shortname.indexOf(" ")>0) {
					shortname = shortname.charAt(0) + shortname.substring((shortname.indexOf(" ")+1),shortname.length());

				}  // otherwise, do nothing, just return the name we have.
			}
			return shortname;
		}
		return "unknown";
		
	}
	private void checkout(ViewMember current, java.io.File currentFile)
			throws IOException {
		checkoutManager.checkoutTo((File) current, currentFile);
		if (checkoutManager.canCommit()) {
			checkoutManager.commit();
		}
	}

	public CheckoutManager initCheckoutManager() {
		if (checkoutManager == null) {
			CheckoutOptions coOptions = new CheckoutOptions(view);
			coOptions.setLockType(Item.LockType.UNLOCKED);
			coOptions.setUpdateStatus(true);
			coOptions.setTimeStampNow(true);
			coOptions.setForceCheckout(true);
			checkoutManager = view.createCheckoutManager(coOptions);

			checkoutManager.addCheckoutListener(new CheckoutListener() {

				@Override
				public void notifyProgress(CheckoutEvent coEvent) {
					LOG.info("Expected: " + coEvent.getCurrentBytesExpected()
							+ ",  so far: " + coEvent.getCurrentBytesSoFar());

				}

				@Override
				public void startFile(CheckoutEvent coEvent) {
					// System.out.println("start:" + coEvent.get);
				}
			});
		}
		return checkoutManager;
	}

	private Stack<ViewMember> getHistories(Iterator<ViewMember> it,
			int startRevision) {
		Stack<ViewMember> fileHistories = new Stack<ViewMember>();
		ViewMember tempVm;
		while (it.hasNext()) {
			tempVm = it.next();
			if (tempVm.getRevisionNumber() >= startRevision) {
				fileHistories.push(tempVm);
			} else {
				break;
			}
		}
		return fileHistories;
	}

	private String getPathForConn() {
		return this.hostName + "/" + this.port + "/" + this.projectName + "/"
				+ this.viewName;
	}


	public void close() {
		if (server.isConnected()) {
			view.discard();
			project.discard();
			server.disconnect();
		}
	}

	/**
	 * @param server
	 * @param projectname
	 * @return Project specified by the projectname
	 * @throws StarTeamSCMException
	 */
	static Project findProjectOnServer(final Server server,
			final String projectname) throws StarteamSCMException {
		for (Project project : server.getProjects()) {
			if (project.getName().equals(projectname)) {
				return project;
			}
		}
		throw new StarteamSCMException("Couldn't find project " + projectname
				+ " on server " + server.getAddress());
	}

	/**
	 * @param project
	 * @param viewname
	 * @return
	 * @throws StarTeamSCMException
	 */
	static View findViewInProject(final Project project, final String viewname)
			throws StarteamSCMException {
		for (View view : project.getAccessibleViews()) {
			if (view.getName().equals(viewname)) {
				return view;
			}
		}
		throw new StarteamSCMException("Couldn't find view " + viewname
				+ " in project " + project.getName());
	}

	private void populateDescription(ServerInfo serverInfo) {
		// Increment a counter until the description is unique
		int counter = 0;
		while (!setDescription(serverInfo, counter))
			++counter;
	}

	private boolean setDescription(ServerInfo serverInfo, int counter) {
		try {
			serverInfo.setDescription("StarTeam connection to "
					+ this.hostName
					+ ((counter == 0) ? "" : " (" + Integer.toString(counter)
							+ ")"));
			return true;
		} catch (DuplicateServerListEntryException e) {
			return false;
		}
	}

	private void checkParameters(String hostName, int port, String userName,
			String password, String projectName, String viewName
		) {
		if (null == hostName)
			throw new NullPointerException("hostName cannot be null");
		if (null == userName)
			throw new NullPointerException("user cannot be null");
		if (null == password)
			throw new NullPointerException("passwd cannot be null");
		if (null == projectName)
			throw new NullPointerException("projectName cannot be null");
		if (null == viewName)
			throw new NullPointerException("viewName cannot be null");
	
		if ((port < 1) || (port > 65535))
			throw new IllegalArgumentException("Invalid port: " + port);
	}
}
