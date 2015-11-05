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

package org.eclipse.gemini.blueprint.service.importer;

import org.osgi.framework.Filter;
import org.springframework.util.ObjectUtils;

/**
 * Default, immutable implementation for {@link OsgiServiceDependency}.
 * 
 * @author Costin Leau
 * 
 */
public class DefaultOsgiServiceDependency implements OsgiServiceDependency {

	private final String beanName;
	private final Filter filter;
	private final boolean mandatoryService;
	private final String toString;
	private final int hashCode;


	/**
	 * Constructs a new <code>DefaultOsgiServiceDependency</code> instance.
	 * 
	 * @param beanName dependency bean name (can be null)
	 * @param filter dependency OSGi filter (can be null)
	 * @param mandatoryService flag indicating whether the dependency is
	 * mandatory or not
	 */
	public DefaultOsgiServiceDependency(String beanName, Filter filter, boolean mandatoryService) {
		this.beanName = beanName;
		this.filter = filter;
		this.mandatoryService = mandatoryService;

		// calculate internal fields
		toString = "DependencyService[Name=" + (beanName != null ? beanName : "null") + "][Filter=" + filter
				+ "][Mandatory=" + mandatoryService + "]";

		int result = 17;
		result = 37 * result + DefaultOsgiServiceDependency.class.hashCode();
		result = 37 * result + (filter == null ? 0 : filter.hashCode());
		result = 37 * result + (beanName == null ? 0 : beanName.hashCode());
		result = 37 * result + (mandatoryService ? 0 : 1);
		hashCode = result;
	}

	public String getBeanName() {
		return beanName;
	}

	public Filter getServiceFilter() {
		return filter;
	}

	public boolean isMandatory() {
		return mandatoryService;
	}

	public String toString() {
		return toString;
	}

	public boolean equals(Object obj) {
		if (obj instanceof OsgiServiceDependency) {
			OsgiServiceDependency other = (OsgiServiceDependency) obj;
			return (other.isMandatory() == mandatoryService && filter.equals(other.getServiceFilter()) && ObjectUtils.nullSafeEquals(
				beanName, other.getBeanName()));
		}
		return false;
	}

	public int hashCode() {
		return hashCode;
	}
}
