/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.properties.model;

import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class PropertiesModelLabelProvider extends BeansModelLabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof IBeansConfig) {
			return ((IBeansConfig) element).getElementName();
		}
		return super.getText(element);
	}
}
