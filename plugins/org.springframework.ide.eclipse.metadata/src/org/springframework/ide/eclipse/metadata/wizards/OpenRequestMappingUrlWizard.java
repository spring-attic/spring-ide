/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.metadata.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingMethodToClassMap;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingViewLabelProvider;


/**
 * @author Leo Dos Santos
 */
public class OpenRequestMappingUrlWizard extends Wizard {

	private RequestMappingMethodToClassMap input;

	private RequestMappingViewLabelProvider labelProvider;

	private IProject project;

	private OpenRequestMappingUrlWizardPage page;

	public OpenRequestMappingUrlWizard(RequestMappingMethodToClassMap input,
			RequestMappingViewLabelProvider labelProvider, IProject project) {
		this.input = input;
		this.labelProvider = labelProvider;
		this.project = project;
		setWindowTitle(Messages.OpenRequestMappingUrlWizard_TITLE);
	}

	@Override
	public void addPages() {
		page = new OpenRequestMappingUrlWizardPage(input, labelProvider,
				project);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		page.performPageFinish();
		return true;
	}

}
