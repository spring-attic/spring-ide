/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Version;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager;
import org.springsource.ide.eclipse.commons.frameworks.core.workspace.ClasspathListenerManager.ClasspathListener;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * Checks for Boot projects in the workspace that have version < 1.5.x. These versions
 * are not compatible with JDK9 and will cause problems if STS is running with a JDK 9 
 * runtime (irrespective of what JRE is configured in the project/workspace).
 * 
 * See: https://www.pivotaltracker.com/story/show/146914165
 * 
 * @author Kris De Volder
 */
public class JDK9CompatibilityCheck {

	
	public static void initialize() {
		String version = System.getProperty("java.version");
		if (isJava9(version)) {
			//To avoid kicking of heavy activity and classloading during early startup...
			//... postpone initializing the checker into a job to execute a while later.
			Job job = new Job("Start JDK9 Compatibility Check") {
				
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					new Checker();
					return Status.OK_STATUS;
				}
			};
			job.schedule(Duration.ofSeconds(40).toMillis());
		}
	}

	private static boolean isJava9(String version) {
		try {
			int major = Integer.parseInt(version.split("\\D")[0]);
			return major>=9;
		} catch (Exception e) {
			//Couldn't parse version string.
			return false;
		}
	}
	
	private static class Checker implements ClasspathListener {
		
		Version REQUIRED = new Version("1.5");
		Pattern BOOT_JAR = Pattern.compile("spring-boot-(\\d+\\.\\d+\\.\\d).*\\.jar");
		
		private Disposable disposable;

		public Checker() {
			disposable = new ClasspathListenerManager(this, true);
		}

		@Override
		public void classpathChanged(IJavaProject jp) {
			try {
				String versionStr = getBootVersion(jp);
				if (versionStr!=null) {
					Version version = new Version(versionStr);
					if (version.compareTo(REQUIRED)<0) {
						showWarning(jp, versionStr);
					}
				}
			} catch (Exception e) {
				// Silently ignore... nothing we do in here is very critical.
			}
		}

		private synchronized void showWarning(IJavaProject jp, String bootVersion) {
			//Use the disposable to ensure we only show the warning at most once (per session)
			if (disposable!=null) {
				disposable.dispose();
				disposable = null;
			}
			Display.getDefault().asyncExec(() -> {
				MessageDialog.openWarning(null, "JDK 9 Compatibility Issue Detected",
						"STS is currently running with a JDK 9 (java.version="+System.getProperty("java.version")+").\n" + 
						"\n" +
						"Project '"+jp.getElementName()+"' uses Spring Boot version '"+bootVersion+"'.\n" +
						"\n" +
						"Boot projects with version < 1.5 are not compatible with this setup. They will not build properly "+ 
						"and may even cause STS itself to behave unpredictably\n" + 
						"\n" +
						"Recommended actions:\n" + 
						"- either upgrade your projects to a later version ...\n" +
						"- or run STS with a JDK 8 by changing your `STS.ini` file\n"
				);
			});
		}

		private String getBootVersion(IJavaProject jp) throws JavaModelException {
			for (IClasspathEntry entry : jp.getResolvedClasspath(true)) {
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY && entry.getContentKind()==IPackageFragmentRoot.K_BINARY) {
					String jarName = entry.getPath().lastSegment();
					Matcher matcher = BOOT_JAR.matcher(jarName);
					if (matcher.matches()) {
						String versionString = jarName.substring(matcher.start(1), matcher.end(1));
						return versionString;
					}
				}
			};
			return null;
		}
	}

}
