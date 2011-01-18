/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.wizards;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * {@link WizardPage} that allows to select one or more {@link IBeansConfigSet}that the new {@link IBeansConfig} should be added to.
 * @author Christian Dupuis
 * @since 2.0
 */
public class LinkToBeansConfigSetWizardPage extends WizardPage {

	private class BeansConfigContentProvider implements
			IStructuredContentProvider {

		private Set<IModelElement> configs;

		public BeansConfigContentProvider(Set<IModelElement> configs) {
			this.configs = configs;
		}

		public Object[] getElements(Object obj) {
			return configs.toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}
	}

	private class BeansConfigSetLabelProvider implements ILabelProvider {

		private BeansModelLabelProvider labelProvider = new BeansModelLabelProvider();

		public String getText(Object element) {
			String str = labelProvider.getText(element);
			if (element instanceof IBeansModelElement) {
				str += " - "
						+ ((IBeansModelElement) element).getElementParent()
								.getElementName();
			}
			return str;
		}

		public Image getImage(Object element) {
			return labelProvider.getImage(element);
		}

		public void addListener(ILabelProviderListener listener) {
			labelProvider.addListener(listener);
		}

		public void dispose() {
			labelProvider.dispose();
		}

		public boolean isLabelProperty(Object element, String property) {
			return labelProvider.isLabelProperty(element, property);
		}

		public void removeListener(ILabelProviderListener listener) {
			labelProvider.removeListener(listener);
		}
	}

	private static final int BEANS_CONFIG_LIST_VIEWER_HEIGHT = 150;

	private static final int LIST_VIEWER_WIDTH = 340;

	private CheckboxTableViewer beansConfigSetViewer;

	protected LinkToBeansConfigSetWizardPage(String pageName) {
		super(pageName);
		setTitle(BeansWizardsMessages.NewConfig_title);
		setDescription(BeansWizardsMessages.NewConfig_configSetDescription);
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

		Label beansLabel = new Label(composite, SWT.NONE);
		beansLabel
				.setText("Select Beans Config Sets to add the new Spring Bean definition to:");
		// config set list viewer
		beansConfigSetViewer = CheckboxTableViewer.newCheckList(composite,
				SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = LIST_VIEWER_WIDTH;
		gd.heightHint = BEANS_CONFIG_LIST_VIEWER_HEIGHT;
		beansConfigSetViewer.getTable().setLayoutData(gd);
		beansConfigSetViewer.setContentProvider(new BeansConfigContentProvider(
				createBeansConfigList()));
		beansConfigSetViewer
				.setLabelProvider(new BeansConfigSetLabelProvider());
		beansConfigSetViewer.setInput(this); // activate content provider
	}

	private Set<IModelElement> createBeansConfigList() {
		// Create new list with config files from this config set
		Set<IModelElement> configs = new HashSet<IModelElement>();
		Set<IBeansProject> beansProjects = BeansCorePlugin.getModel()
				.getProjects();
		for (IBeansProject project : beansProjects) {
			configs.addAll(project.getConfigSets());
		}
		return configs;
	}

	public Set<IBeansConfigSet> getBeansConfigSets() {
		Set<IBeansConfigSet> configs = new HashSet<IBeansConfigSet>();
		Object[] checkedElements = beansConfigSetViewer.getCheckedElements();
		if (checkedElements != null) {
			for (int i = 0; i < checkedElements.length; i++) {
				configs.add((IBeansConfigSet) checkedElements[i]);
			}
		}
		return configs;
	}
}
