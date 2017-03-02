/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.cloud.cli;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * <p>
 * Launch process created for Spring Cloud CLI local cloud service. Launch
 * creates 3 process:
 * <p>
 * <ul>
 * <li>bash Boot CLI process</li>
 * <li>Spring Cloud Deployer process forked by Boot CLI</li>
 * <li>Local cloud service Spring Boot App forked by the deployer</li>
 * </ul>
 *
 * <p>
 * Killing Spring Cloud Deployer process would kill the cloud service process.
 * Killing Boot CLI process doesn't kill the the Deployer and cloud service
 * processes.
 * </p>
 *
 * <p>
 * Deployer process PID is retrieved from the log, Then OS specific command is
 * executed to kill the deployer process
 * </p>
 *
 * @author Alex Boyko
 *
 */
class CloudCliProcess extends RuntimeProcess {

	private static final Pattern PID_PATTERN = Pattern.compile(".*\\s+INFO\\s+(\\d+)\\s+");

	private int delegateAppPid = -1;

	public CloudCliProcess(ILaunch launch, Process process, String name, Map<String, String> attributes) {
		super(launch, process, name, attributes);
		getStreamsProxy().getOutputStreamMonitor().addListener(new IStreamListener() {

			@Override
			public void streamAppended(String text, IStreamMonitor monitor) {
				for (String line : text.split("\n")) {
					Matcher matcher = PID_PATTERN.matcher(line);
					if (matcher.find()) {
						delegateAppPid = Integer.valueOf(matcher.group(1));
						monitor.removeListener(this);
						return;
					}
				}
			}

		});
	}

	@Override
	public void terminate() throws DebugException {
		if (delegateAppPid != -1) {
			// Terminate Deployer process. If successful the bash process would be terminated automatically.
			try {
				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					Runtime.getRuntime().exec("taskkill /pid " + delegateAppPid + " /t /f").waitFor(500, TimeUnit.MILLISECONDS);
				} else {
					Runtime.getRuntime().exec("kill -SIGINT " + delegateAppPid).waitFor(500, TimeUnit.MILLISECONDS);
				}
			} catch (IOException | InterruptedException e) {
				Log.log(ExceptionUtil.coreException(String.format("Spring Cloud CLI Deployer process with PID: %d cannot be killed. Consider stopping it manually", delegateAppPid), e));
			}
		} else {
			// No pid of the Deployer process? Not good... User may need to kill this process manually.
			Log.log(ExceptionUtil.coreException("Spring Cloud CLI Deployer process PID is undefined. Consider stopping it manually"));
		}
		super.terminate();
	}


}
