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
package org.springframework.ide.eclipse.beans.ui.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.properties.ProjectPropertyPage;
import org.springframework.ide.eclipse.core.internal.model.resources.ISpringResourceChangeEvents;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeEventsAdapter;

/**
 * {@link ISpringResourceChangeEvents} event handler that opens the scan for XML bean definition
 * dialog on project change events that indicate that a projects has been assigned with the spring
 * nature.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class SpringNatureAddedEventHandler extends SpringResourceChangeEventsAdapter implements
		ISpringResourceChangeEvents {

	public void springNatureAdded(final IProject project, int eventType) {

		// only post build to make resources exist already and we can scan
		if (eventType == IResourceChangeEvent.POST_BUILD) {
			// run in the UI thread
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					Map<String, Object> data = new HashMap<String, Object>();
					data.put(ProjectPropertyPage.BLOCK_ID, 0);
					data.put(ProjectPropertyPage.SCAN, Boolean.TRUE);
					BeansUIUtils.showProjectPropertyPage(project, data);
				}
			});
		}
	}

	public boolean isSpringProject(IProject project, int eventType) {
		return eventType == IResourceChangeEvent.POST_BUILD
				&& BeansCorePlugin.getModel().getProject(project) != null;
	}

}
