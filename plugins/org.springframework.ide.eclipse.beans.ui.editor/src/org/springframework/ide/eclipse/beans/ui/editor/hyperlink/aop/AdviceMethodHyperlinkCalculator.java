/*******************************************************************************
 * Copyright (c) 2008 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.aop;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.MethodHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;

/**
 * {@link IHyperlinkCalculator} for the aop advice method attribute.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.2.1
 */
public class AdviceMethodHyperlinkCalculator extends MethodHyperlinkCalculator {

	@Override
	protected IType calculateType(String name, String target, Node node, Node parentNode,
			IDocument document) {
		if (BeansEditorUtils.hasAttribute(parentNode, "ref")) {
			String ref = BeansEditorUtils.getAttribute(parentNode, "ref");

			if (ref != null) {
				IFile file = BeansEditorUtils.getFile(document);
				String className = BeansEditorUtils.getClassNameForBean(file, node
						.getOwnerDocument(), ref);
				if (file != null && file.exists()) {
					return JdtUtils.getJavaType(file.getProject(), className);
				}
			}
		}
		return null;
	}

}
