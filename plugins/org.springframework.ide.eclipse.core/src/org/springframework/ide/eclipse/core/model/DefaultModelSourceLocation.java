/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model;

import java.io.Serializable;

import org.springframework.core.io.Resource;

/**
 * Simple implementation of {@link IModelSourceLocation} interface that takes
 * all relevant information in the constructor.
 * @author Christian Dupuis
 * @since 2.0
 */
public class DefaultModelSourceLocation implements IModelSourceLocation, Serializable {

	private static final long serialVersionUID = -1229230546428047429L;

	private final int endLine;

	private final int startline;

	private final Resource resource;
	
	public DefaultModelSourceLocation(final int startline, final int endLine,
			final Resource resource) {
		this.endLine = endLine;
		this.startline = startline;
		this.resource = resource;
	}

	public int getEndLine() {
		return this.endLine;
	}

	public Resource getResource() {
		return this.resource;
	}

	public int getStartLine() {
		return this.startline;
	}
}
