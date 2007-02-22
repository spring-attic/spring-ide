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

package org.springframework.ide.eclipse.beans.core.namespaces;

import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This interface defines a protocol used by the extension point
 * "org.springframework.ide.eclipse.beans.core.namespaces" to convert a
 * {@link ComponentDefinition} into a {@link ISourceModelElement}.
 * 
 * @author Torsten Juergeleit
 */
public interface IModelElementProvider {

	/**
	 * Returns the corresponding {@link ISourceModelElement} for the given
	 * {@link ComponentDefinition}.
	 * 
	 * @param config
	 *            the config the requested model element belongs to
	 * @param definition
	 *            the Spring component the model element is created from
	 */
	ISourceModelElement getElement(IBeansConfig config,
			ComponentDefinition definition);
}
