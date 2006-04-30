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

package org.springframework.ide.eclipse.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Torsten Juergeleit
 */
public final class SpringUIMessages extends NLS {

	private static final String BUNDLE_NAME = "org.springframework.ide." +
										   "eclipse.beans.ui.SpringUIMessages";
	private SpringUIMessages() {
		// Do not instantiate
	}

	public static String Plugin_internalError;

	public static String ProjectNature_errorMessage;
	public static String ProjectNature_addError;
	public static String ProjectNature_removeError;

	public static String OpenInEditor_errorMessage;

	public static String ImageDescriptorRegistry_wrongDisplay;

	static {
		NLS.initializeMessages(BUNDLE_NAME, SpringUIMessages.class);
	}
}
