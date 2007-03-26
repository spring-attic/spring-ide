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

import org.springframework.core.io.Resource;

/**
 * Defines a storage for source information of {@link IModelElement}s.
 *
 * @author Torsten Juergeleit
 */
public interface IModelSourceLocation {

	Resource getResource();

	int getStartLine();

	int getEndLine();
}
