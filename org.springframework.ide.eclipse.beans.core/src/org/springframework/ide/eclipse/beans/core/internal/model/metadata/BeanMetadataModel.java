/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.metadata;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadataModel;
import org.springframework.ide.eclipse.beans.core.model.metadata.IMethodMetadata;

/**
 * {@link IBeanMetadataModel} implementation that saves and reloads its contents
 * from a backing store.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataModel implements IBeanMetadataModel {

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID + "/model/metadata/debug";

	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	private Map<String, BeanMetadataHolder> beanMetadata = null;

	public Set<IBeanMetadata> getBeanMetaData(IBean bean) {
		if (beanMetadata.containsKey(bean.getElementID())) {
			return beanMetadata.get(bean.getElementID()).getBeanMetaData();
		}
		return Collections.emptySet();
	}

	public void setBeanMetaData(IBean bean, Set<IBeanMetadata> bMetaData,
			Set<IMethodMetadata> methodMetaData) {
		BeanMetadataHolder holder = new BeanMetadataHolder();
		holder.setElemenetId(bean.getElementID());
		holder.setBeanMetaData(bMetaData);
		holder.setMethodMetaData(methodMetaData);
		beanMetadata.put(bean.getElementID(), holder);
	}

	public void clearBeanMetaData(IBean bean) {
		beanMetadata.remove(bean.getElementID());
	}
	
	/**
	 * Starts and loads the internal model.
	 */
	public void startup() {
		this.beanMetadata = BeanMetadataPersistence.loadMetaData();
	}
	
	/**
	 * Stops and saves the internal model. 
	 */
	public void shutdown() {
		BeanMetadataPersistence.storeMetaData(beanMetadata);
	}

}
