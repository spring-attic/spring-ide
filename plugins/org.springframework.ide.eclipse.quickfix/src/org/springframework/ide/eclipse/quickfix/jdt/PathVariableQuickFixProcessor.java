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
package org.springframework.ide.eclipse.quickfix.jdt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.springframework.ide.eclipse.quickfix.jdt.processors.PathVariableAnnotationQuickAssistProcessor;


public class PathVariableQuickFixProcessor implements IQuickFixProcessor {

	private static final Collection<IJavaCompletionProposal> EMPTY_RESULT = new ArrayList<IJavaCompletionProposal>(0);

	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		return MissingPathVariableWarning.PROBLEM_ID == problemId;
	}

	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {
		List<IJavaCompletionProposal> collectedCorrections = new ArrayList<IJavaCompletionProposal>();
		if (locations != null && locations.length > 0) {
			for (int i = 0; i < locations.length; i++) {
				collectedCorrections.addAll(getCorrections(context, locations[i]));
			}
		}
		return collectedCorrections.toArray(new IJavaCompletionProposal[collectedCorrections.size()]);
	}

	private Collection<IJavaCompletionProposal> getCorrections(IInvocationContext context, IProblemLocation location) {
		if (MissingPathVariableWarning.MARKER_TYPE.equals(location.getMarkerType())) {
			AssistContext assistContext = new AssistContext(context.getCompilationUnit(), null, location.getOffset(),
					location.getLength());
			ASTNode node = assistContext.getCoveringNode();
			return new PathVariableAnnotationQuickAssistProcessor().getAssists(node, assistContext);
		}
		return EMPTY_RESULT;
	}

}
