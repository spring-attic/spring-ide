/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.importing;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.gettingstarted.content.CodeSet;
import org.springframework.ide.eclipse.maven.MavenCorePlugin;

/**
 * Importer strategy implementation for importing CodeSets into the workspace and set them
 * up to use Maven Tooling.
 * 
 * @author Kris De Volder
 */
public class MavenStrategy extends ImportStrategy {
	
	public MavenStrategy() {
		//Ensure this strategy can only be instantiated if m2e is installed.
		//If this fails a default 'NullStrategy' instance will be created instead.
		Assert.isNotNull(Platform.getBundle("org.eclipse.m2e.core"), "M2E is not installed");
	}
	
	/**
	 * Implements the import by means of 'NewGradleProjectOperation'
	 */
	private static class MavenCodeSetImport implements IRunnableWithProgress {

		private String projectName;
		private File location;
		private CodeSet codeset;

		public MavenCodeSetImport(ImportConfiguration conf) {
			this.projectName = conf.getProjectName();
			this.location = new File(conf.getLocation());
			this.codeset = conf.getCodeSet();
		}

		@Override
		public void run(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
			mon.beginTask("Create maven project "+projectName, 4);
			try {
				//1: 1 copy codeset data
				codeset.createAt(location);
				mon.worked(1);
				
				//2..4: materialize eclipse project from pom.xml
				File pomFile = new File(location, "pom.xml");
				Assert.isTrue(pomFile.isFile(), "No pom file found: "+pomFile);
				Assert.isTrue(pomFile.length()>0, "Pom file contains no data: "+pomFile);
				MavenCorePlugin.createEclipseProjectFromExistingMavenProject(pomFile, new SubProgressMonitor(mon, 3));
			} catch (InterruptedException e) {
				throw e;
			} catch (InvocationTargetException e) {
				throw e;
			} catch (Throwable e) {
				throw new InvocationTargetException(e);
			}
			finally {
				mon.done();
			}
		}

		
	}
	
	@Override
	public IRunnableWithProgress createOperation(ImportConfiguration conf) {
		return new MavenCodeSetImport(conf);
	}

	
	
}
