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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinitionFactory;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.validation.IValidator;
import org.springframework.ide.eclipse.ui.SpringUIMessages;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * UI component that enables the use to enable and disable {@link IValidator}s
 * and {@link IValidationRule}s on a per project basis.
 * @author Christian Dupuis
 * @since 2.0
 */
public class ProjectValidatorPropertyTab {

	private static class ProjectValidatorContentProvider implements
			ITreeContentProvider {

		private Set<ValidatorDefinition> validatorDefinitions;

		private Map<ValidatorDefinition, Set<ValidationRuleDefinition>> validationRuleDefinitions;

		public ProjectValidatorContentProvider(
				Set<ValidatorDefinition> validatorDefinitions,
				Map<ValidatorDefinition, Set<ValidationRuleDefinition>> validationRuleDefinitions) {
			this.validatorDefinitions = validatorDefinitions;
			this.validationRuleDefinitions = validationRuleDefinitions;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			return this.validatorDefinitions.toArray();
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ValidatorDefinition) {
				return this.validationRuleDefinitions.get(
						(ValidatorDefinition) parentElement).toArray();
			}
			else {
				return IModelElement.NO_CHILDREN;
			}
		}

		public Object getParent(Object element) {
			if (element instanceof ValidationRuleDefinition) {
				for (ValidatorDefinition def : this.validatorDefinitions) {
					if (((ValidationRuleDefinition) element).getValidatorId()
							.equals(def.getID())) {
						return def;
					}
				}
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}

	private static class ProjectBuilderLabelProvider extends LabelProvider {

		public String getText(Object element) {
			if (element instanceof ValidatorDefinition) {
				return ((ValidatorDefinition) element).getName();
			}
			else if (element instanceof ValidationRuleDefinition) {
				return ((ValidationRuleDefinition) element).getDescription();
			}
			return super.getText(element);
		}

		public Image getImage(Object element) {
			Image image = null;
			if (element instanceof ValidatorDefinition) {
				String icon = ((ValidatorDefinition) element).getIconUri();
				String ns = ((ValidatorDefinition) element).getNamespaceUri();
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
				return super.getImage(element);
			}
			else {
				return image;
			}
		}
	}

	private ITreeContentProvider contentProvider;

	private Set<ValidatorDefinition> validatorDefinitions;

	private Map<ValidatorDefinition, Set<ValidationRuleDefinition>> validationRuleDefinitions;

	private CheckboxTreeViewer validatorViewer;

	private Text descriptionText;

	private IProject project;

	private Shell shell;

	public ProjectValidatorPropertyTab(Shell shell, IProject project) {
		this.validatorDefinitions = ValidatorDefinitionFactory
				.getValidatorDefinitions();
		this.validationRuleDefinitions = new HashMap<ValidatorDefinition, Set<ValidationRuleDefinition>>();
		for (ValidatorDefinition def : this.validatorDefinitions) {
			validationRuleDefinitions.put(def, ValidationRuleDefinitionFactory
					.getRuleDefinitions(def.getID()));
		}
		this.project = project;
		this.shell = shell;
	}

	public Control createContents(Composite parent) {
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label beansLabel = new Label(composite, SWT.NONE);
		beansLabel
				.setText(SpringUIMessages.ProjectValidatorPropertyPage_description);
		// config set list viewer
		validatorViewer = new CheckboxTreeViewer(composite);
		// validatorViewer.setUseHashlookup(true);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		this.contentProvider = new ProjectValidatorContentProvider(
				this.validatorDefinitions, this.validationRuleDefinitions);
		validatorViewer.setContentProvider(this.contentProvider);
		validatorViewer.setLabelProvider(new ProjectBuilderLabelProvider());
		validatorViewer.setInput(this); // activate content provider

		GridData data = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_VERTICAL);
		data.heightHint = 150;
		validatorViewer.getControl().setLayoutData(data);
		validatorViewer.getControl().setFont(font);

		validatorViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						if (event.getSelection() instanceof IStructuredSelection) {
							IStructuredSelection sel = (IStructuredSelection) event
									.getSelection();
							if (sel.getFirstElement() == null)
								clearDescription();
							else
								showDescription(sel.getFirstElement());
						}
					}
				});

		validatorViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChange(event);
			}
		});

		Label descriptionLabel = new Label(composite, SWT.NONE);
		descriptionLabel
				.setText(SpringUIMessages.ProjectValidatorPropertyPage_builderDescription);

		descriptionText = new Text(composite, SWT.MULTI | SWT.WRAP
				| SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 30;
		descriptionText.setLayoutData(data);

		initializeCheckedState();

		return composite;
	}

	private void handleCheckStateChange(final CheckStateChangedEvent event) {
		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				boolean state = event.getChecked();
				setSubtreeChecked(event.getElement(), state, true);
				updateParentState(event.getElement());
			}
		});
	}

	private void updateParentState(Object child) {
		if (child == null || contentProvider.getParent(child) == null) {
			return;
		}
		Object parent = contentProvider.getParent(child);
		boolean childChecked = false;
		Object[] members = contentProvider.getChildren(parent);
		for (int i = members.length - 1; i >= 0; i--) {
			if (validatorViewer.getChecked(members[i])
					|| validatorViewer.getGrayed(members[i])) {
				childChecked = true;
				break;
			}
		}
		validatorViewer.setGrayChecked(parent, childChecked);
		updateParentState(parent);
	}

	private void setSubtreeChecked(Object container, boolean state,
			boolean checkExpandedState) {
		Object[] members = contentProvider.getChildren(container);
		for (int i = members.length - 1; i >= 0; i--) {
			Object element = members[i];
			boolean elementGrayChecked = validatorViewer.getGrayed(element)
					|| validatorViewer.getChecked(element);
			if (state) {
				validatorViewer.setChecked(element, true);
				validatorViewer.setGrayed(element, false);
			}
			else {
				validatorViewer.setGrayChecked(element, false);
			} // unchecked state only
			// needs
			if ((state || elementGrayChecked)) {
				setSubtreeChecked(element, state, true);
			}
		}
	}

	private List<Object> getEnabledProjectBuilderDefinitions() {
		Set<ValidatorDefinition> validatorDefinitions = this.validatorDefinitions;
		List<Object> filteredValidatorDefinitions = new ArrayList<Object>();
		for (ValidatorDefinition builderDefinition : validatorDefinitions) {
			if (builderDefinition.isEnabled(project)) {
				filteredValidatorDefinitions.add(builderDefinition);
			}
		}
		for (Set<ValidationRuleDefinition> defs : this.validationRuleDefinitions
				.values()) {
			for (ValidationRuleDefinition def : defs) {
				if (def.isEnabled(project)) {
					filteredValidatorDefinitions.add(def);
				}
			}
		}
		return filteredValidatorDefinitions;
	}

	/**
	 * Show the selected description in the text.
	 */
	private void showDescription(Object definition) {
		if (descriptionText == null || descriptionText.isDisposed()) {
			return;
		}
		String text = null;
		if (definition instanceof ValidatorDefinition) {
			text = ((ValidatorDefinition) definition).getDescription();
		}
		else if (definition instanceof ValidationRuleDefinition) {
			text = ((ValidationRuleDefinition) definition).getDescription();
		}
		if (text == null || text.length() == 0)
			descriptionText
					.setText(SpringUIMessages.ProjectValidatorPropertyPage_noBuilderDescription);
		else
			descriptionText.setText(text);
	}

	private void initializeCheckedState() {

		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				List items = getEnabledProjectBuilderDefinitions();
				validatorViewer.setCheckedElements(items.toArray());

				for (int i = 0; i < items.size(); i++) {
					Object item = items.get(i);
					updateParentState(item);
				}
			}
		});
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
		final List checkElements = Arrays.asList(this.validatorViewer
				.getCheckedElements());

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor)
					throws CoreException, InvocationTargetException,
					InterruptedException {
				for (Map.Entry<ValidatorDefinition, Set<ValidationRuleDefinition>> def : validationRuleDefinitions
						.entrySet()) {
					boolean enableValidator = false;
					for (ValidationRuleDefinition rule : def.getValue()) {
						if (checkElements.contains(rule)) {
							enableValidator = true;
							rule.setEnabled(true, project);
						}
						else {
							rule.setEnabled(false, project);
						}
					}
					def.getKey().setEnabled(enableValidator, project);
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
