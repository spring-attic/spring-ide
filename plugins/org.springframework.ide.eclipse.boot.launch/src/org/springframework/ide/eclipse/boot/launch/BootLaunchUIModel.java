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

import org.springframework.ide.eclipse.boot.launch.livebean.EnableLiveBeanSupportModel;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

/**
 * Model for the 'main type' selection widgetry on a launchconfiguration tab.
 * <p>
 * Contains the 'logic' for the UI except for the widgets themselves.
 * Can be unit tested without having to instantiate launch configuration dialogs
 * etc.
 *
 * @author Kris De Volder
 */
public class BootLaunchUIModel {

	// TODO pulling out all of the logic for regression testing is a work in progress.
	//   Only some of the UI elements are represented in here. The other ones
	//   still are 'tangled' with the UI widgetry code.

	public static class MainTypeValidator extends Validator {

		private LiveVariable<String> mainTypeName;

		public MainTypeValidator(LiveVariable<String> n) {
			this.mainTypeName = n;
			dependsOn(mainTypeName);
		}

		protected ValidationResult compute() {
			String name = mainTypeName.getValue();
			if (!StringUtil.hasText(name)) {
				return ValidationResult.error("No Main type selected");
			}
			return ValidationResult.OK;
		}
	}

	public final SelectProjectLaunchTabModel project;
	public final MainTypeNameLaunchTabModel mainTypeName;
	public final ProfileLaunchTabModel profile;
	public final LaunchTabSelectionModel<Boolean> enableDebug;
	public final EnableLiveBeanSupportModel enableLiveBean;

	public BootLaunchUIModel(IProfileHistory profileHistory) {
		project = SelectProjectLaunchTabModel.create();
		mainTypeName = MainTypeNameLaunchTabModel.create();
		profile = ProfileLaunchTabModel.create(project.selection, profileHistory);
		enableDebug = EnableDebugLaunchTabModel.create();
		enableLiveBean = new EnableLiveBeanSupportModel();
	}

}
