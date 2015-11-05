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

package org.eclipse.gemini.blueprint.service.exporter.support;

import java.util.Map;

import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.service.exporter.OsgiServicePropertiesResolver;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.internal.MapBasedDictionary;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link OsgiServicePropertiesResolver} that creates a service property set with the following properties: <ul>
 * <li>Bundle-SymbolicName=&lt;bundle symbolic name&gt;</li> <li>Bundle-Version=&lt;bundle version&gt;</li>
 * <li>org.eclipse.gemini.blueprint.bean.name="&lt;bean name&gt;</li> <li>osgi.service.blueprint.compname="&lt;bean
 * name&gt;</li> </ul>
 * 
 * If the name is null/empty, the keys that refer to it will not be created.
 * 
 * @author Adrian Colyer
 * @author Hal Hildebrand
 * 
 * @see OsgiServicePropertiesResolver
 * @see OsgiServiceFactoryBean
 * 
 */
public class BeanNameServicePropertiesResolver implements OsgiServicePropertiesResolver, BundleContextAware,
		InitializingBean {

	private BundleContext bundleContext;

	public Map getServiceProperties(String beanName) {
		Map p = new MapBasedDictionary();
		if (StringUtils.hasText(beanName)) {
			p.put(BEAN_NAME_PROPERTY_KEY, beanName);
			p.put(SPRING_DM_BEAN_NAME_PROPERTY_KEY, beanName);
			p.put(BLUEPRINT_COMP_NAME, beanName);
		}

		String name = getSymbolicName();
		if (StringUtils.hasLength(name)) {
			p.put(Constants.BUNDLE_SYMBOLICNAME, name);
		}
		String version = getBundleVersion();
		if (StringUtils.hasLength(version)) {
			p.put(Constants.BUNDLE_VERSION, version);
		}
		return p;
	}

	private String getBundleVersion() {
		return OsgiBundleUtils.getBundleVersion(bundleContext.getBundle()).toString();
	}

	private String getSymbolicName() {
		return this.bundleContext.getBundle().getSymbolicName();
	}

	public void setBundleContext(BundleContext context) {
		this.bundleContext = context;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(bundleContext, "required property bundleContext has not been set");
	}
}