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
package org.springframework.ide.eclipse.beans.ui.model.metadata;

import java.util.HashSet;
import java.util.Set;

import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.util.ObjectUtils;

/**
 * Holder that is used in the Spring and Project Explorer to link between a
 * certain meta data type identified by <code>key</code> and the children.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataReference {

	private final Set<Object> beanMetaData;

	private String key;

	protected final IBeansProject beansProject;

	public BeanMetadataReference(IBeansProject project, Set<Object> beanMetaData, String key) {
		this.beansProject = project;
		this.beanMetaData = beanMetaData;
		this.key = key;
	}

	public BeanMetadataReference(IBeansProject project, String key) {
		this(project, new HashSet<Object>(), key);
	}

	public void addChild(Object metadata) {
		this.beanMetaData.add(metadata);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanMetadataReference)) {
			return false;
		}
		BeanMetadataReference that = (BeanMetadataReference) other;
		if (!ObjectUtils.nullSafeEquals(this.key, that.key))
			return false;
		return ObjectUtils.nullSafeEquals(this.beansProject, that.beansProject);
	}

	public Object firstChild() {
		if (beanMetaData.size() > 0) {
			return beanMetaData.iterator().next();
		}
		return null;
	}

	public IBeansProject getBeansProject() {
		return beansProject;
	}

	public Object[] getChildren() {
		return beanMetaData.toArray();
	}
	
	public String getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(beansProject);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(key);
	}

}
