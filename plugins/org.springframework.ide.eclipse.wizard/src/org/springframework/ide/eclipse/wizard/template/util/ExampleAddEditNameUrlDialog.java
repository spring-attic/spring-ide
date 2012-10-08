/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.util;

import org.eclipse.swt.widgets.Shell;

public class ExampleAddEditNameUrlDialog extends AddEditNameUrlDialog {

	public ExampleAddEditNameUrlDialog(Shell parent, AbstractNameUrlPreferenceModel aModel, NameUrlPair nameUrl,
			String headerText) {
		super(parent, aModel, nameUrl, headerText);
	}

	@Override
	protected boolean validateUrl(String urlString) {
		if (!super.validateUrl(urlString)) {
			return false;
		}

		if (urlString.indexOf("github.com") <= 0) {
			errorTextLabel.setText(AddEditNameUrlDialogMessages.ExampleProjects_mustBeAtGithub);
			composite.update();
			return false;
		}

		return true;
	}

}
