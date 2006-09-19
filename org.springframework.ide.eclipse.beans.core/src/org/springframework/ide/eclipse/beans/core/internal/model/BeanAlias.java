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

import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.AbstractSourceModelElement;

/**
 * This class defines an alias within a Spring beans configuration.
 * @author Torsten Juergeleit
 */
public class BeanAlias extends AbstractSourceModelElement
														implements IBeanAlias {
	private String name;

	public BeanAlias(IBeansConfig config, String alias, String name) {
		super(config, alias);
		this.name = name;
	}

	public int getElementType() {
		return IBeansModelElementTypes.ALIAS_TYPE;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append(getElementName());
		text.append(" (");
		text.append(getElementStartLine());
		text.append("): name=");
		text.append(name);
		return text.toString();
	}
}
