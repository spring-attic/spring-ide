/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import org.springframework.ide.eclipse.boot.launch.util.CheckboxSection;
import org.springframework.ide.eclipse.boot.launch.util.DelegatingLaunchConfigurationTabSection;
import org.springsource.ide.eclipse.commons.livexp.ui.IPageWithSections;

/**
 * Section for hosting UI for enabling/disabling the ANSI console output
 * 
 * @author Alex Boyko
 *
 */
public class EnableAnsiConsoleOutput extends DelegatingLaunchConfigurationTabSection {

	public EnableAnsiConsoleOutput(IPageWithSections owner, LaunchTabSelectionModel<Boolean> model) {
		super(owner, model, new CheckboxSection(owner, model, "Enable ANSI console output"));
	}
	
}
