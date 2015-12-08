package org.springframework.ide.eclipse.boot.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withStarters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.dialogs.EditStartersModel;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.json.InitializrServiceSpec.Dependency;

public class EditStartersModelTest {

	BootProjectTestHarness harness = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());

	/**
	 * Tests that the EditStartersModel is parsed and that existing starters already present on the
	 * project are initially selected.
	 *
	 * @throws Exception
	 */
	@Test
	public void existingStartersSelected() throws Exception {
		IProject project = harness.createBootProject("foo", withStarters("web", "actuator"));
		ISpringBootProject bootProject = SpringBootCore.create(project);
		EditStartersModel wizard = createWizard(project);
		assertEquals(bootProject.getBootVersion(), wizard.getBootVersion());
		assertStarters(wizard.dependencies.getCurrentSelection(), "web", "actuator");
	}

	private EditStartersModel createWizard(IProject project) throws Exception {
		return new EditStartersModel(project);
	}

	private void assertStarters(List<Dependency> starters, String... expectedIds) {
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
}
