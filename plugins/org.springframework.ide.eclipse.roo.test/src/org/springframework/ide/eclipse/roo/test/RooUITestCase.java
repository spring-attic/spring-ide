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
package org.springframework.ide.eclipse.roo.test;

import static org.springsource.ide.eclipse.commons.frameworks.test.util.SWTBotUtils.openPerspective;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ResourceExists;
import org.springsource.ide.eclipse.commons.frameworks.test.util.SWTBotUtils;
import org.springsource.ide.eclipse.commons.frameworks.test.util.UITestCase;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;


/**
 * @author Kris De Volder
 */
public abstract class RooUITestCase extends UITestCase {

	private static final long TIMEOUT_CREATE_APP = 60000;

	@Override
	public void setupClass() throws Exception {
		super.setupClass();
		openPerspective(bot, "Spring");
	}
	
	protected void createRooProject(String projectName, String pkgPrefix) throws CoreException {

		System.out.println("Create Roo project:" + projectName);
		System.out.println("    package prefix:" + pkgPrefix);
		
		SWTBotShell wizard = activateFileNewWizardShell("Roo Project", "New Roo Project");
		
		SWTBotText projectNameTxt = bot.textWithLabel("Project name:");
		projectNameTxt.setText(projectName);
		
		SWTBotText pkgPrefixTxt = bot.textWithLabel("Top level package name:");
		pkgPrefixTxt.setText(pkgPrefix);
		
		bot.button("Next >").click();
		bot.button("Finish").click();
		
		// May take a while so long timeout value needed
		bot.waitUntil(Conditions.shellCloses(wizard), TIMEOUT_CREATE_APP);
		bot.waitUntil(new ResourceExists(projectName), TIMEOUT_CREATE_APP);
		System.out.println("Resources created... Waiting for build.");
		StsTestUtil.waitForAutoBuild(); // New resources will cause auto
										// rebuilds.
		System.out.println("Build finished!");
	}

	
}
