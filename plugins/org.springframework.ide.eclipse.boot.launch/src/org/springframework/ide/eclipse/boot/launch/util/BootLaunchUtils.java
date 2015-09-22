/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.util;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

import static org.springsource.ide.eclipse.commons.ui.launch.LaunchUtils.*;

/**
 * @author Kris De Volder
 */
public class BootLaunchUtils {

	/**
	 * Boot aware launch termination. Tries to use JMX lifecycle managment bean if available.
	 */
	public static void terminate(List<ILaunch> launches) {
		//TODO: this terminates launches sequentially. It would be better to try to terminate all of them
		// in parallel and then wait for completion of each operation.
		for (ILaunch l : launches) {
			try {
				terminate(l);
			} catch (Exception e) {
				BootActivator.log(e);
			}
		}
	}

	/**
	 * Boot aware launch termination. Tries to use JMX lifecycle managment bean if available.
	 */
	public static void terminate(ILaunch l) throws DebugException, CoreException {
		ILaunchConfiguration conf = l.getLaunchConfiguration();
//		try {
			if (conf!=null
					&& conf.getType().getIdentifier().equals(BootLaunchConfigurationDelegate.TYPE_ID)
					&& BootLaunchConfigurationDelegate.canUseLifeCycle(conf)
			) {
				int jmxPort = BootLaunchConfigurationDelegate.getJMXPortAsInt(conf);
				SpringApplicationLifeCycleClientManager clientMgr = new SpringApplicationLifeCycleClientManager(jmxPort);
				SpringApplicationLifecycleClient client = clientMgr.getLifeCycleClient();
				try {
					if (client!=null) {
						client.stop();
						try {
							whenTerminated(l).get(BootLaunchConfigurationDelegate.getTerminationTimeoutAsLong(l), TimeUnit.MILLISECONDS);
							return; //Success
						} catch (TimeoutException e) {
							//ignore... allows fallback below to kick in
						}
					}
				} catch (Exception e) {
					//Nice termination failed. We'll log exception and allow fallback to kick in.
					BootActivator.log(e);
				} finally {
					clientMgr.disposeClient();
				}
			}
			// Fallback to default implementation if client not available.
			l.terminate();
//		} catch (Exception e) {
//			BootActivator.log(e);
//		}
	}

}
