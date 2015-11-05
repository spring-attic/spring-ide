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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.util.internal.ReflectionUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Local utility class used by adapters. Handles things such as method discovery.
 * 
 * 
 * @author Costin Leau
 * 
 */
public abstract class CustomListenerAdapterUtils {

	private static final Log log = LogFactory.getLog(CustomListenerAdapterUtils.class);

	/**
	 * Specialised reflection utility that determines all methods that accept two parameters such:
	 * 
	 * <pre> methodName(Type serviceType, Type1 arg)
	 * 
	 * methodName(Type serviceType, Type2 arg)
	 * 
	 * methodName(AnotherType serviceType, Type1 arg)
	 * 
	 * methodName(Type serviceType) </pre>
	 * 
	 * It will return a map which has the serviceType (first argument) as type and contains as list the variants of
	 * methods using the second argument. This method is normally used by listeners when determining custom methods.
	 * 
	 * @param target
	 * @param methodName
	 * @param possibleArgumentTypes
	 * @param modifier
	 * @return
	 */
	static Map<Class<?>, List<Method>> determineCustomMethods(final Class<?> target, final String methodName,
			final Class<?>[] possibleArgumentTypes, final boolean onlyPublic) {

		if (!StringUtils.hasText(methodName)) {
			return Collections.emptyMap();
		}

		Assert.notEmpty(possibleArgumentTypes);

		if (System.getSecurityManager() != null) {
			return AccessController.doPrivileged(new PrivilegedAction<Map<Class<?>, List<Method>>>() {
				public Map<Class<?>, List<Method>> run() {
					return doDetermineCustomMethods(target, methodName, possibleArgumentTypes, onlyPublic);
				}
			});
		} else {
			return doDetermineCustomMethods(target, methodName, possibleArgumentTypes, onlyPublic);
		}
	}

	private static Map<Class<?>, List<Method>> doDetermineCustomMethods(final Class<?> target, final String methodName,
			final Class<?>[] possibleArgumentTypes, final boolean onlyPublic) {
		final Map<Class<?>, List<Method>> methods = new LinkedHashMap<Class<?>, List<Method>>(3);

		final boolean trace = log.isTraceEnabled();

		org.springframework.util.ReflectionUtils.doWithMethods(target,
				new org.springframework.util.ReflectionUtils.MethodCallback() {

					public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
						if (!method.isBridge() && methodName.equals(method.getName())) {
							if (onlyPublic && !Modifier.isPublic(method.getModifiers())) {
								if (trace)
									log.trace("Only public methods are considered; ignoring " + method);
								return;
							}
							// take a look at the variables
							Class<?>[] args = method.getParameterTypes();

							if (args != null) {
								// Properties can be ignored
								if (args.length == 1) {
									addMethod(args[0], method, methods);
								}
								// or passed as Map, Dictionary
								else if (args.length == 2) {
									Class<?> propType = args[1];

									for (int i = 0; i < possibleArgumentTypes.length; i++) {
										Class<?> clazz = possibleArgumentTypes[i];
										if (clazz.isAssignableFrom(propType)) {
											addMethod(args[0], method, methods);
										}
									}
								}
							}
						}
					}

					private void addMethod(Class<?> key, Method mt, Map<Class<?>, List<Method>> methods) {

						if (trace)
							log.trace("discovered custom method [" + mt.toString() + "] on " + target);

						List<Method> mts = methods.get(key);
						if (mts == null) {
							mts = new ArrayList<Method>(2);
							methods.put(key, mts);
							org.springframework.util.ReflectionUtils.makeAccessible(mt);
							mts.add(mt);
							return;
						}
						// add a method only if there is still space
						if (mts.size() == 1) {
							Method m = mts.get(0);
							if (m.getParameterTypes().length == mt.getParameterTypes().length) {
								if (trace)
									log.trace("Method w/ signature " + methodSignature(m)
											+ " has been already discovered; ignoring it");
							} else {
								org.springframework.util.ReflectionUtils.makeAccessible(mt);
								mts.add(mt);
							}
						}
					}

					private String methodSignature(Method m) {
						StringBuilder sb = new StringBuilder();
						int mod = m.getModifiers();
						if (mod != 0) {
							sb.append(Modifier.toString(mod) + " ");
						}
						sb.append(m.getReturnType() + " ");
						sb.append(m.getName() + "(");
						Class<?>[] params = m.getParameterTypes();
						for (int j = 0; j < params.length; j++) {
							sb.append(params[j]);
							if (j < (params.length - 1))
								sb.append(",");
						}
						sb.append(")");
						return sb.toString();
					}
				});

		return methods;
	}

	/**
	 * Shortcut method that uses as possible argument types, Dictionary.class, Map.class or even nothing.
	 * 
	 * @param target
	 * @param methodName
	 * @return
	 */
	static Map<Class<?>, List<Method>> determineCustomMethods(Class<?> target, final String methodName,
			boolean onlyPublic) {
		return determineCustomMethods(target, methodName, new Class[] { Dictionary.class, Map.class }, onlyPublic);
	}

	/**
	 * Invoke the custom listener method. Takes care of iterating through the method map (normally acquired through
	 * {@link #determineCustomMethods(Class, String, Class[])} and invoking the method using the arguments.
	 * 
	 * @param target
	 * @param methods
	 * @param service
	 * @param properties
	 */
	// the properties field is Dictionary implementing a Map interface
	static void invokeCustomMethods(Object target, Map<Class<?>, List<Method>> methods, Object service, Map properties) {
		if (methods != null && !methods.isEmpty()) {
			boolean trace = log.isTraceEnabled();

			Object[] argsWMap = new Object[] { service, properties };
			Object[] argsWOMap = new Object[] { service };
			for (Iterator<Map.Entry<Class<?>, List<Method>>> iter = methods.entrySet().iterator(); iter.hasNext();) {
				Map.Entry<Class<?>, List<Method>> entry = iter.next();
				Class<?> key = entry.getKey();
				// find the compatible types (accept null service)
				if (service == null || key.isInstance(service)) {
					List<Method> mts = entry.getValue();
					for (Method method : mts) {
						if (trace)
							log.trace("Invoking listener custom method " + method);

						Class<?>[] argTypes = method.getParameterTypes();
						Object[] arguments = (argTypes.length > 1 ? argsWMap : argsWOMap);
						try {
							ReflectionUtils.invokeMethod(method, target, arguments);
						}
						// make sure to log exceptions and continue with the
						// rest of the methods
						catch (Exception ex) {
							Exception cause = ReflectionUtils.getInvocationException(ex);
							log.warn("Custom method [" + method + "] threw exception when passing service ["
									+ ObjectUtils.identityToString(service) + "]", cause);
						}
					}
				}
			}
		}
	}
}