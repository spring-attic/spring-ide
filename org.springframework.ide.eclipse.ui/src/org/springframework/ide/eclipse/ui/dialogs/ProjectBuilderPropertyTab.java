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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinitionFactory;
import org.springframework.ide.eclipse.ui.SpringUIImages;
import org.springframework.ide.eclipse.ui.SpringUIMessages;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * UI component that allows to enable or disable {@link IProjectBuilder}s on a
 * per project basis.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ProjectBuilderPropertyTab {

	private static class ProjectBuilderDefinitionContentProvider implements
			IStructuredContentProvider {

		private List<ProjectBuilderDefinition> projectBuilderDefinitions;

		public ProjectBuilderDefinitionContentProvider(
				List<ProjectBuilderDefinition> projectBuilderDefinitions) {
			this.projectBuilderDefinitions = projectBuilderDefinitions;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			return projectBuilderDefinitions.toArray();
		}
	}

	private static class ProjectBuilderLabelProvider extends LabelProvider {

		public String getText(Object element) {
			if (element instanceof ProjectBuilderDefinition) {
				return ((ProjectBuilderDefinition) element).getName();
			}
			return super.getText(element);
		}
		
		public Image getImage(Object element) {
			Image image = null;
			if (element instanceof ProjectBuilderDefinition) {
				String icon = ((ProjectBuilderDefinition) element).getIconUri();
				String ns = ((ProjectBuilderDefinition) element).getNamespaceUri();
				if (icon != null && ns != null) {
					image = SpringUIPlugin.getDefault().getImageRegistry().get(
							icon);
					if (image == null) {
						ImageDescriptor imageDescriptor = SpringUIPlugin
								.imageDescriptorFromPlugin(ns, icon);
						SpringUIPlugin.getDefault().getImageRegistry().put(
								icon, imageDescriptor);
						image = SpringUIPlugin.getDefault().getImageRegistry()
								.get(icon);
					}
				}
			}
			if (image == null) {
				return SpringUIImages.getImage(SpringUIImages.IMG_OBJS_RULE);
			}
			else {
				return image;
			}
		}
	}

	private List<ProjectBuilderDefinition> projectBuilderDefinitions;

	private CheckboxTableViewer builderViewer;

	private Text descriptionText;

	private IProject project;

	public ProjectBuilderPropertyTab(IProject project) {
		this.projectBuilderDefinitions = ProjectBuilderDefinitionFactory
				.getProjectBuilderDefinitions();
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
		beansLabel
				.setText(SpringUIMessages.ProjectBuilderPropertyPage_description);
		// config set list viewer
		builderViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		builderViewer.getTable().setLayoutData(gd);
		builderViewer
				.setContentProvider(new ProjectBuilderDefinitionContentProvider(
						this.projectBuilderDefinitions));
		builderViewer.setLabelProvider(new ProjectBuilderLabelProvider());
		builderViewer.setInput(this); // activate content provider
		builderViewer.setCheckedElements(getEnabledProjectBuilderDefinitions()
				.toArray());
		builderViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						if (event.getSelection() instanceof IStructuredSelection) {
							IStructuredSelection sel = (IStructuredSelection) event
									.getSelection();
							ProjectBuilderDefinition definition = (ProjectBuilderDefinition) sel
									.getFirstElement();
							if (definition == null)
								clearDescription();
							else
								showDescription(definition);
						}
					}
				});

		Label descriptionLabel = new Label(composite, SWT.NONE);
		descriptionLabel
				.setText(SpringUIMessages.ProjectBuilderPropertyPage_builderDescription);

		descriptionText = new Text(composite, SWT.MULTI | SWT.WRAP
				| SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 30;
		descriptionText.setLayoutData(data);

		return composite;
	}

	private List<ProjectBuilderDefinition> getEnabledProjectBuilderDefinitions() {
		List<ProjectBuilderDefinition> builderDefinitions = this.projectBuilderDefinitions;
		List<ProjectBuilderDefinition> filteredBuilderDefinitions = new ArrayList<ProjectBuilderDefinition>();
		for (ProjectBuilderDefinition builderDefinition : builderDefinitions) {
			if (builderDefinition.isEnabled(project)) {
				filteredBuilderDefinitions.add(builderDefinition);
			}
		}
		return filteredBuilderDefinitions;
	}

	/**
	 * Show the selected description in the text.
	 */
	private void showDescription(ProjectBuilderDefinition definition) {
		if (descriptionText == null || descriptionText.isDisposed()) {
			return;
		}
		String text = definition.getDescription();
		if (text == null || text.length() == 0)
			descriptionText
					.setText(SpringUIMessages.ProjectBuilderPropertyPage_noBuilderDescription);
		else
			descriptionText.setText(text);
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
		final List checkElements = Arrays.asList(this.builderViewer
				.getCheckedElements());

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException,
					InterruptedException {
				for (ProjectBuilderDefinition projectBuilderDefinition : projectBuilderDefinitions) {
					if (checkElements.contains(projectBuilderDefinition)) {
						projectBuilderDefinition.setEnabled(true, project);
					}
					else {
						projectBuilderDefinition.getProjectBuilder().cleanup(
								project, new NullProgressMonitor());
						projectBuilderDefinition.setEnabled(false, project);
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
