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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;
import org.springframework.ide.eclipse.quickfix.proposals.AddStaticToFieldQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.proposals.CreateNewClassQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.proposals.CreateNewFieldQuickFixProposal;


/**
 * @author Terry Denney
 */
public class NameSpaceElementsQuickAssistProcessor extends BeanQuickAssistProcessor {

	private final String problemId;

	private final IJavaProject javaProject;

	private final ValidationProblemAttribute[] problemAttributes;

	private final IDOMNode node;

	private final IFile file;

	public NameSpaceElementsQuickAssistProcessor(String problemId, int offset, int length, String text,
			boolean missingEndQuote, IProject project, IDOMNode node, IFile file,
			ValidationProblemAttribute... problemAttributes) {
		super(offset, length, text, missingEndQuote);
		this.problemId = problemId;
		this.node = node;
		this.file = file;
		this.problemAttributes = problemAttributes;

		this.javaProject = JavaCore.create(project).getJavaProject();

	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		String className = null, fieldName = null, methodName = null, beanName = null;

		for (ValidationProblemAttribute problemAttribute : problemAttributes) {
			if ("CLASS".equals(problemAttribute.getKey())) {
				className = (String) problemAttribute.getValue();
			}
			else if ("FIELD".equals(problemAttribute.getKey())) {
				fieldName = (String) problemAttribute.getValue();
			}
			else if ("METHOD".equals(problemAttribute.getKey())) {
				methodName = (String) problemAttribute.getValue();
			}
			else if ("BEAN".equals(problemAttribute.getKey())) {
				beanName = (String) problemAttribute.getValue();
			}
		}
		if ("CLASS_NOT_FOUND".equals(problemId) && className != null) {
			proposals.add(new CreateNewClassQuickFixProposal(offset, length, className, missingEndQuote, javaProject,
					new HashSet<String>(), 0));
		}
		else if ("FIELD_NOT_FOUND".equals(problemId) && className != null && fieldName != null) {
			IType type = JdtUtils.getJavaType(javaProject.getProject(), className);
			if (!type.isReadOnly()) {
				// rename to similar field names if one exists

				// create new field
				proposals.add(new CreateNewFieldQuickFixProposal(offset, length, text, missingEndQuote, javaProject,
						className, fieldName));
			}
		}
		else if ("FIELD_NOT_STATIC".equals(problemId) && className != null && fieldName != null) {
			IType type = JdtUtils.getJavaType(javaProject.getProject(), className);
			if (!type.isReadOnly()) {
				proposals.add(new AddStaticToFieldQuickFixProposal(offset, length, missingEndQuote, javaProject,
						className, fieldName));
			}
		}
		else if ("METHOD_NOT_FOUND".equals(problemId) && className != null && methodName != null) {
			ICompletionProposal proposal = QuickfixUtils.getNewMethodQuickFixProposal(methodName, "Object",
					new String[0], javaProject, className, offset, length, text, missingEndQuote, false, "method");
			if (proposal != null) {
				proposals.add(proposal);
			}
		}
		else if ("UNDEFINED_REFERENCED_BEAN".equals(problemId) && beanName != null) {
			proposals.addAll(BeanReferenceQuickAssistProcessor.computeBeanReferenceQuickAssistProposals(node,
					BeansSchemaConstants.ATTR_REF, file, text, beanName, offset, length, missingEndQuote));
		}

		// TODO: CLASS_NOT_FOUND
		// TODO: CLASS_IS_NOT_IN_HIERACHY
		// TODO: CLASS_IS_INTERFACE
		// TODO: CLASS_IS_CLASS

		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}
}
