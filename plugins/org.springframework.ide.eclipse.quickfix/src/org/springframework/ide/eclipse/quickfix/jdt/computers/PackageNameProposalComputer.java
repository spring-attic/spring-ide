/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.computers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.PackageNameCompletionProposal;

/**
 * 
 * Annotation proposal computer for package name
 * 
 * @author Terry Denney
 * @since 3.3.0
 * 
 */
public class PackageNameProposalComputer extends AnnotationProposalComputer {

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(SourceType type, String value,
			IAnnotation annotation, JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		IPackageFragment[] packages = javaContext.getProject().getPackageFragments();
		Set<String> foundPackages = new HashSet<String>();
		for (IPackageFragment currPackage : packages) {
			String packageName = currPackage.getElementName();
			if (packageName.startsWith(value) && !foundPackages.contains(packageName)) {
				proposals.add(new PackageNameCompletionProposal(packageName, annotation, javaContext));
				foundPackages.add(packageName);
			}
		}

		return proposals;
	}

}
