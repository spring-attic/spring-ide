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

package org.eclipse.gemini.blueprint.config.internal.adapter;

import org.springframework.beans.factory.FactoryBean;

/**
 * Simple adapter class used for maintaing configuration compatibility when
 * using <interface> parameter with classes instead of class names.
 * 
 * @author Costin Leau
 */
public class ToStringClassAdapter implements FactoryBean<String> {

	private final String toString;


	private ToStringClassAdapter(Object target) {
		if (target instanceof Class) {
			toString = ((Class<?>) target).getName();
		}
		else {
			toString = (target == null ? "" : target.toString());
		}
	}

	public String getObject() throws Exception {
		return toString;
	}

	public Class<? extends String> getObjectType() {
		return String.class;
	}

	public boolean isSingleton() {
		return true;
	}
}
