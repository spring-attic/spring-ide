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
package org.springframework.ide.eclipse.webflow.ui.graph.properties;

import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.commands.EditPropertiesCommand;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AdvancedEditSection extends AbstractPropertySection {

	/**
	 * 
	 */
	private Button advancedEditButton;

	/**
	 * 
	 */
	private IWebflowModelElement state;

	/**
	 * 
	 */
	private IWebflowModelElement oldState;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);
		FormData data;

		advancedEditButton = getWidgetFactory().createButton(composite,
				"Advanced...", SWT.PUSH); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(90, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		advancedEditButton.setLayoutData(data);

		advancedEditButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IEditorPart editor = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getActiveEditor();
				ActionRegistry actionRegistry = (ActionRegistry) editor
						.getAdapter(ActionRegistry.class);
				EditPropertiesAction action = (EditPropertiesAction) actionRegistry
						.getAction(EditPropertiesAction.EDITPROPERTIES);
				action.setOpenDialog(true);
				
				EditPropertiesCommand command = new EditPropertiesCommand();

				IWebflowModelElement clone = null;

				if (state instanceof ICloneableModelElement) {
					clone = ((ICloneableModelElement) state)
							.cloneModelElement();
				}

				command.setChild(
						(ICloneableModelElement<IWebflowModelElement>) state,
						clone);
				if (action != null) {
					action.runWithCommand(command);
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#setInput(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof IStructuredSelection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		if (oldState != null) {
		}
		if (input instanceof AbstractEditPart
				&& ((AbstractEditPart) input).getModel() instanceof IWebflowModelElement) {
			state = (IWebflowModelElement) ((AbstractEditPart) input).getModel();
			oldState = state;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
	 */
	public void refresh() {
	}

}
