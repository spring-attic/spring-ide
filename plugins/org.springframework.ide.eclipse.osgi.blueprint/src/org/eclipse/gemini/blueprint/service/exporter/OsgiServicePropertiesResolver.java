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

package org.eclipse.gemini.blueprint.service.exporter;

import java.util.Map;

/**
 * An OsgiServicePropertiesResolver is responsible for providing the properties that a bean exposed as a service will be
 * published with.
 * 
 * @author Adrian Colyer
 * @author Costin Leau
 * 
 * @see org.eclipse.gemini.blueprint.service.exporter.support.OsgiServiceFactoryBean
 */
public interface OsgiServicePropertiesResolver {

	/**
	 * Compatibility (with Spring DM) Predefined property indicating the bean name of an exported Spring managed object.
	 */
	String SPRING_DM_BEAN_NAME_PROPERTY_KEY = "org.springframework.osgi.bean.name";

	/**
	 * Predefined property indicating the bean name of an exported Spring managed object.
	 */
	String BEAN_NAME_PROPERTY_KEY = "org.eclipse.gemini.blueprint.bean.name";

	/**
	 * OSGi 4.2 Blueprint specification predefined property indicating the name of the component exported as a service.
	 * Equivalent to Spring DM {@link #BEAN_NAME_PROPERTY_KEY}.
	 */
	String BLUEPRINT_COMP_NAME = "osgi.service.blueprint.compname";

	/**
	 * Returns a map containing the service properties associated with the given Spring managed bean identified by its
	 * name. The name can be null (for example if nested beans are exported).
	 * 
	 * @param beanName Spring managed bean name
	 * @return map containing the service properties
	 */
	Map getServiceProperties(String beanName);

}
