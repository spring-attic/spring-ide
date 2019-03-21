/*
 * Copyright 2012 by the original author(s).
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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.metadata.model.BeanMetadataProviderAdapter;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadataProvider;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;
import org.springframework.ide.eclipse.data.SpringDataUtils;

/**
 * Custom {@link IBeanMetadataProvider} to create {@link RepositoriesBeanMetadata} instances for Spring Data repository
 * beans.
 * 
 * @author Oliver Gierke
 */
public class RepositoriesBeanMetadataProvider extends BeanMetadataProviderAdapter {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.core.metadata.model.BeanMetadataProviderAdapter#provideBeanMetadata(org.springframework.ide.eclipse.beans.core.model.IBean, org.springframework.ide.eclipse.beans.core.model.IBeansConfig, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Set<IBeanMetadata> provideBeanMetadata(final IBean bean, IBeansConfig beansConfig,
			IProgressMonitor progressMonitor) {

		Set<IBeanMetadata> beanMetadata = new HashSet<IBeanMetadata>();

		if (SpringDataUtils.isRepositoryBean(bean)) {

			IType type = JdtUtils.getJavaType(bean.getElementResource().getProject(),
					BeansModelUtils.getBeanClass(bean, null));

			try {
				beanMetadata.add(new RepositoriesBeanMetadata(bean, new JavaModelSourceLocation(type)));
			} catch (JavaModelException e) {

			}
		}

		return beanMetadata;
	}
}
