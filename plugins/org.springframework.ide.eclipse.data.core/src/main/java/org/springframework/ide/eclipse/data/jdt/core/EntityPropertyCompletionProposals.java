/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.jdt.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.ui.SharedImages;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Completion proposal computer to calculate Spring Data query keyword proposals and entity property proposals.
 * 
 * @author Oliver Gierke
 */
@SuppressWarnings("restriction")
public class EntityPropertyCompletionProposals extends JavaCompletionProposalComputer {

	private static final ISharedImages IMAGES = new SharedImages();
	private static final Image KEYWORD = IMAGES.getImage(ISharedImages.IMG_OBJS_ANNOTATION);
	private static final Image PRIVATE_FIELD = IMAGES.getImage(ISharedImages.IMG_FIELD_PRIVATE);

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer#computeCompletionProposals(org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {

		if (!(context instanceof JavaContentAssistInvocationContext)) {
			return Collections.emptyList();
		}

		JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

		if (!SpringCoreUtils.isSpringProject(javaContext.getProject().getProject())) {
			return Collections.emptyList();
		}

		ICompilationUnit cu = javaContext.getCompilationUnit();

		try {
			int invocationOffset = context.getInvocationOffset();
			IJavaElement element = cu.getElementAt(invocationOffset);

			if (element instanceof SourceMethod) {
				return computeCompletionProposals((SourceMethod) element, javaContext);
			}

		} catch (JavaModelException e) {

		}

		return Collections.emptyList();
	}

	/**
	 * @param element
	 * @param javaContext
	 * @return
	 * @throws JavaModelException
	 */
	private List<ICompletionProposal> computeCompletionProposals(SourceMethod element,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {

		RepositoryInformation information = RepositoryInformation.create(element);
		if (null == information) {
			return Collections.emptyList();
		}

		IJavaProject project = javaContext.getProject();
		IType domainType = project.findType(information.getManagedDomainClass().getName());
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		int offset = javaContext.getCoreContext().getOffset();
		int positionInMethodName = offset - element.getNameRange().getOffset();

		KeywordProvider keywordProvider = information.getKeywordProvider(project);
		QueryMethodCandidate candidate = new QueryMethodCandidate(element.getElementName(),
				information.getManagedDomainClass());
		QueryMethodPart part = candidate.getPartAtPosition(positionInMethodName);

		if (part == null) {
			return proposals;
		}

		KeywordProposalsProvider keywordProposalsProvider = new KeywordProposalsProvider(keywordProvider);

		IType type = part.isRoot() ? domainType : project.findType(part.getPathLeaf().getType().getName());

		if (type != null) {
			proposals.addAll(getProposalsFor(type, part, offset));
			proposals.addAll(keywordProposalsProvider.getProposalsFor(type, offset, part));
		}

		return proposals;
	}

	private static List<ICompletionProposal> getProposalsFor(IType type, QueryMethodPart part, int offset)
			throws JavaModelException {

		if (isJdkType(type)) {
			return Collections.emptyList();
		}

		List<ICompletionProposal> result = new ArrayList<ICompletionProposal>();

		for (IField field : type.getFields()) {
			if (part.isProposalCandidate(field)) {
				result.add(new EntityFieldNameCompletionProposal(field, offset, part.getSeed()));
			}
		}

		return result;
	}

	/**
	 * Code completion proposal for a query keyword.
	 * 
	 * @author Oliver Gierke
	 */
	private static class KeyWordCompletionProposal extends JavaCompletionProposal {
		public KeyWordCompletionProposal(String keyword, int offset, String seed) {
			super(getReplacement(keyword, seed), offset, offset + keyword.length(), KEYWORD, StringUtils.capitalize(keyword),
					450);
		}
	}

	/**
	 * Provides {@link KeyWordCompletionProposal}s using a given {@link KeywordProvider}.
	 * 
	 * @author Oliver Gierke
	 */
	private static class KeywordProposalsProvider {

		private final KeywordProvider provider;

		/**
		 * Creates a new {@link KeywordProposalsProvider} using the given {@link KeywordProposalsProvider}.
		 * 
		 * @param provider must not be {@literal null}.
		 */
		public KeywordProposalsProvider(KeywordProvider provider) {

			Assert.notNull(provider);
			this.provider = provider;
		}

		public List<ICompletionProposal> getProposalsFor(IType type, int offset, QueryMethodPart part) {

			if (part.isRoot()) {
				return Collections.emptyList();
			}

			String seed = StringUtils.capitalize(part.isKeywordComplete() ? part.getKeyword() : part.getSeed());
			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

			for (String keyword : provider.getKeywordsForPropertyOf(type, seed)) {
				if (!part.isKeywordComplete()) {
					proposals.add(new KeyWordCompletionProposal(keyword, offset, seed));
				}
			}

			seed = part.getSeed();

			for (String concatenator : Arrays.asList("And", "Or")) {
				if (part.isKeywordComplete() || !StringUtils.hasText(seed) || concatenator.startsWith(seed)) {
					proposals.add(new KeyWordCompletionProposal(concatenator, offset, seed));
				}
			}

			return proposals;
		}
	}

	/**
	 * Returns whether the given {@link IType} is a JDK type.
	 * 
	 * @param type must not be {@literal null}.
	 * @return
	 */
	private static boolean isJdkType(IType type) {
		return type.getFullyQualifiedName().startsWith("java");
	}

	/**
	 * Code completion proposal for an entity field.
	 * 
	 * @author Oliver Gierke
	 */
	private static class EntityFieldNameCompletionProposal extends JavaCompletionProposal {

		public EntityFieldNameCompletionProposal(IField field, int offset, String seed) {
			super(getReplacement(field.getElementName(), seed), offset, offset + (seed == null ? 0 : seed.length()),
					getFieldImage(field), field.getElementName(), 500);
		}

		private static Image getFieldImage(IField field) {
			// TODO : alter field image based on field modifier
			return PRIVATE_FIELD;
		}
	}

	/**
	 * Returns either the source capitalized if no seed is given or the part of the source starting at the length index of
	 * the seed.
	 * 
	 * @param source must not be {@literal null} or shorter than the seed.
	 * @param seed can be {@literal null} or empty.
	 * @return
	 */
	private static String getReplacement(String source, String seed) {
		return StringUtils.hasText(seed) ? source.substring(seed.length()) : StringUtils.capitalize(source);
	}
}
