/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.graph.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.EditPropertiesCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelDecorator;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelProvider;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class StateTransitionSection extends AbstractPropertySection implements
		PropertyChangeListener {

	/**
	 * 
	 */
	private Text onText;

	/**
	 * 
	 */
	private Text toText;

	/**
	 * 
	 */
	private Button browseStateButton;

	/**
	 * 
	 */
	private IStateTransition stateTransition;

	/**
	 * 
	 */
	private IStateTransition oldStateTransition;

	/**
	 * 
	 */
	private ModifyListener listener = new ModifyListener() {

		public void modifyText(ModifyEvent arg0) {

			IEditorPart editor = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			ActionRegistry actionRegistry = (ActionRegistry) editor
					.getAdapter(ActionRegistry.class);
			EditPropertiesAction action = (EditPropertiesAction) actionRegistry
					.getAction(EditPropertiesAction.EDITPROPERTIES);
			EditPropertiesCommand command = new EditPropertiesCommand();

			IStateTransition clone = null;

			if (stateTransition instanceof ICloneableModelElement) {
				clone = (IStateTransition) ((ICloneableModelElement) stateTransition)
						.cloneModelElement();
			}
			clone.setOn(onText.getText());
			command
					.setChild(
							(ICloneableModelElement<IWebflowModelElement>) stateTransition,
							clone);

			if (action != null) {
				stateTransition
						.removePropertyChangeListener(StateTransitionSection.this);
				action.runWithCommand(command);
				stateTransition
						.addPropertyChangeListener(StateTransitionSection.this);
			}
		}
	};

	/**
	 * 
	 */
	private ModifyListener toStateListener = new ModifyListener() {

		public void modifyText(ModifyEvent arg0) {

			IEditorPart editor = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			ActionRegistry actionRegistry = (ActionRegistry) editor
					.getAdapter(ActionRegistry.class);
			EditPropertiesAction action = (EditPropertiesAction) actionRegistry
					.getAction(EditPropertiesAction.EDITPROPERTIES);
			EditPropertiesCommand command = new EditPropertiesCommand();

			IState state = WebflowModelUtils.getStateById(
					(IWebflowState) stateTransition.getElementParent()
							.getElementParent(), toText.getText());
			command.setNewTarget((ITransitionableTo) state);
			command
					.setChild((ICloneableModelElement<IWebflowModelElement>) stateTransition);
			command.setOnlyReconnect(true);

			if (action != null) {
				stateTransition
						.removePropertyChangeListener(StateTransitionSection.this);
				action.runWithCommand(command);
				stateTransition
						.addPropertyChangeListener(StateTransitionSection.this);
			}
		}
	};

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);
		FormData data;

		onText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		onText.setLayoutData(data);
		onText.addModifyListener(listener);

		CLabel labelLabel = getWidgetFactory().createCLabel(composite,
				"Event id:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(onText,
				-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(onText, 0, SWT.CENTER);
		labelLabel.setLayoutData(data);

		browseStateButton = getWidgetFactory().createButton(composite,
				"...", SWT.PUSH); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(96, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(labelLabel,
				ITabbedPropertyConstants.VSPACE);
		browseStateButton.setLayoutData(data);
		browseStateButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleSelectionChanged();
			}

		});

		toText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		toText.setEditable(false);
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(95, 0);
		data.top = new FormAttachment(browseStateButton, 0, SWT.CENTER);
		toText.setLayoutData(data);
		toText.addModifyListener(toStateListener);

		CLabel comboLabel = getWidgetFactory().createCLabel(composite,
				"To State id:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(toText,
				-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(browseStateButton, 0, SWT.CENTER);
		comboLabel.setLayoutData(data);
	}

	/**
	 * 
	 */
	protected void handleSelectionChanged() {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getPart().getSite().getShell(), new DecoratingLabelProvider(
						new WebflowModelLabelProvider(),
						new WebflowModelLabelDecorator()));
		dialog.setBlockOnOpen(true);
		dialog.setElements(WebflowModelUtils.getStates(
				this.stateTransition.getElementParent(), false).toArray());
		dialog.setEmptySelectionMessage("Enter a valid state id");
		dialog.setTitle("State reference");
		dialog.setMessage("Please select a state reference");
		dialog.setMultipleSelection(false);
		if (Dialog.OK == dialog.open()) {
			this.toText.setText(((IState) dialog.getFirstResult()).getId());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#setInput(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof IStructuredSelection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		if (oldStateTransition != null) {
			oldStateTransition.removePropertyChangeListener(this);
		}
		if (input instanceof AbstractConnectionEditPart
				&& ((AbstractConnectionEditPart) input).getModel() instanceof IStateTransition) {
			stateTransition = (IStateTransition) ((AbstractConnectionEditPart) input)
					.getModel();
			stateTransition.addPropertyChangeListener(this);
			oldStateTransition = stateTransition;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	public void refresh() {
		onText.removeModifyListener(listener);
		toText.removeModifyListener(toStateListener);
		if (stateTransition.getOn() != null) {
			onText.setText(stateTransition.getOn());
		}
		if (stateTransition.getToState() != null) {
			toText.setText(stateTransition.getToState().getId());
		}
		onText.addModifyListener(listener);
		toText.addModifyListener(toStateListener);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#dispose()
	 */
	public void dispose() {
		if (stateTransition != null) {
			stateTransition.removePropertyChangeListener(this);
		}
		super.dispose();
	}
}
