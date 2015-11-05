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

package org.eclipse.gemini.blueprint.service.importer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.service.importer.support.internal.aop.ServiceProxyCreator;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.CollectionProxy;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceCollection;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceList;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceSet;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceSortedList;
import org.eclipse.gemini.blueprint.service.importer.support.internal.collection.OsgiServiceSortedSet;
import org.eclipse.gemini.blueprint.service.importer.support.internal.controller.ImporterController;
import org.eclipse.gemini.blueprint.service.importer.support.internal.controller.ImporterInternalActions;
import org.eclipse.gemini.blueprint.service.importer.support.internal.dependency.ImporterStateListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.springframework.util.Assert;

/**
 * OSGi service (collection) importer. This implementation creates a managed (read-only) collection of OSGi services.
 * The returned collection automatically handles the OSGi services dynamics. If a new service that matches the
 * configuration criteria appears, it will be automatically added to the collection. If a service that matches the
 * criteria disappears (is unregistered), it will be automatically removed from the collection.
 * 
 * <p/> Due to the dynamic nature of OSGi services, the collection content can change at runtime, even during iteration.
 * This implementation will correctly update all the collection <code>Iterator</code>s so they reflect the collection
 * content. This approach (as opposed to the 'snapshot' strategy) prevents dealing with <em>dead</em> services which can
 * appear when imported services go down while iterating. This means that iterating while the collection is being
 * changed is safe.
 * 
 * <p/> Note that the <code>Iterator</code> still has to be fulfilled meaning the <code>next()</code> method always obey
 * the result of the previous <code>hasNext()</code> invocation:
 * 
 * <p/> <table border="1"> <tr> <th><code>hasNext()</code> returned value</th> <th><code>next()</code> behaviour</th>
 * </th> <tr> <td> <code>true</code></td> <td><em>Always</em> return a non-null value, even when the collection has
 * shrunk as services when away. This means returning a proxy that will throw an exception on an invocation that
 * requires the backing service to be present.</td> </tr> <tr> <td><code>false</code></td> <td>per <code>Iterator</code>
 * contract, <code>NoSuchElementException</code> is thrown. This applies even if other services are added to the
 * collection.</td> </tr> </table>
 * 
 * <p/> Due to the dynamic nature of OSGi, <code>hasNext()</code> invocation made on the same <code>Iterator</code> can
 * return different values based on the services availability. However, as explained above, <code>next()</code> will
 * always obey the result of the last <code>hasNext()</code> method.
 * 
 * <p/> <strong>Note:</strong> Even though the collection and its iterators communicate in a thread-safe manner,
 * iterators themselves are not thread-safe. Concurrent access on the iterators should be properly synchronized. Due to
 * the light nature of the iterators, consider creating a new one rather then reusing or sharing.
 * 
 * 
 * @see java.util.Iterator
 * @see java.util.Collection
 * @see java.util.List
 * @see java.util.Set
 * @see java.util.SortedSet
 * 
 * @author Costin Leau
 */
public final class OsgiServiceCollectionProxyFactoryBean extends AbstractServiceImporterProxyFactoryBean {

	/**
	 * Wrapper around internal commands.
	 * 
	 * @author Costin Leau
	 * 
	 */
	private class Executor implements ImporterInternalActions {

		public void addStateListener(ImporterStateListener stateListener) {
			stateListeners.add(stateListener);
		}

		public void removeStateListener(ImporterStateListener stateListener) {
			stateListeners.remove(stateListener);
		}

		public boolean isSatisfied() {
			return (exposedProxy == null ? true : exposedProxy.isSatisfied());
		}
	};

	private static final Log log = LogFactory.getLog(OsgiServiceCollectionProxyFactoryBean.class);

	/** proxy casted to a specific interface to allow specific method calls */
	private CollectionProxy exposedProxy;

	/** proxy infrastructure hook exposed to allow clean up */
	private Runnable proxyDestructionCallback;
	private Runnable initializationCallback;

	/** proxy creator */
	private ServiceProxyCreator proxyCreator;

	private Comparator comparator;

	private CollectionType collectionType = CollectionType.LIST;
	/** greedy-proxying */
	private boolean greedyProxying = false;

	private MemberType memberType = MemberType.SERVICE_OBJECT;

	/** internal listeners */
	private final List<ImporterStateListener> stateListeners =
			Collections.synchronizedList(new ArrayList<ImporterStateListener>(4));

	private final ImporterInternalActions controller;

	public OsgiServiceCollectionProxyFactoryBean() {
		controller = new ImporterController(new Executor());
	}

	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		proxyCreator =
				new StaticServiceProxyCreator(getInterfaces(), getAopClassLoader(), getBeanClassLoader(),
						getBundleContext(), getImportContextClassLoader(), greedyProxying, isUseBlueprintExceptions());
	}

	/**
	 * Create the managed-collection given the existing settings. This method creates the osgi managed collection and
	 * wraps it with an unmodifiable map to prevent exposing infrastructure methods and write access.
	 * 
	 * @return importer proxy
	 */
	@SuppressWarnings("unchecked")
	@Override
	Object createProxy(boolean lazyProxy) {
		if (log.isDebugEnabled())
			log.debug("Creating a multi-value/collection proxy");

		OsgiServiceCollection collection;
		Collection delegate;

		BundleContext bundleContext = getBundleContext();
		ClassLoader classLoader = getAopClassLoader();
		Filter filter = getUnifiedFilter();
		boolean useServiceReferences = MemberType.SERVICE_REFERENCE.equals(memberType);

		if (CollectionType.LIST.equals(collectionType)) {
			collection =
					(comparator == null ? new OsgiServiceList(filter, bundleContext, classLoader, proxyCreator,
							useServiceReferences) : new OsgiServiceSortedList(filter, bundleContext, classLoader,
							comparator, proxyCreator, useServiceReferences));
			delegate = Collections.unmodifiableList((List) collection);
		} else if (CollectionType.SET.equals(collectionType)) {
			collection =
					(comparator == null ? new OsgiServiceSet(filter, bundleContext, classLoader, proxyCreator,
							useServiceReferences) : new OsgiServiceSortedSet(filter, bundleContext, classLoader,
							comparator, proxyCreator, useServiceReferences));

			delegate = Collections.unmodifiableSet((Set) collection);
		} else if (CollectionType.SORTED_LIST.equals(collectionType)) {
			collection =
					new OsgiServiceSortedList(filter, bundleContext, classLoader, comparator, proxyCreator,
							useServiceReferences);

			delegate = Collections.unmodifiableList((List) collection);
		}

		else if (CollectionType.SORTED_SET.equals(collectionType)) {
			collection =
					new OsgiServiceSortedSet(filter, bundleContext, classLoader, comparator, proxyCreator,
							useServiceReferences);
			delegate = Collections.unmodifiableSortedSet((SortedSet) collection);
		}

		else {
			throw new IllegalArgumentException("Unknown collection type:" + collectionType);
		}

		// assign the proxy early to avoid multiple collection creation
		// when calling the listeners
		proxy = delegate;

		collection.setRequiredAtStartup(Availability.MANDATORY.equals(getAvailability()));
		collection.setListeners(getListeners());
		collection.setStateListeners(stateListeners);
		collection.setServiceImporter(this);
		collection.setServiceImporterName(getBeanName());
		collection.setUseBlueprintExceptions(isUseBlueprintExceptions());

		// start the lookup only after the proxy has been assembled
		if (!lazyProxy) {
			collection.afterPropertiesSet();
		} else {
			final OsgiServiceCollection col = collection;
			initializationCallback = new Runnable() {
				public void run() {
					col.afterPropertiesSet();
				}
			};
		}

		exposedProxy = collection;
		proxyDestructionCallback = new DisposableBeanRunnableAdapter(collection);

		return delegate;
	}

	@Override
	Runnable getProxyInitializer() {
		return initializationCallback;
	}

	@Override
	Runnable getProxyDestructionCallback() {
		return proxyDestructionCallback;
	}

	/**
	 * Sets the (optional) comparator for ordering the resulting collection. The presence of a comparator will force the
	 * FactoryBean to use a <em>sorted</em> collection even though, the specified collection type does not imply
	 * ordering.
	 * 
	 * <p/> Thus, instead of list a sorted list will be created and instead of a set, a sorted set.
	 * 
	 * @see #setCollectionType(CollectionType)
	 * 
	 * @param comparator Comparator (can be null) used for ordering the resulting collection.
	 */
	public void setComparator(Comparator comparator) {
		this.comparator = comparator;
	}

	/**
	 * Sets the collection type this FactoryBean will produce. Note that if a comparator is set, a sorted collection
	 * will be created even if the specified type is does not imply ordering. If no comparator is set but the collection
	 * type implies ordering, the natural order of the elements will be used.
	 * 
	 * @see #setComparator(Comparator)
	 * @see java.lang.Comparable
	 * @see java.util.Comparator
	 * @see CollectionType
	 * 
	 * @param collectionType the collection type as string using one of the values above.
	 */
	public void setCollectionType(CollectionType collectionType) {
		Assert.notNull(collectionType);
		this.collectionType = collectionType;
	}

	/**
	 * Dictates whether <em>greedy</em> proxies are created or not (default).
	 * 
	 * <p> Greedy proxies will proxy <b>all</b> the (visible) classes published by the imported OSGi services. This
	 * means that the individual service proxy, might implement/extend additional classes. </p> By default, greedy
	 * proxies are disabled (false) meaning that only the specified classes are used for generating the the imported
	 * OSGi service proxies.
	 * 
	 * <p/> <b>Note:</b>Greedy proxying will use the proxy mechanism dictated by this factory configuration. This means
	 * that if JDK proxies are used, greedy proxing will consider only additional interfaces exposed by the OSGi service
	 * and none of the extra classes. When CGLIB is used, all extra published classes (whether interfaces or
	 * <em>non-final</em> concrete classes) will be considered.
	 * 
	 * @param greedyProxying true if greedy proxying should be enabled, false otherwise.
	 */
	public void setGreedyProxying(boolean greedyProxying) {
		this.greedyProxying = greedyProxying;
	}

	/**
	 * Sets the member type of this service collection.
	 * 
	 * @return the collection member type
	 */
	public MemberType getMemberType() {
		return memberType;
	}

	/**
	 * Sets the member type of this service collection. The collection can hold either service proxies (the default)
	 * indicated by {@link MemberType#SERVICE_OBJECT} or service references {@link MemberType#SERVICE_REFERENCE}.
	 * 
	 * @param type the collection member type
	 */
	public void setMemberType(MemberType type) {
		Assert.notNull(type);
		this.memberType = type;
	}
}