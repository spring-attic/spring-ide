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
package org.springframework.ide.eclipse.config.core.contentassist;

import java.util.ArrayList;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.DefaultContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.DefaultContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.w3c.dom.Attr;


/**
 * {@link XmlBackedContentProposalProvider} provides content proposals for use
 * in text fields, given an XML-based input. This is done by invoking a
 * {@link ContentAssistRequest} on the backing XML and then converting the
 * resulting {@link ICompletionProposal} objects into {@link IContentProposal}objects. Clients must provide their own {@link IContentAssistCalculator} to
 * compute the completion proposals.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Terry Denney
 * @see IContentProposalProvider
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public abstract class XmlBackedContentProposalProvider implements IContentProposalProvider {

	private IDOMElement input;

	private final String name;

	private ContentAssistRequest request;

	/**
	 * Constructs a content proposal provider for an XML attribute.
	 * 
	 * @param input the XML element to serve as the model for this proposal
	 * provider
	 * @param attrName the name of the attribute to compute proposals for
	 */
	public XmlBackedContentProposalProvider(IDOMElement input, String attrName) {
		super();
		this.input = input;
		this.name = attrName;
	}

	/**
	 * This method is called when a content proposal request is invoked. Clients
	 * must provide an appropriate {@link IContentAssistCalculator} in this
	 * method.
	 * 
	 * @return content assist calculator for this proposal provider
	 */
	protected abstract IContentAssistCalculator createContentAssistCalculator();

	/**
	 * This method is called when a content proposal request is invoked. Clients
	 * must provider an appropriate {@link IContentAssistContext} in this
	 * method.
	 * 
	 * @param contents the current contents of the text field
	 * @return content assist context for this proposal provider
	 */
	protected IContentAssistContext createContentAssistContext(String contents) {
		return new DefaultContentAssistContext(request, name, contents);
	}

	/**
	 * This method is called when a content proposal request is invoked. Clients
	 * must provider an appropriate {@link IContentAssistProposalRecorder} in
	 * this method.
	 * 
	 * @return content assist proposal recorder for this proposal provider
	 */
	protected IContentAssistProposalRecorder createContentAssistRecorder() {
		return new DefaultContentAssistProposalRecorder(request);
	}

	/**
	 * Returns the attribute to compute proposal for, or null if the attribute
	 * does not exist.
	 * 
	 * @return the attribute to compute proposals for
	 */
	protected IDOMAttr getAttribute() {
		Attr attrNode = input.getAttributeNode(name);
		if (attrNode != null) {
			return (IDOMAttr) attrNode;
		}
		return null;
	}

	/**
	 * Returns the attribute name to compute proposal for
	 * 
	 * @return the attribute name to compute proposals for
	 */
	public String getAttributeName() {
		return name;
	}

	private ITextRegion getCompletionRegion() {
		IDOMAttr attr = getAttribute();
		if (attr != null) {
			return attr.getValueRegion();
		}
		return null;
	}

	/**
	 * Returns the XML element that serves as the model for the proposal
	 * provider.
	 * 
	 * @return element to invoke proposals on
	 */
	protected IDOMElement getInput() {
		return input;
	}

	public IContentProposal[] getProposals(String contents, int position) {
		request = null;
		if (getAttribute() != null && getCompletionRegion() != null) {
			int start = input.getStartStructuredDocumentRegion().getStart() + getCompletionRegion().getStart();
			request = new ContentAssistRequest(input, input, input.getStartStructuredDocumentRegion(),
					getCompletionRegion(), start, getCompletionRegion().getLength(), contents);
		}
		else {
			request = new ContentAssistRequest(input, input, input.getStartStructuredDocumentRegion(),
					getCompletionRegion(), position, contents.length(), contents);
		}

		// TODO: define a context and recorder that don't require a request.
		IContentAssistContext context = createContentAssistContext(contents);
		IContentAssistProposalRecorder recorder = createContentAssistRecorder();
		IContentAssistCalculator calc = createContentAssistCalculator();
		calc.computeProposals(context, recorder);

		ArrayList<IContentProposal> proposals = new ArrayList<IContentProposal>();
		ICompletionProposal[] results = request.getCompletionProposals();

		if (results != null && results.length > 0) {
			for (ICompletionProposal completionProp : request.getCompletionProposals()) {
				proposals.add(makeContentProposal(completionProp));
			}
		}
		return proposals.toArray(new IContentProposal[proposals.size()]);
	}

	// May need to abstract this method in the future
	private IContentProposal makeContentProposal(final ICompletionProposal proposal) {
		return new XmlBackedContentProposal() {

			public String getContent() {
				if (proposal instanceof BeansJavaCompletionProposal) {
					BeansJavaCompletionProposal beanProp = (BeansJavaCompletionProposal) proposal;
					String str = beanProp.getReplacementString();
					if (str.startsWith("\"")) { //$NON-NLS-1$
						str = str.substring(1);
					}
					if (str.endsWith("\"")) { //$NON-NLS-1$
						str = str.substring(0, str.length() - 1);
					}
					return str;
				}
				else {
					return proposal.getDisplayString();
				}
			}

			public int getCursorPosition() {
				return getContent().length();
			}

			public String getDescription() {
				return ConfigCoreUtils.stripTags(proposal.getAdditionalProposalInfo());
			}

			public Image getImage() {
				return proposal.getImage();
			}

			public String getLabel() {
				return proposal.getDisplayString();
			}

		};
	}

	void setInput(IDOMElement input) {
		this.input = input;
	}

}
