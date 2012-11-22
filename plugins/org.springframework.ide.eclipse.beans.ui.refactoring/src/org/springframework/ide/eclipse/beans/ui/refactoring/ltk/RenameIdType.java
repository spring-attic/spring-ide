/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.ltk;

/**
 * @author Martin Lippert
 * @since 2.6.0
 */
public enum RenameIdType {
	
	BEAN(	"Bean",
			"bean",
			"http://www.springframework.org/schema/beans",
			new String[] { "depends-on", "bean", "local", "parent", "ref", "key-ref", "value-ref" },
			new String[] { "p:", "c:" },
			new String[] { "-ref", "-ref" }),

	ADVICE(	"Advice",
			"advice",
			"http://www.springframework.org/schema/tx",
			new String[] {"advice-ref"},
			new String[0],
			new String[0]),

	POINTCUT(	"Pointcut",
				"pointcut",
				"http://www.springframework.org/schema/aop",
				new String[] {"pointcut-ref"},
				new String[0],
				new String[0]);


	private final String type;
	private final String elementName;
	private final String elementNamespaceURI;
	private final String[] referenceAttributeNames;
	private final String[] referenceAttributeStarts;
	private final String[] referenceAttributeEnds;

	private RenameIdType(String type, String elementName, String elementNamespaceURI, String[] referenceAttributeNames,
			String[] referenceAttributeStarts, String[] referenceAttributeEnds) {
		this.type = type;
		this.elementName = elementName;
		this.elementNamespaceURI = elementNamespaceURI;
		this.referenceAttributeNames = referenceAttributeNames;
		this.referenceAttributeStarts = referenceAttributeStarts;
		this.referenceAttributeEnds = referenceAttributeEnds;
	}
	
	public String getType() {
		return type;
	}
	
	public String getElementName() {
		return elementName;
	}

	public String getElementNamespaceURI() {
		return elementNamespaceURI;
	}
	
	public String[] getReferenceAttributeNames() {
		return this.referenceAttributeNames;
	}
	
	public String[] getReferenceAttributeStarts() {
		return referenceAttributeStarts;
	}
	
	public String[] getReferenceAttributeEnds() {
		return referenceAttributeEnds;
	}

}
