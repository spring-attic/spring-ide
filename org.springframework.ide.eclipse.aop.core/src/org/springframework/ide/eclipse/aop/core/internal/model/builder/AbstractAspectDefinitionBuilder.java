/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.aop.core.internal.model.BeanAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * Utility class that encapsulates the loading of a {@link IDOMDocument} from the given
 * {@link IFile}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractAspectDefinitionBuilder implements IAspectDefinitionBuilder {

	protected String getAttribute(Node node, String attributeName) {
		if (hasAttribute(node, attributeName)) {
			String value = node.getAttributes().getNamedItem(attributeName).getNodeValue();
			value = StringUtils.replace(value, "\n", " ");
			value = StringUtils.replace(value, "\t", " ");
			return StringUtils.replace(value, "\r", " ");
		}
		return null;
	}

	protected boolean hasAttribute(Node node, String attributeName) {
		return (node != null && node.hasAttributes() && node.getAttributes().getNamedItem(
				attributeName) != null);
	}
	
	protected void extractLineNumbers(IAspectDefinition def, IDOMNode node) {
		if (def instanceof BeanAspectDefinition) {
			BeanAspectDefinition bDef = (BeanAspectDefinition) def;
			bDef.setAspectStartLineNumber(((IDOMDocument) node.getOwnerDocument())
					.getStructuredDocument().getLineOfOffset(node.getStartOffset()) + 1);
			bDef.setAspectEndLineNumber(((IDOMDocument) node.getOwnerDocument())
					.getStructuredDocument().getLineOfOffset(node.getEndOffset()) + 1);
		}
	}

}
