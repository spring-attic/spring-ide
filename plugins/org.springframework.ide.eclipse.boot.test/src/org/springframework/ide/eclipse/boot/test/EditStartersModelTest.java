/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.ARTIFACT_ID;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCIES;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCY;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCY_MANAGEMENT;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.GROUP_ID;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.SCOPE;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.TYPE;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.VERSION;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.childEquals;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.findChild;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.findChilds;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.bootVersion;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withStarters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.MavenId;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrDependencySpec;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.test.util.TestBracketter;
import org.springframework.ide.eclipse.boot.wizard.EditStartersModel;
import org.springframework.ide.eclipse.boot.wizard.PopularityTracker;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class EditStartersModelTest {

	private static final String BOOT_1_3_X_RELEASE = "1.3.8.RELEASE";
	private static final String REPOSITORY = "repository";
	private static final String REPOSITORIES = "repositories";
	private MockInitializrService initializr = new MockInitializrService();
	private SpringBootCore springBootCore = new SpringBootCore(initializr);
	private BootProjectTestHarness harness = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
	private IPreferenceStore prefs = new MockPrefsStore();

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
	 * Tests that the EditStartersModel is parsed and that existing starters already present on the
	 * project are initially selected.
	 */
	@Test
	public void existingStartersSelected() throws Exception {
		IProject project = harness.createBootProject("existingStartersSelected", withStarters("web", "actuator"));
		ISpringBootProject bootProject = springBootCore.project(project);
		EditStartersModel wizard = createWizard(project);
		assertTrue(wizard.isSupported());

		assertEquals(bootProject.getBootVersion(), wizard.getBootVersion());
		assertStarterDeps(wizard.dependencies.getCurrentSelection(), "web", "actuator");
	}


	/**
	 * Tests that we are able to remove a starter.
	 */
	@Test
	public void removeStarter() throws Exception {
		IProject project = harness.createBootProject("removeStarter", withStarters("web", "actuator"));
		final ISpringBootProject bootProject = springBootCore.project(project);
		EditStartersModel wizard = createWizard(project);
		assertEquals(bootProject.getBootVersion(), wizard.getBootVersion());
		assertStarterDeps(wizard.dependencies.getCurrentSelection(), "web", "actuator");

		wizard.removeDependency("web");
		assertStarterDeps(wizard.dependencies.getCurrentSelection(), /* removed: "web",*/ "actuator");
		performOk(wizard);

		StsTestUtil.assertNoErrors(project); //force project build

		assertStarters(bootProject.getBootStarters(), "actuator");
	}

	private void performOk(EditStartersModel wizard) throws Exception {
		wizard.performOk();
		Job.getJobManager().join(EditStartersModel.JOB_FAMILY, null);
	}

	/**
	 * Tests that we are able to add a basic starter.
	 */
	@Test
	public void addStarter() throws Exception {
		IProject project = harness.createBootProject("addStarter", withStarters("web"));
		final ISpringBootProject bootProject = springBootCore.project(project);
		EditStartersModel wizard = createWizard(project);
		assertEquals(bootProject.getBootVersion(), wizard.getBootVersion());
		assertStarterDeps(wizard.dependencies.getCurrentSelection(), "web");

		PopularityTracker popularities = new PopularityTracker(prefs);
		assertUsageCounts(bootProject, popularities  /*none*/);

		wizard.addDependency("actuator");
		assertStarterDeps(wizard.dependencies.getCurrentSelection(), "web", "actuator");
		performOk(wizard);

		assertUsageCounts(bootProject, popularities, "actuator:1");

		StsTestUtil.assertNoErrors(project); //force project build

		assertStarters(bootProject.getBootStarters(), "web", "actuator");

		//check that the 'scope' is not set in the pom.xml:
		IDOMDocument pom = parsePom(project);

		Element depEl = findDependency(bootProject, pom, "actuator");
		assertEquals("org.springframework.boot", getGroupId(depEl));
		assertEquals("spring-boot-starter-actuator", getArtifactId(depEl));
		assertEquals(null, getScope(depEl));
	}

	@Test
	public void addStarterWithTestScope() throws Exception {
		IProject project = harness.createBootProject("addStarterWithTestScope", withStarters("web"));
		final ISpringBootProject bootProject = springBootCore.project(project);
		EditStartersModel wizard = createWizard(project);
		wizard.addDependency("restdocs");
		performOk(wizard);

		StsTestUtil.assertNoErrors(project); //force project build

		assertStarters(bootProject.getBootStarters(), "web", "restdocs");

		//check that the 'scope' is set properly
		IDOMDocument pom = parsePom(project);
		Element depEl = findDependency(bootProject, pom, "restdocs");
		assertEquals("spring-restdocs-mockmvc", getArtifactId(depEl));
		assertEquals("test", getScope(depEl));
	}

	@Test
	public void addStarterWithBom() throws Exception {
// We'll be adding this starter:
//	      "id": "cloud-eureka",
//	      "groupId": "org.springframework.cloud",
//	      "artifactId": "spring-cloud-starter-eureka",
//	      "scope": "compile",
//	      "bom": "cloud-bom"

		IProject project = harness.createBootProject("addStarterWithBom", withStarters("web"));
		final ISpringBootProject bootProject = springBootCore.project(project);
		EditStartersModel wizard = createWizard(project);
		wizard.addDependency("cloud-eureka");
		performOk(wizard);

		System.out.println(">>> pom.xml (after dialog closed)");
		System.out.println(IOUtil.toString(project.getFile("pom.xml").getContents()));
		System.out.println("<<< pom.xml (after dialog closed)");

		StsTestUtil.assertNoErrors(project); //force project build

		assertStarters(bootProject.getBootStarters(), "web", "cloud-eureka");
	}

	@Test
	public void addMultipleStartersWithSameBom() throws Exception {
		//This test uses more 'controlled' parameters:
		IProject project = harness.createBootProject("addMultipleStartersWithSameBom",
				bootVersion(BOOT_1_3_X_RELEASE), // boot version fixed
				withStarters("web")
		);
		initializr.setInputs("sample"); // sample intializr json captured for this version
		final ISpringBootProject bootProject = springBootCore.project(project);
		int initialBomCount = getBomCount(parsePom(project));

		EditStartersModel wizard = createWizard(project);
		wizard.addDependency("cloud-eureka");
		wizard.addDependency("cloud-config-client");
		performOk(wizard);

		StsTestUtil.assertNoErrors(project); //force project build

		assertStarters(bootProject.getBootStarters(), "web", "cloud-eureka", "cloud-config-client");

		IDOMDocument pom = parsePom(project);
		int finalBomCount = getBomCount(pom);
		assertEquals(initialBomCount+1, finalBomCount);

		//check that both repos got added
		assertRepoCount(2, pom);

		Element repo = getRepo(pom, "spring-snapshots");
		assertNotNull(repo);
		assertEquals("Spring Snapshots", getTextChild(repo, "name"));
		assertEquals("https://repo.spring.io/snapshot", getTextChild(repo, "url"));
		assertEquals("true", getSnapshotsEnabled(repo));

		repo = getRepo(pom, "spring-milestones");
		assertNotNull(repo);
		assertEquals("Spring Milestones", getTextChild(repo, "name"));
		assertEquals("https://repo.spring.io/milestone", getTextChild(repo, "url"));
		assertEquals("false", getSnapshotsEnabled(repo));
	}

	@Test
	public void addMultipleStartersWithDifferentBom() throws Exception {
		//This test uses more 'controlled' parameters:
		IProject project = harness.createBootProject("addMultipleStartersWithDifferentBom",
				bootVersion(BOOT_1_3_X_RELEASE), // boot version fixed
				withStarters("web")
		);
		initializr.setInputs("sample"); // sample intializr json captured for this version
		final ISpringBootProject bootProject = springBootCore.project(project);
		int initialBomCount = getBomCount(parsePom(project));

		EditStartersModel wizard = createWizard(project);
		wizard.addDependency("cloud-eureka");
		wizard.addDependency("vaadin");
		performOk(wizard);

		StsTestUtil.assertNoErrors(project); //force project build

		assertStarters(bootProject.getBootStarters(), "web", "cloud-eureka", "vaadin");

		IDOMDocument pom = parsePom(project);
		int finalBomCount = getBomCount(pom);
		assertEquals(initialBomCount+2, finalBomCount);
		{
			Element bom = getBom(pom, "spring-cloud-starter-parent");
			assertEquals("org.springframework.cloud", getTextChild(bom,GROUP_ID));
			assertEquals("Brixton.M3", getTextChild(bom,VERSION));
			assertEquals("pom", getTextChild(bom,TYPE));
			assertEquals("import", getTextChild(bom,SCOPE));
		}
		{
			Element bom = getBom(pom, "vaadin-bom");
			assertEquals("com.vaadin", getTextChild(bom,GROUP_ID));
			assertEquals("7.5.5", getTextChild(bom,VERSION));
			assertEquals("pom", getTextChild(bom,TYPE));
			assertEquals("import", getTextChild(bom,SCOPE));
		}
	}

	@Test
	public void addBomWithSubsetOfRepos() throws Exception {
		//This test uses more 'controlled' parameters:
		String bootVersion = BOOT_1_3_X_RELEASE;
		IProject project = harness.createBootProject("addBomWithSubsetOfRepos",
				bootVersion(bootVersion), // boot version fixed
				withStarters("web")
		);

		initializr.setInputs("sample-with-fakes"); // must use 'fake' data because the situation we are after doesn't exist in the real data

		EditStartersModel wizard = createWizard(project);
		wizard.addDependency("cloud-eureka");
		performOk(wizard);

		//!!! fake data may not produce a project that builds without
		//!!! problem so don't check for build errors in this test

		//check that only ONE repo got added
		IDOMDocument pom = parsePom(project);

		assertNotNull(getRepo(pom, "spring-milestones"));
		assertNull(getRepo(pom, "spring-snapshots"));
		assertRepoCount(1, pom);
	}

	@Test
	public void addDependencyWithRepo() throws Exception {
		//This test uses more 'controlled' parameters:
		String bootVersion = BOOT_1_3_X_RELEASE;
		IProject project = harness.createBootProject("addDependencyWithRepo",
				bootVersion(bootVersion), // boot version fixed
				withStarters("web")
		);

		initializr.setInputs("sample-with-fakes"); // must use 'fake' data because the situation we are after doesn't exist in the real data

		EditStartersModel wizard = createWizard(project);
		wizard.addDependency("fake-dep");
		performOk(wizard);

		//!!! fake data may not produce a project that builds without
		//!!! problem so don't check for build errors in this test

		IDOMDocument pom = parsePom(project);

		//check the dependency got added to the pom
		assertNotNull(findDependency(pom, new MavenId("org.springframework.fake", "spring-fake-dep")));

		//check that just the expected repo got added
		assertNotNull(getRepo(pom, "spring-milestones"));
		assertNull(getRepo(pom, "spring-snapshots"));
		assertRepoCount(1, pom);
	}

	@Test
	public void serviceUnavailable() throws Exception {
		String bootVersion = BOOT_1_3_X_RELEASE;
		IProject project = harness.createBootProject("serviceUnavailable",
				bootVersion(bootVersion), // boot version fixed
				withStarters("web")
		);

		initializr.makeUnavailable();

		EditStartersModel wizard = createWizard(project);
		assertFalse(wizard.isSupported());
	}

	//TODO: testing of...
	// - repository field in individual dependency taken into account?

	////////////// Harness code below ///////////////////////////////////////////////


	private String getSnapshotsEnabled(Element repo) {
		if (repo!=null) {
			Element snapshots = findChild(repo, "snapshots");
			if (snapshots!=null) {
				return getTextChild(snapshots, "enabled");
			}
		}
		return null;
	}

	private Element getRepo(IDOMDocument pom, String id) {
		Element doc = pom.getDocumentElement();
		Element repos = findChild(doc, REPOSITORIES);
		if (repos!=null) {
			return findChild(repos, REPOSITORY, childEquals("id", id));
		}
		return null;
	}

	private String getScope(Element depEl) {
		return getTextChild(depEl, SCOPE);
	}

	private String getTextChild(Element depEl, String name) {
		Element child = findChild(depEl, name);
		if (child!=null) {
			return PomEdits.getTextValue(child);
		}
		return null;
	}

	private String getGroupId(Element depEl) {
		return getTextChild(depEl, GROUP_ID);
	}

	private String getArtifactId(Element depEl) {
		return getTextChild(depEl, ARTIFACT_ID);
	}

	public IDOMDocument parsePom(IProject project) throws IOException, CoreException {
		return ((IDOMModel) StructuredModelManager.getModelManager().createUnManagedStructuredModelFor(project.getFile("pom.xml"))).getDocument();
	}

	private Element findDependency(ISpringBootProject project, Document pom, String id) {
		SpringBootStarters starters = project.getStarterInfos();
		MavenId mid = starters.getMavenId(id);
		if (mid!=null) {
			return findDependency(pom, mid);
		}
		return null;
	}

	public static Element findDependency(Document document, MavenId dependency) {
		Element dependenciesElement = findChild(document.getDocumentElement(), DEPENDENCIES);
		return findChild(dependenciesElement, DEPENDENCY, childEquals(GROUP_ID, dependency.getGroupId()),
				childEquals(ARTIFACT_ID, dependency.getArtifactId()));
	}


	private EditStartersModel createWizard(IProject project) throws Exception {
		return new EditStartersModel(
				project,
				springBootCore,
				prefs
		);
	}

	private void assertStarters(List<SpringBootStarter> starters, String... expectedIds) {
		Set<String> expecteds = new HashSet<>(Arrays.asList(expectedIds));
		for (SpringBootStarter starter : starters) {
			String id = starter.getId();
			if (expecteds.remove(id)) {
				//okay
			} else {
				fail("Unexpected starter found: "+starter);
			}
		}
		if (!expecteds.isEmpty()) {
			fail("Expected starters not found: "+expecteds);
		}
	}

	private void assertStarterDeps(List<Dependency> starters, String... expectedIds) {
		Set<String> expecteds = new HashSet<>(Arrays.asList(expectedIds));
		for (Dependency starter : starters) {
			String id = starter.getId();
			if (expecteds.remove(id)) {
				//okay
			} else {
				fail("Unexpected starter found: "+starter);
			}
		}
		if (!expecteds.isEmpty()) {
			fail("Expected starters not found: "+expecteds);
		}
	}

	private void assertRepoCount(int expect, IDOMDocument pom) {
		assertEquals(expect, getRepoCount(pom));
	}

	private int getRepoCount(IDOMDocument pom) {
		Element reposEl = findChild(pom.getDocumentElement(), REPOSITORIES);
		if (reposEl!=null) {
			List<Element> repos = findChilds(reposEl, REPOSITORY);
			if (repos!=null) {
				return repos.size();
			}
		}
		return 0;
	}

	/**
	 * Find a bom element in pom based on its artifactId (can't use its own id as that isn't
	 * found anywhere in the element's xml).
	 */
	private Element getBom(IDOMDocument pom, String aid) {
		Element depman = findChild(pom.getDocumentElement(), DEPENDENCY_MANAGEMENT);
		if (depman!=null) {
			Element deps = findChild(depman, DEPENDENCIES);
			if (deps!=null) {
				return findChild(deps, DEPENDENCY, childEquals(ARTIFACT_ID, aid));
			}
		}
		return null;
	}

	private int getBomCount(IDOMDocument pom) {
		Element depman = findChild(pom.getDocumentElement(), DEPENDENCY_MANAGEMENT);
		if (depman!=null) {
			Element deps = findChild(depman, DEPENDENCIES);
			if (deps!=null) {
				List<Element> boms = findChilds(deps, DEPENDENCY);
				if (boms!=null) {
					return boms.size();
				}
			}
		}
		return 0;
	}

	private void assertUsageCounts(ISpringBootProject project, PopularityTracker popularities, String... idAndCount) throws Exception {
		Map<String, Integer> expect = new HashMap<>();
		for (String pair : idAndCount) {
			String[] pieces = pair.split(":");
			assertEquals(2, pieces.length);
			String id = pieces[0];
			int count = Integer.parseInt(pieces[1]);
			expect.put(id, count);
		}


		List<SpringBootStarter> knownStarters = project.getKnownStarters();
		assertFalse(knownStarters.isEmpty());
		for (SpringBootStarter starter : knownStarters) {
			String id = starter.getId();
			Integer expectedCountOrNull = expect.get(id);
			int expectedCount = expectedCountOrNull==null ? 0 : expectedCountOrNull;
			assertEquals("Usage count for '"+id+"'", expectedCount, popularities.getUsageCount(id));
			expect.remove(id);
		}

		assertTrue("Expected usage counts not found: "+expect, expect.isEmpty());
	}

	public class MockInitializrService implements InitializrService {

		private SpringBootStarters starters;
		private boolean unavailable = false;

		/**
		 * Causes the mock to parse input from given input streams instead of calling out to
		 * the real web service.
		 */
		public void setInputs(InputStream main, InputStream dependencies) throws Exception {
			starters = new SpringBootStarters(
					InitializrServiceSpec.parseFrom(main),
					InitializrDependencySpec.parseFrom(dependencies)
			);
		}

		/**
		 * Causes the mock to parse input from some resources located relative to the test
		 * class.
		 */
		public void setInputs(String name) throws Exception {
			setInputs(getResource(name, "main"), getResource(name, "dependencies"));
		}

		private InputStream getResource(String name, String endPoint) {
			return getClass().getResourceAsStream("edit-starters-test-inputs/"+name+"-"+endPoint+".json");
		}

		@Override
		public SpringBootStarters getStarters(String bootVersion) {
			if (unavailable) {
				return null;
			} else if (starters!=null) {
				return starters;
			} else {
				return InitializrService.DEFAULT.getStarters(bootVersion);
			}
		}

		/**
		 * Make the mock behave as if the 'dependencies' endpoint is not available (either the service is down,
		 * there is no internet connection, or this is an old service that doesn't implement the endpoint yet).
		 */
		public void makeUnavailable() {
			this.unavailable = true;
		}

	}

}
