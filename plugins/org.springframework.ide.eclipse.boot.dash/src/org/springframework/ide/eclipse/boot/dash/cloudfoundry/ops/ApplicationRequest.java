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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public abstract class ApplicationRequest<T> extends ClientRequest<T> {

	public ApplicationRequest(CloudFoundryBootDashModel model, String appName) {
		super(model, appName, "Getting application " + appName);
		addRequestListener(new ApplicationInstanceErrorListener(this));
	}

	class ApplicationInstanceErrorListener extends RequestErrorListener {

		public ApplicationInstanceErrorListener(ClientRequest<?> request) {
			super(request);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.
		 * RequestErrorListener#shouldIgnoreError(java.lang.Exception)
		 */
		protected boolean shouldIgnoreError(Exception exception) {
			return CloudErrors.is503Error(exception) || CloudErrors.isBadRequest(exception)
					|| CloudErrors.isNotFoundException(exception);
		}

	}

}
