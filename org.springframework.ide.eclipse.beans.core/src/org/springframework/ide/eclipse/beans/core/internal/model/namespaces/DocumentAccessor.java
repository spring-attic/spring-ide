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
package org.springframework.ide.eclipse.beans.core.internal.model.namespaces;

import java.util.Stack;

import org.w3c.dom.Document;

/**
 * Utility that manages an internal stack of {@link Document}.
 * @author Christian Dupuis
 * @since 2.2.7
 */
public class DocumentAccessor {
	
	private Stack<Document> documents = new Stack<Document>();
	
	/** Push a new document onto the internal stack structure */
	public void pushDocument(Document doc) {
		documents.push(doc);
	}
	
	/** Returns the current document; meaning the first element in the stack */
	public Document getCurrentDocument() {
		return documents.peek();
	}
	
	/** Removes the first element from the stack */
	public Document popDocument() {
		return documents.pop();
	}
	
}
