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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;
import org.springframework.ide.eclipse.quickfix.proposals.AddConstructorArgQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.proposals.AddConstructorParamQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.proposals.CreateConstructorQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.proposals.RemoveConstructorArgQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.proposals.RemoveConstructorParamQuickFixProposal;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * Quick assist processor for constructor-arg in beans XML editor.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class ConstructorArgQuickAssistProcessor extends BeanQuickAssistProcessor {

	private final List<String> constructorArgClassNames;

	private final IJavaProject javaProject;

	private final String className;

	private final JavaElementLabelProvider labelProvider;

	private final IDOMNode beanNode;

	public ConstructorArgQuickAssistProcessor(int offset, int length, String text, IProject project,
			boolean missingEndQuote, List<String> constructorArgClassNames, IDOMNode beanNode) {
		super(offset, length, text, missingEndQuote);

		this.className = text;
		this.constructorArgClassNames = constructorArgClassNames;
		this.beanNode = beanNode;
		this.javaProject = JavaCore.create(project);
		this.labelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		try {
			IType type = javaProject.findType(className);
			IMethod[] methods = type.getMethods();
			for (IMethod method : methods) {
				if (method.isConstructor()) {
					proposals.addAll(createProposalForConstructor(method));
				}
			}
		}
		catch (JavaModelException e) {
			StatusHandler.log(e.getStatus());
		}

		proposals.add(new CreateConstructorQuickFixProposal(offset, length, className, missingEndQuote, javaProject,
				constructorArgClassNames));
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	private List<ICompletionProposal> createProposalForConstructor(IMethod method) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		int diff = method.getNumberOfParameters() - constructorArgClassNames.size();
		if (diff > 0) {
			proposals.addAll(createProposalForMoreParameters(method, diff));
		}
		else if (diff < 0) {
			proposals.addAll(createProposalForLessParameters(method, diff * -1));
		}
		return proposals;
	}

	/**
	 * Create proposals when number of <constructor-arg> is more than number of
	 * parameters in constructor
	 * @param constructor
	 * @param numAdditionalParams
	 * @return
	 */
	private List<ICompletionProposal> createProposalForLessParameters(IMethod constructor, int numAdditionalParams) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		String label = "Remove <constructor-arg> to match " + getConstructorDisplay(constructor);
		proposals.add(new RemoveConstructorArgQuickFixProposal(offset, length, missingEndQuote, numAdditionalParams,
				beanNode, label));

		label = "Change constructor " + getConstructorDisplay(constructor);
		proposals.add(new AddConstructorParamQuickFixProposal(offset, length, missingEndQuote, numAdditionalParams,
				constructor, label, javaProject));
		return proposals;
	}

	/**
	 * Create proposals when number of <constructor-arg> is less than number of
	 * parameters in constructor
	 * @param constructor
	 * @param numAdditionalArgs
	 * @return
	 */
	private List<ICompletionProposal> createProposalForMoreParameters(IMethod constructor, int numAdditionalArgs) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		String label = "Add <constructor-arg> to match " + getConstructorDisplay(constructor);
		proposals.add(new AddConstructorArgQuickFixProposal(offset, length, missingEndQuote, numAdditionalArgs,
				beanNode, label));

		IMethodBinding methodBinding = QuickfixUtils.getMethodBinding(javaProject, constructor);
		if (methodBinding != null) {
			label = "Change constructor " + getConstructorDisplay(constructor);
			proposals.add(new RemoveConstructorParamQuickFixProposal(offset, length, missingEndQuote,
					numAdditionalArgs, constructor, label, javaProject));
		}

		return proposals;
	}

	private String getConstructorDisplay(IMethod constructor) {
		return "'" + labelProvider.getText(constructor) + "'";
	}

}
