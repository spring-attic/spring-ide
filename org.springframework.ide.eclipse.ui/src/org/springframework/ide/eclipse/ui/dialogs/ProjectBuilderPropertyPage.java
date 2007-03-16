/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.PropertyPage;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinitionFactory;
import org.springframework.ide.eclipse.ui.SpringUIMessages;

/**
 * Provides an {@link PropertyPage} that allows to manage the enablement of
 * {@link IProjectBuilder} for the underlying {@link IProject}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class ProjectBuilderPropertyPage extends PropertyPage {

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
	}

	private List<ProjectBuilderDefinition> projectBuilderDefinitions;

	private CheckboxTableViewer builderViewer;

	private Text descriptionText;

	public ProjectBuilderPropertyPage() {
		this.projectBuilderDefinitions = ProjectBuilderDefinitionFactory
				.getProjectBuilderDefinitions();
		noDefaultAndApplyButton();
	}

	@Override
	protected Control createContents(Composite parent) {
		TabFolder folder = new TabFolder(parent, SWT.NONE);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText(SpringUIMessages.ProjectBuilderPropertyPage_title);

		Composite composite = new Composite(folder, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
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

		item.setControl(composite);

		Dialog.applyDialogFont(folder);

		return folder;
	}

	private List<ProjectBuilderDefinition> getEnabledProjectBuilderDefinitions() {
		List<ProjectBuilderDefinition> builderDefinitions = this.projectBuilderDefinitions;
		List<ProjectBuilderDefinition> filteredBuilderDefinitions = new ArrayList<ProjectBuilderDefinition>();
		for (ProjectBuilderDefinition builderDefinition : builderDefinitions) {
			if (builderDefinition.isEnabled((IProject) getElement())) {
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
		final IProject project = (IProject) getElement();
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

		return super.performOk();
	}
}
