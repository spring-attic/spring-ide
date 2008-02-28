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
package org.springframework.ide.eclipse.osgi.ui.editor.hyperlink.osgi;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ClassHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.MethodHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;

/**
 * {@link INamespaceHyperlinkDetector} implementation responsible for the
 * <code>osgi:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class OsgiHyperlinkDetector extends NamespaceHyperlinkDetectorSupport implements
		IHyperlinkDetector {

	@Override
	public void init() {
		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();
		registerHyperlinkCalculator("depends-on", beanRef);
		registerHyperlinkCalculator("ref", beanRef);
		registerHyperlinkCalculator("interface", new ClassHyperlinkCalculator());

		MethodHyperlinkCalculator methodRef = new MethodHyperlinkCalculator() {

			@Override
			protected IType calculateType(String name, String target, Node node, Node parentNode,
					IDocument document) {
				if (node != null && "registration-listener".equals(node.getLocalName())) {
					String ref = BeansEditorUtils.getAttribute(node, "ref");
					if (ref != null) {
						IFile file = BeansEditorUtils.getFile(document);
						String className = BeansEditorUtils.getClassNameForBean(file, node
								.getOwnerDocument(), ref);
						return JdtUtils.getJavaType(file.getProject(), className);
					}
				}
				return null;
			}
		};
		registerHyperlinkCalculator("registration-method", methodRef);
		registerHyperlinkCalculator("unregistration-method", methodRef);

	}
}
