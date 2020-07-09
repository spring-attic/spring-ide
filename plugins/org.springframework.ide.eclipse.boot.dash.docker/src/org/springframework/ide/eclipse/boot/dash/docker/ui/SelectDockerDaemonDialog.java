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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.springsource.ide.eclipse.commons.core.util.OsUtils;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.CheckboxSection;
import org.springsource.ide.eclipse.commons.livexp.ui.DialogWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.OkButtonHandler;
import org.springsource.ide.eclipse.commons.livexp.ui.StringFieldSection;
import org.springsource.ide.eclipse.commons.livexp.ui.WizardPageSection;

import com.google.common.collect.ImmutableList;

public class SelectDockerDaemonDialog extends DialogWithSections {
	
	private Model model;

	public static class Model implements OkButtonHandler {
		private static final String DEFAULT_UNIX_DOCKER_URL = "unix:///var/run/docker.sock";
		private static final String DEFAUL_WINDOWS_DOCKER_URL = "https://localhost:2375";
		
		public final LiveVariable<Boolean> useLocalDaemon = new LiveVariable<>(true);
		public final LiveExpression<Boolean> daemonUrlEnabled = useLocalDaemon.apply(local -> !local);
		public final LiveVariable<String> daemonUrl = new LiveVariable<>(getDefaultDaemonUrl());
		public final LiveVariable<Boolean> okPressed = new LiveVariable<>(false);
		
		{
			daemonUrlEnabled.onChange((e,v) -> {
				if (useLocalDaemon.getValue()) {
					daemonUrl.setValue(getDefaultDaemonUrl());
				}
			});
		}

		@Override
		public void performOk() throws Exception {
			okPressed.setValue(true);
		}

		private String getDefaultDaemonUrl() {
			if (OsUtils.isWindows()) {
				return DEFAUL_WINDOWS_DOCKER_URL;
			} else {
				return DEFAULT_UNIX_DOCKER_URL;
			}
		}
	}

	public SelectDockerDaemonDialog(Model model, Shell shell) {
		super("Connect to Docker Daemon", model, shell);
		this.model = model;
	}
	
	@SuppressWarnings("resource") @Override
	protected List<WizardPageSection> createSections() throws CoreException {
		StringFieldSection sf = new StringFieldSection(this, "Url", model.daemonUrl);
		return ImmutableList.of(
				new CheckboxSection(this, model.useLocalDaemon, "Use Local Daemon"), 
				new StringFieldSection(this, "Url", model.daemonUrl).setEnabler(model.daemonUrlEnabled)
		);
	}
}
