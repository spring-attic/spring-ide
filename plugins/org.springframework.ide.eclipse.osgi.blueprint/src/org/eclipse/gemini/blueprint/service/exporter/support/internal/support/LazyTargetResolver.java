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

package org.eclipse.gemini.blueprint.service.exporter.support.internal.support;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.springframework.beans.factory.BeanFactory;

/**
 * Class encapsulating the lazy dependency lookup of the target bean. Handles caching as well as the lazy listener
 * notifications.
 * 
 * @author Costin Leau
 */
public class LazyTargetResolver implements UnregistrationNotifier {

	private final BeanFactory beanFactory;
	private final String beanName;
	private final boolean cacheService;
	private volatile Object target;
	private final Object lock = new Object();
	private final AtomicBoolean activated;
	private final ListenerNotifier notifier;
	private volatile ServiceRegistrationDecorator decorator;

	public LazyTargetResolver(Object target, BeanFactory beanFactory, String beanName, boolean cacheService,
			ListenerNotifier notifier, boolean lazyListeners) {
		this.target = target;
		this.beanFactory = beanFactory;
		this.beanName = beanName;
		this.cacheService = cacheService;
		this.notifier = notifier;
		this.activated = new AtomicBoolean(!lazyListeners);
	}

	public void activate() {
		if (activated.compareAndSet(false, true) && notifier != null) {
			// no service registered
			if (decorator == null) {
				notifier.callUnregister(null, null);
			} else {
				Object target = getBeanIfPossible();
				Map properties = (Map) OsgiServiceReferenceUtils.getServicePropertiesSnapshot(decorator.getReference());
				notifier.callRegister(target, properties);
			}
		}
	}

	private Object getBeanIfPossible() {
		if (target == null) {
			if (cacheService || beanFactory.isSingleton(beanName)) {
				getBean();
			}
		}

		return target;
	}

	public Object getBean() {
		if (target == null) {
			if (cacheService) {
				synchronized (lock) {
					if (target == null) {
						target = beanFactory.getBean(beanName);
					}
				}
			} else {
				return beanFactory.getBean(beanName);
			}
		}
		return target;
	}

	public Class<?> getType() {
		return (target == null ? (beanFactory.isSingleton(beanName) ? beanFactory.getBean(beanName).getClass()
				: beanFactory.getType(beanName)) : target.getClass());

	}

	public void unregister(Map properties) {
		if (activated.get() && notifier != null) {
			Object target = getBeanIfPossible();
			notifier.callUnregister(target, properties);
		}
	}

	public void setDecorator(ServiceRegistrationDecorator decorator) {
		this.decorator = decorator;
		if (decorator != null) {
			decorator.setNotifier(this);
		}
	}

	public void notifyIfPossible() {
		if (activated.get() && notifier != null) {
			Object target = getBeanIfPossible();
			Map properties = (Map) OsgiServiceReferenceUtils.getServicePropertiesSnapshot(decorator.getReference());
			notifier.callRegister(target, properties);
		}
	}

	// called when the exporter is activated but no service is published
	public void startupUnregisterIfPossible() {
		if (activated.get() && notifier != null) {
			notifier.callUnregister(null, null);
		}
	}
}

