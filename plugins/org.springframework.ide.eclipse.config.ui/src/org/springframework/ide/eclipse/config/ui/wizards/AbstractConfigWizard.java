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

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Node;


/**
 * The base class for all wizards contributed to an {@link AbstractConfigEditor}through the <code>com.springsource.sts.config.ui.commonActions</code>
 * extension point.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractConfigWizard extends Wizard {

	/**
	 * An XML formatter.
	 */
	protected ShallowFormatProcessorXML formatter;

	/**
	 * The namespace URI associated with this wizard.
	 */
	protected String namespaceUri;

	/**
	 * The resource file of the XML source
	 */
	protected IFile file;

	/**
	 * The original document object model of the XML source file.
	 */
	protected IDOMDocument domDocument;

	/**
	 * The new element returned by the wizard.
	 */
	protected IDOMElement newElement;

	/**
	 * A temporary element created when the wizard is initialized.
	 */
	protected IDOMElement input;

	private IDOMModel copiedModel;

	/**
	 * Creates a new wizard.
	 */
	public AbstractConfigWizard() {
		super();
		formatter = new ShallowFormatProcessorXML();
	}

	/**
	 * This method is called automatically when the wizard is initialized.
	 * Clients must override to create a temporary element with empty attributes
	 * for the wizard to work on.
	 * 
	 * @param copiedDocument
	 */
	protected abstract void createInput(IDOMDocument copiedDocument);

	@Override
	public void dispose() {
		if (copiedModel != null) {
			copiedModel.releaseFromEdit();
			copiedModel = null;
		}
		super.dispose();
	}

	/**
	 * Returns the {@link IDOMDocument} representation of the XML source file.
	 * 
	 * @return document object model of the XML source file
	 */
	protected IDOMDocument getDomDocument() {
		return domDocument;
	}

	/**
	 * Returns the XML element that serves as the model for the wizard.
	 * 
	 * @return temporary element operated on by the wizard
	 */
	protected IDOMElement getInput() {
		return input;
	}

	/**
	 * Returns the new element created by the wizard.
	 * 
	 * @return new element created by the wizard
	 */
	public IDOMElement getNewElement() {
		return newElement;
	}

	/**
	 * Returns the namespace prefix used in the XML elements of interest to this
	 * wizard.
	 * 
	 * @return namespace prefix of relevant XML elements
	 */
	protected String getPrefixForNamespaceUri() {
		return ConfigCoreUtils.getPrefixForNamespaceUri(domDocument, namespaceUri);
	}

	/**
	 * Returns the {@link IFile} of the XML source.
	 * 
	 * @return resource file of the XML source
	 */
	protected IFile getResourceFile() {
		return file;
	}

	/**
	 * Returns the root <code>beans</code> node.
	 * 
	 * @return root node
	 */
	protected Node getRootNode() {
		if (domDocument != null) {
			return domDocument.getDocumentElement();
		}
		return null;
	}

	/**
	 * Initializes the wizard with a document model to work from and a namespace
	 * URI. Clients must invoke this method before opening the wizard.
	 * 
	 * @parem file the file to operate on
	 * @param domDocument the document object model of the XML source file
	 * @param namespaceUri the namespace URI associated with this wizard
	 */
	public void initialize(IFile file, IDOMDocument domDocument, String namespaceUri) {
		this.file = file;
		this.domDocument = domDocument;
		this.namespaceUri = namespaceUri;

		try {
			IStructuredModel model = domDocument.getModel();
			IModelManager modelManager = model.getModelManager();
			copiedModel = (IDOMModel) modelManager.createNewInstance(model);
			IDOMDocument copiedDocument = copiedModel.getDocument();
			createInput(copiedDocument);
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
					.getString("AbstractConfigWizard.ERROR_INITIALIZING_WIZARD"), e)); //$NON-NLS-1$
		}
	}

}
