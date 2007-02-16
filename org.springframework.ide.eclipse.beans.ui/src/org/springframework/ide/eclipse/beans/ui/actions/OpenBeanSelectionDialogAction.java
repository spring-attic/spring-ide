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

package org.springframework.ide.eclipse.beans.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.dialogs.BeanListSelectionDialog;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Action that opens the Bean SelectionDialog
 * 
 * @author Christian Dupuis
 */
public class OpenBeanSelectionDialogAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;

	public void run(IAction action) {
		BeanListSelectionDialog dialog = new BeanListSelectionDialog(workbenchWindow.getShell(),
				new BeansModelLabelProvider());
		if (Dialog.OK == dialog.open()) {
			IBean bean = (IBean) dialog.getFirstResult();
			SpringUIUtils.openInEditor(bean);
		}
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.workbenchWindow = window;
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
}
