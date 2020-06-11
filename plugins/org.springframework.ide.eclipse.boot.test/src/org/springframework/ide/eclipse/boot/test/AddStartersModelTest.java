/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.bootVersion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Option;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrUrl;
import org.springframework.ide.eclipse.boot.test.InitializrWizardModelHarness.MockInitializrService;
import org.springframework.ide.eclipse.boot.test.util.TestBracketter;
import org.springframework.ide.eclipse.boot.test.util.TestResourcesUtil;
import org.springframework.ide.eclipse.boot.wizard.CheckBoxesSection.CheckBoxModel;
import org.springframework.ide.eclipse.boot.wizard.HierarchicalMultiSelectionFieldModel;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersInitializrService;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersPreferences;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersWizardModel;
import org.springframework.ide.eclipse.boot.wizard.starters.InitializrModel;
import org.springsource.ide.eclipse.commons.frameworks.core.downloadmanager.URLConnectionFactory;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

@SuppressWarnings("restriction")
public class AddStartersModelTest {

	private InitializrWizardModelHarness initializrHarness = new InitializrWizardModelHarness();
	private MockInitializrService initializr = initializrHarness.getInitializrService();

	private BootProjectTestHarness harness = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
	private IPreferenceStore prefs = new MockPrefsStore();


	private static final String MOCK_VALID_INITIALIZR_URL = "https://add.starters.start.spring.io";
	private static final String[] SUPPORTED_BOOT_VERSIONS_230 = new String[] {"2.3.0.RELEASE","2.2.7.RELEASE", "2.1.14.RELEASE"};
	private static final String CURRENT_BOOT_VERSION = "2.3.0.RELEASE";

	private static boolean wasAutobuilding;

	@BeforeClass
	public static void setupClass() throws Exception {
		wasAutobuilding = StsTestUtil.isAutoBuilding();
		StsTestUtil.setAutoBuilding(false);
	}

	@AfterClass
	public static void teardownClass() throws Exception {
		StsTestUtil.setAutoBuilding(wasAutobuilding);
	}

	@Before
	public void setup() throws Exception {
		StsTestUtil.cleanUpProjects();
	}

	@Rule
	public TestBracketter testBracketer = new TestBracketter();

	/**
	 * Tests that the initializr model with dependencies is loaded in the wizard
	 */
	@Test
	public void loadInitializrModelInWizard() throws Exception {
		String projectBootVersion = CURRENT_BOOT_VERSION;
		IProject project = harness.createBootProject("loadInitializrModelInWizard", bootVersion(projectBootVersion));

		// Set up the properties to mock the initializr service: a "valid" initializr
		// URL, a
		// zip file containing the "downloaded" project, and the supported boot versions
		// for the service
		String starterZipFile = "/initializr/boot-230-web-actuator/starter.zip";
		String initializrUrl = MOCK_VALID_INITIALIZR_URL;
		String[] supportedBootVersions = SUPPORTED_BOOT_VERSIONS_230;
		setMockInitializrInfo();

		AddStartersWizardModel wizard = createWizard(project, starterZipFile, initializrUrl, supportedBootVersions);
		InitializrModel initializrModel = wizard.getModel().getValue();
		// Initializr Model should not be available yet until it is loaded
		assertNull(initializrModel);

		loadInitializrModel(wizard);

		// Verify the fields and model are set in the wizard after loading
		assertEquals(initializrUrl, wizard.getServiceUrl().getValue());
		assertEquals(projectBootVersion, wizard.getBootVersion().getValue());
		initializrModel = wizard.getModel().getValue();
		assertNotNull(initializrModel);
		assertEquals(ValidationResult.OK, wizard.getValidator().getValue());

		// Ensure dependency info are loaded in the initializr Model
		HierarchicalMultiSelectionFieldModel<Dependency> dependencies = initializrModel.dependencies;
		List<CheckBoxModel<Dependency>> allDependencies = dependencies.getAllBoxes();
		assertTrue(!allDependencies.isEmpty());
	}

	/*
	 *
	 * Helper methods and mock classes
	 *
	 */

	public static class MockAddStartersInitializrService extends AddStartersInitializrService {


		// Empty URL factory. Shouldn't do anything as an actual URL connection is not
		// needed for testing
		private static final URLConnectionFactory EMPTY_URL_FACTORY = new URLConnectionFactory() {

			@Override
			public URLConnection createConnection(URL url) throws IOException {
				return null;
			}

		};

		private final String starterZipPath;

		private final Set<String> supportedBootVersions;

		private final String validInitializrUrl;

		public MockAddStartersInitializrService(String starterZipPath,
				String validInitializrUrl,
				String[] supportedBootVersions) {
			super(EMPTY_URL_FACTORY);
			this.starterZipPath = starterZipPath;
			this.validInitializrUrl = validInitializrUrl;
			this.supportedBootVersions = ImmutableSet.copyOf(supportedBootVersions);
		}

		@Override
		public InitializrService getService(Supplier<String> url) {
			return new MockInitializrService() {
				@Override
				public SpringBootStarters getStarters(String bootVersion) throws Exception {
					// Mock unsupported boot version. This is an actual error thrown in the real wizard
					if (!supportedBootVersions.contains(bootVersion)) {
						throw new FileNotFoundException();
					} else {
						return super.getStarters(bootVersion);
					}
				}
			};
		}

		@Override
		public InitializrProjectDownloader getProjectDownloader(InitializrUrl url) {

			return new InitializrProjectDownloader(urlConnectionFactory, url) {

				@Override
				public File getProject(List<Dependency> dependencies, ISpringBootProject bootProject) throws Exception {
					return TestResourcesUtil.getTestFile(starterZipPath);
				}

			};
		}

		@Override
		public Option[] getSupportedBootReleaseVersions(String url) throws Exception {

			Builder<Object> options = ImmutableList.builder();
			for (String v : SUPPORTED_BOOT_VERSIONS_230) {
				Option option = new Option();
				option.setId(v);
				options.add(option);
			}

			return options.build().toArray(new Option[0]);
		}

		@Override
		public void checkBasicConnection(URL url) throws Exception {
			// Tests an actual error thrown by initializr service: a valid URL (e.g. http://www.google.com) that is
			// not an initializr URL
			if (!validInitializrUrl.equals(url.toString())) {
				throw new ConnectException();
			}
		}

	}

	public class MockAddStartersPreferences extends AddStartersPreferences {

		private final List<String> storedUrls = new ArrayList<>();


		public MockAddStartersPreferences(String validUrl) {
			super(prefs);
			storedUrls.add(validUrl);
		}

		@Override
		public String getInitializrUrl() {
			return storedUrls.get(0);
		}

		@Override
		public String[] getInitializrUrls() {
			return storedUrls.toArray(new String[] {});
		}

		@Override
		public void addInitializrUrl(String url) {
			storedUrls.add(url);
		}

	}

	/**
	 * Mocks initializr info that in the real scenario would be downloaded from initializr service
	 */
	private void setMockInitializrInfo() throws Exception {
		// This sets initializr info from a JSON file that captures data that would
		// otherwise be downloaded from initializr. This is using the old edit starter testing harness
		// which is also applicable to add starters
		initializr.setInputs("sample");
	}

	private void loadInitializrModel(AddStartersWizardModel wizard) throws Exception {
		wizard.addModelLoader(() -> wizard.createInitializrModel(new NullProgressMonitor()));
		// Wait for it to finish
		waitForWizardJob();
	}

	private void waitForWizardJob() throws InterruptedException {
		Job.getJobManager().join(AddStartersWizardModel.JOB_FAMILY, null);
	}

	private void performOk(AddStartersWizardModel wizard) throws Exception {
		wizard.performOk();
		waitForWizardJob();
	}

	private AddStartersWizardModel createWizard(IProject project, String starterZipFile, String validInitializrUrl,
			String[] supportedBootVersions) throws Exception {
		AddStartersInitializrService initializrService = new MockAddStartersInitializrService(starterZipFile,
				validInitializrUrl, supportedBootVersions);
		AddStartersPreferences preferences = new MockAddStartersPreferences(validInitializrUrl);
		return new AddStartersWizardModel(project, preferences, initializrService);
	}

}
