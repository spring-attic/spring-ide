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
package org.springframework.ide.eclipse.beans.core.internal.model.update;

import org.eclipse.core.resources.IMarker;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.update.IBeansModelUpdate;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * For version prior to 2.0.3 Spring IDE used to create different
 * {@link IMarker} instances.
 * <p>
 * If this {@link IBeansModelUpdate} is not installed on a project, the error
 * marker will probably hang around for ever.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class UpdateFor203 implements IBeansModelUpdate {

	public String getName() {
		return "Updating Spring model description file to 2.0.3 version";
	}

	public boolean requiresUpdate(IBeansProject beansProject) {
		return !SpringCoreUtils.isVersionSameOrNewer(
				((BeansProject) beansProject).getVersion(), 2, 0, 3);
	}

	public void updateProject(IBeansProject beansProject) {
		MarkerUtils.deleteMarkers(beansProject.getProject(),
				BeansCorePlugin.PLUGIN_ID + ".problemmarker");
		((BeansProject) beansProject).saveDescription();
	}

}
