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
package org.springframework.ide.eclipse.config.ui.editors.namespaces;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.namespaces.INamespaceDefinition;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigLabelProvider;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class NamespacesLabelProvider extends AbstractConfigLabelProvider {

	private final IResource resource;

	public NamespacesLabelProvider(IResource resource) {
		this.resource = resource;
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Image getColumnImage(Object element, int columnIndex) {
		return getImage(element);
	}

	public String getColumnText(Object element, int columnIndex) {
		return getText(element);
	}

	public Image getImage(Object element) {
		if (element instanceof INamespaceDefinition) {
			INamespaceDefinition xsdDef = (INamespaceDefinition) element;
			return xsdDef.getNamespaceImage();
		}
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
	}

	public String getText(Object element) {
		if (element instanceof INamespaceDefinition) {
			INamespaceDefinition xsdDef = (INamespaceDefinition) element;
			return xsdDef.getNamespacePrefix(resource) + " - " + xsdDef.getNamespaceURI(); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

}
