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

import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Terry Denney
 */
public class BeanValidatorVisitor {

	private final IBeansConfig config;

	private final IResourceModelElement contextElement;

	private final IReporter reporter;

	private final BeansEditorValidator editorValidator;

	public BeanValidatorVisitor(IBeansConfig config, IResourceModelElement contextElement, IReporter reporter,
			BeansEditorValidator editorValidator) {
		this.config = config;
		this.contextElement = contextElement;
		this.reporter = reporter;
		this.editorValidator = editorValidator;

	}

	private boolean visitAttributes(IDOMNode node, boolean reportError) {
		boolean errorFound = false;

		NamedNodeMap attributes = node.getAttributes();
		if (attributes == null) {
			return false;
		}

		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);

			if (attribute instanceof AttrImpl) {
				AttrImpl attrImpl = (AttrImpl) attribute;

				errorFound |= QuickfixUtils.validateAttribute(config, contextElement, attrImpl, node, reporter,
						reportError, editorValidator);
			}
		}

		return errorFound;
	}

	private boolean visitChildren(IDOMNode node, boolean reportError) {
		NodeList childNodes = node.getChildNodes();
		boolean errorFound = false;

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode instanceof IDOMNode) {
				visitNode((IDOMNode) childNode, true, reportError);
			}
		}

		return errorFound;
	}

	public boolean visitNode(IDOMNode node, boolean visitChildren, boolean reportError) {
		boolean errorFound = visitAttributes(node, reportError);
		if (errorFound) {
			return true;
		}
		if (visitChildren) {
			return visitChildren(node, reportError);
		}
		return false;
	}

}
