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

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.SelectionModel;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

/**
 * Model for the 'main type' selection widgetry on a launchconfiguration tab.
 *
 * @author Kris De Volder
 */
public class MainTypeSelectionModel {

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

	public final SelectionModel<IProject> project;
	public final SelectionModel<String> mainTypeName;

	public MainTypeSelectionModel() {
		LiveVariable<IProject> p = new LiveVariable<IProject>();
		ExistingBootProjectSelectionValidator pv = new ExistingBootProjectSelectionValidator(p);
		project = new SelectionModel<IProject>(p, pv);

		LiveVariable<String> n = new LiveVariable<String>("");
		MainTypeValidator nv = new MainTypeValidator(n);
		mainTypeName = new SelectionModel<String>(n, nv);
	}

}
