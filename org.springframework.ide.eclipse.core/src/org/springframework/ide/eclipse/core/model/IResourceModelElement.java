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
package org.springframework.ide.eclipse.core.model;

import org.eclipse.core.resources.IResource;

/**
 * Common protocol for all model elements that map to a {@link IResource} in the
 * Eclipse workspace.
 * 
 * @author Torsten Juergeleit
 */
public interface IResourceModelElement extends IModelElement {

	/**
	 * Returns the nearest enclosing resource for this element.
	 */
	IResource getElementResource();

	/**
	 * Returns <code>true</code> if this element belongs to a ZIP file. In
	 * this case the element's resource specifies the ZIP file and the
	 * elements's name defines the ZIP file name plus the corresponding ZIP
	 * entry (delimited by {@link ZipEntryStorage#DELIMITER}).
	 */
	boolean isElementArchived();
}
