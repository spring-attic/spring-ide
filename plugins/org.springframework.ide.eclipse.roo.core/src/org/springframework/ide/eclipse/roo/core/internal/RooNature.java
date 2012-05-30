/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.core.internal;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;


/**
 * @author Christian Dupuis
 * @author Andrew Eisenberg
 */
@SuppressWarnings("restriction")
public class RooNature implements IProjectNature {

	public static final String NATURE_ID = RooCoreActivator.NATURE_ID;
	
	private IProject project;

	public void configure() throws CoreException {
		addOutxmlOption(project);
	}

	public static void addOutxmlOption(IProject project) {
		if (AspectJPreferences.isUsingProjectSettings(project) &&
				AspectJPreferences.getBooleanPrefValue(project, AspectJPreferences.OPTION_Outxml)) {
			// nothing to do
			return;
		}
		
		// ensure that the -outxml preference is set on the project
		// so that aj builds will produce the aop-ajc.xml file.
		AspectJPreferences.setUsingProjectSettings(project, true);
		
		// this should really go in AJDT
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.PLUGIN_ID);
		projectNode.putBoolean(AspectJPreferences.OPTION_Outxml, true);
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
			RooCoreActivator.log(e);
		}
	}

	public void deconfigure() throws CoreException {
	}

	public IProject getProject() {
		return this.project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
