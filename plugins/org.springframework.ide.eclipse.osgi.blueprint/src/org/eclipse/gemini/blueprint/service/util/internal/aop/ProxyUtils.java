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

package org.eclipse.gemini.blueprint.service.util.internal.aop;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.eclipse.gemini.blueprint.util.DebugUtils;
import org.eclipse.gemini.blueprint.util.internal.ClassUtils;
import org.osgi.framework.BundleContext;
import org.springframework.aop.framework.ProxyFactory;

/**
 * Simple utility for creating Spring AOP proxies.
 * 
 * @author Costin Leau
 * 
 */
public abstract class ProxyUtils {

	public static Object createProxy(Class<?>[] classes, Object target, ClassLoader classLoader,
			BundleContext bundleContext, List advices) {
		return createProxy(classes, target, classLoader, bundleContext, (advices != null ? (Advice[]) advices
				.toArray(new Advice[advices.size()]) : new Advice[0]));
	}

	public static Object createProxy(Class<?>[] classes, Object target, final ClassLoader classLoader,
			BundleContext bundleContext, Advice[] advices) {
		final ProxyFactory factory = new ProxyFactory();

		ClassUtils.configureFactoryForClass(factory, classes);

		for (int i = 0; i < advices.length; i++) {
			factory.addAdvice(advices[i]);
		}

		if (target != null)
			factory.setTarget(target);

		// no need to add optimize since it means implicit usage of CGLib always
		// which is determined automatically anyway
		// factory.setOptimize(true);
		factory.setFrozen(true);
		factory.setOpaque(true);
		boolean isSecurityOn = (System.getSecurityManager() != null);
		try {
			if (isSecurityOn) {
				return AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						return factory.getProxy(classLoader);
					}
				});
			} else {
				return factory.getProxy(classLoader);
			}

		} catch (NoClassDefFoundError ncdfe) {
			DebugUtils.debugClassLoadingThrowable(ncdfe, bundleContext.getBundle(), classes);
			throw ncdfe;
		}
	}
}