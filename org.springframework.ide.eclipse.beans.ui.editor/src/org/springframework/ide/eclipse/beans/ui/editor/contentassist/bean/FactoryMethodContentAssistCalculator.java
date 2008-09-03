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
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
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
 * @since 2.1.1
 */
@SuppressWarnings("restriction")
public class FactoryMethodContentAssistCalculator implements IContentAssistCalculator {

	public void computeProposals(ContentAssistRequest request, String matchString,
			String attributeName, String namespace, String namepacePrefix) {
		Node node = request.getNode();
		NamedNodeMap attributes = node.getAttributes();
		Node factoryBean = attributes.getNamedItem("factory-bean");
		
		String factoryClassName = null;
		boolean isStatic;
		if (factoryBean != null) {
			// instance factory method
			factoryClassName = BeansEditorUtils.getClassNameForBean(BeansEditorUtils
					.getFile(request), node.getOwnerDocument(), factoryBean.getNodeValue());
			isStatic = false;
		}
		else {
			// static factory method
			factoryClassName = BeansEditorUtils.getAttribute(node, "class");
			isStatic = true;
		}
		
		if (factoryClassName != null) {
			addFactoryMethodAttributeValueProposals(request, matchString, factoryClassName,
					isStatic);
		}
	}

	private void addFactoryMethodAttributeValueProposals(ContentAssistRequest request,
			String prefix, final String factoryClassName, boolean isStatic) {
		if (BeansEditorUtils.getFile(request) instanceof IFile) {
			final IFile file = BeansEditorUtils.getFile(request);

			IMethodFilter filter = null;
			if (isStatic) {
				filter = new FlagsMethodFilter(FlagsMethodFilter.STATIC
						| FlagsMethodFilter.NOT_VOID | FlagsMethodFilter.NOT_INTERFACE
						| FlagsMethodFilter.NOT_CONSTRUCTOR);
			}
			else {
				filter = new FlagsMethodFilter(FlagsMethodFilter.NOT_VOID
						| FlagsMethodFilter.NOT_INTERFACE | FlagsMethodFilter.NOT_CONSTRUCTOR);
			}

			IContentAssistCalculator calculator = new MethodContentAssistCalculator(filter) {

				@Override
				protected IType calculateType(ContentAssistRequest request, String attributeName) {
					return JdtUtils.getJavaType(file.getProject(), factoryClassName);
				}
			};

			calculator.computeProposals(request, prefix, null, null, null);
		}
	}

}
