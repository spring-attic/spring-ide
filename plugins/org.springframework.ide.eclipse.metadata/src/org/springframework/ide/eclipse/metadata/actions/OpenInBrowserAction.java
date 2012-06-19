/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.metadata.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.metadata.MetadataUIImages;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingMethodToClassMap;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingView;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingViewLabelProvider;
import org.springframework.ide.eclipse.metadata.wizards.OpenRequestMappingUrlWizard;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class OpenInBrowserAction extends BaseSelectionListenerAction {

	private RequestMappingView viewPart;

	private RequestMappingViewLabelProvider labelProvider;

	private IBeansModelElement element;

	public OpenInBrowserAction(RequestMappingView viewPart,
			RequestMappingViewLabelProvider labelProvider) {
		super(Messages.OpenInBrowserAction_TITLE);
		setImageDescriptor(MetadataUIImages.DESC_OBJS_BROWSER);
		this.viewPart = viewPart;
		this.labelProvider = labelProvider;
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		element = viewPart.getInput();
		if (getProject(element) == null) {
			return false;
		}

		Object obj = selection.getFirstElement();
		if (obj instanceof RequestMappingMethodToClassMap) {
			String methodType = labelProvider.getColumnText(obj,
					RequestMappingView.COLUMN_REQUEST_METHOD);
			return "RequestMethod.GET".equalsIgnoreCase(methodType); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	public void run() {
		String url = labelProvider.getColumnText(getStructuredSelection()
				.getFirstElement(), RequestMappingView.COLUMN_URL);
		if (url != null) {
			IWizard wizard = new OpenRequestMappingUrlWizard(
					(RequestMappingMethodToClassMap) getStructuredSelection()
							.getFirstElement(), labelProvider,
					getProject(element));
			Shell shell = viewPart.getSite().getShell();
			if (shell != null) {
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.create();
				dialog.setBlockOnOpen(true);
				dialog.open();
			}
		}
	}

	private IProject getProject(IBeansModelElement modelElement) {
		if (modelElement == null) {
			return null;
		}
		if (modelElement instanceof IBeansProject) {
			return ((IBeansProject) modelElement).getProject();
		}
		return BeansModelUtils.getParentOfClass(modelElement,
				IBeansProject.class).getProject();
	}

}
