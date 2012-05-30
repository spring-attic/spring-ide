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
package org.springframework.ide.eclipse.config.ui.wizards;

import org.eclipse.osgi.util.NLS;

/**
 * Message constants for bean configuration wizards and dialogs.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public final class ConfigWizardsMessages extends NLS {

	private static final String FULLY_QUALIFIED_CLASS_NAME = "org.springframework.ide.eclipse.config.ui.wizards.ConfigWizardsMessages";

	public static String NamespaceConfig_title;

	public static String NamespaceConfig_xsdDescription;

	public static String NamespaceConfig_selectSpecificXsd;

	public static String NamespaceConfig_selectNamespace;

	public static String NamespaceConfig_default;

	public static String NamespaceConfig_windowTitle;

	public static String NamespaceConfig_mustIncludeDefault;

	static {
		NLS.initializeMessages(FULLY_QUALIFIED_CLASS_NAME, ConfigWizardsMessages.class);
	}

	private ConfigWizardsMessages() {
		// Do not instantiate
	}
}
