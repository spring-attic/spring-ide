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
package org.springframework.ide.eclipse.maven.internal.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.maven.MavenCorePlugin;


/**
 * @author Christian Dupuis
 */
public class MavenPreferencePage extends PropertyPage {

	private IProject project;

	private Button automaticallyUpdateDependenciesButton;

	private boolean modified = false;

	private boolean automaticallyUpdateDependencies;

	protected Control createContents(Composite parent) {

		Font font = parent.getFont();

		Composite parentComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		parentComposite.setLayout(layout);
		parentComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parentComposite.setFont(font);

		initialize();

		automaticallyUpdateDependenciesButton = new Button(parentComposite, SWT.CHECK);
		automaticallyUpdateDependenciesButton.setText("Automatically update dependencies from Maven pom.xml");
		automaticallyUpdateDependenciesButton.setSelection(automaticallyUpdateDependencies);
		automaticallyUpdateDependenciesButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				modified = true;
			}
		});

		return parentComposite;
	}

	private void initialize() {
		project = (IProject) getElement().getAdapter(IResource.class);
		noDefaultAndApplyButton();
		setDescription("Automatically update project configuration and dependency based on Maven pom.xml");

		if (project != null) {
			automaticallyUpdateDependencies = SpringCorePreferences.getProjectPreferences(project,
					MavenCorePlugin.PLUGIN_ID).getBoolean(MavenCorePlugin.AUTOMATICALLY_UPDATE_DEPENDENCIES_KEY,
					MavenCorePlugin.AUTOMATICALLY_UPDATE_DEPENDENCIES_DEFAULT);
		}
		else {
			automaticallyUpdateDependencies = MavenCorePlugin.AUTOMATICALLY_UPDATE_DEPENDENCIES_DEFAULT;
		}
	}

	public boolean performOk() {
		if (!modified) {
			return true;
		}
		SpringCorePreferences.getProjectPreferences(project, MavenCorePlugin.PLUGIN_ID).putBoolean(
				MavenCorePlugin.AUTOMATICALLY_UPDATE_DEPENDENCIES_KEY,
				automaticallyUpdateDependenciesButton.getSelection());

		return true;
	}

}
