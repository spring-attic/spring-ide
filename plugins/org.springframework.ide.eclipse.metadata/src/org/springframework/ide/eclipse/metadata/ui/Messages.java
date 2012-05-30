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
package org.springframework.ide.eclipse.metadata.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Leo Dos Santos
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.metadata.ui.messages"; //$NON-NLS-1$
	public static String AnnotationMetadataLabelProvider_DESCRIPTION_STEREOTYPE_ANNOTATION_GROUPING;
	public static String RequestMappingView_DESCRIPTION_EMPTY_JAVADOC;
	public static String RequestMappingView_DESCRIPTION_EMPTY_REQUESTMAPPINGS;
	public static String RequestMappingView_ERROR_GENERATING_JAVADOC;
	public static String RequestMappingView_ERROR_PROCESSING_RESOURCE_CHANGE;
	public static String RequestMappingView_HEADER_HANDLER_METHOD;
	public static String RequestMappingView_HEADER_REQUEST_METHOD;
	public static String RequestMappingView_HEADER_RESOURCE_URL;
	public static String RequestMappingView_PREFIX_CONFIG_FILE;
	public static String RequestMappingView_PREFIX_CONFIG_SET;
	public static String RequestMappingView_PREFIX_PROJECT;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
