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

package org.eclipse.gemini.blueprint.service.importer.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.context.BundleContextAware;
import org.eclipse.gemini.blueprint.service.exporter.OsgiServicePropertiesResolver;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.eclipse.gemini.blueprint.util.OsgiFilterUtils;
import org.eclipse.gemini.blueprint.util.internal.ClassUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Base class for importing OSGi services. Provides the common properties and contracts between importers.
 * 
 * @author Costin Leau
 * @author Adrian Colyer
 * @author Hal Hildebrand
 */
public abstract class AbstractOsgiServiceImportFactoryBean implements FactoryBean<Object>, InitializingBean,
		DisposableBean, BundleContextAware, BeanClassLoaderAware, BeanNameAware {

	private static final Log log = LogFactory.getLog(AbstractOsgiServiceImportFactoryBean.class);

	/** context classloader */
	private ClassLoader classLoader;

	private BundleContext bundleContext;

	private ImportContextClassLoaderEnum contextClassLoader = ImportContextClassLoaderEnum.CLIENT;

	// not required to be an interface, but usually should be...
	private Class<?>[] interfaces;

	// filter used to narrow service matches, may be null
	private String filter;

	// Cumulated filter string between the specified classes/interfaces and the
	// given filter
	private Filter unifiedFilter;

	// service lifecycle listener
	private OsgiServiceLifecycleListener[] listeners;

	/** Service Bean property of the OSGi service * */
	private String serviceBeanName;

	private Availability availability = Availability.MANDATORY;

	/** bean name */
	private String beanName = "";

	public void afterPropertiesSet() {
		Assert.notNull(this.bundleContext, "Required 'bundleContext' property was not set.");
		Assert.notNull(classLoader, "Required 'classLoader' property was not set.");
		Assert.isTrue(!ObjectUtils.isEmpty(interfaces) || StringUtils.hasText(filter)
				|| StringUtils.hasText(serviceBeanName),
				"At least the interface or filter or service name needs to be defined to import an OSGi service");
		if (ObjectUtils.isEmpty(interfaces)) {
			log.warn("OSGi importer [" + beanName + "] definition contains no interfaces: "
					+ "all invocations will be executed on the proxy and not on the backing service");
		}

		// validate specified classes
		Assert.isTrue(!ClassUtils.containsUnrelatedClasses(interfaces),
				"More then one concrete class specified; cannot create proxy.");

		this.listeners = (listeners == null ? new OsgiServiceLifecycleListener[0] : listeners);
		this.interfaces = (interfaces == null ? new Class<?>[0] : interfaces);
		this.filter = (StringUtils.hasText(filter) ? filter : "");

		getUnifiedFilter(); // eager initialization of the cache to catch filter errors
	}

	/**
	 * Assembles the configuration properties into one unified OSGi filter. Note that this implementation creates the
	 * filter on the first call and caches it afterwards.
	 * 
	 * @return unified filter based on this factory bean configuration
	 */
	public Filter getUnifiedFilter() {
		if (unifiedFilter != null) {
			return unifiedFilter;
		}

		String filterWithClasses =
				(!ObjectUtils.isEmpty(interfaces) ? OsgiFilterUtils.unifyFilter(interfaces, filter) : filter);

		boolean trace = log.isTraceEnabled();
		if (trace)
			log.trace("Unified classes=" + ObjectUtils.nullSafeToString(interfaces) + " and filter=[" + filter
					+ "]  in=[" + filterWithClasses + "]");

		// add the serviceBeanName/Blueprint component name constraint
		String nameFilter;
		if (StringUtils.hasText(serviceBeanName)) {
			StringBuilder nsFilter = new StringBuilder("(|(");
			nsFilter.append(OsgiServicePropertiesResolver.BEAN_NAME_PROPERTY_KEY);
			nsFilter.append("=");
			nsFilter.append(serviceBeanName);
			nsFilter.append(")(");
			nsFilter.append(OsgiServicePropertiesResolver.SPRING_DM_BEAN_NAME_PROPERTY_KEY);
			nsFilter.append("=");
			nsFilter.append(serviceBeanName);
			nsFilter.append(")(");
			nsFilter.append(OsgiServicePropertiesResolver.BLUEPRINT_COMP_NAME);
			nsFilter.append("=");
			nsFilter.append(serviceBeanName);
			nsFilter.append("))");
			nameFilter = nsFilter.toString();
		} else {
			nameFilter = null;
		}

		String filterWithServiceBeanName = filterWithClasses;
		if (nameFilter != null) {
			StringBuilder finalFilter = new StringBuilder();
			finalFilter.append("(&");
			finalFilter.append(filterWithClasses);
			finalFilter.append(nameFilter);
			finalFilter.append(")");
			filterWithServiceBeanName = finalFilter.toString();
		}

		if (trace)
			log.trace("Unified serviceBeanName [" + ObjectUtils.nullSafeToString(serviceBeanName) + "] and filter=["
					+ filterWithClasses + "]  in=[" + filterWithServiceBeanName + "]");

		// create (which implies validation) the actual filter
		unifiedFilter = OsgiFilterUtils.createFilter(filterWithServiceBeanName);

		return unifiedFilter;
	}

	/**
	 * Sets the classes that the imported service advertises.
	 * 
	 * @param interfaces array of advertised classes.
	 */
	public void setInterfaces(Class<?>[] interfaces) {
		this.interfaces = interfaces;
	}

	/**
	 * Sets the thread context class loader management strategy to use for services imported by this service. By default
	 * {@link ImportContextClassLoaderEnum#CLIENT} is used.
	 * 
	 * @param contextClassLoader import context class loader management strategy
	 */
	public void setImportContextClassLoader(ImportContextClassLoaderEnum contextClassLoader) {
		Assert.notNull(contextClassLoader);
		this.contextClassLoader = contextClassLoader;
	}

	public void setBundleContext(BundleContext context) {
		this.bundleContext = context;
	}

	/**
	 * Sets the OSGi service filter. The filter will be concatenated with the rest of the configuration properties
	 * specified (such as interfaces) so there is no need to include them in the filter.
	 * 
	 * @param filter OSGi filter describing the importing OSGi service
	 */
	public void setFilter(String filter) {
		this.filter = filter;
	}

	/**
	 * Sets the lifecycle listeners interested in receiving events for this importer.
	 * 
	 * @param listeners importer listeners
	 */
	public void setListeners(OsgiServiceLifecycleListener[] listeners) {
		this.listeners = listeners;
	}

	/**
	 * Sets the OSGi service bean name. This setting should be normally used when the imported service has been exported
	 * by Spring DM exporter. You may specify additional filtering criteria if needed (using the filter property) but
	 * this is not required.
	 * 
	 * @param serviceBeanName importer service bean name
	 */
	public void setServiceBeanName(String serviceBeanName) {
		this.serviceBeanName = serviceBeanName;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * This method is called automatically by the container.
	 */
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Returns the class loader used by this FactoryBean.
	 * 
	 * @return factory bean class loader
	 */
	public ClassLoader getBeanClassLoader() {
		return classLoader;
	}

	/**
	 * Returns the bundleContext used by this FactoryBean.
	 * 
	 * @return factory bean class loader
	 */
	public BundleContext getBundleContext() {
		return bundleContext;
	}

	/**
	 * Returns the interfaces used for discovering the imported service(s).
	 * 
	 * @return interfaces advertised by services in the OSGi space
	 */
	public Class<?>[] getInterfaces() {
		return interfaces;
	}

	/**
	 * Returns the filter describing the imported service(s).
	 * 
	 * @return filter describing the imported service(s)
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * Returns the listeners interested in receiving events for this importer.
	 * 
	 * @return lifecycle listeners used by this importer
	 */
	public OsgiServiceLifecycleListener[] getListeners() {
		return listeners;
	}

	/**
	 * Returns the context class loader management strategy.
	 * 
	 * @return the context class loader management strategy
	 */
	public ImportContextClassLoaderEnum getImportContextClassLoader() {
		return contextClassLoader;
	}

	public Availability getAvailability() {
		return availability;
	}

	/**
	 * Sets the importer availability. Default is mandatory ({@link Availability#MANDATORY}
	 * @param availability
	 */
	public void setAvailability(Availability availability) {
		Assert.notNull(availability);
		this.availability = availability;
	}

	/**
	 * Returns the bean name associated with the instance of this class (when running inside the Spring container).
	 * 
	 * @return component bean name
	 */
	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String name) {
		beanName = name;
	}
}