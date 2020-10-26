/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.preferences;

import org.springframework.ide.eclipse.editor.support.preferences.ProblemSeverityPreferencesUtil;

public class PreferenceConstants {

	public static final String AUTO_CONFIGURE_APT_M2E_PREF = "org.springframework.ide.eclipse.apt.autoconfigure.m2e";
	public static final boolean AUTO_CONFIGURE_APT_M2E_DEFAULT = true;
	public static final String AUTO_CONFIGURE_APT_GRADLE_PREF = "org.springframework.ide.eclipse.apt.autoconfigure.gradle";
	public static final boolean AUTO_CONFIGURE_APT_GRADLE_DEFAULT = true;

	public static final ProblemSeverityPreferencesUtil severityUtils = new ProblemSeverityPreferencesUtil("spring.properties.editor.problem.");

}
