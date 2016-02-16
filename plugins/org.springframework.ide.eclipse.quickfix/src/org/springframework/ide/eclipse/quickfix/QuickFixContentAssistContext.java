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
package org.springframework.ide.eclipse.quickfix;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Content assist context for calculating proposals for quick fix
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class QuickFixContentAssistContext implements IContentAssistContext {

	private final String attributeName, prefix;

	private final IFile file;

	private final Node node;

	private final Document document;

	public QuickFixContentAssistContext(String attributeName, IFile file, Node node) {
		this(attributeName, file, node, "");
	}

	public QuickFixContentAssistContext(String attributeName, IFile file, Node node, String prefix) {
		this.attributeName = attributeName;
		this.file = file;
		this.node = node;
		this.prefix = prefix;
		this.document = node.getOwnerDocument();
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Document getDocument() {
		return document;
	}

	public IFile getFile() {
		return file;
	}

	public String getMatchString() {
		return prefix;
	}

	public Node getNode() {
		return node;
	}

	public Node getParentNode() {
		if (node != null) {
			return node.getParentNode();
		}
		return null;
	}

}
