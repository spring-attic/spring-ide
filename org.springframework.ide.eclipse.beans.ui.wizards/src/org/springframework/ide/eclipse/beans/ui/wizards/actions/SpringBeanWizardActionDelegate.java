/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.wizards.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.beans.ui.wizards.BeansWizardsPlugin;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.SpringBeanWizardWorkbenchAware;
import org.springframework.ide.eclipse.core.ui.actions.AbstractObjectActionDelegate;
import org.springframework.ide.eclipse.core.ui.dialogs.message.ErrorDialog;
import org.springframework.ide.eclipse.core.ui.dialogs.wizards.WizardFormsDialog;
import org.springframework.ide.eclipse.core.ui.utils.PluginUtils;

public class SpringBeanWizardActionDelegate extends AbstractObjectActionDelegate {

	private static final String PREFIX = "Wizards.BeansDeclarationAction";

	protected Shell getShell() {
		Shell result = null;
		if (this.getDefaultShell() != null) {
			result = this.getDefaultShell();
		} else {
			result = PluginUtils.getActiveShell(BeansWizardsPlugin.getDefault());
		}
		return result;
	}

	public boolean isEnabled() {
		return true;
	}

	public SpringBeanWizardActionDelegate() {
		super();
	}

	public void run(IAction action) {
		try {
			SpringBeanWizardWorkbenchAware springBeansDeclarationWizard=new SpringBeanWizardWorkbenchAware();
			springBeansDeclarationWizard.init(PluginUtils.getWorkbench(), getSelection());
			new WizardFormsDialog(getShell(),
					springBeansDeclarationWizard).open();
		} catch (Throwable e) {
			ErrorDialog errorDialog = new org.springframework.ide.eclipse.core.ui.dialogs.message.ErrorDialog("Spring IDE Error",
					"Error while launching wizard.", e);
			errorDialog.open();
		}
	}
}
