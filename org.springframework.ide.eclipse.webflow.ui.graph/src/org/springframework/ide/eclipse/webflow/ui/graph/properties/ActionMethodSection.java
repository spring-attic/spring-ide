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
package org.springframework.ide.eclipse.webflow.ui.graph.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.Assert;
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
import org.springframework.ide.eclipse.webflow.core.internal.model.AbstractAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.EditPropertiesCommand;
import org.springframework.ide.eclipse.webflow.ui.graph.dialogs.DialogUtils;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class ActionMethodSection extends AbstractPropertySection implements
		PropertyChangeListener {

	/**
	 * 
	 */
	private Text methodText;

	/**
	 * 
	 */
	private Button browseMethodButton;

	/**
	 * 
	 */
	private AbstractAction action;

	/**
	 * 
	 */
	private AbstractAction oldAction;

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
			EditPropertiesAction editAction = (EditPropertiesAction) actionRegistry
					.getAction(EditPropertiesAction.EDITPROPERTIES);
			editAction.setOpenDialog(false);
			EditPropertiesCommand command = new EditPropertiesCommand();

			AbstractAction clone = null;

			if (action instanceof Action) {
				clone = (AbstractAction) ((Action) action)
						.cloneModelElement();
			}
			else if (action instanceof BeanAction) {
				clone = (AbstractAction) ((BeanAction) action)
						.cloneModelElement();
			}
			clone.setMethod(methodText.getText());
			command
					.setChild(
							(ICloneableModelElement<IWebflowModelElement>) action,
							clone);

			if (editAction != null) {
				action
						.removePropertyChangeListener(ActionMethodSection.this);
				editAction.runWithCommand(command);
				action
						.addPropertyChangeListener(ActionMethodSection.this);
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);
		FormData data;

		browseMethodButton = getWidgetFactory().createButton(composite,
				"...", SWT.PUSH); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(96, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		browseMethodButton.setLayoutData(data);
		browseMethodButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleSelectionChanged();
			}

		});

		methodText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		methodText.setEditable(false);
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(95, 0);
		data.top = new FormAttachment(browseMethodButton, 0, SWT.CENTER);
		methodText.setLayoutData(data);
		methodText.addModifyListener(listener);

		CLabel comboLabel = getWidgetFactory().createCLabel(composite,
				"Method:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(methodText,
				-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(browseMethodButton, 0, SWT.CENTER);
		comboLabel.setLayoutData(data);
	}

	/**
	 * 
	 */
	protected void handleSelectionChanged() {
		ElementListSelectionDialog dialog = DialogUtils
				.openActionMethodReferenceDialog(oldAction.getNode());
		if (Dialog.OK == dialog.open()) {
			this.methodText.setText(((IMethod) dialog.getFirstResult())
					.getElementName());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#setInput(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof IStructuredSelection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		if (oldAction != null) {
			oldAction.removePropertyChangeListener(this);
		}
		if (input instanceof AbstractStatePart
				&& ((AbstractStatePart) input).getModel() instanceof AbstractAction) {
			action = (AbstractAction) ((AbstractStatePart) input)
					.getModel();
			action.addPropertyChangeListener(this);
			oldAction = action;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	public void refresh() {
		methodText.removeModifyListener(listener);
		if (action != null && action.getMethod() != null) {
			methodText.setText(action.getMethod());
		}
		else {
			methodText.setText("");
		}
		methodText.addModifyListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#dispose()
	 */
	public void dispose() {
		if (action != null) {
			action.removePropertyChangeListener(this);
		}
		super.dispose();
	}
}
