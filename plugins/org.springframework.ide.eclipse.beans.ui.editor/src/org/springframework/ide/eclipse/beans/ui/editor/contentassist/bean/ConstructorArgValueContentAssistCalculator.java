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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Extension of {@link ClassContentAssistCalculator} that is applied to the name attribute of the
 * constructor-arg element.
 * <p>
 * @author Leo Dos Santos
 * @since 2.8.0
 */
public class ConstructorArgValueContentAssistCalculator extends ClassContentAssistCalculator implements
	IContentAssistCalculator {
	
	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		String matchString = context.getMatchString();
		// If no matchString is given don't do any content assist calculation
		if (matchString == null || matchString.length() == 0) {
			return;
		}

		if (context.getParentNode() != null
				&& "bean".equals(context.getParentNode().getLocalName())) {
			String propertyName = BeansEditorUtils.getAttribute(context.getNode(), "name");
			if (StringUtils.hasText(propertyName)) {
				String className = BeansEditorUtils.getClassNameForBean(context.getFile(), context
						.getDocument(), context.getParentNode());
				IType type = JdtUtils.getJavaType(context.getFile().getProject(), className);
				if (type != null) {
					try {
						IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(context.getFile()));
						if (config != null && context.getParentNode() instanceof Element) {
							IModelElement element = BeansModelUtils.getModelElement((Element) context.getParentNode(), config);
							int argIndex = getArgumentIndex(context.getNode());
							if (argIndex >= 0) {
								if (element instanceof IBean) {
									IBean bean = (IBean) element;
									int count = bean.getConstructorArguments().size();
									if (count > 0) {
										IMethod method = null;
										Set<IMethod> methods = Introspector.getConstructors(type, count, false);
										if (methods.size() == 1) {
											IMethod[] array = methods.toArray(new IMethod[]{});
											IMethod candidate = array[0];
											if (isConstructorArgComputable(type, candidate, argIndex)) {
												method = candidate;
											}
										} else {
											Iterator<IMethod> iter = methods.iterator();
											while (iter.hasNext()) {
												IMethod candidate = iter.next();
												if (isConstructorArgComputable(type, candidate, argIndex)) {
													method = candidate;
													break;
												}
											}
										}
										if (method != null) {
											super.computeProposals(context, recorder);
										}
									}
								}
							}
						}
					}
					catch (JavaModelException e) {
						// do nothing
					}
				}
			}
		}
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
	
	private boolean isConstructorArgComputable(IType type, IMethod candidate, int argIndex) {
		String parameterType = JdtUtils.resolveClassNameBySignature(candidate
				.getParameterTypes()[argIndex], type);
		// Class and String can be converted in Class instances
		if (Class.class.getName().equals(parameterType)
				|| String.class.getName().equals(parameterType)) {
			return true;
		}
		return false;
	}
	
}
