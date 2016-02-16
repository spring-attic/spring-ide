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

/**
 * Wrapper for result of content assist calculator
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class ContentAssistProposalWrapper {

	private final String name, displayText;

	private Object proposedObject;

	public ContentAssistProposalWrapper(String name, String displayText) {
		this.name = name;
		this.displayText = displayText;
	}

	public ContentAssistProposalWrapper(String name, String displayText, Object proposedObject) {
		this(name, displayText);
		this.proposedObject = proposedObject;
	}

	public String getDisplayText() {
		return displayText;
	}

	public String getName() {
		return name;
	}

	public Object getProposedObject() {
		return proposedObject;
	}

}
