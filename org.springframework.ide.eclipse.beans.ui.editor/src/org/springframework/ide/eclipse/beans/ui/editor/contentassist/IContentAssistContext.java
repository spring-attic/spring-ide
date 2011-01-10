/*******************************************************************************
 * Copyright (c) 2008, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Describes the context of the current content assist request.
 * <p>
 * Provides access to relevant data such as <code>matchString</code>, <code>attributeName</code> and
 * <code>file</code> the user requested content assist.
 * @author Christian Dupuis
 * @since 2.2.1
 */
public interface IContentAssistContext {

	/**
	 * Returns the name of the attribute for which value the user requested content assist.
	 * @return the name of the attribute
	 */
	String getAttributeName();

	/**
	 * Returns the string that is already entered by the user prior to requesting content assist.
	 * @return the entered string
	 */
	String getMatchString();

	/**
	 * Returns the underlying resource. That resource <b>must</b> exist and point to a valid xml
	 * file.
	 * @return the underlying resource
	 */
	IFile getFile();

	/**
	 * Returns the node for which the user requested content assist.
	 * @return the node the user requested content assist for
	 */
	Node getNode();

	/**
	 * Returns the parent node of the node for which the user requested content assist.
	 * @return the parent node of the node the user requested content assist for
	 */
	Node getParentNode();
	
	/**
	 * Returns the {@link Document} of the current content assist request
	 * @return the owning document
	 */
	Document getDocument();
	
}
