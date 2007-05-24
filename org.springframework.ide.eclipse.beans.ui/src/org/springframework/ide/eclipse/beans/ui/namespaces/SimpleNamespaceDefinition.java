/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.namespaces;

import org.eclipse.swt.graphics.Image;

/**
 * Default implementation of {@link INamespaceDefinition}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class SimpleNamespaceDefinition implements INamespaceDefinition {

	private final String prefix;

	private final String uri;

	private final String location;

	private final Image image;

	public SimpleNamespaceDefinition(final String prefix, final String uri,
			final String location, final Image image) {
		this.prefix = prefix;
		this.uri = uri;
		this.location = location;
		this.image = image;
	}

	public String getNamespacePrefix() {
		return prefix;
	}

	public String getNamespaceURI() {
		return uri;
	}

	public String getSchemaLocation() {
		return location;
	}

	public Image getNamespaceImage() {
		return image;
	}
}
