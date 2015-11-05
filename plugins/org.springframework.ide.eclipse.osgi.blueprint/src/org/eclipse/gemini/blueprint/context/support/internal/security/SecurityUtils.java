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

package org.eclipse.gemini.blueprint.context.support.internal.security;

import java.security.AccessControlContext;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author Costin Leau
 */
public abstract class SecurityUtils {

	public static AccessControlContext getAccFrom(BeanFactory beanFactory) {
		AccessControlContext acc = null;
		if (beanFactory != null) {
			if (beanFactory instanceof ConfigurableBeanFactory) {
				return ((ConfigurableBeanFactory) beanFactory).getAccessControlContext();
			}
		}
		return acc;
	}

	public static AccessControlContext getAccFrom(ApplicationContext ac) {
		return (ac != null ? getAccFrom(ac.getAutowireCapableBeanFactory()) : null);
	}
}
