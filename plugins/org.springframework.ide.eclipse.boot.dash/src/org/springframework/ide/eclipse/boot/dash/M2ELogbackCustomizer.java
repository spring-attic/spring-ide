package org.springframework.ide.eclipse.boot.dash;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

public class M2ELogbackCustomizer extends Job {

	/**
	 * Snippet to add to the logback.xml file.
	 */
	private static String SNIPPET = "<logger name=\"net.schmizz\" level=\"OFF\" />";

	public M2ELogbackCustomizer() {
		super("M2ELogbackCustomizer");
		setSystem(true);
	}

	int retries = 120;

	private boolean isStateLocationInitialized() {
		if(!Platform.isRunning()) {
			return false;
		}

		Bundle resourcesBundle = Platform.getBundle("org.eclipse.core.resources");
		if(resourcesBundle == null) {
			return false;
		}

		return resourcesBundle.getState() == Bundle.ACTIVE;
	}

	@Override
	protected IStatus run(IProgressMonitor arg0) {
		try {
			if (isStateLocationInitialized()) {
				Bundle logbackConfigBundle = Platform.getBundle("org.eclipse.m2e.logback.configuration");
				String version = logbackConfigBundle.getVersion().toString();
				IPath statelocationPath = Platform.getStateLocation(logbackConfigBundle);
				if (statelocationPath!=null) {
					File stateDir = statelocationPath.toFile();
					File logbackFile = new File(stateDir, "logback."+version+".xml");
					if (logbackFile.isFile()) {
						String logbackConf = IOUtil.toString(new FileInputStream(logbackFile));
						int insertionPoint = logbackConf.indexOf("</configuration>");
						if (insertionPoint>=0) {
							if (logbackConf.contains(SNIPPET)) {
								//nothing to do
								return Status.OK_STATUS;
							} else {
								logbackConf = logbackConf.substring(0, insertionPoint)
										+SNIPPET+"\n" + logbackConf.substring(insertionPoint);
								IOUtil.pipe(new ByteArrayInputStream(logbackConf.getBytes("UTF8")), logbackFile);
							}
						}
					}
				}
				System.out.println(statelocationPath);
			}
		}catch (Exception e) {
			//ignore
		}
		retry();
		return Status.OK_STATUS;
	}

	private void retry() {
		if (retries-- > 0) {
			this.schedule(1000);
		}
	}

}
