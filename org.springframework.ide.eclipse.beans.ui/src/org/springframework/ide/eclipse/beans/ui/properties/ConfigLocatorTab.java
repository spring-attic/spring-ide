/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.properties;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.springframework.ide.eclipse.beans.core.model.locate.BeansConfigLocatorDefinition;
import org.springframework.ide.eclipse.beans.core.model.locate.BeansConfigLocatorFactory;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.ui.SpringUIMessages;

/**
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class ConfigLocatorTab {

	private static class BeansConfigLocatorDefinitionContentProvider implements
			IStructuredContentProvider {

		private List<BeansConfigLocatorDefinition> beansConfigLocatorDefinitions;

		public BeansConfigLocatorDefinitionContentProvider(
				List<BeansConfigLocatorDefinition> projectBuilderDefinitions) {
			this.beansConfigLocatorDefinitions = projectBuilderDefinitions;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			return beansConfigLocatorDefinitions.toArray();
		}
	}

	private class BeansConfigLocatorLabelProvider extends LabelProvider implements IColorProvider {

		public String getText(Object element) {
			if (element instanceof BeansConfigLocatorDefinition) {
				return ((BeansConfigLocatorDefinition) element).getName();
			}
			return super.getText(element);
		}

		public Image getImage(Object element) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}

		public Color getBackground(Object element) {
			return null;
		}

		public Color getForeground(Object element) {
			if (element instanceof BeansConfigLocatorDefinition
					&& !((BeansConfigLocatorDefinition) element).getBeansConfigLocator().supports(
							project)) {
				return grayColor;
			}
			return null;
		}

		@Override
		public void dispose() {
			super.dispose();
			if (grayColor != null) {
				grayColor.dispose();
			}
		}
	}

	private List<BeansConfigLocatorDefinition> beansConfigLocatorDefinitions;

	private CheckboxTableViewer builderViewer;

	private Text descriptionText;

	private IProject project;

	private Color grayColor = new Color(Display.getDefault(), 150, 150, 150);

	public ConfigLocatorTab(IProject project) {
		this.beansConfigLocatorDefinitions = BeansConfigLocatorFactory
				.getBeansConfigLocatorDefinitions();
		this.project = project;
	}

	public Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 3;
		layout.marginWidth = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label beansLabel = new Label(composite, SWT.NONE);
		beansLabel.setText(BeansUIPlugin
				.getResourceString("ConfigurationPropertyPage.tabConfigLocators.description"));
		// config set list viewer
		builderViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		builderViewer.getTable().setLayoutData(gd);
		builderViewer.setContentProvider(new BeansConfigLocatorDefinitionContentProvider(
				this.beansConfigLocatorDefinitions));
		builderViewer.setLabelProvider(new BeansConfigLocatorLabelProvider());
		builderViewer.setInput(this); // activate content provider
		builderViewer.setCheckedElements(getEnabledBeansConfigLocatorDefinitions().toArray());
		builderViewer.setGrayedElements(getGreyedBeansConfigLocatorDefinitions().toArray());
		builderViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					BeansConfigLocatorDefinition definition = (BeansConfigLocatorDefinition) sel
							.getFirstElement();
					if (definition == null)
						clearDescription();
					else
						showDescription(definition);
				}
			}
		});
		builderViewer.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				BeansConfigLocatorDefinition obj = (BeansConfigLocatorDefinition) event
						.getElement();
				if (!obj.getBeansConfigLocator().supports(project) && event.getChecked()) {
					builderViewer.setChecked(obj, false);
				}
			}
		});

		Label descriptionLabel = new Label(composite, SWT.NONE);
		descriptionLabel.setText(SpringUIMessages.ProjectBuilderPropertyPage_builderDescription);

		descriptionText = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER
				| SWT.H_SCROLL);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 30;
		descriptionText.setLayoutData(data);

		return composite;
	}

	private List<BeansConfigLocatorDefinition> getEnabledBeansConfigLocatorDefinitions() {
		List<BeansConfigLocatorDefinition> builderDefinitions = beansConfigLocatorDefinitions;
		List<BeansConfigLocatorDefinition> filteredBuilderDefinitions = new ArrayList<BeansConfigLocatorDefinition>();
		for (BeansConfigLocatorDefinition builderDefinition : builderDefinitions) {
			if (builderDefinition.isEnabled(project)
					&& builderDefinition.getBeansConfigLocator().supports(project)) {
				filteredBuilderDefinitions.add(builderDefinition);
			}
		}
		return filteredBuilderDefinitions;
	}

	private List<BeansConfigLocatorDefinition> getGreyedBeansConfigLocatorDefinitions() {
		List<BeansConfigLocatorDefinition> builderDefinitions = beansConfigLocatorDefinitions;
		List<BeansConfigLocatorDefinition> filteredBuilderDefinitions = new ArrayList<BeansConfigLocatorDefinition>();
		for (BeansConfigLocatorDefinition builderDefinition : builderDefinitions) {
			if (!builderDefinition.getBeansConfigLocator().supports(project)) {
				filteredBuilderDefinitions.add(builderDefinition);
			}
		}
		return filteredBuilderDefinitions;
	}

	/**
	 * Show the selected description in the text.
	 */
	private void showDescription(BeansConfigLocatorDefinition definition) {
		if (descriptionText == null || descriptionText.isDisposed()) {
			return;
		}
		String text = "";
		if (!definition.getBeansConfigLocator().supports(project)) {
			text = "This configuration file detector does not support the current project!\n\r";
		}
		text += definition.getDescription();

		if (text == null || text.length() == 0) {
			descriptionText
					.setText(SpringUIMessages.ProjectBuilderPropertyPage_noBuilderDescription);
		}
		else {
			descriptionText.setText(text);
		}
	}

	/**
	 * Clear the selected description in the text.
	 */
	private void clearDescription() {
		if (descriptionText == null || descriptionText.isDisposed()) {
			return;
		}
		descriptionText.setText(""); //$NON-NLS-1$
	}

	public boolean performOk() {
		if (Arrays.deepEquals(getEnabledBeansConfigLocatorDefinitions().toArray(), this.builderViewer.getCheckedElements())) {
			return true;
		}
		final List checkElements = Arrays.asList(this.builderViewer.getCheckedElements());

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException,
					InvocationTargetException, InterruptedException {
				for (BeansConfigLocatorDefinition beansConfigLocatorDefinition : beansConfigLocatorDefinitions) {
					if (checkElements.contains(beansConfigLocatorDefinition)) {
						beansConfigLocatorDefinition.setEnabled(true, project);
					}
					else {
						beansConfigLocatorDefinition.setEnabled(false, project);
					}
				}
			}
		};

		try {
			operation.run(new NullProgressMonitor());
		}
		catch (InvocationTargetException e) {
		}
		catch (InterruptedException e) {
		}
		return true;
	}
}
