/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.cli.install;

import java.io.File;

import org.eclipse.core.runtime.IStatus;

/**
 * Common interface for anything that represents an installation of spring boot.
 * 
 * @author Kris De Volder
 */
public interface IBootInstall {

	String getUrl(); //Url identifying this installation. Two installs are considered the same if their urls are the same.
	File getHome() throws Exception;
	File[] getBootLibJars() throws Exception;
	String getName();
	IStatus validate();
	String getVersion();
	
	/**
	 * For installs that are zipped or non-local this deletes the cached info (i.e. unzipped and locally downloaded copy
	 * of the data. For locally configured installations this does nothing.
	 */
	void clearCache();
	
}
