/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.util;

import org.eclipse.osgi.util.NLS;

public class AddEditNameUrlDialogMessages {

	private static final String BUNDLE_NAME = "org.springframework.ide."
			+ "eclipse.wizard.template.util.AddEditNameUrlDialogMessages";

	public static String ExampleProjects_mustBeAtGithub;

	public static String ExampleProjects_addTitle;

	public static String ExampleProjects_editTitle;

	public static String malformedUrl;

	public static String malformedUrlIgnoring;

	public static String TemplateProjects_addTitle;

	public static String TemplateProjects_editTitle;

	static {
		NLS.initializeMessages(BUNDLE_NAME, AddEditNameUrlDialogMessages.class);
	}
}
