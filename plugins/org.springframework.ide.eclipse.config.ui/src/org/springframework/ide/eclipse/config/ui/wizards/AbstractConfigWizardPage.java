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
package org.springframework.ide.eclipse.config.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.contentassist.XmlBackedContentProposalAdapter;


/**
 * The base class for pages added to an {@link AbstractConfigWizard}.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractConfigWizardPage extends WizardPage {

	/**
	 * The container wizard.
	 */
	protected AbstractConfigWizard wizard;

	/**
	 * Creates a new wizard page.
	 * 
	 * @param wizard the container wizard
	 * @param pageName the name of the page
	 */
	public AbstractConfigWizardPage(AbstractConfigWizard wizard, String pageName) {
		super(pageName);
		this.wizard = wizard;
	}

	/**
	 * This method is called automatically when the page's controls are created.
	 * Clients should implement this method instead of overriding the
	 * {@link AbstractConfigWizardPage#createAttributes(Composite)} method.
	 * 
	 * @param parent the parent composite
	 */
	protected abstract void createAttributes(Composite parent);

	public void createControl(Composite parent) {
		createAttributes(parent);
		hookContentProposalAdapters();
	}

	/**
	 * Returns the {@link IDOMDocument} representation of the XML source file.
	 * 
	 * @return document object model of the XML source file
	 */
	protected IDOMDocument getDomDocument() {
		return wizard.getDomDocument();
	}

	/**
	 * Returns the XML element that serves as the model for the wizard.
	 * 
	 * @return temporary element operated on by the wizard
	 */
	protected IDOMElement getInput() {
		return wizard.getInput();
	}

	/**
	 * Returns the {@link IFile} of the XML source.
	 * 
	 * @return resource file of the XML source
	 */
	protected IFile getResourceFile() {
		return wizard.getResourceFile();
	}

	/**
	 * THis method is called automatically when the page's controls are created.
	 * Clients should consider adding an {@link XmlBackedContentProposalAdapter}
	 * here for every text field they wish to add content proposal assistance
	 * to.
	 */
	protected abstract void hookContentProposalAdapters();

}
