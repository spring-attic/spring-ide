/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import static org.junit.Assert.fail;
import static org.springsource.ide.eclipse.commons.livexp.ui.ProjectLocationSection.getDefaultProjectLocation;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.NewSpringBootWizardModel;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.RadioGroup;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.RadioInfo;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategies;
import org.springframework.ide.eclipse.wizard.gettingstarted.importing.ImportStrategy;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Kris De Volder
 */
public class BootProjectTestHarness {

	public static final long BOOT_PROJECT_CREATION_TIMEOUT = 3*60*1000; // long, may download maven dependencies

	private IWorkspace workspace;

	public BootProjectTestHarness(IWorkspace workspace) {
		this.workspace = workspace;
	}

	public interface WizardConfigurer {

		void apply(NewSpringBootWizardModel wizard);

		WizardConfigurer NULL = new WizardConfigurer(){
			public void apply(NewSpringBootWizardModel wizard) {/*do nothing*/}
		};
	}

	public static WizardConfigurer withImportStrategy(final String id) {
		final ImportStrategy is = ImportStrategies.withId(id);
		Assert.isNotNull(is);
		return new WizardConfigurer() {
			public void apply(NewSpringBootWizardModel wizard) {
				wizard.setImportStrategy(is);
			}
		};
	}

	public static WizardConfigurer withStarters(final String... ids) {
		if (ids.length>0) {
			return new WizardConfigurer() {
				public void apply(NewSpringBootWizardModel wizard) {
					for (String id : ids) {
						wizard.addDependency(id);
					}
				}
			};
		}
		return WizardConfigurer.NULL;
	}

	public static WizardConfigurer setPackage(final String pkgName) {
		return new WizardConfigurer() {
			public void apply(NewSpringBootWizardModel wizard) {
				wizard.getStringInput("packageName").setValue(pkgName);
			}
		};
	}

	/**
	 * @return A wizard configurer that ensures the selected 'boot version' is exactly
	 * a given version of boot.
	 */
	public static WizardConfigurer bootVersion(final String wantedVersion) throws Exception {
		return new WizardConfigurer() {
			public void apply(NewSpringBootWizardModel wizard) {
				RadioGroup bootVersionRadio = wizard.getBootVersion();
				for (RadioInfo option : bootVersionRadio.getRadios()) {
					if (option.getValue().equals(wantedVersion)) {
						bootVersionRadio.setValue(option);
						return;
					}
				}
				fail("The wanted bootVersion '"+wantedVersion+"'is not found in the wizard");
			}
		};
	}

	/**
	 * @return A wizard configurer that ensures the selected 'boot version' is at least
	 * a given version of boot.
	 */
	public static WizardConfigurer bootVersionAtLeast(final String wantedVersion) throws Exception {
		final VersionRange WANTED_RANGE = new VersionRange(wantedVersion);
		return new WizardConfigurer() {
			public void apply(NewSpringBootWizardModel wizard) {
				RadioGroup bootVersionRadio = wizard.getBootVersion();
				RadioInfo selected = bootVersionRadio.getValue();
				Version selectedVersion = getVersion(selected);
				if (WANTED_RANGE.includes(selectedVersion)) {
					//existing selection is fine
				} else {
					//try to select the latest available version and verify it meets the requirement
					bootVersionRadio.setValue(selected =  getLatestVersion(bootVersionRadio));
					selectedVersion = getVersion(selected);
					Assert.isTrue(WANTED_RANGE.includes(selectedVersion));
				}
			}

			private RadioInfo getLatestVersion(RadioGroup bootVersionRadio) {
				RadioInfo[] infos = bootVersionRadio.getRadios();
				Arrays.sort(infos, new Comparator<RadioInfo>() {
					public int compare(RadioInfo o1, RadioInfo o2) {
						Version v1 = getVersion(o1);
						Version v2 = getVersion(o2);
						return v2.compareTo(v1);
					}
				});
				return infos[0];
			}

			private Version getVersion(RadioInfo info) {
				String versionString = info.getValue();
				Version v = new Version(versionString);
				if ("BUILD-SNAPSHOT".equals(v.getQualifier())) {
					// Caveat "M1" will be treated as 'later' than "BUILD-SNAPSHOT" so that is wrong.
					return new Version(v.getMajor(), v.getMinor(), v.getMicro(), "SNAPSHOT"); //Comes after "MX" but before "RELEASE"
				}
				return v;
			}
		};
	}

	public IProject createBootWebProject(final String projectName, final WizardConfigurer... extraConfs) throws Exception {
		return createBootProject(projectName, merge(extraConfs, withStarters("web")));
	}

	private WizardConfigurer[] merge(WizardConfigurer[] confs, WizardConfigurer... moreConfs) {
		WizardConfigurer[] merged = new WizardConfigurer[confs.length + moreConfs.length];
		System.arraycopy(confs, 0, merged, 0, confs.length);
		System.arraycopy(moreConfs, 0, merged, confs.length, moreConfs.length);
		return merged;
	}

	public IProject createBootProject(final String projectName, final WizardConfigurer... extraConfs) throws Exception {
		final Job job = new Job("Create boot project '"+projectName+"'") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					NewSpringBootWizardModel wizard = new NewSpringBootWizardModel(new MockPrefsStore());
					wizard.allowUIThread(true);
					wizard.getProjectName().setValue(projectName);
					wizard.getArtifactId().setValue(projectName);
					//Note: unlike most of the rest of the wizard's behavior, the 'use default location'
					//  checkbox and its effect is not part of the model but part of the GUI code (this is
					//  wrong, really, but that's how it is, so we have to explictly set the project
					//  location in the model.
					wizard.getLocation().setValue(getDefaultProjectLocation(projectName));
					for (WizardConfigurer extraConf : extraConfs) {
						extraConf.apply(wizard);
					}
					wizard.performFinish(new NullProgressMonitor());
					return Status.OK_STATUS;
				} catch (Throwable e) {
					return ExceptionUtil.status(e);
				}
			}
		};
		//job.setRule(workspace.getRuleFactory().buildRule());
		job.schedule();

		new ACondition() {
			@Override
			public boolean test() throws Exception {
				assertOk(job.getResult());
				StsTestUtil.assertNoErrors(getProject(projectName));
				return true;
			}

		}.waitFor(BOOT_PROJECT_CREATION_TIMEOUT);
		return getProject(projectName);
	}

	public IProject getProject(String projectName) {
		return workspace.getRoot().getProject(projectName);
	}

	public static void buildMavenProject(IProject p) throws CoreException {
		ISpringBootProject bp = SpringBootCore.create(p);
		Job job = bp.updateProjectConfiguration();
		if (job!=null) {
			try {
				job.join();
			} catch (InterruptedException e) {
				throw ExceptionUtil.coreException(e);
			}
		}
		bp.getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
	}

	public static void assertNoErrors(IProject p) throws CoreException {
		ISpringBootProject bp = SpringBootCore.create(p);
		Job job = bp.updateProjectConfiguration();
		if (job!=null) {
			try {
				job.join();
			} catch (InterruptedException e) {
				throw ExceptionUtil.coreException(e);
			}
		}
		StsTestUtil.assertNoErrors(p);
	}

	public static void assertOk(IStatus result) throws Exception {
		if (result==null || !result.isOK()) {
			throw ExceptionUtil.coreException(result);
		}
	}
}
