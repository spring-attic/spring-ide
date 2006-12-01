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

package org.springframework.ide.eclipse.beans.core.internal.model;

import org.eclipse.core.resources.IResource;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.AbstractSourceModelElement;

/**
 * This class defines an import within a Spring beans configuration.
 * @author Torsten Juergeleit
 */
public class BeansImport extends AbstractSourceModelElement
		implements IBeansImport {

	private String resourceName;

	public BeansImport(IBeansConfig config, ImportDefinition definition) {
		super(config, definition.getImportedResource());
		setSourceRange(definition);
	}

	public int getElementType() {
		return IBeansModelElementTypes.IMPORT_TYPE;
	}

	public IResource getImportedResource() {
		// TODO
		return null;
	}

	public String toString() {
		StringBuffer text = new StringBuffer("import \"");
		text.append(resourceName);
		text.append('"');
		return text.toString();
	}
}
