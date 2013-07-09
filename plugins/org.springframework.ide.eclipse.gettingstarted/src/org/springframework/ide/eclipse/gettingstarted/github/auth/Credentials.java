/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.github.auth;

import java.net.URLConnection;

import org.eclipse.swt.browser.Browser;
import org.springframework.web.client.RestTemplate;

/**
 * Credentials provide a means to authenticate for using the gihub rest API.
 * <p>
 * Github support two mechanims basic authentication and oauth. Separate
 * subclasses can be created to implement these mechanisms. For now
 * only basic auth will be implemented.
 * 
 * @author Kris De Volder
 */
public abstract class Credentials {

	/**
	 * Apply the credentials to a given RestTemplate instance. 
	 * <p>
	 * This returns a RestTemplate instance base on the target 
	 * instance adding some processing to authenticate with
	 * given Credentials.
	 * <p>
	 * This method may return a wrapped version of target
	 * RestTemplate, or it may mutate the original.
	 * <p>
	 * @return new (wrapped) RestTemplate or mutated original 
	 * RestTemplate.
	 */
	public abstract RestTemplate apply(RestTemplate rest);

	/**
	 * Add authentication headers to a url connection.
	 */
	public abstract void apply(URLConnection conn);

}
