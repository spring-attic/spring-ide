/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.db;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractHyperLinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ExternalBeanHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 */
public class DbHyperLinkDetector extends AbstractHyperLinkDetector implements
		IHyperlinkDetector {

	/**
	 * Checks if a given attribute is linkable
	 * @return true if the attribute allows hyperlinking
	 */
	@Override
	protected boolean isLinkableAttr(Attr attr) {
		String attrName = attr.getName();
		return "driver-class-name".equals(attrName)
				|| "connection-properties-ref".equals(attrName);
	}

	/**
	 * Creates the {@link IHyperlink} instance
	 */
	@Override
	protected IHyperlink createHyperlink(String name, String target,
			Node parentNode, IRegion hyperlinkRegion, IDocument document,
			Node node, ITextViewer textViewer, IRegion cursor) {
		if (name == null) {
			return null;
		}
		if ("driver-class-name".equals(name)) {
			IFile file = BeansEditorUtils.getFile(document);
			IType type = BeansModelUtils.getJavaType(file.getProject(), target);
			if (type != null) {
				return new JavaElementHyperlink(hyperlinkRegion, type);
			}
		}
		else if ("connection-properties-ref".equals(name)) {
			IFile file = BeansEditorUtils.getFile(document);
			// assume this is an external reference
			Iterator<?> beans = BeansEditorUtils.getBeansFromConfigSets(file)
					.iterator();
			while (beans.hasNext()) {
				IBean modelBean = (IBean) beans.next();
				if (modelBean.getElementName().equals(target)) {
					return new ExternalBeanHyperlink(modelBean, hyperlinkRegion);
				}
			}
		}
		return null;
	}
}
