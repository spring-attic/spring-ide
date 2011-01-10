/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.search.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public final class BeansSearchMessages extends NLS {

	private static final String BUNDLE_NAME = "org.springframework.ide." +
						"eclipse.beans.ui.search.internal.BeansSearchMessages";
	private BeansSearchMessages() {
		// Do not instantiate
	}

	public static String SearchPage_expression;
	public static String SearchPage_expressionHint;
	public static String SearchPage_caseSensitive;
	public static String SearchPage_regularExpression;
	public static String SearchPage_searchFor;
	public static String SearchPage_searchFor_name;
	public static String SearchPage_searchFor_reference;
	public static String SearchPage_searchFor_class;
	public static String SearchPage_searchFor_child;
	public static String SearchPage_searchFor_property;

	public static String SearchScope_workspace;
	public static String SearchScope_selection;
	public static String SearchScope_workingSets;
	public static String SearchScope_selectedProjects;

	public static String SearchQuery_status;
	public static String SearchQuery_searchFor_name;
	public static String SearchQuery_searchFor_reference;
	public static String SearchQuery_searchFor_class;
	public static String SearchQuery_searchFor_child;
	public static String SearchQuery_searchFor_property;

	static {
		NLS.initializeMessages(BUNDLE_NAME, BeansSearchMessages.class);
	}
}
