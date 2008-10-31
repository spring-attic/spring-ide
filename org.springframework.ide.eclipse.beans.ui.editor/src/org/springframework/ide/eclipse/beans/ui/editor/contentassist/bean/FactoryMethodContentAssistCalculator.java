/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.MethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.FlagsMethodFilter;
import org.springframework.ide.eclipse.core.java.IMethodFilter;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * {@link IContentAssistCalculator} that calculates bean factory-method proposals based on the bean
 * class.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.2.0
 */
public class FactoryMethodContentAssistCalculator implements IContentAssistCalculator {

	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		Node node = context.getNode();
		NamedNodeMap attributes = node.getAttributes();
		Node factoryBean = attributes.getNamedItem("factory-bean");

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
			protected IType calculateType(IContentAssistContext context) {
				return JdtUtils.getJavaType(file.getProject(), factoryClassName);
			}
		};

		calculator.computeProposals(context, recorder);
	}

}
