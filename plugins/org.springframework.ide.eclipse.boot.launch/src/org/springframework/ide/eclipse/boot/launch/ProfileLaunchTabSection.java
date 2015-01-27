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

import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.DEFAULT_PROFILE;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.getProfile;
import static org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate.setProfile;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.launch.util.LaunchConfigurationTabSection;
import org.springframework.ide.eclipse.boot.util.JavaProjectUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;
import org.springsource.ide.eclipse.commons.livexp.ui.ChooseOneSectionCombo;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;
import org.springsource.ide.eclipse.commons.livexp.util.Parser;

/**
 * @author Kris De Volder
 */
public class ProfileLaunchTabSection extends LaunchConfigurationTabSection {

	private final LiveVariable<String> profileSelection = new LiveVariable<String>(DEFAULT_PROFILE);
	private final ChooseOneSectionCombo<String> profileChooser;

	/**
	 * LiveExpression that computes list of suggested profiles for the selected
	 * project.
	 *
	 * @author Kris De Volder
	 */
	public static class ProfileOptions extends LiveExpression<String[]> {

		private static final Pattern pat = Pattern.compile("^application-(.*)\\.properties$");
		private static final int pat_group = 1;

		private static final String[] NO_PROFILES = new String[0];
		private final LiveExpression<IProject> project;

		public ProfileOptions(LiveExpression<IProject> project) {
			super(NO_PROFILES);
			this.project = project;
			dependsOn(project);
		}

		@Override
		protected String[] compute() {
			ArrayList<String> profiles = new ArrayList<String>();
			try {
				for (IContainer srcFolder : JavaProjectUtil.getSourceFolders(project.getValue())) {
					for (IResource rsrc : srcFolder.members()) {
						if (rsrc.getType()==IResource.FILE) {
							String name = rsrc.getName();
							Matcher matcher = pat.matcher(name);
							if (matcher.matches()) {
								profiles.add(matcher.group(pat_group));
							}
						}
					}
				}
			} catch (Exception e) {
				BootActivator.log(e);
			}
			return profiles.toArray(new String[profiles.size()]);
		}

	}

	@Override
	public LiveExpression<ValidationResult> getValidator() {
		return profileChooser.getValidator();
	}

	public ProfileLaunchTabSection(IPageWithSections owner, LiveExpression<IProject> project) {
		super(owner);
		this.profileChooser = new ChooseOneSectionCombo<String>(owner, "Profile",
				new SelectionModel<String>(profileSelection), new ProfileOptions(project));
		this.profileChooser.allowTextEdits(Parser.IDENTITY);
	}

	@Override
	public void createContents(Composite page) {
		profileChooser.createContents(page);
		profileSelection.addListener(new ValueListener<String>() {
			public void gotValue(LiveExpression<String> exp, String value) {
				getDirtyState().setValue(true);
			}
		});
	}

	@Override
	public void initializeFrom(ILaunchConfiguration conf) {
		String profile = getProfile(conf);
		profileSelection.setValue(profile);
		getDirtyState().setValue(false);
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy conf) {
		setProfile(conf, profileSelection.getValue());
		getDirtyState().setValue(false);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy conf) {
		setProfile(conf, DEFAULT_PROFILE);
	}

}
