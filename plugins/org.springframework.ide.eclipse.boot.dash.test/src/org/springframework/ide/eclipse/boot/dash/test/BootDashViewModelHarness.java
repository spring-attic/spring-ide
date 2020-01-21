/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.springsource.ide.eclipse.commons.tests.util.StsTestCase.assertContains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.Assert;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.ButtonModel;
import org.springframework.ide.eclipse.boot.dash.model.LocalBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.RunTargets;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.test.mocks.MockMultiSelection;
import org.springframework.ide.eclipse.boot.dash.views.BootDashLabels;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.ui.Stylers;

import com.google.common.collect.ImmutableSet;

import junit.framework.AssertionFailedError;

public class BootDashViewModelHarness {

	public TestBootDashModelContext context;
	public BootDashViewModel model;
	public final MockMultiSelection<BootDashElement> selection = new MockMultiSelection<>(BootDashElement.class);
	public final LiveVariable<BootDashModel> sectionSelection = new LiveVariable<>();

	/**
	 * This is private now. Use the Builder instead for a convenient way to create a harness with some targets in it.
	 */
	public BootDashViewModelHarness(TestBootDashModelContext context) throws Exception {
		this.context = context;
		this.model = new BootDashViewModel(context);
	}

	/**
	 * Dipose model and reinitialze it reusing the same stores (for testing functionality
	 * around persisting stuff)
	 */
	public void reload() throws Exception {
		dispose();
		context = context.reload();
		this.model = new BootDashViewModel(context);
	}

//	public static class MockContext implements BootDashModelContext {
//
//		private IPropertyStore viewProperties = new InMemoryPropertyStore();
//		private IPropertyStore privateProperties = new InMemoryPropertyStore();
//
//		private IScopedPropertyStore<IProject> projectProperties = new MockScopedPropertyStore<>();
//		private IScopedPropertyStore<RunTargetType> runtargetProperties = new MockScopedPropertyStore<>();
//		private SecuredCredentialsStore secureStore = new MockSecuredCredentialStore();
//		private File stateLocation;
//		private LiveVariable<Pattern> bootProjectExclusion = new LiveVariable<>(BootPreferences.DEFAULT_BOOT_PROJECT_EXCLUDE);
//
//		public MockContext() throws Exception {
//			stateLocation = StsTestUtil.createTempDirectory();
//		}
//
//		@Override
//		public IWorkspace getWorkspace() {
//			return ResourcesPlugin.getWorkspace();
//		}
//
//		@Override
//		public ILaunchManager getLaunchManager() {
//			return DebugPlugin.getDefault().getLaunchManager();
//		}
//
//		@Override
//		public IPath getStateLocation() {
//			return new Path(stateLocation.toString());
//		}
//
//		@Override
//		public IScopedPropertyStore<IProject> getProjectProperties() {
//			return projectProperties;
//		}
//
//		@Override
//		public IScopedPropertyStore<RunTargetType> getRunTargetProperties() {
//			return runtargetProperties;
//		}
//
//		@Override
//		public SecuredCredentialsStore getSecuredCredentialsStore() {
//			return secureStore;
//		}
//
//		@Override
//		public void log(Exception e) {
//		}
//
//		@Override
//		public LiveExpression<Pattern> getBootProjectExclusion() {
//			return bootProjectExclusion;
//		}
//
//		@Override
//		public IPropertyStore getViewProperties() {
//			return viewProperties;
//		}
//
//		@Override
//		public IPropertyStore getPrivatePropertyStore() {
//			return privateProperties;
//		}
//
//		@Override
//		public BootInstallManager getBootInstallManager() {
//			return bootI
//		}
//	}

	public BootDashModel getRunTargetModel(RunTargetType type) {
		List<BootDashModel> models = getRunTargetModels(type);
		Assert.assertEquals(1, models.size());
		return models.get(0);
	}

	public LocalBootDashModel getLocalTargetModel() {
		return (LocalBootDashModel) getRunTargetModel(RunTargetTypes.LOCAL);
	}

	public List<BootDashModel> getRunTargetModels(RunTargetType type) {
		ArrayList<BootDashModel> models = new ArrayList<>();
		for (BootDashModel m : model.getSectionModels().getValue()) {
			if (m.getRunTarget().getType().equals(type)) {
				models.add(m);
			}
		}
		return Collections.unmodifiableList(models);
	}

	public void dispose() {
		model.dispose();
	}

	public ImmutableSet<RunTarget> getRunTargets() {
		return model.getRunTargets().getValues();
	}

	public RunTarget getRunTarget(RunTargetType targetType) {
		List<RunTarget> targets = getRunTargets(targetType);
		Assert.assertEquals(1, targets.size());
		return targets.get(0);
	}

	private List<RunTarget> getRunTargets(RunTargetType targetType) {
		ArrayList<RunTarget> list = new ArrayList<>();
		for (RunTarget runTarget : model.getRunTargets().getValues()) {
			if (runTarget.getType().equals(targetType)) {
				list.add(runTarget);
			}
		}
		return list;
	}

	public BootDashElement getElementWithName(String name) {
		BootDashElement found = null;
		for (BootDashModel section : model.getSectionModels().getValue()) {
			for (BootDashElement e : section.getElements().getValues()) {
				if (name.equals(e.getName())) {
					assertNull("Found more than one element with name '"+name+"'", found);
					found = e;
				}
			}
		}
		assertNotNull("No element with name '"+name+"'", found);
		return found;
	}

	public BootDashElement getElementFor(ILaunchConfiguration conf) {
		LocalBootDashModel localSection = (LocalBootDashModel) model.getSectionByTargetId(RunTargets.LOCAL.getId());
		return localSection.getLaunchConfElementFactory().createOrGet(conf);
	}

	public BootProjectDashElement getElementFor(IProject project) {
		LocalBootDashModel localSection = (LocalBootDashModel) model.getSectionByTargetId(RunTargets.LOCAL.getId());
		return localSection.getProjectElementFactory().createOrGet(project);
	}

	public static void assertOk(LiveExpression<ValidationResult> validator) {
		ValidationResult status = validator.getValue();
		if (!status.isOk()) {
			fail(status.toString());
		}
	}

	public static void assertLabelContains(String expectSnippet, Object element) {
		assertContains(expectSnippet, getLabel(element));
	}

	public static String getLabel(Object element) {
		Stylers stylers = new Stylers(null);
		BootDashLabels labels = new BootDashLabels(stylers);
		try {
			return labels
					.getStyledText(element, BootDashColumn.TREE_VIEWER_MAIN)
					.getString();
		} finally {
			labels.dispose();
			stylers.dispose();
		}
	}

	public ButtonModel assertButton(BootDashModel model, String expectedLabel) {
		StringBuilder labels = new StringBuilder();
		for (ButtonModel button : model.getButtons().getValues()) {
			labels.append(button.getLabel()+"\n");
			if (expectedLabel.equals(button.getLabel())) {
				return button;
			}
		}
		throw new AssertionFailedError("Label not found: "+expectedLabel+"\nActual labels are:\n"+labels);
	}

	public ButtonModel getButton(BootDashModel model, String label) {
		for (ButtonModel button : model.getButtons().getValues()) {
			if (label.equals(button.getLabel())) {
				return button;
			}
		}
		return null;
	}

}
