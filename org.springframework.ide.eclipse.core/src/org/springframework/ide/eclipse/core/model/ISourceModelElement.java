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

/**
 * Common protocol for all {@link IModelElement}s related to source code.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface ISourceModelElement extends IResourceModelElement {

	/**
	 * Returns the element's source code element.
	 */
	IResourceModelElement getElementSourceElement();

	/**
	 * Returns the element's source code information.
	 */
	IModelSourceLocation getElementSourceLocation();

	/**
	 * Returns the line number with the start of the element's source code.
	 */
	int getElementStartLine();
	
	/**
	 * Returns the line number with the logical end of the element's source
	 * code.
	 */
	int getElementEndLine();
}
