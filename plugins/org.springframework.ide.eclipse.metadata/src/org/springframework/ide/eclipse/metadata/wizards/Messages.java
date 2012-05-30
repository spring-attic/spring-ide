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
package org.springframework.ide.eclipse.metadata.wizards;

import org.eclipse.osgi.util.NLS;

/**
 * @author Leo Dos Santos
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.metadata.wizards.messages"; //$NON-NLS-1$
	public static String OpenRequestMappingUrlWizard_TITLE;
	public static String OpenRequestMappingUrlWizardPage_DESCRIPTION;
	public static String OpenRequestMappingUrlWizardPage_ERROR_LOADING_CACHE;
	public static String OpenRequestMappingUrlWizardPage_ERROR_SAVING_CACHE;
	public static String OpenRequestMappingUrlWizardPage_HEADER_TITLE;
	public static String OpenRequestMappingUrlWizardPage_LABEL_URL_PREFIX;
	public static String OpenRequestMappingUrlWizardPage_PAGE_TITLE;
	public static String OpenRequestMappingUrlWizardPage_WARNING_URL_CONSTRUCTION;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
