/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.ssh;

import java.util.Arrays;
import java.util.Map;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CompositeApplicationOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.FullApplicationRestartOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.Operation;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class SshDebugSupport extends DebugSupport {

	private static final int REMOTE_DEBUG_PORT = 47822;
	private static final String REMOTE_DEBUG_JVM_ARGS = "-Xdebug -Xrunjdwp:server=y,transport=dt_socket,suspend=n,address="+REMOTE_DEBUG_PORT;
	private static final String JAVA_OPTS = "JAVA_OPTS";

	public SshDebugSupport(CloudDashElement app) {
		super(app);
	}

	public boolean isSupported() {
		CloudFoundryRunTarget target = app.getTarget();
		//TODO: only on PWS for now, but this can be broadened. How do we know/determine when it is supported?
		// Probably a combination of:
		//   PCF version >= 1.6 and using the java client to ask whether diego is enabled and whether ssh support is enabled.
		return target.isPWS();
	}

	@Override
	public String getNotSupportedMessage() {
		return "SSH debugging is only supported on PWS";
	}

	@Override
	public Operation<?> createOperation(String opName, UserInteractions ui) {
		CloudFoundryBootDashModel cloudModel = app.getCloudModel();
		return new CompositeApplicationOperation(opName, cloudModel, app.getName(),
				Arrays.asList(new CloudApplicationOperation[] {
						new FullApplicationRestartOperation(opName, cloudModel, app.getName(), RunState.DEBUGGING, this, ui),
						new SshDebugStartOperation(app, this)
				}),
				RunState.STARTING
		);
	}

	@Override
	public void setupEnvVars(Map<String, String> env) {
		String javaOpts = clearJavaOpts(env.get(JAVA_OPTS));
		StringBuilder sb = new StringBuilder(javaOpts);
		if (sb.length() > 0) {
			sb.append(' ');
		}
		sb.append(REMOTE_DEBUG_JVM_ARGS);
		env.put(JAVA_OPTS, sb.toString());

	}

	private static String clearJavaOpts(String opts) {
		if (opts!=null) {
			opts = opts.replaceAll(REMOTE_DEBUG_JVM_ARGS + "\\s*", "");
		}
		return "";
	}


	@Override
	public void clearEnvVars(Map<String, String> env) {
		env.put(JAVA_OPTS, clearJavaOpts(env.get(JAVA_OPTS)));
	}

	public int getRemotePort() {
		return REMOTE_DEBUG_PORT;
	}

}
