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
package org.springframework.ide.eclipse.wizard.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.wizard.listeners.PropertiesPageShownListener;


/**
 * Wizard dialog for Bean Wizard. Use factory create methods to create these
 * dialogs to ensure Bean Wizard to work properly.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeanWizardDialog extends WizardDialog {

	public static BeanWizardDialog createBeanWizardDialog(Shell shell) {
		return createBeanWizardDialog(shell, null, true);
	}

	public static BeanWizardDialog createBeanWizardDialog(Shell shell, IFile beanFile, boolean enabledFileBrowsing) {
		BeanWizard wizard = new BeanWizard(beanFile, enabledFileBrowsing);
		return createDialog(shell, wizard);
	}

	public static WizardDialog createBeanWizardDialog(Shell shell, IFile file, boolean enabledFileBrowsing,
			String qualifiedTypeName) {
		BeanWizard wizard = new BeanWizard(file, enabledFileBrowsing);
		IDOMElement newBean = wizard.getNewBean();
		newBean.setAttribute(BeansSchemaConstants.ATTR_CLASS, qualifiedTypeName);
		return createDialog(shell, wizard);
	}

	private static BeanWizardDialog createDialog(Shell shell, BeanWizard wizard) {
		BeanWizardDialog dialog = new BeanWizardDialog(shell, wizard);
		dialog.addPageChangedListener(new PropertiesPageShownListener(wizard));
		return dialog;
	}

	public static BeanWizardDialog createModifyBeanWizardDialog(Shell shell, IFile beanFile, IDOMElement existingNode) {
		BeanWizard wizard = new BeanWizard(existingNode, beanFile);
		return createDialog(shell, wizard);
	}

	private final BeanWizard wizard;

	private BeanWizardDialog(Shell shell, BeanWizard wizard) {
		super(shell, wizard);
		this.wizard = wizard;
	}

	public IDOMElement getNewBean() {
		return wizard.getNewBean();
	}

}
