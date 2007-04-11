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
package org.springframework.ide.eclipse.beans.core.model;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This interface provides information for a Spring beans import.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface IBeansImport extends IBeansModelElement, ISourceModelElement {

	IFile getImportedFile();
}
