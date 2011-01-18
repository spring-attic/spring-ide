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
			new String[] { "depends-on", "bean", "local", "parent", "ref", "key-ref", "value-ref" },
			new String[] { "p:" },
			new String[] { "-ref" }),

	ADVICE(	"Advice",
			"tx:advice",
			new String[] {"advice-ref"},
			new String[0],
			new String[0]),

	POINTCUT(	"Pointcut",
				"aop:pointcut",
				new String[] {"pointcut-ref"},
				new String[0],
				new String[0]);


	private final String type;
	private final String elementName;
	private final String[] referenceAttributeNames;
	private final String[] referenceAttributeStarts;
	private final String[] referenceAttributeEnds;

	private RenameIdType(String type, String elementName, String[] referenceAttributeNames,
			String[] referenceAttributeStarts, String[] referenceAttributeEnds) {
		this.type = type;
		this.elementName = elementName;
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
