/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;

/**
 * {@link IHyperlinkCalculator} implementation that can be used to link to a
 * {@link IType} those name is qualified in the given <code>target</code>.
 * @author Christian Dupuis
 * @author Terry Denney
 * @since 2.0.2
 */
public class ClassHyperlinkCalculator implements IHyperlinkCalculator {

	public IHyperlink createHyperlink(String name, String target, Node node,
			Node parentNode, IDocument document, ITextViewer textViewer,
			IRegion hyperlinkRegion, IRegion cursor) {
		IHyperlink[] detectedHyperlinks = HyperlinkUtils.getXmlJavaHyperlinks(textViewer, hyperlinkRegion);
		
		// return null if xml Java hyperlink will be created to avoid duplicates
		if (detectedHyperlinks != null && detectedHyperlinks.length > 0) {
			return null;
		}
		
		IFile file = BeansEditorUtils.getFile(document);
		IType type = JdtUtils.getJavaType(file.getProject(), target);
		if (type != null) {
			return new JavaElementHyperlink(hyperlinkRegion, type);
		}
		
		return null;
	}

}
