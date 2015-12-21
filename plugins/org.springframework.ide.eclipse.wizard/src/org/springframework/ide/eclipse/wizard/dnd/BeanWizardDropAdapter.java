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
package org.springframework.ide.eclipse.wizard.dnd;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.wizard.ui.BeanWizardDialog;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class BeanWizardDropAdapter extends CommonDropAdapterAssistant {

	public BeanWizardDropAdapter() {
	}

	@Override
	public IStatus handleDrop(CommonDropAdapter dropAdapter,
			DropTargetEvent dropTargetEvent, Object target) {
		dropTargetEvent.detail = DND.DROP_NONE;
		
		if (dropTargetEvent.data instanceof IStructuredSelection && target instanceof IFile) {
			IFile file = (IFile) target;
			IStructuredSelection selection = (IStructuredSelection) dropTargetEvent.data;
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof ICompilationUnit) {
				ICompilationUnit cu = (ICompilationUnit) firstElement;
				String elementName = cu.getElementName();
				int pos = elementName.indexOf(".");
				String primaryTypeName = elementName.substring(0, pos);
				IType type = cu.getType(primaryTypeName);
				if (type != null) {
					return openWizard(type, file);
				}
			} else if (firstElement instanceof IType) {
				return openWizard((IType) firstElement, file);
			}
		}
		
		return Status.CANCEL_STATUS;
	}

	private IStatus openWizard(IType type, IFile file) {
		String qualifiedTypeName = type.getFullyQualifiedName();
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		WizardDialog dialog = BeanWizardDialog.createBeanWizardDialog(shell, file, true, qualifiedTypeName);
		dialog.create();
		dialog.setBlockOnOpen(true);
		dialog.open();
		return Status.OK_STATUS;
	}

	@Override
	public IStatus validateDrop(Object target, int operation,
			TransferData transferType) {
		if (target instanceof IResource) {
			if (BeansCoreUtils.isBeansConfig((IResource) target)) {
				return Status.OK_STATUS;
			}
		}
		return Status.CANCEL_STATUS;
	}

}
