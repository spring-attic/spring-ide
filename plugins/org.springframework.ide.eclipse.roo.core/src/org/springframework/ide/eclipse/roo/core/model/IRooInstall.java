/*******************************************************************************
 *  Copyright (c) 2012, 2015 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *      DISID Corporation, S.L - Spring Roo maintainer
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.core.model;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;

/**
 * @author Christian Dupuis
 * @author Juan Carlos García
 */
public interface IRooInstall {

	String SUPPORTED_VERSION = "1.1.0.RELEASE";

	URL[] getClasspath();

	String getHome();

	String getName();

	String getVersion();

	boolean isDefault();

	IStatus validate();

}
