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
package org.springframework.ide.eclipse.beans.ui.editor.util;

import java.util.Collections;
import java.util.List;

import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.CMNamedNodeMap;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.contentmodel.util.DOMNamespaceHelper;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xsd.contentmodel.internal.XSDImpl.XSDAttributeUseAdapter;
import org.eclipse.xsd.XSDAttributeUse;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class to providing helper methods to work with the tool annotations defined in Spring
 * core.
 * @author Christian Dupuis
 * @since 2.0.3
 */
@SuppressWarnings("restriction")
public abstract class ToolAnnotationUtils {

	public static final String ASSIGNABLE_TO_ELEMENT = "assignable-to";

	public static final String TYPE_ATTRIBUTE = "type";

	public static final String EXPECTED_TYPE_ELEMENT = "expected-type";

	public static final String EXPECTED_METHOD_ELEMENT = "expected-method";

	public static final String TYPE_REF_ATTRIBUTE = "type-ref";

	public static final String EXPRESSION_ATTRIBUTE = "expression";

	public static final String RESTRITION_ATTRIBUTE = "restriction";

	public static final String KIND_ATTRIBUTE = "kind";

	public static final String TOOL_NAMESPACE_URI = "http://www.springframework.org/schema/tool";

	public static final String ANNOTATION_ELEMENT = "annotation";

	/**
	 * Return the {@link CMElementDeclaration} defined for the given node.
	 */
	protected static CMElementDeclaration getCMElementDeclaration(Node node) {
		CMElementDeclaration result = null;
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			ModelQuery modelQuery = ModelQueryUtil.getModelQuery(node.getOwnerDocument());
			if (modelQuery != null) {
				result = modelQuery.getCMElementDeclaration((Element) node);
			}
		}
		return result;
	}

	/**
	 * Return a list of annotations that are defined for a given attribute for a specific node.
	 */
	public static List<Element> getApplicationInformationElements(Node node, String attributeName) {
		// Retrieve the declaration
		CMElementDeclaration elementDecl = ToolAnnotationUtils.getCMElementDeclaration(node);
		CMAttributeDeclaration attrDecl = null;

		// No CMElementDeclaration means no attribute metadata, but
		// retrieve the declaration for the attribute otherwise
		if (elementDecl != null) {
			CMNamedNodeMap attributes = elementDecl.getAttributes();
			String noprefixName = DOMNamespaceHelper.getUnprefixedName(attributeName);
			if (attributes != null) {
				attrDecl = (CMAttributeDeclaration) attributes.getNamedItem(noprefixName);
				if (attrDecl == null) {
					attrDecl = (CMAttributeDeclaration) attributes.getNamedItem(attributeName);
				}
			}
			if (attrDecl instanceof XSDAttributeUseAdapter) {
				XSDAttributeUse attribute = (XSDAttributeUse) ((XSDAttributeUseAdapter) attrDecl)
						.getKey();
				// 1. Check if annotation and tool annotation are actually
				// present
				if (attribute.getAttributeDeclaration() != null
						&& attribute.getAttributeDeclaration().getAnnotation() != null
						&& attribute.getAttributeDeclaration().getAnnotation()
								.getApplicationInformation() != null) {
					return attribute.getAttributeDeclaration().getAnnotation()
							.getApplicationInformation();
				}
				// 2. If no directly attached annotation could be
				// found, try the referenced type definition if any.
				if (attribute.getAttributeDeclaration() != null
						&& attribute.getAttributeDeclaration().getTypeDefinition() != null
						&& attribute.getAttributeDeclaration().getTypeDefinition().getAnnotation() != null
						&& attribute.getAttributeDeclaration().getTypeDefinition().getAnnotation()
								.getApplicationInformation() != null) {
					return attribute.getAttributeDeclaration().getTypeDefinition().getAnnotation()
							.getApplicationInformation();
				}

			}
		}
		return Collections.emptyList();
	}

	/**
	 * Returns a instance of {@link ToolAnnotationData}. This data holder carries information of the
	 * annotation values.
	 */
	public static ToolAnnotationData getToolAnnotationData(Node annotation) {
		ToolAnnotationData data = new ToolAnnotationData();
		data.setKind(BeansEditorUtils.getAttribute(annotation, KIND_ATTRIBUTE));

		NodeList children = annotation.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (EXPECTED_TYPE_ELEMENT.equals(child.getLocalName())
					&& TOOL_NAMESPACE_URI.equals(child.getNamespaceURI())) {
				data.setExpectedType(BeansEditorUtils.getAttribute(child, TYPE_ATTRIBUTE));
			}
			else if (ASSIGNABLE_TO_ELEMENT.equals(child.getLocalName())
					&& TOOL_NAMESPACE_URI.equals(child.getNamespaceURI())) {
				data.setAssignableTo(BeansEditorUtils.getAttribute(child, TYPE_ATTRIBUTE));
				data.setAssignableToRestriction(BeansEditorUtils.getAttribute(child,
						RESTRITION_ATTRIBUTE));
			}
			else if (EXPECTED_METHOD_ELEMENT.equals(child.getLocalName())
					&& TOOL_NAMESPACE_URI.equals(child.getNamespaceURI())) {
				data.setExpectedMethodType(BeansEditorUtils.getAttribute(child, TYPE_ATTRIBUTE));
				data.setExpectedMethodRef(BeansEditorUtils.getAttribute(child, TYPE_REF_ATTRIBUTE));
				data.setExpectedMethodExpression(BeansEditorUtils.getAttribute(child,
						EXPRESSION_ATTRIBUTE));
			}

		}
		return data;
	}

	/**
	 * Helper class carrying information from attribute annotations.
	 */
	public static class ToolAnnotationData {

		private String kind;

		private String assignableTo;

		private String assignableToRestriction;

		private String expectedType;

		private String expectedMethodType;

		private String expectedMethodRef;

		private String expectedMethodExpression;

		public String getKind() {
			return kind;
		}

		public String getAssignableTo() {
			return assignableTo;
		}

		public String getExpectedType() {
			return expectedType;
		}

		public String getAssignableToRestriction() {
			return assignableToRestriction;
		}

		public void setAssignableToRestriction(String assignableToRestriction) {
			this.assignableToRestriction = assignableToRestriction;
		}

		public String getExpectedMethodType() {
			return expectedMethodType;
		}

		public void setExpectedMethodType(String expectedMethodType) {
			this.expectedMethodType = expectedMethodType;
		}

		public String getExpectedMethodRef() {
			return expectedMethodRef;
		}

		public void setExpectedMethodRef(String expectedMethodRef) {
			this.expectedMethodRef = expectedMethodRef;
		}

		public String getExpectedMethodExpression() {
			return expectedMethodExpression;
		}

		public void setExpectedMethodExpression(String expectedMethodExpression) {
			this.expectedMethodExpression = expectedMethodExpression;
		}

		public void setKind(String kind) {
			this.kind = kind;
		}

		public void setAssignableTo(String assignableTo) {
			this.assignableTo = assignableTo;
		}

		public void setExpectedType(String expectedType) {
			this.expectedType = expectedType;
		}

	}
}
