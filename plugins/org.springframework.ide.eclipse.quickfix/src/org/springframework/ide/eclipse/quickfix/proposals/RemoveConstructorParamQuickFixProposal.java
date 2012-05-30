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
package org.springframework.ide.eclipse.quickfix.proposals;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;


/**
 * Quick fix proposal for removing constructor parameters from a constructor
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class RemoveConstructorParamQuickFixProposal extends BeanAttributeQuickFixProposal {

	private final int numAdditionalParams;

	private final IMethod constructor;

	private final String label;

	private final IJavaProject javaProject;

	public RemoveConstructorParamQuickFixProposal(int offset, int length, boolean missingEndQuote,
			int numAdditionalParams, IMethod constructor, String label, IJavaProject javaProject) {
		super(offset, length, missingEndQuote);
		this.numAdditionalParams = numAdditionalParams;
		this.constructor = constructor;
		this.label = label;
		this.javaProject = javaProject;
	}

	@Override
	public void applyQuickFix(IDocument document) {
		int numParams = constructor.getNumberOfParameters();
		Object[] changeDesc = QuickfixReflectionUtils.createChangeDescriptionArray(numParams);

		for (int i = numParams - 1; i >= numParams - numAdditionalParams; i--) {
			changeDesc[i] = QuickfixReflectionUtils.createRemoveDescription();
		}

		ICompilationUnit targetCU = constructor.getCompilationUnit();

		String[] paramTypes = new String[numParams];
		for (int i = 0; i < numParams; i++) {
			paramTypes[i] = "Object";
		}

		IMethodBinding methodBinding = QuickfixUtils.getMethodBinding(javaProject, constructor);
		if (methodBinding != null) {
			ClassInstanceCreation invocationNode = QuickfixUtils.getMockConstructorInvocation(constructor
					.getDeclaringType().getFullyQualifiedName(), paramTypes);
			Object proposal = QuickfixReflectionUtils.createChangeMethodSignatureProposal(label, targetCU,
					invocationNode, methodBinding, changeDesc, 5, getImage());
			QuickfixReflectionUtils.applyProposal(proposal, document);
		}
	}

	public String getDisplayString() {
		String params = "";
		int numParams = constructor.getNumberOfParameters();

		IMethodBinding methodBinding = QuickfixUtils.getMethodBinding(javaProject, constructor);
		ITypeBinding[] typeParameters = methodBinding.getParameterTypes();

		for (int i = numParams - numAdditionalParams; i < numParams; i++) {
			if (params.length() > 0) {
				params += ", ";
			}
			params += "'" + ASTResolving.getTypeSignature(typeParameters[i]) + "'";
		}

		return label + ": Remove parameter " + params;
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_REMOVE);
	}

}
