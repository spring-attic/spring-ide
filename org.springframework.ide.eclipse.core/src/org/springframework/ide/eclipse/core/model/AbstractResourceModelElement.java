/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.core.model;

import org.eclipse.core.resources.IResource;

public abstract class AbstractResourceModelElement extends AbstractModelElement
											 implements IResourceModelElement {
	protected AbstractResourceModelElement(IModelElement parent, String name) {
		super(parent, name);
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IResource.class) {
			return getElementResource();
		}
		return super.getAdapter(adapter);
	}
}
