/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.locate;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;

/**
 * Base {@link IBeansConfigLocator} implementation that only implements
 * {@link #isBeansConfig(IFile)}.
 * <p>
 * @author Christian Dupuis
 * @since 2.0.5
 */
public abstract class AbstractBeansConfigLocator implements IBeansConfigLocator {

	/** Internal list of allowed file extensions */
	protected static final String[] FILE_EXTENSIONS = new String[] { "xml" };

	/**
	 * Checks if the given <code>file</code> is accessible and its file extension is in the list
	 * of allowed file extensions.
	 */
	public final boolean isBeansConfig(IFile file) {
		if (file.isAccessible()
				&& Arrays.asList(getAllowedFileExtensions()).contains(file.getFileExtension())) {
			return locateBeansConfigs(file.getProject()).contains(file);
		}
		return false;
	}

	/**
	 * Returns a list of allowed file extensions. Subclasses may override this method to return
	 * other allowed file extensions.
	 * @return list of allowed file extensions.
	 */
	protected String[] getAllowedFileExtensions() {
		return FILE_EXTENSIONS;
	}

}
