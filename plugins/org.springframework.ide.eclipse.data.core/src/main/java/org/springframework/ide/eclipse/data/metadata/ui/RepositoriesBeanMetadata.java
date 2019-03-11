/*
 * Copyright 2011-2012 by the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.data.metadata.ui;

import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.data.SpringDataUtils;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Custom {@link IBeanMetadata bean metdata} implementation.
 * <p>
 * TODO: remove skinning as soon as custom
 * {@link org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataLabelProvider} s work.
 * 
 * @author Oliver Gierke
 */
public class RepositoriesBeanMetadata implements IBeanMetadata {

	private static final long serialVersionUID = 5730990435174474138L;

	private final String beanId;
	private final IModelSourceLocation location;

	/**
	 * Creates a new {@link RepositoriesBeanMetadata} instance.
	 * 
	 * @param bean an {@link IBean} of a Spring Data repository, must not be {@literal null}.
	 * @param location must not be {@literal null}
	 */
	public RepositoriesBeanMetadata(IBean bean, IModelSourceLocation location) {

		Assert.notNull(location);
		Assert.notNull(bean);
		Assert.isTrue(SpringDataUtils.isRepositoryBean(bean));

		this.beanId = bean.getElementID();
		this.location = location;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata#getValueAsText()
	 */
	public String getValueAsText() {
		return SpringDataUtils.asText(getValue());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata#getHandleIdentifier()
	 */
	public String getHandleIdentifier() {
		return beanId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata#getKey()
	 */
	public String getKey() {
		return "Spring Data Repositories";
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata#getValue()
	 */
	public IBean getValue() {
		return (IBean) BeansCorePlugin.getModel().getElement(beanId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata#getElementSourceLocation()
	 */
	public IModelSourceLocation getElementSourceLocation() {
		return location;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		RepositoriesBeanMetadata that = (RepositoriesBeanMetadata) obj;

		return ObjectUtils.nullSafeEquals(this.beanId, that.beanId) && ObjectUtils.nullSafeEquals(this.location, that.location);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(beanId) + ObjectUtils.nullSafeHashCode(location);
	}
}
