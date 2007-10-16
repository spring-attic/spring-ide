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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;

/**
 * {@link IHyperlinkCalculator} implementation that can be used to link to
 * {@link IField}.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class FieldHyperlinkCalculator implements IHyperlinkCalculator {

	public IHyperlink createHyperlink(String name, String target, Node node,
			Node parentNode, IDocument document, ITextViewer textViewer,
			IRegion hyperlinkRegion, IRegion cursor) {
		int ix = target.lastIndexOf('.');
		if (ix > 0) {
			String typeName = target.substring(0, ix);
			IFile file = BeansEditorUtils.getFile(document);
			IType type = JdtUtils.getJavaType(file.getProject(), typeName);
			if (type != null) {
				try {
					IField[] fields = type.getFields();
					for (IField field : fields) {
						if (target.endsWith('.' + field.getElementName())) {
							return new JavaElementHyperlink(hyperlinkRegion,
									field);
						}
					}
				}
				catch (JavaModelException e) {
				}
				
				// if we reach here no matching field could be located
				return new JavaElementHyperlink(hyperlinkRegion, type);
			}
			// fallback
			else {
				type = JdtUtils.getJavaType(file.getProject(), target);
				if (type != null) {
					return new JavaElementHyperlink(hyperlinkRegion, type);
				}
			}
		}
		return null;
	}
}
