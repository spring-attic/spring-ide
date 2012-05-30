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
package org.springframework.ide.eclipse.wizard.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Content assist context for bean wizard field.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WizardContentAssistContext implements IContentAssistContext {

	private final IDOMNode node;

	private final IFile file;

	private final String prefix;

	private final Document originalDocument;

	private final String attributeName;

	public WizardContentAssistContext(IDOMNode node, String attributeName, IFile file, String prefix,
			Document originalDocument) {
		this.node = node;
		this.attributeName = attributeName;
		this.file = file;
		this.prefix = prefix;
		this.originalDocument = originalDocument;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Document getDocument() {
		return originalDocument;
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
		return node.getParentNode();
	}

}
