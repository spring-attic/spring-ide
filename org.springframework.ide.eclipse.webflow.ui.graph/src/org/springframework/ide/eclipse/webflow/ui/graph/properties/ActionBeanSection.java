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

import org.eclipse.gef.ui.actions.ActionRegistry;
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
import org.springframework.ide.eclipse.beans.core.model.IBean;
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
public class ActionBeanSection extends AbstractPropertySection implements
		PropertyChangeListener {

	/**
	 * 
	 */
	private Text beanText;

	/**
	 * 
	 */
	private Button browseBeanButton;

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
			clone.setBean(beanText.getText());
			command
					.setChild(
							(ICloneableModelElement<IWebflowModelElement>) action,
							clone);

			if (editAction != null) {
				action
						.removePropertyChangeListener(ActionBeanSection.this);
				editAction.runWithCommand(command);
				action
						.addPropertyChangeListener(ActionBeanSection.this);
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

		browseBeanButton = getWidgetFactory().createButton(composite,
				"...", SWT.PUSH); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(96, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		browseBeanButton.setLayoutData(data);
		browseBeanButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleSelectionChanged();
			}

		});

		beanText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		beanText.setEditable(false);
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(95, 0);
		data.top = new FormAttachment(browseBeanButton, 0, SWT.CENTER);
		beanText.setLayoutData(data);
		beanText.addModifyListener(listener);

		CLabel comboLabel = getWidgetFactory().createCLabel(composite, "Bean:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(beanText,
				-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(browseBeanButton, 0, SWT.CENTER);
		comboLabel.setLayoutData(data);
	}

	/**
	 * 
	 */
	protected void handleSelectionChanged() {
		ElementListSelectionDialog dialog = DialogUtils
				.openBeanReferenceDialog(this.beanText.getText(), false);
		if (Dialog.OK == dialog.open()) {
			this.beanText.setText(((IBean) dialog.getFirstResult())
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
		beanText.removeModifyListener(listener);
		if (action != null && action.getBean() != null) {
			beanText.setText(action.getBean());
		}
		else {
			beanText.setText("");
		}
		beanText.addModifyListener(listener);
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
