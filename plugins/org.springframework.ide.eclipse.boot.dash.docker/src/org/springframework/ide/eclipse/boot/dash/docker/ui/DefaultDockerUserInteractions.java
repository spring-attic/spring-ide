/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.docker.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.docker.ui.SelectDockerDaemonDialog.Model;
import org.springframework.ide.eclipse.boot.dash.views.DefaultUserInteractions.UIContext;

public class DefaultDockerUserInteractions implements DockerUserInteractions {

	private SimpleDIContext injections;

	public DefaultDockerUserInteractions(SimpleDIContext injections) {
		this.injections = injections;}

	@Override
	public void selectDockerDaemonDialog(Model model) {
		Display.getDefault().syncExec(() -> new SelectDockerDaemonDialog(model, getShell()).open());
	}

	private Shell getShell() {
		return injections.getBean(UIContext.class).getShell();
	}
	
}
