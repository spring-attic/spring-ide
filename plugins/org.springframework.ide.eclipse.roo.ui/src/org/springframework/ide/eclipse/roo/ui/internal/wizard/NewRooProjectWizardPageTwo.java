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
package org.springframework.ide.eclipse.roo.ui.internal.wizard;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.roo.ui.internal.RooUiColors;
import org.springframework.ide.eclipse.roo.ui.internal.StyledTextAppender;


/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class NewRooProjectWizardPageTwo extends WizardPage {

	private static final String PAGE_NAME = "NewRooProjectWizardPageTwo"; //$NON-NLS-1$

	private StyledText text;

	/**
	 * Creates a new {@link NewRooProjectWizardPageTwo}.
	 */
	public NewRooProjectWizardPageTwo() {
		super(PAGE_NAME);
		setPageComplete(true);
		setTitle("Create a new Roo Project");
		setDescription("Create a Roo project in the workspace or in an external location.");

	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		text = new StyledText(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		GridData data = new GridData(GridData.FILL_BOTH);
		text.setLayoutData(data);
		text.setFont(JFaceResources.getTextFont());
		text.setEditable(false);

		RooUiColors.applyShellBackground(text);
		RooUiColors.applyShellForeground(text);
		RooUiColors.applyShellFont(text);
		text.setText("Please click 'Finish' to create the new project using Roo." + StyledTextAppender.NL);

		setControl(composite);
	}

	public void dispose() {
		super.dispose();
	}

	public StyledText getRooShell() {
		return text;
	}

	@Override
	public boolean isPageComplete() {
		return getContainer().getCurrentPage().equals(this);
	}

	private GridLayout initGridLayout(GridLayout layout, boolean margins) {
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		if (margins) {
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		}
		else {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		return layout;
	}

	protected void setControl(Control newControl) {
		Dialog.applyDialogFont(newControl);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(newControl, IJavaHelpContextIds.NEW_JAVAPROJECT_WIZARD_PAGE);

		super.setControl(newControl);
	}

}
