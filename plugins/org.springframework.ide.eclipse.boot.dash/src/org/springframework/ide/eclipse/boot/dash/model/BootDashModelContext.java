/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.dash.metadata.IScopedPropertyStore;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;

public interface BootDashModelContext {

	//TODO: many places where this being passed around it is accompanited by a BootDashViewModel.
	// These two parameters passed together represent the real 'BootDashModelContext'.
	//So the proper thing to do is:
	//
	//  - rename this interface to BootDashViewModelContext (it represents the context of the viewmodel not of the indivual sections within
	//  - create a new class or interface called BootDashModelContext which contains
	//      - a BootDashModelContext
	//      - a BootDashViewModel
	//  - where both of these types occur together, replace with a reference to the new BootDashViewModelContext

	IWorkspace getWorkspace();

	ILaunchManager getLaunchManager();

	IPath getStateLocation();

	IScopedPropertyStore<IProject> getProjectProperties();
	IScopedPropertyStore<RunTargetType> getRunTargetProperties();


	SecuredCredentialsStore getSecuredCredentialsStore();

	void log(Exception e);


}
