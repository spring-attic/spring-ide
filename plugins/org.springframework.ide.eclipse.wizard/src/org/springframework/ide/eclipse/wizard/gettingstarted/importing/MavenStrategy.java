/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.importing;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.maven.MavenCorePlugin;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.CodeSet;

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

		//TODO: This import startegy doesn't even read projectName. The name actually comes from the
		//   maven pom file. Actually makes sense for inport to determine projectName from project
		//   content. So maybe projectName should not be in an ImportConfig at all!

		private final String projectName;
		private final File location;
		private final CodeSet codeset;

		public MavenCodeSetImport(ImportConfiguration conf) {
			this.projectName = conf.getProjectName();
			this.location = new File(conf.getLocation());
			this.codeset = conf.getCodeSet();
		}

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
