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
package org.springframework.ide.eclipse.quickfix.validator;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;


/**
 * Standard validator for a bean configuration. It traverses and validates all
 * attributes and children. Subclass should supply additional validation for
 * node or attribute.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeanValidator {

	private String namespaceURI, nodeName, attributeName;

	private String namespaceURIException;

	protected final String[] BEANS_VALIDATOR_IDS = { // "com.springsource.sts.bestpractices.beansvalidator",
	// "com.springsource.sts.server.quickfix.manifestvalidator",
	"org.springframework.ide.eclipse.beans.core.beansvalidator" // ,
	// "org.springframework.ide.eclipse.core.springvalidator",
	// "org.springframework.ide.eclipse.webflow.core.validator"
	};

	public final boolean accept(Node node) {
		return acceptNamespaceURI(node.getNamespaceURI()) && acceptNodeName(node.getLocalName());
	}

	public final boolean acceptAttribute(Node node, Attr attr) {
		return accept(node) && acceptAttributeName(attr.getLocalName());
	}

	private boolean acceptAttributeName(String attributeName) {
		if (this.attributeName == null) {
			return true;
		}

		if (attributeName == null) {
			return false;
		}

		return this.attributeName.equals(attributeName);
	}

	private boolean acceptNamespaceURI(String namespaceURI) {
		if (this.namespaceURI == null) {
			if (namespaceURIException != null) {
				return !namespaceURIException.equals(namespaceURI);
			}
			return true;
		}

		if (namespaceURI == null) {
			return false;
		}

		if (this.namespaceURI.startsWith("!")) {
			return !this.namespaceURI.substring(1).equals(namespaceURI);
		}

		return this.namespaceURI.equals(namespaceURI);
	}

	private boolean acceptNodeName(String nodeName) {
		if (this.nodeName == null) {
			return true;
		}

		if (nodeName == null) {
			return false;
		}

		return this.nodeName.equals(nodeName);
	}

	public final IDOMNode getParentBeanNode(IDOMNode node) {
		String localName = node.getLocalName();
		if (localName == null) {
			return null;
		}

		if (localName.equals(BeansSchemaConstants.ELEM_BEAN)) {
			return node;
		}

		Node parentNode = node.getParentNode();
		if (parentNode != null && parentNode instanceof IDOMNode) {
			return getParentBeanNode((IDOMNode) parentNode);
		}

		return null;
	}

	protected final ValidationRuleDefinition getValidationRule(IProject project, Class<?> clazz) {
		for (String id : BEANS_VALIDATOR_IDS) {
			Set<ValidationRuleDefinition> rules = ValidationRuleDefinitionFactory
					.getEnabledRuleDefinitions(id, project);
			for (ValidationRuleDefinition rule : rules) {
				if (rule.getRule().getClass().equals(clazz)) {
					return rule;
				}
			}
		}
		return null;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	public void setNamespaceURIException(String namespaceURIException) {
		this.namespaceURIException = namespaceURIException;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public final boolean validateAttributeWithConfig(IBeansConfig config, IResourceModelElement contextElement,
			AttrImpl attribute, IDOMNode parent, IReporter reporter, boolean reportError, BeansEditorValidator validator) {
		String text = attribute.getValueRegionText();
		if (text == null) {
			return true;
		}
		IFile file = validator.getFile();

		return validateAttributeWithConfig(config, contextElement, file, attribute, parent, reporter, reportError,
				validator, text.replaceAll("\"", ""));
	}

	public boolean validateAttributeWithConfig(IBeansConfig config, IResourceModelElement contextElement, IFile file,
			AttrImpl attribute, IDOMNode parent, IReporter reporter, boolean reportError,
			BeansEditorValidator validator, String text) {
		return false;
	}

}
