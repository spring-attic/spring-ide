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
package org.springframework.ide.eclipse.quickfix.jdt.processors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

/**
 * @author Terry Denney
 */
public class JavaEditorQuickAssistProcessor implements IQuickAssistProcessor {

	private IJavaCompletionProposal[] getAssists(IInvocationContext context) {
		ASTNode coveringNode = context.getCoveringNode();
		List<IJavaCompletionProposal> result = new ArrayList<IJavaCompletionProposal>();

		for (AbstractAnnotationQuickAssistProcessor processor : AnnotationQuickAssistProcessorRegistry.processors) {
			result.addAll(processor.getAssists(coveringNode, context));
		}

		return result.toArray(new IJavaCompletionProposal[result.size()]);
	}

	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {
		return getAssists(context);
	}

	public boolean hasAssists(IInvocationContext context) throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();
		if (coveringNode != null) {
			return getAssists(context).length > 0;
		}
		return false;
	}

}
