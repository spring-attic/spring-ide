/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.data.beans.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataReference;
import org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataContentProvider;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceContentProvider;
import org.springframework.ide.eclipse.data.metadata.ui.RepositoriesBeanMetadata;

/**
 * Removes repository factory properties from the outline view.
 * 
 * @author Oliver Gierke
 */
public class RepositoriesNamespaceContentProvider extends DefaultNamespaceContentProvider implements
		IBeanMetadataContentProvider {

	private static final List<String> FILTER_PROPERTIES = Arrays.asList("repositoryInterface", "domainClass",
			"transactionManager");

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement) {

		// Filter bean properties
		if (parentElement instanceof IBean) {
			return getFilteredProperties((IBean) parentElement).toArray();
		}

		if (parentElement instanceof RepositoriesBeanMetadata) {
			return getFilteredProperties(((RepositoriesBeanMetadata) parentElement).getValue()).toArray();
		}

		return super.getChildren(parentElement);
	}

	public List<IBeanProperty> getFilteredProperties(IBean bean) {

		List<IBeanProperty> filtered = new ArrayList<IBeanProperty>();

		for (Object child : super.getChildren(bean)) {

			if (child instanceof IBeanProperty) {

				IBeanProperty property = (IBeanProperty) child;
				if (!FILTER_PROPERTIES.contains(property.getElementName())) {
					filtered.add(property);
				}
			}
		}

		return filtered;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataContentProvider#getBeanMetadataReference(org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata, org.springframework.ide.eclipse.beans.core.model.IBeansProject)
	 */
	public BeanMetadataReference getBeanMetadataReference(IBeanMetadata metadata, IBeansProject project) {
		return new BeanMetadataReference(project, metadata.getKey());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataContentProvider#supports(java.lang.Object)
	 */
	public boolean supports(Object element) {
		return element instanceof RepositoriesBeanMetadata;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		return true;
	}
}
