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
package org.springframework.ide.eclipse.config.ui.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.config.ui.wizards.NamespaceConfigWizard;


/**
 * Command Handler that opens a dialog to configure namespaces.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class OpenNamespaceConfigWizardCommandHandler implements IHandler2 {

	private boolean isEnabled;

	public void addHandlerListener(IHandlerListener handlerListener) {

	}

	public void dispose() {

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			EvaluationContext context = (EvaluationContext) event.getApplicationContext();
			IFile xmlFile = getFile(context);
			if (BeansCoreUtils.isBeansConfig(xmlFile)) {
				EditorPart editor = (EditorPart) (context.getParent().getVariable("activePart"));
				NamespaceConfigWizard namespaceWizard = new NamespaceConfigWizard(xmlFile, editor);
				WizardDialog wizardDialog = new WizardDialog(Display.getDefault().getActiveShell(), namespaceWizard);
				wizardDialog.setBlockOnOpen(true);
				wizardDialog.create();
				wizardDialog.open();
			}
			else {
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Configure Namespaces",
						"Namespaces can not be configured for this file.");
			}

		}
		catch (CoreException ce) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Configure Namespaces",
					"Namespaces can not be configured for this file.");
		}

		return null;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {

	}

	public void setEnabled(Object evaluationContext) {
		IFile file = getFile(evaluationContext);
		if (BeansCoreUtils.isBeansConfig(file)) {
			isEnabled = true;
		}
		else {
			isEnabled = false;
		}
	}

	private IFile getFile(Object evaluationContext) {
		if (evaluationContext instanceof EvaluationContext) {
			EvaluationContext context = (EvaluationContext) evaluationContext;
			Object activePart = context.getParent().getVariable("activePart");
			if (activePart instanceof EditorPart) {
				EditorPart editor = (EditorPart) activePart;
				IEditorInput editorInput = editor.getEditorInput();
				if (editorInput instanceof FileEditorInput) {
					return ((FileEditorInput) editor.getEditorInput()).getFile();
				}
			}
		}
		return null;
	}

}
