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
package org.springframework.ide.eclipse.quickfix.processors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean.FactoryMethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean.InitDestroyMethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Factory class for creating quickfix processors.
 * @author Terry Denney
 * @since 2.0
 */
public enum QuickfixProcessorFactory {
	CLASS, PROPERTY, REF, FACTORY_METHOD, INIT_DESTROY_METHOD, CONSTRUCTOR_ARG, RENAME_PROPERTY, RENAME_METHOD, DEPRECATED, BEAN_DEFINITION, REQUIRED_PROPERTY, NAMESPACE, NAMESPACE_ELEMENTS, FACTORY_BEAN, ALIAS;

	@SuppressWarnings("unchecked")
	public BeanQuickAssistProcessor create(int offset, int length, String text, boolean missingEndQuote,
			IDOMNode parentNode, BeansEditorValidator validator, String problemId,
			ValidationProblemAttribute... problemAttributes) {
		boolean isStatic = true;
		IProject project = validator.getProject();

		IFile file = validator.getFile();

		Set<String> properties = new HashSet<String>();
		List<String> refClassNames = new ArrayList<String>();
		Map<String, Node> referenceableNodes = BeansEditorUtils.getReferenceableNodes(parentNode.getOwnerDocument(),
				file);

		NodeList childNodes = parentNode.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			String localName = child.getLocalName();
			if (localName != null) {
				if (localName.equals(BeansSchemaConstants.ELEM_PROPERTY)) {
					properties.add(getPropertyName(child));
				}
				else if (localName.equals(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG)) {
					NamedNodeMap attributes = child.getAttributes();
					Node refAttr = attributes.getNamedItem(BeansSchemaConstants.ATTR_REF);
					String refClassName = "Object";
					if (refAttr != null) {
						String ref = refAttr.getNodeValue();
						if (ref != null) {
							Node refNode = referenceableNodes.get(ref);
							refClassName = BeansEditorUtils.getClassNameForBean(refNode);
						}
					}
					refClassNames.add(refClassName);
				}
			}
		}

		String className = null, beanName = null;
		IMethod method = null;
		int numConstructorArgs = -1;
		List<String> missingProperties = null;
		for (ValidationProblemAttribute problemAttribute : problemAttributes) {
			String attributeKey = problemAttribute.getKey();
			if ("CLASS".equals(attributeKey)) {
				className = (String) problemAttribute.getValue();
			}
			else if ("BEAN".equals(attributeKey)) {
				beanName = (String) problemAttribute.getValue();
			}
			else if ("MISSING_PROPERTIES".equals(attributeKey)) {
				missingProperties = (List<String>) problemAttribute.getValue();
			}
			else if ("METHOD_OBJ".equals(attributeKey)) {
				method = (IMethod) problemAttribute.getValue();
			}
			else if ("NUM_CONSTRUCTOR_ARGS".equals(attributeKey)) {
				numConstructorArgs = (Integer) problemAttribute.getValue();
			}
		}

		if (numConstructorArgs < 0) {
			numConstructorArgs = refClassNames.size();
		}

		switch (this) {
		case CLASS:
			if ("CLASS_NOT_FOUND".equals(problemId)) {
				return new ClassAttributeQuickAssistProcessor(offset, length, text, project, missingEndQuote,
						properties, numConstructorArgs);
			}
		case PROPERTY:
			if (className != null) {
				if ("NO_GETTER".equals(problemId)) {
					return new PropertyAttributeQuickAssistProcessor(offset, length, className, text, project,
							missingEndQuote, PropertyAttributeQuickAssistProcessor.Type.GETTER);
				}
				if ("NO_SETTER".equals(problemId)) {
					return new PropertyAttributeQuickAssistProcessor(offset, length, className, text, project,
							missingEndQuote, PropertyAttributeQuickAssistProcessor.Type.SETTER);
				}
			}
			break;
		case REF:
			if ("UNDEFINED_PARENT_BEAN".equals(problemId) || "UNDEFINED_DEPENDS_ON_BEAN".equals(problemId)
					|| "UNDEFINED_FACTORY_BEAN".equals(problemId) || "UNDEFINED_REFERENCED_BEAN".equals(problemId)) {
				return new BeanReferenceQuickAssistProcessor(offset, length, text, missingEndQuote, parentNode,
						BeansSchemaConstants.ATTR_REF, beanName, file);
			}
			break;
		case FACTORY_METHOD:
			if ("NO_FACTORY_METHOD".equals(problemId) || "UNDEFINED_FACTORY_BEAN_METHOD".equals(problemId)) {
				return new MethodAttributeQuickAssistProcessor(offset, length, className, text, missingEndQuote,
						parentNode, BeansSchemaConstants.ATTR_FACTORY_METHOD, project, isStatic,
						new FactoryMethodContentAssistCalculator(), file);
			}
			break;
		case FACTORY_BEAN:
			if ("UNDEFINED_FACTORY_BEAN".equals(problemId)) {
				return new BeanReferenceQuickAssistProcessor(offset, length, text, missingEndQuote, parentNode,
						BeansSchemaConstants.ATTR_FACTORY_BEAN, beanName, file);
			}
			if ("NO_FACTORY_METHOD".equals(problemId) || "UNDEFINED_FACTORY_BEAN_METHOD".equals(problemId)) {
				return new MissingFactoryMethodAttributeQuickAssistProcessor(offset, length, text, missingEndQuote,
						parentNode);
			}
		case INIT_DESTROY_METHOD:
			if ("UNDEFINED_INIT_METHOD".equals(problemId)) {
				return new MethodAttributeQuickAssistProcessor(offset, length, className, text, missingEndQuote,
						parentNode, BeansSchemaConstants.ATTR_INIT_METHOD, project, isStatic,
						new InitDestroyMethodContentAssistCalculator(), file);
			}
			else if ("UNDEFINED_DESTROY_METHOD".equals(problemId)) {
				return new MethodAttributeQuickAssistProcessor(offset, length, className, text, missingEndQuote,
						parentNode, BeansSchemaConstants.ATTR_DESTROY_METHOD, project, isStatic,
						new InitDestroyMethodContentAssistCalculator(), file);
			}
		case CONSTRUCTOR_ARG:
			if ("NO_CONSTRUCTOR".equals(problemId)) {
				return new ConstructorArgQuickAssistProcessor(offset, length, text, project, missingEndQuote,
						refClassNames, parentNode);
			}
			if ("MISSING_CONSTRUCTOR_ARG_NAME".equals(problemId)) {
				return new ConstructorArgNameQuickAssistProcessor(offset, length, text, className, project,
						missingEndQuote, numConstructorArgs, parentNode);
			}
			break;
		case RENAME_PROPERTY:
			return new RenamePropertyQuickAssistProcessor(offset, length, className, text, project, missingEndQuote,
					validator.getFile());
		case RENAME_METHOD:
			return new RenameMethodQuickAssistProcessor(offset, length, className, text, project, missingEndQuote,
					validator.getFile());
		case NAMESPACE_ELEMENTS:
			return new NameSpaceElementsQuickAssistProcessor(problemId, offset, length, text, missingEndQuote, project,
					parentNode, file, problemAttributes);

			// TODO: processor for bean definition?
			// TODO: processor for required property missing?
			// TODO: processor for namespace element

		case REQUIRED_PROPERTY:
			if (missingProperties != null) {
				return new RequiredPropertyQuickAssistProcessor(offset, length, text, missingEndQuote,
						missingProperties, parentNode);
			}
			break;

		case DEPRECATED:
			if ("CLASS_IS_DEPRECATED".equals(problemId) && className != null) {
				return new ClassDeprecatedQuickAssistProcessor(offset, length, text, missingEndQuote, className,
						project);
			}
			if ("METHOD_IS_DEPRECATED".equals(problemId) && method != null && className != null) {
				return new MethodDeprecatedQuickAssistProcessor(offset, length, text, missingEndQuote, className,
						method.getElementName(), method);
			}
			break;
		}

		return null;
	}

	private String getPropertyName(Node child) {
		NamedNodeMap attributes = child.getAttributes();
		Node attribute = attributes.getNamedItem(BeansSchemaConstants.ATTR_NAME);
		return attribute.getNodeValue();
	}

}
