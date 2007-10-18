/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.wizards;

import org.eclipse.osgi.util.NLS;

/**
 * @author Torsten Juergeleit
 */
public final class BeansWizardsMessages extends NLS {

	private static final String BUNDLE_NAME = "org.springframework.ide." +
							   "eclipse.beans.ui.wizards.BeansWizardsMessages";
	private BeansWizardsMessages() {
		// Do not instantiate
	}

	public static String ExceptionDialog_seeErrorLogMessage;

	public static String NewResource_op_error_title;
	public static String NewResource_op_error_message;

	public static String NewProject_windowTitle;
	public static String NewProject_title;
	public static String NewProject_description;

	public static String NewProject_referenceTitle;
	public static String NewProject_referenceDescription;

	public static String NewProject_createProject;
	public static String NewProject_createNewProject;
	public static String NewProject_addProjectNature;

	public static String NewProject_errorMessage;
	public static String NewProject_caseVariantExistsError;
	public static String NewProject_internalError;

	public static String NewProjectPage_springSettings;
	public static String NewProjectPage_suffixes;
	public static String NewProjectPage_noSuffixes;
	public static String NewProjectPage_invalidSuffixes;

	public static String NewProjectPage_javaSettings;
	public static String NewProjectPage_java;
	public static String NewProjectPage_source;
	public static String NewProjectPage_output;
	public static String NewProjectPage_noOutput;
	public static String NewProjectPage_enableProjectFacets;

	public static String NewConfig_windowTitle;
	public static String NewConfig_title;
	public static String NewConfig_xsdDescription;
	public static String NewConfig_fileDescription;
	public static String NewConfig_configSetDescription;

	static {
		NLS.initializeMessages(BUNDLE_NAME, BeansWizardsMessages.class);
	}
}
