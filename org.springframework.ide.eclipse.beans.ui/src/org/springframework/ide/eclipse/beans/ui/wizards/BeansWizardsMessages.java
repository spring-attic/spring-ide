/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

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

	public static String NewProjectPage_settings;
	public static String NewProjectPage_java;
	public static String NewProjectPage_source;
	public static String NewProjectPage_output;
	public static String NewProjectPage_noOutput;
	public static String NewProjectPage_extensions;
	public static String NewProjectPage_noExtensions;
	public static String NewProjectPage_invalidExtensions;

	static {
		NLS.initializeMessages(BUNDLE_NAME, BeansWizardsMessages.class);
	}
}
