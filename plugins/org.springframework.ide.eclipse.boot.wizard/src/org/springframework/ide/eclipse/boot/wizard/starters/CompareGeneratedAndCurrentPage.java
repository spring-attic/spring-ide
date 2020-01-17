/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springframework.ide.eclipse.boot.wizard.InitializrFactoryModel;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class CompareGeneratedAndCurrentPage extends WizardPage {

	private final InitializrFactoryModel<AddStartersModel> factoryModel;
	private Composite contentsContainer;
	private Control compareViewer = null;

	public CompareGeneratedAndCurrentPage(InitializrFactoryModel<AddStartersModel> factoryModel) {
		super("Compare", "Compare Generated POM with the current POM", null);
		this.factoryModel = factoryModel;
	}

	@Override
	public void createControl(Composite parent) {
		contentsContainer = new Composite(parent, SWT.NONE);
		contentsContainer.setLayout(GridLayoutFactory.fillDefaults().create());
		setControl(contentsContainer);
	}

	private void setupCompareViewer() {
		try {

			AddStartersModel model = factoryModel.getModel().getValue();
			CompareEditorInput compareInput = model.generateCompareInput();

			compareViewer = compareInput.createContents(contentsContainer);
			compareViewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
			contentsContainer.layout();
		} catch (Exception e) {
			Log.log(e);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			adjustCompareViewer();
		}
		super.setVisible(visible);
	}

	private void adjustCompareViewer() {
		if (compareViewer != null) {
			compareViewer.dispose();
		}
		setupCompareViewer();
	}

	@Override
	public boolean isPageComplete() {
		return getWizard().getContainer().getCurrentPage() == this;
	}

}
