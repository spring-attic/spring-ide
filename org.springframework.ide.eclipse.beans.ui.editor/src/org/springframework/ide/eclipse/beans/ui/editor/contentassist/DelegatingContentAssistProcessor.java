/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.springframework.el.EvaluationException;
import org.springframework.el.ParseException;
import org.springframework.el.ParserFactory;
import org.springframework.el.SpelExpression;
import org.springframework.el.SpelParser;
import org.springframework.el.ast.PossibleCompletion;
import org.springframework.el.context.AbstractEvaluationContext;
import org.springframework.el.context.EvaluationContext;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.IAnnotationBasedContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.ToolAnnotationUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IContentAssistProcessor} that delegates to
 * {@link INamespaceContentAssistProcessor}s contribute via the
 * <code>org.springframework.ide.eclipse.beans.ui.editor</code> extension
 * point.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class DelegatingContentAssistProcessor extends XMLContentAssistProcessor {

	@Override
	protected void addAttributeValueProposals(
			ContentAssistRequest contentAssistRequest) {
		// ////////////////////////////////////////////////////////////
		try {
			SpringExpressionLanguageContentAssistProcessor elProcessor = new SpringExpressionLanguageContentAssistProcessor();
			elProcessor.addAttributeValueProposals(this, contentAssistRequest);
		}
		catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		// ////////////////////////////////////////////////////////////

		int proposalCount = 0;
		if (contentAssistRequest.getCompletionProposals() != null) {
			proposalCount = contentAssistRequest.getCompletionProposals().length;

		}

		IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils
				.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addAttributeValueProposals(this, contentAssistRequest);
		}

		// only calculate content assists based on annotations if no other
		// processor
		// kicked in already.
		if (contentAssistRequest.getCompletionProposals() == null
				|| contentAssistRequest.getCompletionProposals().length == proposalCount) {
			addAnnotationBasedAttributeValueProposals(contentAssistRequest,
					node);
		}

		super.addAttributeValueProposals(contentAssistRequest);
	}

	private void addAnnotationBasedAttributeValueProposals(
			ContentAssistRequest contentAssistRequest, IDOMNode node) {

		IStructuredDocumentRegion open = node
				.getFirstStructuredDocumentRegion();
		ITextRegionList openRegions = open.getRegions();
		int i = openRegions.indexOf(contentAssistRequest.getRegion());
		if (i < 0) {
			return;
		}
		ITextRegion nameRegion = null;
		while (i >= 0) {
			nameRegion = openRegions.get(i--);
			if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				break;
			}
		}

		// the name region is REQUIRED to do anything useful
		if (nameRegion != null) {
			String attributeName = open.getText(nameRegion);
			List<Element> appInfo = ToolAnnotationUtils
					.getApplicationInformationElements(node, attributeName);
			for (Element elem : appInfo) {
				NodeList children = elem.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node child = children.item(j);
					if (child.getNodeType() == Node.ELEMENT_NODE) {
						invokeAnnotationBasedContentAssistProcessor(
								contentAssistRequest, child);
					}
				}
			}
		}
	}

	private void invokeAnnotationBasedContentAssistProcessor(
			ContentAssistRequest contentAssistRequest, Node child) {

		IAnnotationBasedContentAssistProcessor annotationProcessor = NamespaceUtils
				.getAnnotationBasedContentAssistProcessor(child
						.getNamespaceURI());
		if (annotationProcessor != null) {
			annotationProcessor.addAttributeValueProposals(this,
					contentAssistRequest, child);
		}
	}

	@Override
	protected void addAttributeNameProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils
				.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addAttributeNameProposals(this, request);
		}
		super.addAttributeNameProposals(request);
	}

	@Override
	protected void addTagCloseProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils
				.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addTagCloseProposals(this, request);
		}
		super.addTagCloseProposals(request);
	}

	@Override
	protected void addTagInsertionProposals(ContentAssistRequest request,
			int childPosition) {
		IDOMNode node = (IDOMNode) request.getNode();
		String namespace = node.getNamespaceURI();
		INamespaceContentAssistProcessor processor = NamespaceUtils
				.getContentAssistProcessor(namespace);
		if (processor != null) {
			processor.addTagInsertionProposals(this, request, childPosition);
		}
		super.addTagInsertionProposals(request, childPosition);
	}

	public ITextViewer getTextViewer() {
		return fTextViewer;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.', '=', '\"', '<' };
	}

	static class SpringExpressionLanguageContentAssistProcessor extends
			AbstractContentAssistProcessor {

		/** Default placeholder prefix: "${" */
		public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

		@Override
		protected void computeAttributeNameProposals(
				ContentAssistRequest request, String prefix, String namespace,
				String namespacePrefix, Node attributeNode) {

		}

		@Override
		protected void computeAttributeValueProposals(
				ContentAssistRequest request, IDOMNode node,
				String matchString, String attributeName, String namespace,
				String prefix) {

			String test = node.getAttributes().getNamedItem(attributeName)
					.getNodeValue();

			if (matchString.startsWith(DEFAULT_PLACEHOLDER_PREFIX)) {
				matchString = matchString.substring(DEFAULT_PLACEHOLDER_PREFIX
						.length());
				SpelParser parser = ParserFactory.getExpressionParser();
				SpelExpression ex;
				try {
					EvaluationContext ec = new BeanEvaluationContext(
							BeansCorePlugin.getModel().getConfig(
									BeansEditorUtils.getFile(request)));
					ec.getCompletionProcessor().clear();
					ex = parser.parse(matchString);
					Object obj = ex.getValue(ec);
					if (obj == null || !(obj instanceof String)) {
						List<PossibleCompletion> p = ec
								.getCompletionProcessor()
								.getPossibleCompletions();

						for (PossibleCompletion possibleCompletion : p) {
							possibleCompletion.prettyPrint(matchString,
									System.out);
							String replacementString = "${"
									+ matchString.substring(0,
											possibleCompletion.start)
									+ possibleCompletion.completion.toString()
									+ test
											.substring(possibleCompletion.end + 2);
							if (!replacementString.equals(test)) {
								request
										.addProposal(new BeansJavaCompletionProposal(
												replacementString,
												request
														.getReplacementBeginPosition(),
												request.getReplacementLength(),
												replacementString.length(),
												BeansUIImages
														.getImage(BeansUIImages.IMG_OBJS_SPRING),
												possibleCompletion.completion
														.toString()
														+ " - "
														+ replacementString,
												null, 10, null));
							}

						}
					}
				}
				catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (EvaluationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		@Override
		protected void computeTagInsertionProposals(
				ContentAssistRequest request, IDOMNode node) {
		}

	}

	static class BeanEvaluationContext extends AbstractEvaluationContext {
		private final IBeansConfig beansConfig;

		BeanEvaluationContext(IBeansConfig beanFactory) {
			super();
			this.beansConfig = beanFactory;
		}

		@Override
		public Object lookupReference(Object contextName, Object objectName)
				throws EvaluationException {
			if (objectName == null) {
				Map<String, IBean> beans = new HashMap<String, IBean>();
				for (IBean bean : beansConfig.getBeans()) {
					beans.put(bean.getElementName(), bean);
				}
				return beans;
			}
 
			try {
				Object bean = beansConfig.getBean((String) objectName);
				if (bean != null) {
					return bean;
				}
			}
			catch (RuntimeException e) {

			}

			return null;
		}

		@Override
		public List<Object> lookupReferenceProposals(Object contextName,
				Object objectName) {
			List<Object> completions = new ArrayList<Object>();
			// add the fields from the object
			Set<IBean> beans = beansConfig.getBeans();
			for (IBean bean : beans) {
				if (objectName == null
						|| bean.getElementName()
								.startsWith((String) objectName)) {
					completions.add(bean.getElementName());
				}
			}

			return completions;
		}

		@Override
		public Object lookupPropertyOrField(String name)
				throws EvaluationException {
			// Expected to be looked up using the current object context,
			// available through:
			// getActiveContextObject();

			// Field f = null;
			// if (getActiveContextObject() != null)
			// f = findField(name, getActiveContextObject().getClass());
			// if (f != null) {
			// try {
			// f.setAccessible(true); // TODO should expressions respect
			// // java visibility rules?
			// return f.get(getActiveContextObject());
			// }
			// catch (IllegalArgumentException e) {
			// throw new EvaluationException("Unable to access field: "
			// + e.getMessage());
			// }
			// catch (IllegalAccessException e) {
			// throw new EvaluationException("Unable to access field: "
			// + e.getMessage());
			// }
			// // } else {
			// // List<PossibleCompletion> completions = new
			// // ArrayList<PossibleCompletion>();
			// // // add the fields from the object
			// // completions.add(new PossibleCompletion(0,0,"name"));
			// //
			// getCompletionProcessor().recordPossibleCompletions(completions);
			// // return null;
			// }
			return null;
		}

		@Override
		public List<Object> lookupPropertyOrFieldOrMethodProposals(String name) {
			List<Object> objects = new ArrayList<Object>();
			Object obj = getActiveContextObject();
			if (obj instanceof IBean) {
				String className = ((IBean) obj).getClassName();
				IType type = JdtUtils.getJavaType(((IBean) obj)
						.getElementResource().getProject(), className);
				try {
					for (IMethod method : org.springframework.ide.eclipse.core.java.Introspector
							.getAllMethods(type)) {
						if (name == null
								|| method.getElementName().startsWith(name)) {
							objects.add(method.getElementName() + "()");
						}
					}
				}
				catch (JavaModelException e) {
				}
			}
			return objects;
		}

	}

}
