/*******************************************************************************
 * Copyright (c) 2010 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.editor.hyperlink.webflow;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.w3c.dom.Node;

/**
 * {@link IHyperlinkCalculator} for the method attribute.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.3.4
 */
public class WebflowActionMethodHyperlinkCalculator implements IHyperlinkCalculator {

	public IHyperlink createHyperlink(String name, String target, Node node,
			Node parentNode, IDocument document, ITextViewer textViewer,
			IRegion hyperlinkRegion, IRegion cursor) {
		if (BeansEditorUtils.hasAttribute(node, "bean")) {
			String bean = BeansEditorUtils.getAttribute(node, "bean");
			IFile file = BeansEditorUtils.getFile(document);
			if (file != null && file.exists()) {
				IWebflowConfig config = Activator.getModel().getProject(file.getProject()).getConfig(
						file);
				if (config != null) {
					String className = null;
					Set<IBean> beans = WebflowModelUtils.getBeans(config);
					for (IBean modelBean : beans) {
						if (modelBean.getElementName().equals(bean)) {
							className = BeansModelUtils.getBeanClass(modelBean, null);
						}
					}
					IType type = JdtUtils.getJavaType(file.getProject(), className);
					if (type != null) {
						try {
							Set<IMethod> methods = Introspector.getAllMethods(type);
							if (methods != null) {
								for (IMethod method : methods) {
									if (method.getElementName().equals(target)) {
										return new JavaElementHyperlink(hyperlinkRegion, method);
									}
								}
							}
						}
						catch (JavaModelException e) {
						}
					}
				}
			}
		}
		return null;
	}

}
