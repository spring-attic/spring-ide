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
package org.springframework.ide.eclipse.aop.ui.matcher.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @author Christian Dupuis
 * @since 2.0.2
 */
public final class PointcutMatcherMessages extends NLS {

	private static final String BUNDLE_NAME = "org.springframework.ide." +
			"eclipse.aop.ui.matcher.internal.PointcutMatcherMessages";
	public static String MatcherPage_expression;

	public static String MatcherPage_expressionHint;
	public static String MatcherPage_proxyTargetClass;
	public static String MatcherScope_workspace;

	public static String MatcherScope_selection;
	public static String MatcherScope_workingSets;
	public static String MatcherScope_selectedProjects;
	public static String MatcherQuery_status;

	public static String MatcherQuery_label;
	public static String MatcherResult_label;

	static {
		NLS.initializeMessages(BUNDLE_NAME, PointcutMatcherMessages.class);
	}
	
	private PointcutMatcherMessages() {
		// Do not instantiate
	}
}
