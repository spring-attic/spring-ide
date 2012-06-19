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
package org.springframework.ide.eclipse.config.ui.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * The base content provider for the viewer in {@link AbstractConfigMasterPart},
 * where all elements are rooted to a <code>beans</code> element. Clients may
 * extend or override.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public class SpringConfigContentProvider implements ITreeContentProvider {

	private final AbstractConfigFormPage page;

	/**
	 * Constructs a content provider for a section part inside an
	 * {@link AbstractConfigFormPage}.
	 * 
	 * @param page the parent form page
	 */
	public SpringConfigContentProvider(AbstractConfigFormPage page) {
		super();
		this.page = page;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	/**
	 * This method is called automatically when the viewer retrieves the
	 * children of an element.
	 * 
	 * @param element the parent element
	 * @return list of child names for the given parent
	 */
	protected List<String> getChildNames(IDOMElement element) {
		return page.getXmlProcessor().getChildNames(element);
	}

	public Object[] getChildren(Object parentElement) {
		ArrayList<Object> result = new ArrayList<Object>();
		String uri = page.getNamespaceUri();
		if (parentElement instanceof IDOMElement) {
			IDOMElement node = (IDOMElement) parentElement;
			Node grandParent = node.getParentNode();
			NodeList list = node.getChildNodes();
			List<String> children = getChildNames(node);
			for (int i = 0; i < list.getLength(); i++) {
				Node child = list.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE) {
					// Add all elements in the document that match the page URI.
					if (uri == null || uri.equals(child.getNamespaceURI())
							|| isAdapterNamespace(child.getNamespaceURI())) {
						result.add(child);
					}
					// If we are not at the root, add all valid children,
					// regardless of namespace URI.
					else if (!(grandParent instanceof Document) && children.contains(child.getNodeName())) {
						result.add(child);
					}
				}
			}
		}
		return result.toArray();
	}

	public Object[] getElements(Object inputElement) {
		ArrayList<Object> result = new ArrayList<Object>();
		if (inputElement instanceof Document) {
			Element root = ((Document) inputElement).getDocumentElement();
			if (root != null) {
				result.add(root);
			}
		}
		return result.toArray();
	}

	public Object getParent(Object element) {
		if (element instanceof Node) {
			Node node = (Node) element;
			return node.getParentNode();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	private boolean isAdapterNamespace(String uri) {
		if (uri != null) {
			for (IConfigurationElement config : page.getAdapterDefinitions()) {
				if (uri.equals(config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI))) {
					return true;
				}
			}
		}
		return false;
	}

}
