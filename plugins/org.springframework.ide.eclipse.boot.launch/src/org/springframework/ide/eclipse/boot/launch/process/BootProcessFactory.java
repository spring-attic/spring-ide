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

import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;

public class BootProcessFactory implements IProcessFactory {

	@Override
	public IProcess newProcess(ILaunch launch, Process process, String label,
			Map<String, String> attributes) {
		return new RuntimeProcess(launch, process, label, attributes) {
			@Override
			public void terminate() throws DebugException {
				Process sysproc = getSystemProcess();
				if (sysproc!=null) {
					//Superclass 'terminate' method is unreliable so...
					try {
						super.terminate();
					} catch (DebugException de) {
						//Failed so be more aggressive
						sysproc.destroyForcibly();
						boolean done = false;
						while (!done) {
							try {
								sysproc.waitFor();
								done = true;
							} catch (InterruptedException ie) {
								//ignore
							}
						}
					}
				}
			}
		};
	}

}
