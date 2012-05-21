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
package org.springframework.ide.eclipse.data.metadata.ui;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataLabelProvider;

/**
 * Custom {@link IBeanMetadataLabelProvider label provider} to create labels for {@link RepositoriesBeanMetadata}.
 * 
 * @author Oliver Gierke
 */
public class RepositoriesBeanMetadataLabelProvider extends LabelProvider implements IBeanMetadataLabelProvider {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object arg0) {
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object arg0) {

		RepositoriesBeanMetadata metadata = (RepositoriesBeanMetadata) arg0;
		return metadata.getValueAsText();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang.Object)
	 */
	public String getDescription(Object arg0) {
		return "Spring Data Repositories Label Description";
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.model.metadata.IBeanMetadataLabelProvider#supports(java.lang.Object)
	 */
	public boolean supports(Object object) {
		return object instanceof RepositoriesBeanMetadata;
	}
}
