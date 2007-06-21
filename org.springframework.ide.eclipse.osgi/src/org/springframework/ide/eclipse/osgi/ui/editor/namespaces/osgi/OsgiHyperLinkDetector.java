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
package org.springframework.ide.eclipse.osgi.ui.editor.namespaces.osgi;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.AbstractHyperLinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class OsgiHyperLinkDetector extends AbstractHyperLinkDetector implements
		IHyperlinkDetector {

	private static final Set<String> VALID_ATTRIBUTES;

	static {
		VALID_ATTRIBUTES = new LinkedHashSet<String>();
		VALID_ATTRIBUTES.add("ref");
		VALID_ATTRIBUTES.add("depends-on");
		VALID_ATTRIBUTES.add("interface");
	}

	@Override
	protected boolean isLinkableAttr(Attr attr) {
		return VALID_ATTRIBUTES.contains(attr.getLocalName());
	}

	@Override
	protected IHyperlink createHyperlink(String name, String target,
			Node parentNode, IRegion hyperlinkRegion, IDocument document,
			Node node, ITextViewer textViewer, IRegion cursor) {
		if (name == null) {
			return null;
		}
		if ("depends-on".equals(name)) {
			return createBeanReferenceHyperlink(target, hyperlinkRegion,
					document, node, textViewer);
		}
		else if ("ref".equals(name)) {
			return createBeanReferenceHyperlink(target, hyperlinkRegion,
					document, node, textViewer);
		}
		else if ("interface".equals(name)) {
			IFile file = BeansEditorUtils.getFile(document);
			IType type = JdtUtils.getJavaType(file.getProject(), target);
			if (type != null) {
				return new JavaElementHyperlink(hyperlinkRegion, type);
			}
		}
		return null;
	}
}
