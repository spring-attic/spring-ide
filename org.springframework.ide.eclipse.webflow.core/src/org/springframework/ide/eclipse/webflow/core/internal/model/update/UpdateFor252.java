/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.update;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.update.IWebflowModelUpdate;

/**
 * For version prior to 2.5.2 Spring IDE stored project information in .springBeans.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class UpdateFor252 implements IWebflowModelUpdate {

	public String getName() {
		return "Updating Spring model to 2.5.2 version";
	}

	public boolean requiresUpdate(IWebflowProject webflowProject) {
		return webflowProject.getProject().getFile(IWebflowProject.DESCRIPTION_FILE_OLD).exists();
	}

	public void updateProject(IWebflowProject webflowProject) {
		IFile oldFile = webflowProject.getProject().getFile(IWebflowProject.DESCRIPTION_FILE_OLD);
		IFile newFile = webflowProject.getProject().getFile(IWebflowProject.DESCRIPTION_FILE);
		if (oldFile.exists() && !newFile.exists()) {
			// Save the new project description into the new location
			((WebflowProject) webflowProject).saveDescription();
			// Delete the old file
			try {
				if (SpringCoreUtils.validateEdit(oldFile)) {
					oldFile.delete(true, new NullProgressMonitor());
				}
			}
			catch (CoreException e) {
				Activator.log("Problem deleting old '.springWebflow'", e);
			}
		}
	}

}
