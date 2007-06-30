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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.FactoryMethodSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PropertyNameSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PropertyValueSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.VoidMethodSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateContextTypeIds;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansCompletionUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.util.ClassUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Main entry point for the Spring beans xml editor's content assist.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
@SuppressWarnings( { "restriction", "unchecked" })
public class BeansContentAssistProcessor extends AbstractContentAssistProcessor {

	private static final String[] FILTERED_NAMES = new String[] {
			Serializable.class.getName(), InitializingBean.class.getName(),
			FactoryBean.class.getName(), DisposableBean.class.getName() };

	private void addBeanIdProposal(ContentAssistRequest request,
			String matchString, IDOMNode node) {
		String className = BeansEditorUtils.getClassNameForBean(node);
		if (className != null) {
			createBeanIdProposals(request, matchString, className);

			// add interface proposals
			IType type = JdtUtils.getJavaType(BeansEditorUtils.getResource(
					request).getProject(), className);
			Set<IType> allInterfaces = Introspector
					.getAllImplenentedInterfaces(type);
			for (IType interf : allInterfaces) {
				createBeanIdProposals(request, matchString, interf
						.getFullyQualifiedName());
			}
		}
	}

	private void addBeanReferenceProposals(ContentAssistRequest request,
			String prefix, Node node, boolean showExternal) {
		BeansCompletionUtils.addBeanReferenceProposals(request, prefix, node
				.getOwnerDocument(), showExternal);
	}

	private void addClassAttributeValueProposals(ContentAssistRequest request,
			String prefix) {
		BeansJavaCompletionUtils.addClassValueProposals(request, prefix);
	}

	private void addFactoryMethodAttributeValueProposals(
			ContentAssistRequest request, String prefix,
			String factoryClassName, boolean isStatic) {
		if (BeansEditorUtils.getResource(request) instanceof IFile) {
			IFile file = BeansEditorUtils.getResource(request);
			IType type = JdtUtils.getJavaType(file.getProject(),
					factoryClassName);
			if (type != null) {
				try {
					Collection<?> methods = Introspector.findAllMethods(type,
							prefix, -1, Public.DONT_CARE,
							(isStatic ? Static.YES : Static.DONT_CARE));
					if (methods != null && methods.size() > 0) {
						FactoryMethodSearchRequestor requestor = new FactoryMethodSearchRequestor(
								request);
						Iterator<?> iterator = methods.iterator();
						while (iterator.hasNext()) {
							requestor.acceptSearchMatch((IMethod) iterator
									.next());
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

	private void addInitDestroyAttributeValueProposals(
			ContentAssistRequest request, String prefix, String className) {
		if (BeansEditorUtils.getResource(request) instanceof IFile) {
			IFile file = BeansEditorUtils.getResource(request);
			IType type = JdtUtils.getJavaType(file.getProject(), className);
			if (type != null) {
				try {
					Collection<?> methods = Introspector
							.findAllNoParameterMethods(type, prefix);
					if (methods != null && methods.size() > 0) {
						VoidMethodSearchRequestor requestor = new VoidMethodSearchRequestor(
								request);
						Iterator<?> iterator = methods.iterator();
						while (iterator.hasNext()) {
							requestor.acceptSearchMatch((IMethod) iterator
									.next());
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

	private void addPropertyNameAttributeNameProposals(
			ContentAssistRequest request, String prefix, String oldPrefix,
			Node node, List classNames, boolean attrAtLocationHasValue,
			String nameSpacePrefix) {

		PropertyNameSearchRequestor requestor = new PropertyNameSearchRequestor(
				request, oldPrefix, attrAtLocationHasValue, nameSpacePrefix);
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
					Collection methods = Introspector.findReadableProperties(
							type, firstPrefix);
					if (methods != null && methods.size() == 1) {

						Iterator iterator = methods.iterator();
						while (iterator.hasNext()) {
							IMethod method = (IMethod) iterator.next();
							IType returnType = BeansEditorUtils
									.getTypeForMethodReturnType(method, type);

							if (returnType != null) {
								List<IType> typesTemp = new ArrayList<IType>();
								typesTemp.add(returnType);

								String newPrefix = oldPrefix + firstPrefix
										+ ".";
								;

								addPropertyNameAttributeNameProposals(request,
										lastPrefix, newPrefix, node, typesTemp,
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
					Collection methods = Introspector.findWritableProperties(
							type, prefix);
					if (methods != null && methods.size() > 0) {

						Iterator iterator = methods.iterator();
						while (iterator.hasNext()) {
							requestor.acceptSearchMatch((IMethod) iterator
									.next(), false);
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

	private void addPropertyNameAttributeValueProposals(
			ContentAssistRequest request, String prefix, String oldPrefix,
			Node node, List<IType> classNames) {

		PropertyValueSearchRequestor requestor = new PropertyValueSearchRequestor(
				request, oldPrefix);
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
				IType type = classNames.get(i);
				try {
					Collection<?> methods = Introspector
							.findReadableProperties(type, firstPrefix, true);
					if (methods != null && methods.size() == 1) {

						Iterator<?> iterator = methods.iterator();
						while (iterator.hasNext()) {
							IMethod method = (IMethod) iterator.next();
							IType returnType = BeansEditorUtils
									.getTypeForMethodReturnType(method, type);
							if (returnType != null) {
								List<IType> typesTemp = new ArrayList<IType>();
								typesTemp.add(returnType);

								String newPrefix = oldPrefix + firstPrefix
										+ ".";

								addPropertyNameAttributeValueProposals(request,
										lastPrefix, newPrefix, node, typesTemp);
							}
							return;
						}
					}
				}
				catch (CoreException e) {
					// // do nothing
				}
			}
		}
		else {
			for (int i = 0; i < classNames.size(); i++) {
				IType type = classNames.get(i);
				try {
					Collection<?> methods = Introspector
							.findWritableProperties(type, prefix, true);
					if (methods != null && methods.size() > 0) {

						Iterator<?> iterator = methods.iterator();
						while (iterator.hasNext()) {
							requestor.acceptSearchMatch((IMethod) iterator
									.next(), false);
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

	/**
	 * Derive a default bean name from the given bean definition.
	 * <p>
	 * The default implementation simply builds a decapitalized version of the
	 * short class name: e.g. "mypackage.MyJdbcDao" -> "myJdbcDao".
	 * @param definition the bean definition to build a bean name for
	 * @return the default bean name (never <code>null</code>)
	 */
	private String buildDefaultBeanName(String className) {
		String shortClassName = ClassUtils.getShortName(className);
		return java.beans.Introspector.decapitalize(shortClassName);
	}

	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request,
			String prefix, String namespace, String namespacePrefix,
			Node attributeNode) {
		if ("http://www.springframework.org/schema/p".equals(namespace)) {
			// check whether an attribute really exists for the replacement
			// offsets AND if it possesses a value
			IStructuredDocumentRegion sdRegion = request.getDocumentRegion();
			boolean attrAtLocationHasValue = false;
			NamedNodeMap attrs = attributeNode.getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				AttrImpl existingAttr = (AttrImpl) attrs.item(i);
				ITextRegion name = existingAttr.getNameRegion();
				if (sdRegion.getStartOffset(name) <= request
						.getReplacementBeginPosition()
						&& sdRegion.getStartOffset(name) + name.getLength() >= request
								.getReplacementBeginPosition()
								+ request.getReplacementLength()
						&& existingAttr.getValueRegion() != null) {
					attrAtLocationHasValue = true;
					break;
				}
			}

			if (prefix != null) {
				prefix = BeansEditorUtils.attributeNameToPropertyName(prefix);
			}

			List classNames = BeansEditorUtils.getClassNamesOfBean(
					BeansEditorUtils.getResource(request), attributeNode);
			addPropertyNameAttributeNameProposals(request, prefix, "",
					attributeNode, classNames, attrAtLocationHasValue,
					namespacePrefix);
		}
	}

	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName) {

		if ("bean".equals(node.getNodeName())) {
			if ("class".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
			else if ("init-method".equals(attributeName)
					|| "destroy-method".equals(attributeName)) {
				// TODO add support for parent bean
				String className = BeansEditorUtils.getAttribute(node, "class");
				if (className != null) {
					addInitDestroyAttributeValueProposals(request, matchString,
							className);
				}
			}
			else if ("factory-method".equals(attributeName)) {
				NamedNodeMap attributes = node.getAttributes();
				Node factoryBean = attributes.getNamedItem("factory-bean");
				String factoryClassName = null;
				boolean isStatic;
				if (factoryBean != null) {
					// instance factory method
					factoryClassName = BeansEditorUtils.getClassNameForBean(
							BeansEditorUtils.getResource(request), node
									.getOwnerDocument(), factoryBean
									.getNodeValue());
					isStatic = false;
				}
				else {
					// static factory method
					List<?> list = BeansEditorUtils.getClassNamesOfBean(
							BeansEditorUtils.getResource(request), node);
					factoryClassName = (list.size() != 0 ? ((IType) list.get(0))
							.getFullyQualifiedName()
							: null);
					isStatic = true;
				}

				if (factoryClassName != null) {
					addFactoryMethodAttributeValueProposals(request,
							matchString, factoryClassName, isStatic);
				}
			}
			else if ("parent".equals(attributeName)
					|| "depends-on".equals(attributeName)
					|| "factory-bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
			else if ("id".equals(attributeName)) {
				addBeanIdProposal(request, matchString, node);
			}
		}
		else if ("property".equals(node.getNodeName())) {
			Node parentNode = node.getParentNode();
			NamedNodeMap parentAttributes = parentNode.getAttributes();

			if ("name".equals(attributeName) && parentAttributes != null) {
				List classNames = BeansEditorUtils.getClassNamesOfBean(
						BeansEditorUtils.getResource(request), parentNode);
				addPropertyNameAttributeValueProposals(request, matchString,
						"", parentNode, classNames);
			}
			else if ("ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("ref".equals(node.getNodeName())
				|| "idref".equals(node.getNodeName())) {
			if ("local".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, false);
			}
			else if ("bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("constructor-arg".equals(node.getNodeName())) {
			if ("type".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
			else if ("ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("alias".equals(node.getNodeName())) {
			if ("name".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("entry".equals(node.getNodeName())) {
			if ("key-ref".equals(attributeName)
					|| "value-ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("value".equals(node.getNodeName())) {
			if ("type".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request,
			IDOMNode node) {
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

	private void createBeanIdProposals(ContentAssistRequest request,
			String matchString, String className) {
		String beanId = buildDefaultBeanName(className);
		if (beanId.startsWith(matchString) && shouldNotFilter(className)) {
			request.addProposal(new BeansJavaCompletionProposal(beanId, request
					.getReplacementBeginPosition(), request
					.getReplacementLength(), beanId.length(), BeansUIImages
					.getImage(BeansUIImages.IMG_OBJS_BEAN), beanId + " - "
					+ className, null, null, 10, null));
		}
	}

	private boolean shouldNotFilter(String className) {
		for (String filter : FILTERED_NAMES) {
			if (className.startsWith(filter)) {
				return false;
			}
		}
		return true;
	}
}
