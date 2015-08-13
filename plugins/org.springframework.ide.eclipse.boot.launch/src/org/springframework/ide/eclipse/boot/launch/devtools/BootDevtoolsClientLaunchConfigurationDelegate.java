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
package org.springframework.ide.eclipse.boot.launch.devtools;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.launch.AbstractBootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.util.StringUtil;

public class BootDevtoolsClientLaunchConfigurationDelegate extends AbstractBootLaunchConfigurationDelegate {

	public static final String REMOTE_SPRING_APPLICATION = "org.springframework.boot.devtools.RemoteSpringApplication";
	public static final String REMOTE_URL = "spring.devtools.remote.url";
	public static final String REMOTE_SECRET = "spring.devtools.remote.secret";
	public static final String DEFAULT_REMOTE_SECRET = "";

	@Override
	public String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		return REMOTE_SPRING_APPLICATION;
	}

	@Override
	public String getProgramArguments(ILaunchConfiguration conf) throws CoreException {
		List<PropVal> props = getProperties(conf);
		ArrayList<String> args = new ArrayList<String>();
		addPropertiesArguments(args, props);
		String secret = getSecret(conf);
		if (StringUtil.hasText(secret)) {
			args.add(propertyAssignmentArgument(REMOTE_SECRET, secret));
		}
		args.add(getRemoteUrl(conf));
		return DebugPlugin.renderArguments(args.toArray(new String[args.size()]), null);
	}

	private String getSecret(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(REMOTE_SECRET, DEFAULT_REMOTE_SECRET);
		} catch (CoreException e) {
			BootActivator.log(e);
		}
		return "";
	}

	public static String getRemoteUrl(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(REMOTE_URL, (String)null);
		} catch (CoreException e) {
			BootActivator.log(e);
		}
		return null;
	}

	public static void setRemoteUrl(ILaunchConfigurationWorkingCopy conf, String value) {
		conf.setAttribute(REMOTE_URL, value);
	}

}
