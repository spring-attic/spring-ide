/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.processors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.quickfix.proposals.RenameToSimilarNameQuickFixProposal;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Quick assist processor for constructor-arg in beans XML editor.
 * @author Terry Denney
 * @since 2.8
 */
public class ConstructorArgNameQuickAssistProcessor extends BeanQuickAssistProcessor {

	private final IJavaProject javaProject;

	private final String className;

	private final int numArguments;

	private final Set<String> constructorArgNames;

	public ConstructorArgNameQuickAssistProcessor(int offset, int length, String text, String className,
			IProject project, boolean missingEndQuote, int numArguments, IDOMNode constructorArgNode) {
		super(offset, length, text, missingEndQuote);

		this.className = className;
		this.numArguments = numArguments;
		this.javaProject = JavaCore.create(project);

		this.constructorArgNames = new HashSet<String>();
		NodeList siblingNodes = constructorArgNode.getParentNode().getChildNodes();
		for (int i = 0; i < siblingNodes.getLength(); i++) {
			Node siblingNode = siblingNodes.item(i);
			if (siblingNode != constructorArgNode) {
				String localName = siblingNode.getLocalName();
				if (localName != null && localName.equals(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG)) {
					Node nameAttr = siblingNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_NAME);
					if (nameAttr != null) {
						constructorArgNames.add(nameAttr.getNodeValue());
					}
				}
			}
		}
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		try {
			IType type = javaProject.findType(className);
			if (type != null) {
				IMethod[] methods = type.getMethods();
				for (IMethod method : methods) {
					if (method.isConstructor()) {
						proposals.addAll(createProposalForConstructor(method));
					}
				}
			}
		}
		catch (JavaModelException e) {
			StatusHandler.log(e.getStatus());
		}

		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	private List<ICompletionProposal> createProposalForConstructor(IMethod method) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		if (method.getNumberOfParameters() == numArguments) {
			try {
				String[] paramNames = method.getParameterNames();
				for (String paramName : paramNames) {
					if (!constructorArgNames.contains(paramName)) {
						StringBuffer buffer = new StringBuffer();
						buffer.append("'");
						buffer.append(paramName);
						buffer.append("'");
						buffer.append(" in ");

						JavaElementLabels.getMethodLabel(method, JavaElementLabels.M_PARAMETER_TYPES
								| JavaElementLabels.M_PARAMETER_NAMES, buffer);

						proposals.add(new RenameToSimilarNameQuickFixProposal(paramName, offset, length,
								missingEndQuote, buffer.toString()));
					}
				}
			}
			catch (JavaModelException e) {
			}

		}

		return proposals;
	}

}
