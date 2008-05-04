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
import java.util.List;
import java.util.Set;

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
	protected static final List<String> FILE_EXTENSIONS = Arrays.asList(new String[] { "xml" });

	/**
	 * Checks if the given <code>file</code> is accessible and its file extension is in the list
	 * of allowed file extensions.
	 */
	public final boolean isBeansConfig(IFile file) {
		if (file.isAccessible() && getAllowedFileExtensions().contains(file.getFileExtension())) {
			return locateBeansConfigs(file.getProject(), null).contains(file);
		}
		return false;
	}

	/**
	 * Returns a list of allowed file extensions. Subclasses may override this method to return
	 * other allowed file extensions.
	 * @return list of allowed file extensions.
	 */
	protected List<String> getAllowedFileExtensions() {
		return FILE_EXTENSIONS;
	}

	/**
	 * Returns <code>null</code> to express that this locater does not want to organize located
	 * files in a config set.
	 */
	public String getBeansConfigSetName(Set<IFile> files) {
		return null;
	}

}
