/*******************************************************************************
 * Copyright (c) 2008, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.update;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.update.IBeansModelUpdate;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * For version prior to 2.5.2 Spring IDE stored project information in .springBeans.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class UpdateFor252 implements IBeansModelUpdate {

	public String getName() {
		return "Updating Spring model to 2.5.2 version";
	}

	public boolean requiresUpdate(IBeansProject beansProject) {
		return beansProject.getProject().getFile(IBeansProject.DESCRIPTION_FILE_OLD).exists();
	}

	public void updateProject(IBeansProject beansProject) {
		IFile oldFile = beansProject.getProject().getFile(IBeansProject.DESCRIPTION_FILE_OLD);
		IFile newFile = beansProject.getProject().getFile(IBeansProject.DESCRIPTION_FILE);
		if (oldFile.exists() && !newFile.exists()) {
			// Save the new project description into the new location
			((BeansProject) beansProject).saveDescription();
			// Delete the old file
			try {
				if (SpringCoreUtils.validateEdit(oldFile)) {
					oldFile.delete(true, new NullProgressMonitor());
				}
			}
			catch (CoreException e) {
				BeansCorePlugin.log("Problem deleting old '.springBeans'", e);
			}
		}
	}

}
