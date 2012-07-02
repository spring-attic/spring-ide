/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.roo.shell.eclipse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.process.manager.ActiveProcessManager;
import org.springframework.roo.process.manager.ProcessManager;
import org.springframework.roo.shell.AbstractShell;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.shell.ExecutionStrategy;
import org.springframework.roo.shell.Parser;
import org.springframework.roo.shell.Shell;
import org.springframework.roo.shell.event.ShellStatus.Status;
import org.springframework.roo.support.osgi.OSGiUtils;
import org.springframework.roo.support.util.ReflectionUtils;

/**
 * Eclipse-based {@link Shell} implementation.
 * @author Christian Dupuis
 */
@Component(immediate = true)
@Service
public class EclipseShell extends AbstractShell implements CommandMarker, Shell, FileEventListener {

	private static final Logger logger = Logger.getLogger(EclipseShell.class.getName());

	private ComponentContext context;

	private boolean developmentMode = false;

	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Reference
	private ExecutionStrategy executionStrategy;

	private FileWriter fileLog;

	@Reference
	private Parser parser;

	private ProcessManager processManager = null;

	private String projectLocation;

	private Object projectRefresher;

	private String rooHome;

	private TextHandler handler;

	public Integer complete(String command, Integer pos, List<String> completions) {
		if (getParser() != null) {
			try {
				ActiveProcessManager.setActiveProcessManager(processManager);
				return getParser().complete(command, pos, completions);
			}
			finally {
				ActiveProcessManager.clearActiveProcessManager();
			}
		}
		return 0;
	}

	public void close() {
		if (fileLog != null) {
			try {
				fileLog.write("// Spring Roo " + versionInfo() + " log closed at " + df.format(new Date()) + "\n");
				fileLog.flush();
				fileLog.close();
				fileLog = null;
			}
			catch (IOException e) {
			}
		}
		Logger mainLogger = Logger.getLogger("");
		mainLogger.removeHandler(handler);
	}

	@Override
	public boolean executeCommand(String line) {
		try {
			ActiveProcessManager.setActiveProcessManager(processManager);
			boolean status = super.executeCommand(line);
			logger.info(getShellPrompt());
			return status;
		}
		finally {
			ActiveProcessManager.clearActiveProcessManager();
		}
	}

	public void init(Object appender, int identity, Object projectRefresher, Object runTestsCommand,
			Object deployCommand, Object uiCommands, String rooHome, String projectLocation) {
		try {

			ServiceReference ref = context.getBundleContext().getServiceReference(
					"org.springframework.roo.process.manager.ProcessManager");
			processManager = (ProcessManager) context.getBundleContext().getService(ref);

			ActiveProcessManager.setActiveProcessManager(processManager);

			completionKeys = "CTRL+SPACE";
			shellPrompt = "roo> ";

			// this.reflectiveCommands = runTestsCommand;
			// this.deployCommand = deployCommand;
			// this.uiCommands = uiCommands;
			this.rooHome = rooHome;
			this.projectLocation = projectLocation;
			this.projectRefresher = projectRefresher;

			setPromptPath(null);

			handler = new TextHandler(appender, identity);
			Logger mainLogger = Logger.getLogger("");
			mainLogger.addHandler(handler);

			openFileLogIfPossible();

			logger.info(version(null));
			logger.info("Welcome to Spring Roo. For assistance press " + completionKeys
					+ " or type \"hint\" then hit ENTER.");
			logger.info(getShellPrompt());

			setShellStatus(Status.STARTED);

		}
		finally {
			ActiveProcessManager.clearActiveProcessManager();
		}
	}

	public boolean isDevelopmentMode() {
		return developmentMode;
	}

	public void onFileEvent(FileEvent fileEvent) {
		try {
			Method method = ReflectionUtils.findMethod(projectRefresher.getClass(), "refresh", new Class[] {
					File.class, Boolean.class });
			method.invoke(projectRefresher, fileEvent.getFileDetails().getFile(),
					fileEvent.getOperation() == FileOperation.CREATED);
		}
		catch (Throwable e) {

		}
	}

	public void promptLoop() {
	}

	public void setDevelopmentMode(boolean developmentMode) {
		this.developmentMode = developmentMode;
	}

	private void openFileLogIfPossible() {
		try {
			fileLog = new FileWriter(new File(this.projectLocation, "log.roo"), true);
			// first write, so let's record the date and time of the first user
			// command
			fileLog.write("// Spring Roo " + versionInfo() + " log opened at " + df.format(new Date()) + "\n");
			fileLog.flush();
		}
		catch (IOException ignoreIt) {
		}
	}

	protected void activate(ComponentContext context) {
		this.context = context;
	}

	protected void deactivate(ComponentContext context) {
		close();
		this.context = null;
	}

	@Override
	protected boolean executeScriptLine(String line) {
		if (line != null && line.startsWith("project")) {
			return true;
		}
		return super.executeScriptLine(line);
	}
	
	@Override
	protected Collection<URL> findResources(String path) {
		return OSGiUtils.findEntriesByPath(context.getBundleContext(), OSGiUtils.ROOT_PATH + path);
	}

	@Override
	protected ExecutionStrategy getExecutionStrategy() {
		return executionStrategy;
	}

	@Override
	protected String getHomeAsString() {
		return this.rooHome;
	}

	@Override
	protected Parser getParser() {
		return parser;
	}

	@Override
	protected void logCommandToOutput(String processedLine) {
		if (fileLog == null) {
			openFileLogIfPossible();
			if (fileLog == null) {
				// still failing, so give up
				return;
			}
		}
		try {
			fileLog.write(processedLine + "\n"); // unix line endings only from
			// Roo
			fileLog.flush(); // so tail -f will show it's working
			if (getExitShellRequest() != null) {
				// shutting down, so close our file (we can always reopen it
				// later if needed)
				fileLog.write("// Spring Roo " + versionInfo() + " log closed at " + df.format(new Date()) + "\n");
				fileLog.flush();
				fileLog.close();
				fileLog = null;
			}
		}
		catch (IOException ignoreIt) {
		}
	}
	
	@Override
	public void flash(Level level, String message, String slot) {
		// TODO CD: add implementation here
	}

}
