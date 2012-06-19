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
package org.springframework.ide.eclipse.roo.core.model;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;

/**
 * @author Christian Dupuis
 */
public interface IRooInstall {
	
	String SUPPORTED_VERSION = "[1.1.0.RELEASE, 2.0)";

	URL[] getClasspath();

	String getHome();

	String getName();

	String getVersion();

	boolean isDefault();
	
	IStatus validate();

}
