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

import java.util.Comparator;

import org.eclipse.gemini.blueprint.service.importer.ServiceReferenceProxy;
import org.eclipse.gemini.blueprint.util.OsgiPlatformDetector;
import org.osgi.framework.ServiceReference;

/**
 * Utility used for comparing ServiceReferences. This class takes into account OSGi 4.0 platforms by providing its own
 * internal comparator.
 * 
 * @author Costin Leau
 */
public abstract class ServiceComparatorUtil {

	protected static final boolean OSGI_41 = OsgiPlatformDetector.isR41();

	protected static final Comparator COMPARATOR =
			(OsgiPlatformDetector.isR41() ? null : new ServiceReferenceComparator());

	public static int compare(ServiceReference left, Object right) {

		if (right instanceof ServiceReferenceProxy) {
			right = ((ServiceReferenceProxy) right).getTargetServiceReference();
		}

		if (left == null && right == null) {
			return 0;
		}

		if (left == null || right == null) {
			throw new ClassCastException("Cannot compare null with a non-null object");
		}

		return (OSGI_41 ? left.compareTo(right) : COMPARATOR.compare(left, right));
	}
}
