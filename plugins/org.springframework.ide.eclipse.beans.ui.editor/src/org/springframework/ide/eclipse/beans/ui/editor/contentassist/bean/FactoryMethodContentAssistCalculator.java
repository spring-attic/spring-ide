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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.MethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.FlagsMethodFilter;
import org.springframework.ide.eclipse.core.java.IMethodFilter;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * {@link IContentAssistCalculator} that calculates bean factory-method proposals based on the bean
 * class.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.2.0
 */
public class FactoryMethodContentAssistCalculator implements IContentAssistCalculator {

	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		Node node = context.getNode();
		NamedNodeMap attributes = node.getAttributes();
		Node factoryBean = getFactoryBeanReferenceNode(attributes);

		String factoryClassName = null;
		boolean isStatic;
		if (factoryBean != null) {
			// instance factory method
			factoryClassName = BeansEditorUtils.getClassNameForBean(context.getFile(), context
					.getDocument(), factoryBean.getNodeValue());
			isStatic = false;
		}
		else {
			// static factory method
			factoryClassName = BeansEditorUtils.getAttribute(node, "class");
			isStatic = true;
		}

		if (factoryClassName != null) {
			addFactoryMethodAttributeValueProposals(recorder, context, factoryClassName, isStatic);
		}
	}

	protected Node getFactoryBeanReferenceNode(NamedNodeMap attributes) {
		return attributes.getNamedItem("factory-bean");
	}

	private void addFactoryMethodAttributeValueProposals(IContentAssistProposalRecorder recorder,
			IContentAssistContext context, final String factoryClassName, boolean isStatic) {
		final IFile file = context.getFile();

		IMethodFilter filter = null;
		if (isStatic) {
			filter = new FlagsMethodFilter(FlagsMethodFilter.STATIC | FlagsMethodFilter.NOT_VOID
					| FlagsMethodFilter.NOT_INTERFACE | FlagsMethodFilter.NOT_CONSTRUCTOR);
		}
		else {
			filter = new FlagsMethodFilter(FlagsMethodFilter.NOT_VOID
					| FlagsMethodFilter.NOT_INTERFACE | FlagsMethodFilter.NOT_CONSTRUCTOR);
		}

		IContentAssistCalculator calculator = new MethodContentAssistCalculator(filter) {
			
			@Override
			public void computeProposals(IContentAssistContext context,
					IContentAssistProposalRecorder recorder) {
				super.computeProposals(context, recorder);
				
				IType type = calculateType(context);
				try {
					// Add special valueOf methods for Enum types
					if (type != null && type.isEnum()) {
						IFile contextFile = context.getFile();
						if (contextFile != null && contextFile.exists()) {
							IType enumType = JdtUtils.getJavaType(context.getFile().getProject(), Enum.class.getName());
							Set<String> proposedMethods = new HashSet<String>();
							for (IMethod method : Introspector.findAllMethods(enumType, context
									.getMatchString(), filter)) {
								if (!proposedMethods.contains(method.getElementName())) {
									proposedMethods.add(method.getElementName());
									createMethodProposal(recorder, method);
								}
							}
						}
					}
				}
				catch (JavaModelException e) {
				}
			}

			@Override
			protected IType calculateType(IContentAssistContext context) {
				if (file != null && file.exists()) {
					return JdtUtils.getJavaType(file.getProject(), factoryClassName);
				}
				return null;
			}
		};

		calculator.computeProposals(context, recorder);
	}

}
