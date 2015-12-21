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
package org.springframework.ide.eclipse.wizard.template.infrastructure;

import java.io.File;

import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;


public interface ITemplateProjectData {

	public Descriptor getDescriptor();

	public File getJsonDescriptor();

	// public abstract File getTemplateDirectory();

	public File getZippedProject();

	public static final String TAG_TEMPLATE = "template";

	public static final String TAG_DESCRIPTOR = "descriptor";

	public static final String TAG_PROJECT = "project";

	public static final String TAG_JSON = "json";

	public static final String ATTRIBUTE_PATH = "path";

}