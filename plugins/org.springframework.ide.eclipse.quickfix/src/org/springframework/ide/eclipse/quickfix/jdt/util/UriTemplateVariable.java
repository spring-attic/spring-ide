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
package org.springframework.ide.eclipse.quickfix.jdt.util;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * @author Terry Denney
 * @since 2.6
 */
public class UriTemplateVariable {

	private final String variableName;

	private final int offset;

	private final ASTNode node;

	public UriTemplateVariable(String variableName, int offset, ASTNode node) {
		this.variableName = variableName;
		this.offset = offset;
		this.node = node;
	}

	public String getVariableName() {
		return variableName;
	}

	public int getOffsetFromNode() {
		return offset;
	}

	public int getOffset() {
		return offset + node.getStartPosition();
	}

	public ASTNode getNode() {
		return node;
	}

}
