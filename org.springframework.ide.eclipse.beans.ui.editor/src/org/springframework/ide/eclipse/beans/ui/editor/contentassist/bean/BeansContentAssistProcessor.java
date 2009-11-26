/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.DefaultContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.DefaultContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.PropertyBeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateContextTypeIds;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansCompletionUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * {@link INamespaceContentAssistProcessor} implementation responsible for the standard <code>bean:*</code> namespace.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
@SuppressWarnings("restriction")
public class BeansContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	private void addPropertyNameAttributeNameProposals(ContentAssistRequest request, String prefix, String oldPrefix,
			Node node, List classNames, boolean attrAtLocationHasValue, String nameSpacePrefix) {

		PropertyNameSearchRequestor requestor = new PropertyNameSearchRequestor(request, oldPrefix,
				attrAtLocationHasValue, nameSpacePrefix);
		if (prefix.lastIndexOf(".") >= 0) {
			int firstIndex = prefix.indexOf(".");
			String firstPrefix = prefix.substring(0, firstIndex);
			String lastPrefix = prefix.substring(firstIndex);
			if (".".equals(lastPrefix)) {
				lastPrefix = "";
			}
			else if (lastPrefix.startsWith(".")) {
				lastPrefix = lastPrefix.substring(1);
			}
			for (int i = 0; i < classNames.size(); i++) {
				IType type = (IType) classNames.get(i);
				try {
					Collection methods = Introspector.findReadableProperties(type, firstPrefix);
					if (methods != null && methods.size() == 1) {

						Iterator iterator = methods.iterator();
						while (iterator.hasNext()) {
							IMethod method = (IMethod) iterator.next();
							IType returnType = JdtUtils.getJavaTypeForMethodReturnType(method, type);

							if (returnType != null) {
								List<IType> typesTemp = new ArrayList<IType>();
								typesTemp.add(returnType);

								String newPrefix = oldPrefix + firstPrefix + ".";

								addPropertyNameAttributeNameProposals(request, lastPrefix, newPrefix, node, typesTemp,
										attrAtLocationHasValue, nameSpacePrefix);
							}
							return;
						}
					}
				}
				catch (JavaModelException e1) {
					// do nothing
				}
			}
		}
		else {
			for (int i = 0; i < classNames.size(); i++) {
				IType type = (IType) classNames.get(i);
				try {
					Collection methods = Introspector.findWritableProperties(type, prefix);
					if (methods != null && methods.size() > 0) {

						Iterator iterator = methods.iterator();
						while (iterator.hasNext()) {
							requestor.acceptSearchMatch((IMethod) iterator.next(), false);
						}
					}
				}
				catch (JavaModelException e1) {
					// do nothing
				}
				catch (CoreException e) {
					// // do nothing
				}
			}
		}
	}

	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request, String prefix, String namespace,
			String namespacePrefix, Node attributeNode) {
		if ("http://www.springframework.org/schema/p".equals(namespace)) {
			// check whether an attribute really exists for the replacement
			// offsets AND if it possesses a value
			IStructuredDocumentRegion sdRegion = request.getDocumentRegion();
			boolean attrAtLocationHasValue = false;
			NamedNodeMap attrs = attributeNode.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				AttrImpl existingAttr = (AttrImpl) attrs.item(i);
				ITextRegion name = existingAttr.getNameRegion();
				if (sdRegion.getStartOffset(name) <= request.getReplacementBeginPosition()
						&& sdRegion.getStartOffset(name) + name.getLength() >= request.getReplacementBeginPosition()
								+ request.getReplacementLength() && existingAttr.getValueRegion() != null) {
					attrAtLocationHasValue = true;
					break;
				}
			}

			if (prefix != null) {
				prefix = BeansEditorUtils.attributeNameToPropertyName(prefix);
			}

			List classNames = BeansEditorUtils.getClassNamesOfBean(BeansEditorUtils.getFile(request), attributeNode);
			addPropertyNameAttributeNameProposals(request, prefix, "", attributeNode, classNames,
					attrAtLocationHasValue, namespacePrefix);
		}
	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request, IDOMNode node) {
		if (node != null && node.getParentNode() != null) {
			Node parentNode = node.getParentNode();
			if ("bean".equals(parentNode.getNodeName())) {
				addTemplates(request, BeansTemplateContextTypeIds.BEAN);
			}
			else if ("beans".equals(parentNode.getNodeName())) {
				addTemplates(request, BeansTemplateContextTypeIds.ALL);
			}
			else if ("property".equals(parentNode.getNodeName())) {
				addTemplates(request, BeansTemplateContextTypeIds.PROPERTY);
				addTemplates(request, BeansTemplateContextTypeIds.ALL);
			}
		}
	}

	@Override
	public void init() {
		ClassContentAssistCalculator clazz = new ClassContentAssistCalculator(false);
		registerContentAssistCalculator("bean", "class", clazz);
		registerContentAssistCalculator("constructor-arg", "type", clazz);
		registerContentAssistCalculator("arg-type", "match", clazz);
		registerContentAssistCalculator("value", "type", clazz);
		registerContentAssistCalculator("value-type", clazz);
		registerContentAssistCalculator("key-type", clazz);

		PropertyBeanReferenceContentAssistCalculator propertyBean = new PropertyBeanReferenceContentAssistCalculator();
		registerContentAssistCalculator(NamespaceUtils.DEFAULT_NAMESPACE_URI, "bean", "property", "ref", propertyBean);

		BeanReferenceContentAssistCalculator globalBean = new BeanReferenceContentAssistCalculator();
		registerContentAssistCalculator("bean", "parent", globalBean);
		registerContentAssistCalculator("bean", "depends-on", globalBean);
		registerContentAssistCalculator("bean", "factory-bean", globalBean);
		registerContentAssistCalculator("ref", "bean", globalBean);
		registerContentAssistCalculator("idref", "bean", globalBean);
		registerContentAssistCalculator("constructor-arg", "ref", globalBean);
		registerContentAssistCalculator("alias", "name", globalBean);
		registerContentAssistCalculator("replaced-method", "replacer", globalBean);
		registerContentAssistCalculator("entry", "value-ref", globalBean);
		registerContentAssistCalculator("entry", "key-ref", globalBean);
		registerContentAssistCalculator("lookup-method", "bean", globalBean);

		BeanReferenceContentAssistCalculator localBean = new BeanReferenceContentAssistCalculator(false);
		registerContentAssistCalculator("ref", "local", localBean);
		registerContentAssistCalculator("idref", "local", localBean);

		InitDestroyMethodContentAssistCalculator initDestroy = new InitDestroyMethodContentAssistCalculator();
		registerContentAssistCalculator("bean", "init-method", initDestroy);
		registerContentAssistCalculator("bean", "destroy-method", initDestroy);

		registerContentAssistCalculator("bean", "id", new BeanIdContentAssistCalculator());
		registerContentAssistCalculator("replaced-method", "name", new ReplaceMethodContentAssistCalculator());
		registerContentAssistCalculator("lookup-method", "name", new LookupMethodContentAssistCalculator());

		registerContentAssistCalculator("http://www.springframework.org/schema/beans", "bean", "property", "name",
				new PropertyNameContentAssistCalculator());
		registerContentAssistCalculator("http://www.springframework.org/schema/beans", "bean", "property", "value",
				new PropertyValueContentAssistCalculator());

		registerContentAssistCalculator("bean", "factory-method", new FactoryMethodContentAssistCalculator());

		registerContentAssistCalculator("bean", "scope", new ScopeContentAssistCalculator());

	}

	@Override
	protected void postComputeAttributeValueProposals(ContentAssistRequest request, IDOMNode node, String matchString,
			String attributeName, String namespace, String prefix) {

		if ("bean".equals(node.getNodeName())) {
			if ("http://www.springframework.org/schema/p".equals(namespace) && attributeName.endsWith("-ref")) {

				IContentAssistContext context = new DefaultContentAssistContext(request, attributeName, matchString);
				IContentAssistProposalRecorder recorder = new DefaultContentAssistProposalRecorder(request);
				addBeanReferenceProposals(context, recorder);
			}
		}
	}

	private void addBeanReferenceProposals(IContentAssistContext context, IContentAssistProposalRecorder recorder) {
		BeansCompletionUtils.addBeanReferenceProposals(context, recorder, true);

	}

}
