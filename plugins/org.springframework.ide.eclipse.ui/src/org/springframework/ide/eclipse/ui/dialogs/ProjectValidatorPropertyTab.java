/*******************************************************************************
 * Copyright (c) 2007, 2012 Spring IDE Developers
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import org.springframework.ide.eclipse.ui.SpringUIImages;
import org.springframework.ide.eclipse.ui.SpringUIMessages;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * UI component that enables the use to enable and disable {@link IValidator}s and {@link IValidationRule}s on a per
 * project basis.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Terry Denney
 * @since 2.0
 */
public class ProjectValidatorPropertyTab {

	private static class ProjectValidatorContentProvider implements ITreeContentProvider {

		private List<ValidatorDefinition> validatorDefinitions;

		private Map<ValidatorDefinition, List<ValidationRuleDefinition>> validationRuleDefinitions;

		public ProjectValidatorContentProvider(List<ValidatorDefinition> validatorDefinitions,
				Map<ValidatorDefinition, List<ValidationRuleDefinition>> validationRuleDefinitions) {
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
				return this.validationRuleDefinitions.get((ValidatorDefinition) parentElement).toArray();
			}
			else {
				return IModelElement.NO_CHILDREN;
			}
		}

		public Object getParent(Object element) {
			if (element instanceof ValidationRuleDefinition) {
				for (ValidatorDefinition def : this.validatorDefinitions) {
					if (((ValidationRuleDefinition) element).getValidatorId().equals(def.getID())) {
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
				return ((ValidationRuleDefinition) element).getName();
			}
			return super.getText(element);
		}

		public Image getImage(Object element) {
			Image image = null;
			if (element instanceof ValidatorDefinition) {
				String icon = ((ValidatorDefinition) element).getIconUri();
				String ns = ((ValidatorDefinition) element).getNamespaceUri();
				if (icon != null && ns != null) {
					image = SpringUIPlugin.getDefault().getImageRegistry().get(icon);
					if (image == null) {
						ImageDescriptor imageDescriptor = SpringUIPlugin.imageDescriptorFromPlugin(ns, icon);
						SpringUIPlugin.getDefault().getImageRegistry().put(icon, imageDescriptor);
						image = SpringUIPlugin.getDefault().getImageRegistry().get(icon);
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

	private ITreeContentProvider contentProvider;

	private List<ValidatorDefinition> validatorDefinitions;

	private Map<ValidatorDefinition, List<ValidationRuleDefinition>> validationRuleDefinitions;

	private CheckboxTreeViewer validatorViewer;

	private Text descriptionText;

	private IProject project;

	private Shell shell;

	private Button configureButton;

	private Button resetButton;

	private Map<ValidationRuleDefinition, Map<String, String>> changedPropertyValues = new HashMap<ValidationRuleDefinition, Map<String, String>>();

	private Map<ValidationRuleDefinition, Map<String, Integer>> changedMessageSeverities = new HashMap<ValidationRuleDefinition, Map<String, Integer>>();

	private SelectionListener buttonListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	private SelectionListener resetListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if ((Button) e.widget == resetButton) {
				initializeCheckedState(null);
			}
		}
	};

	private Object[] checkedElements;

	public ProjectValidatorPropertyTab(Shell shell, IProject project) {
		this.validatorDefinitions = ValidatorDefinitionFactory.getValidatorDefinitions();
		this.validationRuleDefinitions = new HashMap<ValidatorDefinition, List<ValidationRuleDefinition>>();
		for (ValidatorDefinition def : this.validatorDefinitions) {
			List<ValidationRuleDefinition> rules = new ArrayList<ValidationRuleDefinition>();
			rules.addAll(ValidationRuleDefinitionFactory.getRuleDefinitions(def.getID()));

			Collections.sort(rules, new Comparator<ValidationRuleDefinition>() {

				public int compare(ValidationRuleDefinition o1, ValidationRuleDefinition o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			validationRuleDefinitions.put(def, rules);
		}
		this.project = project;
		this.shell = shell;
	}

	protected void handleButtonPressed(Button widget) {
		if (widget == configureButton) {
			IStructuredSelection selection = (IStructuredSelection) validatorViewer.getSelection();
			if (!selection.isEmpty()) {
				Object obj = selection.getFirstElement();
				if (obj instanceof ValidationRuleDefinition) {
					ValidationRuleDefinition ruleDef = (ValidationRuleDefinition) obj;
					Map<String, String> propertyValues = null;
					if (changedPropertyValues.containsKey(ruleDef)) {
						propertyValues = new HashMap<String, String>(changedPropertyValues.get(ruleDef));
					}
					else {
						propertyValues = new HashMap<String, String>(ruleDef.getPropertyValues());
					}

					Map<String, Integer> messageSeverities = null;
					if (changedMessageSeverities.containsKey(ruleDef)) {
						messageSeverities = new HashMap<String, Integer>(changedMessageSeverities.get(ruleDef));
					}
					else {
						messageSeverities = new HashMap<String, Integer>(ruleDef.getMessageSeverities());
					}

					ValidationRuleConfigurationDialog dialog = new ValidationRuleConfigurationDialog(this.shell,
							propertyValues, messageSeverities, ruleDef);
					if (dialog.open() == Dialog.OK) {
						changedPropertyValues.put(ruleDef, propertyValues);
						changedMessageSeverities.put(ruleDef, messageSeverities);
					}
				}
			}
		}
	}

	public Control createContents(Composite parent) {
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 3;
		layout.marginWidth = 3;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (project != null ) {
			Label beansLabel = new Label(composite, SWT.NONE);
			beansLabel.setText(SpringUIMessages.ProjectValidatorPropertyPage_description);
		}

		Composite tableAndButtons = new Composite(composite, SWT.NONE);
		tableAndButtons.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		tableAndButtons.setLayout(layout);

		// config set list viewer
		validatorViewer = new CheckboxTreeViewer(tableAndButtons);
		// validatorViewer.setUseHashlookup(true);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		this.contentProvider = new ProjectValidatorContentProvider(this.validatorDefinitions,
				this.validationRuleDefinitions);
		validatorViewer.setContentProvider(this.contentProvider);
		validatorViewer.setLabelProvider(new ProjectBuilderLabelProvider());
		validatorViewer.setInput(this); // activate content provider

		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		data.heightHint = 150;
		validatorViewer.getControl().setLayoutData(data);
		validatorViewer.getControl().setFont(font);

		validatorViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					if (sel.getFirstElement() == null) {
						clearDescription();
					}
					else {
						showDescription(sel.getFirstElement());

					}
					updateConfigureButtonEnablement(sel.getFirstElement());
				}
			}
		});
		
		validatorViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChange(event);
				checkedElements = validatorViewer.getCheckedElements();
			}
		});

		// Create button area
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		configureButton = new Button(buttonArea, SWT.PUSH);
		configureButton.setText("Configure");
		configureButton.setEnabled(false);
		configureButton.addSelectionListener(buttonListener);

		Label descriptionLabel = new Label(composite, SWT.NONE);
		descriptionLabel.setText(SpringUIMessages.ProjectValidatorPropertyPage_builderDescription);

		descriptionText = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 30;
		descriptionText.setLayoutData(data);
		
		resetButton = new Button(composite, SWT.PUSH);
		resetButton.setText("Restore Defaults");
		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
		resetButton.setLayoutData(data);
		resetButton.addSelectionListener(resetListener);

		initializeCheckedState(project);

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
			if (validatorViewer.getChecked(members[i]) || validatorViewer.getGrayed(members[i])) {
				childChecked = true;
				break;
			}
		}
		validatorViewer.setGrayChecked(parent, childChecked);
		updateParentState(parent);
	}

	private void setSubtreeChecked(Object container, boolean state, boolean checkExpandedState) {
		Object[] members = contentProvider.getChildren(container);
		for (int i = members.length - 1; i >= 0; i--) {
			Object element = members[i];
			boolean elementGrayChecked = validatorViewer.getGrayed(element) || validatorViewer.getChecked(element);
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

	private List<Object> getEnabledProjectBuilderDefinitions(IProject project) {
		List<ValidatorDefinition> validatorDefinitions = this.validatorDefinitions;
		List<Object> filteredValidatorDefinitions = new ArrayList<Object>();
		for (ValidatorDefinition builderDefinition : validatorDefinitions) {
			if (builderDefinition.isEnabled(project)) {
				filteredValidatorDefinitions.add(builderDefinition);
			}
		}
		for (List<ValidationRuleDefinition> defs : this.validationRuleDefinitions.values()) {
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
		if (text == null || text.length() == 0) {
			descriptionText.setText(SpringUIMessages.ProjectValidatorPropertyPage_noBuilderDescription);
		}
		else {
			descriptionText.setText(text);
		}
	}

	private void initializeCheckedState(final IProject project) {

		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				List items = getEnabledProjectBuilderDefinitions(project);
				validatorViewer.setCheckedElements(items.toArray());

				for (int i = 0; i < items.size(); i++) {
					Object item = items.get(i);
					updateParentState(item);
				}
				
				checkedElements = validatorViewer.getCheckedElements();
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

	private void updateConfigureButtonEnablement(Object firstElement) {
		if (firstElement instanceof ValidationRuleDefinition
				&& (((ValidationRuleDefinition) firstElement).getPropertyValues().size() > 0 || ((ValidationRuleDefinition) firstElement)
						.getMessageSeverities().size() > 0)) {
			configureButton.setEnabled(true);
		}
		else {
			configureButton.setEnabled(false);
		}
	}

	public boolean performOk() {
		final List checkElements = checkedElements != null ? Arrays.asList(checkedElements) : Collections.emptyList();

		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {
				for (Map.Entry<ValidatorDefinition, List<ValidationRuleDefinition>> def : validationRuleDefinitions
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

						if (changedPropertyValues.containsKey(rule) || changedMessageSeverities.containsKey(rule)) {
							rule.setSpecificConfiguration(changedPropertyValues.get(rule), changedMessageSeverities
									.get(rule), project);
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
