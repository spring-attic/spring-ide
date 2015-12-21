/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class CloudFoundryUiUtil {



	public static Shell getShell() {
		final Shell[] shell = new Shell[1];
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				shell[0] = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
			}
		});
		return shell[0];
	}
}
