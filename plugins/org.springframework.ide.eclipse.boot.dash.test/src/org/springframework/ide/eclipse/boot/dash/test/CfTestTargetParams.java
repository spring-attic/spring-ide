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
package org.springframework.ide.eclipse.boot.dash.test;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.core.StringUtils;

/**
 * @author Kris De Volder
 */
public class CfTestTargetParams {

	public static CFClientParams fromEnv() {
		return new CFClientParams(
				fromEnv("CF_TEST_API_URL"),
				fromEnv("CF_TEST_USER"),
				fromEnv("CF_TEST_PASSWORD"),
				false, //self signed
				fromEnv("CF_TEST_ORG"),
				fromEnv("CF_TEST_SPACE")
		);
	}

	private static String fromEnv(String name) {
		String value = System.getenv(name);
		Assert.isLegal(StringUtils.hasText(value), "The environment varable '"+name+"' must be set");
		return value;
	}
}
