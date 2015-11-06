/*******************************************************************************
 *  Copyright (c) 2015 Pivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.buildship;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper.DistributionType;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.wizard.gettingstarted.content.BuildType;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportConfiguration;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategy;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategyFactory;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.util.NatureUtils;

import com.google.common.collect.ImmutableList;

/**
 * Importer strategy implementation for importing CodeSets into the workspace and set them
 * up to use Gradle Tooling.
 *
 * @author Kris De Volder
 */
public class BuildshipImportStrategy extends ImportStrategy {

	public BuildshipImportStrategy(BuildType buildType, String name, String notInstalledMessage) {
		super(buildType, name, notInstalledMessage);
	}

	public static class Factory implements ImportStrategyFactory {
		@Override
		public ImportStrategy create(BuildType buildType, String name, String notInstalledMessage) throws Exception {
			Assert.isLegal(buildType==BuildType.GRADLE);
			Class.forName("org.eclipse.buildship.core.projectimport.ProjectImportConfiguration");
			return new BuildshipImportStrategy(buildType, name, notInstalledMessage);
		}

	}

//
//		private void addPringNature(GradleProject[] projects, ErrorHandler eh, IProgressMonitor mon) {
//			mon.beginTask("Add spring natures", projects.length);
//			try {
//				for (GradleProject gp : projects) {
//					IProject p = gp.getProject();
//					//Should really not be be null if it was actually imported... but...
//					if (p!=null) {
//						try {
//							NatureUtils.ensure(p, new SubProgressMonitor(mon, 1), SpringCoreUtils.NATURE_ID);
//						} catch (CoreException e) {
//							eh.handleError(e);
//						}
//					}
//				}
//			} finally {
//				mon.done();
//			}

	private IProject getProject(File projectLoc) {
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (p.isAccessible()) {
				IPath l = p.getRawLocation();
				if (l!=null) {
					File f = l.toFile();
					if (projectLoc.equals(f)) {
						return p;
					}
				}
			}
		}
		return null;
	}

	@Override
	public IRunnableWithProgress createOperation(final ImportConfiguration conf) {
		return new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor mon) throws InvocationTargetException, InterruptedException {
				mon.beginTask("Import Gradle Buildship project", 10);
				try {
					File loc = new File(conf.getLocation());
					conf.getCodeSet().createAt(loc);

					ProjectImportConfiguration conf = new ProjectImportConfiguration();
					conf.setProjectDir(loc);
					conf.setGradleDistribution(createGradleDistribution());
					conf.setApplyWorkingSets(false);
					SynchronizeGradleProjectJob job = new SynchronizeGradleProjectJob(
							conf.toFixedAttributes(), ImmutableList.<String>of(),
							AsyncHandler.NO_OP);
					job.schedule();
					job.join(0, new SubProgressMonitor(mon, 9));

					IProject p = getProject(loc);
					if (p!=null) {
						NatureUtils.ensure(p, new SubProgressMonitor(mon, 1), SpringCoreUtils.NATURE_ID);
					}
				} catch (InterruptedException|InvocationTargetException e) {
					throw e;
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
				finally {
					mon.done();
				}
			}
		};
	}

    private GradleDistributionWrapper createGradleDistribution() {
        DistributionType distributionType = DistributionType.WRAPPER;
        String distributionConfiguration = null;
        return GradleDistributionWrapper.from(distributionType, distributionConfiguration);
    }


}
