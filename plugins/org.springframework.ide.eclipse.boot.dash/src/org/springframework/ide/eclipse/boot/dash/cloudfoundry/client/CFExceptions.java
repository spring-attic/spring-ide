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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client;

import org.cloudfoundry.client.lib.CloudFoundryException;

public class CFExceptions {

	public static boolean isAuthFailure(Exception e) {
		if (e instanceof CloudFoundryException) {
			//for v1
			return e.getMessage().contains("403");
		}
		//TODO: what about v2, how does it signal auth failure exactly?
		return false;
	}

}
