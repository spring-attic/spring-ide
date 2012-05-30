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

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.config.core.schemas.OsgiSchemaConstants;
import org.w3c.dom.Node;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class OsgiServiceWizard extends AbstractConfigWizard {

	private OsgiServiceWizardPage page;

	private final FormatProcessorXML deepFormatter;

	public OsgiServiceWizard() {
		super();
		deepFormatter = new FormatProcessorXML();
		setWindowTitle(Messages.getString("OsgiServiceWizard.WINDOW_TITLE")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		page = new OsgiServiceWizardPage(this);
		addPage(page);
	}

	@Override
	protected void createInput(IDOMDocument copiedDocument) {
		if (copiedDocument != null) {
			String name;
			String prefix = getPrefixForNamespaceUri();
			if (prefix != null && prefix.length() > 0) {
				name = prefix + ":" + OsgiSchemaConstants.ELEM_SERVICE; //$NON-NLS-1$
			}
			else {
				name = OsgiSchemaConstants.ELEM_SERVICE;
			}
			input = (IDOMElement) copiedDocument.createElement(name);
			input.setAttribute(OsgiSchemaConstants.ATTR_ID, ""); //$NON-NLS-1$
			input.setAttribute(OsgiSchemaConstants.ATTR_REF, ""); //$NON-NLS-1$
		}
	}

	@Override
	public boolean performFinish() {
		String refStr = page.getRef();
		String idStr = page.getId();

		Node parent = getRootNode();
		if (parent != null && input != null) {
			IDOMModel model = domDocument.getModel();
			model.beginRecording(this);
			newElement = (IDOMElement) domDocument.importNode(input, true);
			parent.appendChild(newElement);
			newElement.setAttribute(OsgiSchemaConstants.ATTR_ID, idStr);
			newElement.setAttribute(OsgiSchemaConstants.ATTR_REF, refStr);

			List<Object> interfaces = page.getInterfaces();
			if (interfaces.size() == 1 && interfaces.get(0) instanceof IType) {
				String interfaceStr = ((IType) interfaces.get(0)).getFullyQualifiedName();
				newElement.setAttribute(OsgiSchemaConstants.ATTR_INTERFACE, interfaceStr);
			}
			else if (interfaces.size() > 1) {
				for (Object obj : interfaces) {
					if (obj instanceof IType) {
						String interfaceStr = ((IType) obj).getFullyQualifiedName();
						String name;
						String prefix = getPrefixForNamespaceUri();
						if (prefix != null && prefix.length() > 0) {
							name = getPrefixForNamespaceUri() + ":" + OsgiSchemaConstants.ELEM_INTERFACES; //$NON-NLS-1$
						}
						else {
							name = OsgiSchemaConstants.ELEM_INTERFACES;

						}
						IDOMElement interfaceElem = (IDOMElement) domDocument.createElement(name);
						interfaceElem.setAttribute(BeansSchemaConstants.ATTR_VALUE_TYPE, interfaceStr);
						newElement.appendChild(interfaceElem);
					}
				}
			}

			deepFormatter.formatNode(newElement);
			formatter.formatNode(newElement.getParentNode());
			model.endRecording(this);
		}
		return true;
	}
}
