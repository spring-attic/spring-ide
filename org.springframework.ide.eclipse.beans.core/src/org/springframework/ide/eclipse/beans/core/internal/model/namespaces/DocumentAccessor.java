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
package org.springframework.ide.eclipse.beans.core.internal.model.namespaces;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

/**
 * Utility that manages an internal stack of {@link Document}.
 * @author Christian Dupuis
 * @since 2.2.7
 */
public class DocumentAccessor {

	private Stack<Document> documents = new Stack<Document>();
	
	private Map<Document, SchemaLocations> schemaLocations = new HashMap<Document, SchemaLocations>(); 

	/** Push a new document onto the internal stack structure */
	public void pushDocument(Document doc) {
		documents.push(doc);
	}

	/** Returns the current document; meaning the first element in the stack */
	public Document getCurrentDocument() {
		return documents.peek();
	}
	
	/** Returns the current values of the schemaLocation attribute */
	public synchronized SchemaLocations getCurrentSchemaLocations() {
		Document doc = getCurrentDocument();
		if (!this.schemaLocations.containsKey(doc)) {
			if (doc != null && doc.getDocumentElement() != null) {
				SchemaLocations schemaLocations = new SchemaLocations();
				schemaLocations.initSchemaLocations(doc.getDocumentElement().getAttributeNS(
						"http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
				this.schemaLocations.put(doc, schemaLocations);
			}
		}
		return this.schemaLocations.get(doc);
	}

	/** Removes the first element from the stack */
	public Document popDocument() {
		return documents.pop();
	}
	
	/**
	 * Internal class that parses the value of the <code>schemaLocation</code> attribute and offers accessors to the
	 * mapping.
	 */
	static class SchemaLocations {

		private Map<String, String> mapping = new HashMap<String, String>();

		public void initSchemaLocations(String schemaLocations) {
			if (StringUtils.hasLength(schemaLocations)) {
				String[] tokens = StringUtils.tokenizeToStringArray(schemaLocations, " \r\n");
				if (tokens.length % 2 == 0) {
					for (int i = 0; i < tokens.length; i = i + 2) {
						mapping.put(tokens[i], tokens[i + 1]);
					}
				}
			}
		}

		public String getSchemaLocation(String namespaceUri) {
			return mapping.get(namespaceUri);
		}
	}

}
