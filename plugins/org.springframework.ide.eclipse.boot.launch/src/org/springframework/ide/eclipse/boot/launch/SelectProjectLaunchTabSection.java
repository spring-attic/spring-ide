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
package org.springframework.ide.eclipse.boot.launch;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.SimpleLabelProvider;

/**
 * @author Kris De Volder
 */
public class SelectProjectLaunchTabSection extends ChooseOneSectionCombo<IProject> implements ILaunchConfigurationTabSection {

	private LiveVariable<Boolean> dirtyState = new LiveVariable<Boolean>(false);

	public SelectProjectLaunchTabSection(IPageWithSections owner, SelectionModel<IProject> model) {
		super(owner, "Project", model);
		allowTextEdits(ProjectNameParser.INSTANCE);
		setLabelProvider(new SimpleLabelProvider() {
			public String getText(Object element) {
				if (element instanceof IProject) {
					return ((IProject) element).getName();
				}
				return super.getText(element);
			}
		});
		getSelection().addListener(new ValueListener<IProject>() {
			@Override
			public void gotValue(LiveExpression<IProject> exp, IProject value) {
				dirtyState.setValue(true);
			}
		});
	}

	protected IProject[] computeOptions() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ArrayList<IProject> interesting = new ArrayList<IProject>(allProjects.length);
		for (IProject p : allProjects) {
			if (isInteresting(p)) {
				interesting.add(p);
			}
		}
		return interesting.toArray(new IProject[interesting.size()]);
	}

	/**
	 * Decides whether given IProject drom the workspace is of interest.
	 * Only projects 'of interest' will be selectable from the project
	 * selector's pull-down menu.
	 */
	protected boolean isInteresting(IProject project) {
		return BootPropertyTester.isBootProject(project);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		getSelection().setValue(BootLaunchConfigurationDelegate.getProject(conf));
		dirtyState.setValue(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		BootLaunchConfigurationDelegate.setProject(conf, getSelection().getValue());
		dirtyState.setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		BootLaunchConfigurationDelegate.setProject(conf, null);
	}

	@Override
	public LiveVariable<Boolean> getDirtyState() {
		return dirtyState;
	}

}
