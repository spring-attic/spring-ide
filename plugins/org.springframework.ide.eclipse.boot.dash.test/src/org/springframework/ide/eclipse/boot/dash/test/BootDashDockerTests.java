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
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.api.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.docker.runtarget.DockerRunTargetType;
import org.springframework.ide.eclipse.boot.dash.views.AddRunTargetAction;
import org.springframework.ide.eclipse.boot.dash.views.BootDashActions;

public class BootDashDockerTests {

	private static final String DEFAULT_DOCKER_URL = "unix:///var/run/docker.sock";

	@Test
	public void createDockerTarget() throws Exception {
		DockerRunTargetType target = injections().getBean(DockerRunTargetType.class);
		AddRunTargetAction createTarget = getCreateTargetAction(target);
		assertNotNull(createTarget);
		assertTrue(createTarget.isEnabled());

		when(ui().inputDialog(eq("Connect to Docker Daemon"), eq("Enter docker url:"), anyString())).then(invocation -> {
			Object[] args = invocation.getArguments();
			assertEquals(DEFAULT_DOCKER_URL, args[2]);
			return args[2];
		});
		createTarget.run();
		createTarget.waitFor(/*Duration.ofMillis(2000)*/);

//		ACondition.waitFor("Run target model to appear", 2000, () -> {
			assertNotNull(harness.getRunTargetModel(target));
//		});
	}

	//////////////////////////////////////////////
	/// harness

	private TestBootDashModelContext context = new TestBootDashModelContext(
			ResourcesPlugin.getWorkspace(),
			DebugPlugin.getDefault().getLaunchManager()
	);
	BootDashViewModelHarness harness = new BootDashViewModelHarness(context);
	BootDashActions actions;

	private BootDashActions actions() {
		if (actions==null) {
			actions = new BootDashActions(harness.model, harness.selection.forReading(), injections(), null);
		}
		return actions;
	}

	private SimpleDIContext injections() {
		return context.injections;
	}

	private AllUserInteractions ui() {
		return injections().getBean(AllUserInteractions.class);
	}

	private AddRunTargetAction getCreateTargetAction(RunTargetType<?> type) {
		for (AddRunTargetAction a : actions().getAddRunTargetActions()) {
			if (a.runTargetType.equals(type)) {
				return a;
			}
		}
		throw new NoSuchElementException("Add target action not found for "+type);
	}


}
