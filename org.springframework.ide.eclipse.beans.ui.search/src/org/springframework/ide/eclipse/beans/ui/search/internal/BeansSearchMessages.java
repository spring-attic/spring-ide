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

package org.springframework.ide.eclipse.beans.ui.search.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @author Torsten Juergeleit
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
