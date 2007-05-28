/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;
import org.springframework.ide.eclipse.core.model.validation.IValidator;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.ui.SpringUIMessages;

/**
 * Provides an {@link PropertyPage} that allows to manage the enablement of
 * {@link IProjectBuilder}s and {@link IValidator}s for the underlying
 * {@link IProject}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ProjectPropertyPage extends PropertyPage {

	private ProjectBuilderPropertyTab builderTab = null;

	private ProjectValidatorPropertyTab validatorTab = null;

	public ProjectPropertyPage() {
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem validatorItem = new TabItem(folder, SWT.NONE);
		this.validatorTab = new ProjectValidatorPropertyTab(getShell(),
				((IProject) getElement()));
		validatorItem.setControl(validatorTab.createContents(folder));
		validatorItem.setText(SpringUIMessages.ProjectValidatorPropertyPage_title);

		TabItem builderItem = new TabItem(folder, SWT.NONE);
		this.builderTab = new ProjectBuilderPropertyTab(
				((IProject) getElement()));
		builderItem.setControl(builderTab.createContents(folder));
		builderItem.setText(SpringUIMessages.ProjectBuilderPropertyPage_title);

		Dialog.applyDialogFont(folder);

		return folder;
	}

	public boolean performOk() {
		this.builderTab.performOk();
		this.validatorTab.performOk();
		// always say it is ok
		return super.performOk();
	}
}
