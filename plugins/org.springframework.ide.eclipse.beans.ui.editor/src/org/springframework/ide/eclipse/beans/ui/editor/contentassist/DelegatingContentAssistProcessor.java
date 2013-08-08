/*******************************************************************************
 * Copyright (c) 2006 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Node;

/**
 * {@link IContentAssistProcessor} that delegates to {@link INamespaceContentAssistProcessor}s contribute via the
 * <code>org.springframework.ide.eclipse.beans.ui.editor</code> extension point.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Leo Dos Santos
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class DelegatingContentAssistProcessor extends XMLContentAssistProcessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addAttributeValueProposals(ContentAssistRequest contentAssistRequest) {

		int proposalCount = 0;
		if (contentAssistRequest.getCompletionProposals() != null) {
			proposalCount = contentAssistRequest.getCompletionProposals().length;
		}

		IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addAttributeValueProposals(this, contentAssistRequest);
		}

		// only calculate content assists based on annotations if no other processor kicked in already.
		if (contentAssistRequest.getCompletionProposals() == null
				|| contentAssistRequest.getCompletionProposals().length == proposalCount) {
			new ToolAnnotationContentAssistProcessor().addAttributeValueProposals(this, contentAssistRequest);
		}

		super.addAttributeValueProposals(contentAssistRequest);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addAttributeNameProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addAttributeNameProposals(this, request);
		}

		// Wrap the original request in order to re-sort the proposals
		super.addAttributeNameProposals(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addTagCloseProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addTagCloseProposals(this, request);
		}
		super.addTagCloseProposals(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addTagInsertionProposals(ContentAssistRequest request, int childPosition) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor[] processors = NamespaceUtils.getContentAssistProcessor(namespace);
		for (INamespaceContentAssistProcessor processor : processors) {
			processor.addTagInsertionProposals(this, request, childPosition);
		}
		super.addTagInsertionProposals(request, childPosition);
		filterTagInsertionProposals(request);
	}
	
	private void filterTagInsertionProposals(ContentAssistRequest request) {
		List proposals = request.getProposals();
		Iterator iter = proposals.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof TemplateProposal) {
				TemplateProposal proposal = (TemplateProposal) obj;
				String display = proposal.getDisplayString();
				if (display.startsWith("dispatcherservlet")
						|| display.startsWith("contextloaderlistener")) {
					iter.remove();
				}
			}
		}
	}

	public ITextViewer getTextViewer() {
		return fTextViewer;
	}

	/**
	 * {@link INamespaceContentAssistProcessor} implementation that wraps a {@link ToolAnnotationContentAssistCalulator}
	 * to create content assist proposals basd on tool annotations.
	 * @since 2.3.0
	 */
	private static class ToolAnnotationContentAssistProcessor extends AbstractContentAssistProcessor {

		private final ToolAnnotationContentAssistCalulator calculator = new ToolAnnotationContentAssistCalulator();

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void computeAttributeNameProposals(ContentAssistRequest request, String prefix, String namespace,
				String namespacePrefix, Node attributeNode) {
			// nothing do on annotations
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void computeAttributeValueProposals(ContentAssistRequest request, IDOMNode node, String matchString,
				String attributeName, String namespace, String prefix) {
			IContentAssistContext context = new DefaultContentAssistContext(request, attributeName, BeansEditorUtils
					.prepareMatchString(matchString));
			IContentAssistProposalRecorder recorder = new DefaultContentAssistProposalRecorder(request);

			calculator.computeProposals(context, recorder);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void computeTagInsertionProposals(ContentAssistRequest request, IDOMNode node) {
			// nothing do on annotations
		}

	}

	/*private static class RelevanceApplyingContentAssistRequest extends ContentAssistRequest {

		private final ContentAssistRequest delegate;

		public RelevanceApplyingContentAssistRequest(ContentAssistRequest delegate) {
			super(delegate.getNode(), delegate.getParent(), delegate.getDocumentRegion(), delegate.getRegion(),
					delegate.getReplacementBeginPosition(), delegate.getReplacementLength(), delegate.getMatchString());
			this.delegate = delegate;
		}

		public void addMacro(ICompletionProposal newProposal) {
			delegate.addMacro(newProposal);
		}

		public void addProposal(ICompletionProposal newProposal) {
			if (newProposal instanceof CustomCompletionProposal) {
				CustomCompletionProposal proposal = (CustomCompletionProposal) newProposal;
				if (proposal.getDisplayString().equals("id")) {
					Field field = ReflectionUtils.findField(newProposal.getClass(), "fRelevance");
					field.setAccessible(true);
					ReflectionUtils.setField(field, newProposal, Integer.valueOf(10000));
				}
			}
			delegate.addProposal(newProposal);
		}

		public ICompletionProposal[] getCompletionProposals() {
			return delegate.getCompletionProposals();
		}

		public IStructuredDocumentRegion getDocumentRegion() {
			return delegate.getDocumentRegion();
		}

		public List getMacros() {
			return delegate.getMacros();
		}

		public String getMatchString() {
			return delegate.getMatchString();
		}

		public Node getNode() {
			return delegate.getNode();
		}

		public Node getParent() {
			return delegate.getParent();
		}

		public List getProposals() {
			return delegate.getProposals();
		}

		public ITextRegion getRegion() {
			return delegate.getRegion();
		}

		public int getReplacementBeginPosition() {
			return delegate.getReplacementBeginPosition();
		}

		public int getReplacementLength() {
			return delegate.getReplacementLength();
		}

		public int getStartOffset() {
			return delegate.getStartOffset();
		}

		public String getText() {
			return delegate.getText();
		}

		public int getTextEndOffset() {
			return delegate.getTextEndOffset();
		}

		public void setDocumentRegion(IStructuredDocumentRegion region) {
			if (delegate != null) {
				delegate.setDocumentRegion(region);
			}
		}

		public void setMatchString(String newMatchString) {
			if (delegate != null) {
				delegate.setMatchString(matchString);
			}
		}

		public void setNode(Node newNode) {
			if (delegate != null) {
				delegate.setNode(newNode);
			}
		}

		public void setParent(Node newParent) {
			if (delegate != null) {
				delegate.setParent(newParent);
			}
		}

		public void setRegion(ITextRegion newRegion) {
			if (delegate != null) {
				delegate.setRegion(newRegion);
			}
		}

		public void setReplacementBeginPosition(int newReplacementBeginPosition) {
			if (delegate != null) {
				delegate.setReplacementBeginPosition(newReplacementBeginPosition);
			}
		}

		public void setReplacementLength(int newReplacementLength) {
			if (delegate != null) {
				delegate.setReplacementLength(newReplacementLength);
			}
		}

		public boolean shouldSeparate() {
			return delegate.shouldSeparate();
		}

		@SuppressWarnings("unchecked")
		protected List sortProposals(List proposalsIn) {
			Collections.sort(proposalsIn, new ProposalComparator());
			return proposalsIn;
		}
	} */

}
