/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.tests;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.NewSpringBootWizardModel;

public class ZipUrlImportWizardTest extends TestCase {
	
	public void testImportBasic() throws Exception {
		NewSpringBootWizardModel wizard = new NewSpringBootWizardModel();
		wizard.allowUIThread(true);
		wizard.performFinish(new NullProgressMonitor());
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(wizard.getProjectName().getValue());
		assertTrue(p.isAccessible());
	}

}
