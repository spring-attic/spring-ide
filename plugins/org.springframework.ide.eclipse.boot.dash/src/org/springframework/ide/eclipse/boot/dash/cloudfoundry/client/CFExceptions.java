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

import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Static methods to recognize specific types of exceptions CF client
 * may throw.
 *
 * @author Kris De Volder
 */
public class CFExceptions {

	public static boolean isAuthFailure(Exception e) {
		//TODO: what about v2, how does it signal auth failure exactly? (This doesn't matter now,
		// because we hit it in V1, but it may matter once we completely get rid of V1. We will
		// then likely hit a similar exception in V2 at a later time, but it probably won't
		// be recognized as such.
		return false;
	}

	public static boolean isSSLCertificateFailure(Exception e) {
		Throwable cause = ExceptionUtil.getDeepestCause(e);
		return cause.getClass().getName().equals("sun.security.provider.certpath.SunCertPathBuilderException");
	}
}
