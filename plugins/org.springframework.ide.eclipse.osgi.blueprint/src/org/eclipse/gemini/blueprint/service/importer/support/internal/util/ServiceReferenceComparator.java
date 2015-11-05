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

package org.eclipse.gemini.blueprint.service.importer.support.internal.util;

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.gemini.blueprint.service.importer.ImportedOsgiServiceProxy;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.osgi.framework.ServiceReference;

/**
 * Default comparator for sorted collections. It uses the service id property of an OSGi service to determine the order.
 * Thus, by using this comparator, the services added to a collection will be sorted in the order in which they are
 * published to the OSGi platform.
 * 
 * <p/> This comparator version, provides <i>natural</i> ordering for service references for pre OSGI 4.1 platforms,
 * using the contract from OSGi 4.1 spec:
 * 
 * <blockquote> If this ServiceReference and the specified ServiceReference have the same service id they are equal.
 * This ServiceReference is less than the specified ServiceReference if it has a lower service ranking and greater if it
 * has a higher service ranking. Otherwise, if this ServiceReference and the specified ServiceReference have the same
 * service ranking, this ServiceReference is less than the specified ServiceReference if it has a higher service id and
 * greater if it has a lower service id. </blockquote>
 * 
 * @see Comparator
 * @author Costin Leau
 * 
 */
public class ServiceReferenceComparator implements Comparator, Serializable {

	private static final long serialVersionUID = 7552328574956669890L;

	private static final int hashCode = ServiceReferenceComparator.class.hashCode() * 13;

	public int compare(Object o1, Object o2) {

		ServiceReference ref1, ref2;

		if (o1 == null || o2 == null) {
			if (o1 == o2)
				return 0;
			else
				throw new ClassCastException("Cannot compare null with a non-null object");
		}

		// look first for service references
		if (o1 instanceof ServiceReference) {
			ref1 = (ServiceReference) o1;
		} else {
			ImportedOsgiServiceProxy obj1 = (ImportedOsgiServiceProxy) o1;
			ref1 = obj1.getServiceReference();
		}

		if (o2 instanceof ServiceReference) {
			ref2 = (ServiceReference) o2;
		} else {
			ImportedOsgiServiceProxy obj2 = (ImportedOsgiServiceProxy) o2;
			ref2 = obj2.getServiceReference();
		}

		return compare(ref1, ref2);
	}

	private int compare(ServiceReference ref1, ServiceReference ref2) {
		// compare based on service ranking
		int rank1 = OsgiServiceReferenceUtils.getServiceRanking(ref1);
		int rank2 = OsgiServiceReferenceUtils.getServiceRanking(ref2);

		int result = rank1 - rank2;

		if (result == 0) {
			long id1 = OsgiServiceReferenceUtils.getServiceId(ref1);
			long id2 = OsgiServiceReferenceUtils.getServiceId(ref2);

			// when comparing IDs, make sure to return inverse results (i.e. lower
			// id, means higher service)
			return (int) (id2 - id1);
		}

		return result;
	}

	public boolean equals(Object obj) {
		return (this == obj || obj instanceof ServiceReferenceComparator);
	}

	public int hashCode() {
		return hashCode;
	}
}