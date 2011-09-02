/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IHyperlinkCalculator} for the constructor-arg name
 * @author Leo Dos Santos
 * @since 2.8.0
 */
public class ConstructorArgNameHyperlinkCalculator implements IHyperlinkCalculator {

	/**
	 * {@inheritDoc}
	 */
	public IHyperlink createHyperlink(String name, String target, Node node,
			Node parentNode, IDocument document, ITextViewer textViewer,
			IRegion hyperlinkRegion, IRegion cursor) {
		String parentName = null;
		if (parentNode != null) {
			parentName = parentNode.getNodeName();
		}

		List<String> propertyPaths = new ArrayList<String>();
		hyperlinkRegion = BeansEditorUtils.extractPropertyPathFromCursorPosition(hyperlinkRegion,
				cursor, target, propertyPaths);
		if ("bean".equals(parentName) && StringUtils.hasText(target)) {
			IFile file = BeansEditorUtils.getFile(document);
			String className = BeansEditorUtils.getClassNameForBean(file, node.getOwnerDocument(), parentNode);
			IType type = JdtUtils.getJavaType(file.getProject(), className);
			
			if (type != null) {
				IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
				if (config != null && parentNode instanceof Element) {
					IModelElement element = BeansModelUtils.getModelElement((Element) parentNode, config);
					int argIndex = getArgumentIndex(node);
					if (argIndex >= 0) {
						if (element instanceof IBean) {
							IBean bean = (IBean) element;
							int count = bean.getConstructorArguments().size();
							if (count > 0) {
								try {
									Set<IMethod> methods = Introspector.getConstructors(type, count, false);
									Iterator<IMethod> iter = methods.iterator();
									while (iter.hasNext()) {
										IMethod candidate = iter.next();
										if (target.equalsIgnoreCase(candidate.getParameterNames()[argIndex])) {
											// return new JavaElementHyperlink(hyperlinkRegion, candidate.getParameters()[argIndex]);
											// TODO: just a temporary workaround for making this Eclipse 3.6 compatible
											return new JavaElementHyperlink(hyperlinkRegion, candidate);
										}
									}
								} catch (JavaModelException e) {
									// do nothing
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	private int getArgumentIndex(Node arg) {
		if (arg instanceof Element && "constructor-arg".equals(arg.getNodeName())) {
			Element parent = (Element) arg.getParentNode();
			NodeList list = parent.getElementsByTagName("constructor-arg");
			for (int i = 0; i < list.getLength(); i++) {
				Node candidate = list.item(i);
				if (arg.equals(candidate)) {
					return i;
				}
			}
		}
		return -1;
	}
	
}
