/*******************************************************************************
 *  Copyright (c) 2013-2015 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gradle;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.boot.wizard.content.BuildType;
import org.springframework.ide.eclipse.boot.wizard.content.CodeSet;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportConfiguration;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportStrategy;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportStrategyFactory;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.gradle.core.GradleProject;
import org.springsource.ide.eclipse.gradle.core.samples.SampleProject;
import org.springsource.ide.eclipse.gradle.core.util.ErrorHandler;
import org.springsource.ide.eclipse.gradle.core.util.NatureUtils;
import org.springsource.ide.eclipse.gradle.core.wizards.GradleImportOperation;

/**
 * Importer strategy implementation for importing CodeSets into the workspace and set them
 * up to use Gradle Tooling.
 *
 * @author Kris De Volder
 */
public class GradleImportStrategy extends ImportStrategy {

	public GradleImportStrategy(BuildType buildType, String name, String notInstalledMessage) {
		super(buildType, name, notInstalledMessage);
	}

	public static class Factory implements ImportStrategyFactory {
		@Override
		public ImportStrategy create(BuildType buildType, String name, String notInstalledMessage) throws Exception {
			Assert.isLegal(buildType==BuildType.GRADLE);
			Class.forName("org.springsource.ide.eclipse.gradle.core.samples.SampleProject");
			return new GradleImportStrategy(buildType, name, notInstalledMessage);
		}
	}

	private static SampleProject asSample(final String projectName, final CodeSet codeset) {
		return new SampleProject() {
			@Override
			public String getName() {
				//Probably nobody cares about the name but anyway...
				return projectName;
			}

			@Override
			public void createAt(final File location) throws CoreException {
				if (location.exists()) {
					//Delete anything that is in the way
					FileUtils.deleteQuietly(location);
				}
				try {
					codeset.createAt(location);
				} catch (Throwable e) {
					throw ExceptionUtil.coreException(e);
				}
			}
		};
	}

	/**
	 * Alternate implementation that uses GradleImportOperation. This is able customise a
	 * bit more how the guides are getting imported (i.e. disable DSLD and disable
	 * dependency management. This gives behavior that is more similar to using commandline
	 * tools to generate metadata. So the behavior is more likely to match what guides
	 * authors actually do.
	 */
	private static class GradleCodeSetImport2 implements IRunnableWithProgress {

		private final String location;
		private final String projectName;
		private final CodeSet codeset;

		public GradleCodeSetImport2(ImportConfiguration conf) {
			this.location = conf.getLocation();
			this.projectName = conf.getProjectName();
			this.codeset = conf.getCodeSet();
		}

		//@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			monitor.beginTask("Import "+projectName, 3);
			try {
				//1:
				SampleProject sample = asSample(projectName, codeset);
				File rootFolder = new File(location);
				sample.createAt(rootFolder);
				monitor.worked(1);

				//2:
				GradleImportOperation importOp = GradleImportOperation.importAll(rootFolder);
				importOp.setEnableDependencyManagement(false);

				ErrorHandler eh = ErrorHandler.forImportWizard();
				importOp.perform(eh, new SubProgressMonitor(monitor, 1));

				//3: add spring nature
				addPringNature(importOp.getProjects(), eh, new SubProgressMonitor(monitor, 1));

				eh.rethrowAsCore();
			} catch (CoreException e) {
				throw new InvocationTargetException(e);
			} finally {
				monitor.done();
			}
		}

		private void addPringNature(GradleProject[] projects, ErrorHandler eh, IProgressMonitor mon) {
			mon.beginTask("Add spring natures", projects.length);
			try {
				for (GradleProject gp : projects) {
					IProject p = gp.getProject();
					//Should really not be be null if it was actually imported... but...
					if (p!=null) {
						try {
							NatureUtils.ensure(p, new SubProgressMonitor(mon, 1), SpringCoreUtils.NATURE_ID);
						} catch (CoreException e) {
							eh.handleError(e);
						}
					}
				}
			} finally {
				mon.done();
			}
		}
	}

	@Override
	public IRunnableWithProgress createOperation(ImportConfiguration conf) {
		return new GradleCodeSetImport2(conf);
	}
}
