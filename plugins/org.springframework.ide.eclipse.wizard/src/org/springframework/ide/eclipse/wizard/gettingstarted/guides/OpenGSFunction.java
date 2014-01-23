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
package org.springframework.ide.eclipse.wizard.gettingstarted.guides;

import org.eclipse.ui.PlatformUI;
import org.springsource.ide.eclipse.commons.javafx.browser.IJavaFxBrowserFunction;

public class OpenGSFunction implements IJavaFxBrowserFunction {

	public void call(String argument) {
		GSImportWizard.open(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), argument);
	}

}
