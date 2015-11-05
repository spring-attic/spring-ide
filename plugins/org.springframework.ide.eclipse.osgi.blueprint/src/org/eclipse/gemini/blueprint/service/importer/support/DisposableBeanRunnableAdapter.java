/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.service.importer.support;

import org.springframework.beans.factory.DisposableBean;

/**
 * Simple adapter around a Spring disposable bean.
 * 
 * @author Costin Leau
 * 
 */
class DisposableBeanRunnableAdapter implements Runnable {

	private final DisposableBean bean;


	/**
	 * Constructs a new <code>DisposableBeanRunnableAdapter</code> instance.
	 * 
	 * @param bean
	 */
	public DisposableBeanRunnableAdapter(DisposableBean bean) {
		this.bean = bean;
	}

	public void run() {
		try {
			bean.destroy();
		}
		catch (Exception ex) {
			if (ex instanceof RuntimeException)
				throw (RuntimeException) ex;
			else
				throw new RuntimeException(ex);
		}
	}
}
