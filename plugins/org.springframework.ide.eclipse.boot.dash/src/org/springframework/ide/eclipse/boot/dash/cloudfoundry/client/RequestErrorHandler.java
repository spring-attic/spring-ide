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

public class RequestErrorHandler {

	/**
	 *
	 * @param e
	 * @return true if request error should be treated as an error. False error
	 *         should be ignored.
	 */
	public boolean handleError(Throwable e) {
		return true;
	}

}
