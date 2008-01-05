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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.jms;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.MethodHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;

/**
 * {@link INamespaceHyperlinkDetector} responsible for handling hyperlink
 * detection on elements of the <code>jms:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class JmsHyperlinkDetector extends NamespaceHyperlinkDetectorSupport
		implements IHyperlinkDetector {

	@Override
	public void init() {
		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();
		registerHyperlinkCalculator("listener-container", "connection-factory", beanRef);
		registerHyperlinkCalculator("listener-container", "task-executor", beanRef);
		registerHyperlinkCalculator("listener-container", "destination-resolver", beanRef);
		registerHyperlinkCalculator("listener-container", "message-converter", beanRef);
		registerHyperlinkCalculator("listener-container", "transaction-manager", beanRef);
		registerHyperlinkCalculator("listener", "ref", beanRef);
		registerHyperlinkCalculator("jca-listener-container", "resource-adapter", beanRef);
		registerHyperlinkCalculator("jca-listener-container", "activation-spec-factory", beanRef);
		registerHyperlinkCalculator("jca-listener-container", "message-converter", beanRef);
		
		registerHyperlinkCalculator("listener", "method",
				new MethodHyperlinkCalculator() {

					@Override
					protected IType calculateType(String name, String target,
							Node node, Node parentNode, IDocument document) {
						if (BeansEditorUtils.hasAttribute(node, "ref")) {
							String ref = BeansEditorUtils.getAttribute(
									node, "ref");

							if (ref != null) {
								IFile file = BeansEditorUtils.getFile(document);
								String className = BeansEditorUtils
										.getClassNameForBean(file, node
												.getOwnerDocument(), ref);
								return JdtUtils.getJavaType(file
										.getProject(), className);
							}
						}
						return null;
					}
				});
	}
}
