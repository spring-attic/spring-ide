/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.processors.imports;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor;
import org.eclipse.jdt.internal.ui.text.correction.QuickFixProcessor;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.springframework.ide.eclipse.quickfix.Activator;

/**
 * Specialised QuickFixProcessor that replaces the JDT QuickFixProcessor
 * specifically to recompute relevance of "add import" proposals. The reason
 * this needs to REPLACE the full complete JDT QuickFixProcessor is that the JDT
 * {@link QuickFixProcessor} does not have well defined hooks or extension
 * points to reorder proposals (note that extension point <br/>
 * <br/>
 * "org.eclipse.jdt.ui.javaCompletionProposalSorters" <br/>
 * <br/>
 * allows for sorters to be contributed but <br/>
 * 1. they don't seem to be invoked on quickfix proposals <br/>
 * 2. the API used by this extension point does not seem have enough information
 * to recompute relevance of proposals. In particular, the selected AST Node in
 * the context is not present in the API, although it's possible it is available
 * in the proposals themselves. <br/>
 *
 * <p/>
 * This processor takes over the JDT QuickFixProcessor, computes ALL the
 * proposals that the JDT processor provides, but it ALSO contains additional
 * support to recompute relevance of proposals based on additional criteria
 * defined in {@link AddImportRelevanceResolverFactory}
 * <p/>
 * Note that since this STS quickfix processor REPLACES the JDT
 * QuickFixProcessor, both cannot coexist in the {@link JavaCorrectionProcessor}
 * (the JDT "registry" for quickfix processors) , as it will result in duplicate
 * proposals. Therefore the STS version has support to disable the default JDT
 * QuickFixProcessor from the registry.
 *
 */
public class AddImportsQuickFixProcessor extends QuickFixProcessor {

	private AddImportRelevanceResolverFactory factory;

	public void setProposalRelevanceFactory(AddImportRelevanceResolverFactory factory) {
		if (factory != null) {
			this.factory = factory;
		}
	}

	protected AddImportRelevanceResolverFactory getFactory() {
		if (this.factory == null) {
			setProposalRelevanceFactory(new AddImportRelevanceResolverFactory());
		}
		return this.factory;
	}

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {

		ICompilationUnit cu = context != null ? context.getCompilationUnit() : null;


		try {
			// BUG: when first invoking quickfix, JDT and STS quickfix processor are
			// already loaded since the quickfix processors are loaded by the JDT
			// registry (JavaCorrectionsProcessor)
			// lazily. This will result in duplicate proposals. To avoid this, so
			// far
			// the only solution is to only allow one or the other to contribute
			// proposals, BUT NOT BOTH.
			// Therefore if the JDT processor is being removed below, it means it
			// has already been loaded, so don't return any
			// proposals. ONLY return
			// proposals if the JDT process was removed in a PREVIOUS run
			if (JDTQuickFixProcessorHelper.getInstance().removeJDTQuickFixProcessor(cu)) {
				return null;
			}
		}
		catch (Throwable e) {
			// Any errors while handling the default JDT processor should not
			// propagate to avoid interfering with quickfix operation.
			Activator.log(e);
		}

		// Get the proposals only if the default JDT processor has been removed
		// to
		// avoid duplicate entries
		if (JDTQuickFixProcessorHelper.getInstance().isJDTProcessorRemoved()) {
			IJavaCompletionProposal[] proposals = super.getCorrections(context, locations);

			if (proposals != null && proposals.length > 0) {
				List<IJavaCompletionProposal> proposalsFromJDT = Arrays.asList(proposals);

				try {
					recomputeProposalRelevance(context, locations, proposalsFromJDT);
				}
				catch (Throwable e) {
					Activator.log(e);
				}
			}

			return proposals;
		}
		return null;

	}

	protected void recomputeProposalRelevance(IInvocationContext context, IProblemLocation[] locations,
			List<IJavaCompletionProposal> proposalsFromJDT) throws Exception {
		if (context == null || locations == null || proposalsFromJDT == null) {
			return;
		}
		Set<Integer> handledProblems = new HashSet<Integer>();
		for (int i = 0; i < locations.length; i++) {
			IProblemLocation problem = locations[i];
			Integer id = new Integer(problem.getProblemId());
			if (id != null && handledProblems.add(id)) {

				switch (id) {
				case IProblem.UndefinedType:
				case IProblem.JavadocUndefinedType:
					ICompilationUnit cu = context.getCompilationUnit();

					ASTNode selectedNode = problem.getCoveringNode(context.getASTRoot());
					if (selectedNode != null && cu != null) {
						int kind = evaluateTypeKind(selectedNode, cu.getJavaProject());

						List<AddImportRelevanceResolver> relevanceResolvers = getFactory().getResolvers(cu,
								proposalsFromJDT);

						// only use the first resolver that successfully
						// recomputes the relevance
						if (relevanceResolvers != null) {
							for (AddImportRelevanceResolver resolver : relevanceResolvers) {
								if (resolver.recomputeRelevance(kind, selectedNode)) {
									break;
								}
							}
						}
					}
					break;
				}
			}
		}
	}

	private int evaluateTypeKind(ASTNode node, IJavaProject project) {
		int kind = ASTResolving.getPossibleTypeKinds(node, JavaModelUtil.is50OrHigher(project));
		return kind;
	}

}
