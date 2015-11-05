/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.service.importer;

import org.osgi.framework.Filter;

/**
 * Dependency contract to an OSGi service.
 * 
 * @author Costin Leau
 * @author Andy Piper
 */
public interface OsgiServiceDependency {

	/**
	 * Returns the OSGi filter for the service dependency.
	 * 
	 * @return filter describing the dependent OSGi service
	 */
	Filter getServiceFilter();

	/**
	 * Returns the bean name (if any) that declares this dependency.
	 * 
	 * @return the name of bean declaring the dependency. Can be null.
	 */
	String getBeanName();

	/**
	 * Indicates if the dependency is mandatory or not.
	 * 
	 * @return true if the dependency is mandatory, false otherwise.
	 */
	boolean isMandatory();
}
