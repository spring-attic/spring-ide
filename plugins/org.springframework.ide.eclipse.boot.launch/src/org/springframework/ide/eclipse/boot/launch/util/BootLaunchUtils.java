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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

/**
 * @author Kris De Volder
 */
public class BootLaunchUtils {

	/**
	 * Boot aware launch termination. Tries to use JMX lifecycle managment bean if available.
	 * <p>
	 * Note that termination may be asynchronous. Callers should not assume processes are already terminated
	 * when this method returns.
	 */
	public static void terminate(List<ILaunch> launches) {
		for (ILaunch l : launches) {
			terminate(l);
		}
	}

	/**
	 * Boot aware launch termination. Tries to use JMX lifecycle managment bean if available.
	 * <p>
	 * Note that termination may be asynchronous. Callers should not assume processes are already terminated
	 * when this method returns.
	 */
	public static void terminate(ILaunch l) {
		ILaunchConfiguration conf = l.getLaunchConfiguration();
		try {
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
						return; //Success (well, at least we asked the app to terminate)
					}
				} finally {
					clientMgr.disposeClient();
				}
			}
			// Fallback to default implementation if client not available.
			l.terminate();
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

}
