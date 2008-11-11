/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.bean;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Node;

/**
 * {@link IHyperlinkCalculator} for the property name
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.2.1
 */
public class PropertyNameHyperlinkCalculator implements IHyperlinkCalculator {

	/**
	 * {@inheritDoc}
	 */
	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode,
			IDocument document, ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		String parentName = null;
		if (parentNode != null) {
			parentName = parentNode.getNodeName();
		}

		List<String> propertyPaths = new ArrayList<String>();
		hyperlinkRegion = BeansEditorUtils.extractPropertyPathFromCursorPosition(hyperlinkRegion,
				cursor, target, propertyPaths);
		if ("bean".equals(parentName)) {
			IFile file = BeansEditorUtils.getFile(document);
			List<?> classNames = BeansEditorUtils.getClassNamesOfBean(file, parentNode);

			IMethod method = BeansEditorUtils.extractMethodFromPropertyPathElements(propertyPaths,
					classNames, file, 0);
			if (method != null) {
				return new JavaElementHyperlink(hyperlinkRegion, method);
			}
		}
		return null;
	}

}
