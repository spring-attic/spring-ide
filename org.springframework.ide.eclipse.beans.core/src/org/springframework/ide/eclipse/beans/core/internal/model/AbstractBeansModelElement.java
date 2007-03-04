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

package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.ide.eclipse.core.model.AbstractSourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * Default implementation of the common protocol for all {@link IModelElement}s
 * related to source code retrievable from Spring's {@link BeanMetadataElement}
 * interface.
 * 
 * @author Torsten Juergeleit
 */
public abstract class AbstractBeansModelElement extends
		AbstractSourceModelElement {

	protected AbstractBeansModelElement(IModelElement parent, String name,
			BeanMetadataElement metadata) {
		super(parent, name, ModelUtils.getSourceLocation(metadata));
	}

	protected AbstractBeansModelElement(IModelElement parent, String name,
			IModelSourceLocation location) {
		super(parent, name, location);
	}
}
