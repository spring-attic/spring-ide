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

package org.eclipse.gemini.blueprint.service.importer.support.internal.exception;

import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.ServiceUnavailableException;

/**
 * Utility class that creates either Spring DM or Blueprint exceptions. It's used for performing lazy class loading and
 * similar exception messages between the two.
 * 
 * @author Costin Leau
 */
public abstract class BlueprintExceptionFactory {

	public static RuntimeException createServiceUnavailableException(Filter filter) {
		return BlueprintFactory.createServiceUnavailableException(filter);
	}

	public static RuntimeException createServiceUnavailableException(ServiceReference reference) {
		return BlueprintFactory.createServiceUnavailableException(reference);
	}

	private static abstract class BlueprintFactory {
		private static RuntimeException createServiceUnavailableException(Filter filter) {
			return new ServiceUnavailableException("service matching filter=[" + filter + "] unavailable", filter
					.toString());
		}

		private static RuntimeException createServiceUnavailableException(ServiceReference reference) {
			String id = (reference == null ? "null" : "" + OsgiServiceReferenceUtils.getServiceId(reference));
			return new ServiceUnavailableException("service with id=[" + id + "] unavailable", OsgiFilterUtils
					.getFilter(reference));
		}
	}
}
