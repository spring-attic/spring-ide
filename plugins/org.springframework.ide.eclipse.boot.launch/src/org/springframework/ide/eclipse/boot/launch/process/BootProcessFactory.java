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
package org.springframework.ide.eclipse.boot.launch.process;

import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;

public class BootProcessFactory implements IProcessFactory {

	/**
	 * Method only available on Java 8.
	 */
	public static Method destroyForcibly;
	static {
		try {
			destroyForcibly = Process.class.getMethod("destroyForcibly");
		} catch (Exception e) {
			destroyForcibly = null;
		}
	}

	@Override
	public IProcess newProcess(ILaunch launch, Process process, String label,
			Map<String, String> attributes) {

		if (destroyForcibly!=null) {
			return new RuntimeProcess(launch, process, label, attributes) {
				@Override
				public void terminate() throws DebugException {
					Process sysproc = getSystemProcess();
					if (sysproc!=null) {
						//Superclass 'terminate' method is unreliable so...
						try {
							super.terminate();
						} catch (DebugException de) {
							try {
								//Failed so be more aggressive
								destroyForcibly.invoke(sysproc);
								boolean done = false;
								while (!done) {
									try {
										sysproc.waitFor();
										done = true;
									} catch (InterruptedException ie) {
										//ignore
									}
								}
							} catch (Exception e) {
								//Something unexpected went wrong trying to call 'sysproc.destroyForcibly' or 'sysproc.waitFor'
								throw de;
							}
						}
					}
				}
			};
		} else {
			//Not Java 8 so can't use the destroyForcibly to force faster process termination,
			return new RuntimeProcess(launch, process, label, attributes);
		}
	}

}
