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
package org.springframework.ide.eclipse.webflow.ui.wizards;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.webflow.ui.properties.BeansConfigLabelProvider;
import org.springframework.ide.eclipse.webflow.ui.properties.BeansConfigContentProvider;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class LinkToBeansConfigWizardPage extends WizardPage {

	private static final int BEANS_CONFIG_LIST_VIEWER_HEIGHT = 150;

	private static final int LIST_VIEWER_WIDTH = 340;

	private CheckboxTableViewer beansConfigSetViewer;

	private Text nameText;

	private List<String> config;
	
	protected LinkToBeansConfigWizardPage(String pageName) {
		super(pageName);
		setDescription("Define details for the Spring Web Flow definition file");
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
        // top level group
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginTop = 5;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());
		setControl(composite);
		
		Label nameLabel = new Label(composite, SWT.NONE);
		nameLabel.setText("Specify a flow id");
		nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		if (this.config != null && this.config.size() > 0
				&& this.config.get(0) != null) {
			this.nameText.setText(this.config.get(0));
		}
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label beansLabel = new Label(composite, SWT.NONE);
		beansLabel.setText("Link Spring Beans configs to Web Flow flow");
		// config set list viewer
		beansConfigSetViewer = CheckboxTableViewer.newCheckList(composite,
				SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = LIST_VIEWER_WIDTH;
		gd.heightHint = BEANS_CONFIG_LIST_VIEWER_HEIGHT;
		beansConfigSetViewer.getTable().setLayoutData(gd);
		beansConfigSetViewer.setContentProvider(new BeansConfigContentProvider(
				createBeansConfigList()));
		beansConfigSetViewer.setLabelProvider(new BeansConfigLabelProvider());
		beansConfigSetViewer.setInput(this); // activate content provider
	}
	
	private Set<IBeansConfig> createBeansConfigList() {
		Set<IBeansConfig> configs = new HashSet<IBeansConfig>();
		Set<IBeansProject> beansProjects = BeansCorePlugin.getModel().getProjects();
		for (IBeansProject project : beansProjects) {
			configs.addAll(project.getConfigs());
		}
		// Create new list with config files from this config set
		return configs;
	}

	public Set<IBeansConfig> getBeansConfigs() {
		Set<IBeansConfig> configs = new HashSet<IBeansConfig>();
		Object[] checkedElements = beansConfigSetViewer.getCheckedElements();
		if (checkedElements != null) {
			for (int i = 0; i < checkedElements.length; i++) {
				configs.add((IBeansConfig) checkedElements[i]);
			}
		}
		return configs;
	}
	
	public String getName() {
		return this.nameText.getText();
	}
}
