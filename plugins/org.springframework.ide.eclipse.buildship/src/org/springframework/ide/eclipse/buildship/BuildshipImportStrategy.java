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
import java.net.URI;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper.DistributionType;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.springframework.ide.eclipse.boot.wizard.content.BuildType;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportConfiguration;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportStrategy;
import org.springframework.ide.eclipse.boot.wizard.importing.ImportStrategyFactory;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.util.NatureUtils;

import com.google.common.collect.ImmutableList;

/**
 * Importer strategy implementation for importing CodeSets into the workspace and set them
 * up to use Buildship Gradle Tooling.
 *
 * @author Kris De Volder
 */
public class BuildshipImportStrategy extends ImportStrategy {

	//TODO: the way progress is reported with the sub-job / join is not very nice (a double progress popup appears)
	// Perhaps this so post has the answer to doing it a better way:
	//   http://stackoverflow.com/questions/14530200/eclipse-jobs-api-how-to-track-progress-for-job-scheduled-by-another-job

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

	private IProject getProject(File projectLoc) {
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (p.isAccessible()) {
				IPath l = getLocation(p);
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

	private IPath getLocation(IProject p) {
		//In eclipse... nothing is ever simple. No, you can not just ask a project for its location...
		URI uri = p.getRawLocationURI();
		if (uri==null) {
			//This means project description doesn't specify location, which means project is in the
			// default location
			IPath wsloc = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			return wsloc.append(p.getName());
		} else if (uri.getScheme().equals("file")) {
			return new Path(uri.getPath());
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

					// This doesn't work in e44 (api not available until e45):
					job.join(0, new SubProgressMonitor(mon, 9));
					// The below works, but makes more assumptions on the internal workings of SynchronizeGradleProjectJob
					// Namely: how it implements Job.belongsTo to make itself belong to program family equal to its class name.
					//Job.getJobManager().join(SynchronizeGradleProjectJob.class.getName(), new SubProgressMonitor(mon, 9));

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
