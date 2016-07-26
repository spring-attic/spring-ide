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
package org.springframework.ide.eclipse.cft;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.dash.views.DefaultUserInteractions;

public class CFTIntegrationUserInteractions extends DefaultUserInteractions {

	public CFTIntegrationUserInteractions() {
		super(getDefaultContext());
	}

	public static UIContext getDefaultContext() {
		return new UIContext() {

			@Override
			public Shell getShell() {
				final Shell[] shell = new Shell[1];
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						shell[0] = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
					}
				});
				return shell[0];
			}

		};
	}

}
