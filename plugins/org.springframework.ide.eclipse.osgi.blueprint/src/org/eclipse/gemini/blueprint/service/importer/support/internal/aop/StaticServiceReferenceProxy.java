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

package org.eclipse.gemini.blueprint.service.importer.support.internal.aop;

import org.eclipse.gemini.blueprint.service.importer.ServiceReferenceProxy;
import org.eclipse.gemini.blueprint.service.importer.support.internal.util.ServiceComparatorUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.util.Assert;

/**
 * Simple {@link ServiceReference} proxy which simply does delegation, without any extra features. It's main purpose is
 * to allow the consistent behaviour between dynamic and static proxies.
 * 
 * @author Costin Leau
 * 
 */
public class StaticServiceReferenceProxy implements ServiceReferenceProxy {

	private static final int HASH_CODE = StaticServiceReferenceProxy.class.hashCode() * 13;

	private final ServiceReference target;

	/**
	 * Constructs a new <code>StaticServiceReferenceProxy</code> instance.
	 * 
	 * @param target service reference
	 */
	public StaticServiceReferenceProxy(ServiceReference target) {
		Assert.notNull(target);
		this.target = target;
	}

	public Bundle getBundle() {
		return target.getBundle();
	}

	public Object getProperty(String key) {
		return target.getProperty(key);
	}

	public String[] getPropertyKeys() {
		return target.getPropertyKeys();
	}

	public Bundle[] getUsingBundles() {
		return target.getUsingBundles();
	}

	public boolean isAssignableTo(Bundle bundle, String className) {
		return target.isAssignableTo(bundle, className);
	}

	public ServiceReference getTargetServiceReference() {
		return target;
	}

	public boolean equals(Object obj) {
		if (obj instanceof StaticServiceReferenceProxy) {
			StaticServiceReferenceProxy other = (StaticServiceReferenceProxy) obj;
			return (target.equals(other.target));
		}
		return false;
	}

	public int hashCode() {
		return HASH_CODE + target.hashCode();
	}

	public int compareTo(Object other) {
		return ServiceComparatorUtil.compare(target, other);
	}
}