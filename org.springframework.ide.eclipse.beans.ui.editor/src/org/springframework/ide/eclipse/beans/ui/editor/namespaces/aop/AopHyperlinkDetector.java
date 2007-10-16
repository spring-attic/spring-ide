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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.aop;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ClassHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.MethodHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NodeElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link INamespaceHyperlinkDetector} responsible for the
 * <code>aop:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopHyperlinkDetector extends NamespaceHyperlinkDetectorSupport
		implements IHyperlinkDetector {

	@Override
	public void init() {
		ClassHyperlinkCalculator javaElement = new ClassHyperlinkCalculator();
		registerHyperlinkCalculator("implement-interface", javaElement);
		registerHyperlinkCalculator("default-impl", javaElement);

		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();
		registerHyperlinkCalculator("ref", beanRef);
		registerHyperlinkCalculator("advice-ref", beanRef);
		registerHyperlinkCalculator("delegate-ref", beanRef);

		registerHyperlinkCalculator("method",
				new MethodHyperlinkCalculator() {

					@Override
					protected IType calculateType(String name, String target,
							Node node, Node parentNode, IDocument document) {
						if (BeansEditorUtils.hasAttribute(parentNode, "ref")) {
							String ref = BeansEditorUtils.getAttribute(
									parentNode, "ref");

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

		registerHyperlinkCalculator("pointcut-ref", new IHyperlinkCalculator() {

			public IHyperlink createHyperlink(String name, String target,
					Node node, Node parentNode, IDocument document,
					ITextViewer textViewer, IRegion hyperlinkRegion,
					IRegion cursor) {
				IHyperlink hyperlink = searchPointcutElements(target, parentNode,
						textViewer, hyperlinkRegion);
				if (hyperlink == null && parentNode.getParentNode() != null) {
					hyperlink = searchPointcutElements(target, parentNode
							.getParentNode(), textViewer, hyperlinkRegion);
				}
				return hyperlink;
			}
		});
	}

	private IHyperlink searchPointcutElements(String name, Node node,
			ITextViewer textViewer, IRegion hyperlinkRegion) {
		NodeList beanNodes = node.getChildNodes();
		for (int i = 0; i < beanNodes.getLength(); i++) {
			Node beanNode = beanNodes.item(i);
			if ("pointcut".equals(beanNode.getLocalName())) {
				if (name.equals(BeansEditorUtils.getAttribute(beanNode, "id"))) {
					IRegion region = getHyperlinkRegion(beanNode);
					return new NodeElementHyperlink(hyperlinkRegion, region,
							textViewer);
				}
			}
		}
		return null;
	}
}
