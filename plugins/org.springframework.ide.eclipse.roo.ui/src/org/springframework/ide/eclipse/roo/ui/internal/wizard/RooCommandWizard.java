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
package org.springframework.ide.eclipse.roo.ui.internal.wizard;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.roo.core.commands.RooCommandUtils;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellTab;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.IFrameworkCommand;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.wizard.GenericCommandWizard;


/**
 * Roo Command Wizard entry point.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class RooCommandWizard extends GenericCommandWizard {

	public static final String WIZARD_TITLE = "Roo Command Wizard";
	public static final String WIZARD_IMAGE_LOCATION = "platform:/plugin/org.springframework.ide.eclipse.roo.ui/icons/full/wizban/roo_wizban.png";

	private RooShellTab tab;
	
	public RooCommandWizard(Collection<IProject> projects,
			IFrameworkCommand command, RooShellTab tab) {
		super(command, WIZARD_TITLE, null, WIZARD_IMAGE_LOCATION, projects);
		this.tab = tab;
	}
	
	@Override
	protected void executeCommand(IFrameworkCommand command) {
		tab.executeCommand(RooCommandUtils.constructCommandString(command));
	}
}
