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

import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Container class encapsulating ServiceReference specific operations. Note that this class is highly tied to the
 * behaviour of {@link ServiceDynamicInterceptor} and should not be used elsewhere.
 * 
 * @author Costin Leau
 */
class ReferenceHolder {
	private final ServiceReference reference;
	private final BundleContext bundleContext;
	private final long id;
	private final int ranking;

	private volatile Object service;

	public ReferenceHolder(ServiceReference reference, BundleContext bundleContext) {
		this.reference = reference;
		this.bundleContext = bundleContext;
		id = OsgiServiceReferenceUtils.getServiceId(reference);
		ranking = OsgiServiceReferenceUtils.getServiceRanking(reference);
	}

	public Object getService() {
		if (service != null) {
			return service;
		}
		if (reference != null) {
			service = bundleContext.getService(reference);
			return service;
		}

		return null;
	}

	public long getId() {
		return id;
	}

	public int getRanking() {
		return ranking;
	}

	public ServiceReference getReference() {
		return reference;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof ReferenceHolder) {
			return ((ReferenceHolder) obj).id == id;
		}

		if (obj instanceof ServiceReference) {
			return id == OsgiServiceReferenceUtils.getServiceId(reference);
		}

		return false;
	}

	public boolean isWorseThen(ServiceReference ref) {
		int otherRanking = OsgiServiceReferenceUtils.getServiceRanking(ref);
		// if there is a higher ranking service
		if (otherRanking > ranking) {
			return true;
		}
		// if equal, use the service id
		if (otherRanking == ranking) {
			long otherId = OsgiServiceReferenceUtils.getServiceId(ref);
			if (otherId < id) {
				return true;
			}
		}
		return false;
	}
}