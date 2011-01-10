/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.metadata.internal.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadataModel;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * {@link IBeanMetadataModel} implementation that saves and reloads its contents from a backing
 * store.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataModel implements IBeanMetadataModel {

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID + "/model/metadata/debug";

	public static final boolean DEBUG = SpringCore.isDebug(DEBUG_OPTION);

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	private Map<String, BeanMetadataHolder> beanMetadata = new ConcurrentHashMap<String, BeanMetadataHolder>();

	private Map<String, BeanPropertyDataHolder> beanPropertyData = new ConcurrentHashMap<String, BeanPropertyDataHolder>();

	public Set<IBeanMetadata> getBeanMetadata(IBean bean) {
		try {
			r.lock();
			if (beanMetadata.containsKey(bean.getElementID())) {
				return beanMetadata.get(bean.getElementID()).getBeanMetaData();
			}
			return Collections.emptySet();
		}
		finally {
			r.unlock();
		}
	}

	public void setBeanMetadata(IBean bean, Set<IBeanMetadata> bMetaData,
			Set<IMethodMetadata> methodMetaData) {
		try {
			w.lock();
			BeanMetadataHolder holder = new BeanMetadataHolder();
			holder.setElemenetId(bean.getElementID());
			holder.setBeanMetaData(bMetaData);
			holder.setMethodMetaData(methodMetaData);
			// safe time so we can purge very old entries after a while
			holder.setLastModified(System.currentTimeMillis());
			beanMetadata.put(bean.getElementID(), holder);
		}
		finally {
			w.unlock();
		}
	}

	public void clearBeanMetadata(IBean bean) {
		try {
			w.lock();
			beanMetadata.remove(bean.getElementID());
		}
		finally {
			w.unlock();
		}
	}

	public Set<IBeanProperty> getBeanProperties(IBean bean) {
		try {
			r.lock();
			if (beanPropertyData.containsKey(bean.getElementID())) {
				return beanPropertyData.get(bean.getElementID()).getBeanProperties();
			}
			return Collections.emptySet();
		}
		finally {
			r.unlock();
		}
	}

	public void setBeanProperties(IBean bean, Set<IBeanProperty> beanProperties) {
		try {
			w.lock();
			BeanPropertyDataHolder holder = new BeanPropertyDataHolder();
			holder.setElemenetId(bean.getElementID());
			holder.setBeanProperties(beanProperties);
			// safe time so we can purge very old entries after a while
			holder.setLastModified(System.currentTimeMillis());
			beanPropertyData.put(bean.getElementID(), holder);
		}
		finally {
			w.unlock();
		}
	}

	public void clearBeanProperties(IBean bean) {
		try {
			w.lock();
			beanPropertyData.remove(bean.getElementID());
		}
		finally {
			w.unlock();
		}
	}

	/**
	 * Starts and loads the internal model.
	 */
	public void start() {

		Map<String, BeanMetadataHolder> storedBeanMetadata = BeanMetadataPersistence.loadMetadata();
		Map<String, BeanPropertyDataHolder> storedProperties = BeanMetadataPersistence
				.loadProperties();
		try {
			w.lock();
			beanMetadata = storedBeanMetadata;
			beanPropertyData = storedProperties;
		}
		catch (Exception e) {
			beanMetadata = new ConcurrentHashMap<String, BeanMetadataHolder>();
			beanPropertyData = new ConcurrentHashMap<String, BeanPropertyDataHolder>();
		}
		finally {
			w.unlock();
		}
	}

	/**
	 * Stops and saves the internal model.
	 */
	public void stop() {
		BeanMetadataPersistence.storeMetadata(beanMetadata);
		BeanMetadataPersistence.storeProperties(beanPropertyData);
	}

}
