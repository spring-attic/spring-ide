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
package org.springframework.ide.eclipse.aop.core.model.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMethod;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.IBeanAspectDefinition;

public class AopReference implements IAopReference {

	private ADVICE_TYPES type;

	private IMethod source;

	private IMethod target;

	private IBeanAspectDefinition definition;

	private IResource file;
    
	public AopReference(ADVICE_TYPES type, IMethod source, IMethod target,
			IBeanAspectDefinition def, IResource file) {
		this.type = type;
		this.source = source;
		this.target = target;
		this.definition = def;
		this.file = file;
    }
    
	public IBeanAspectDefinition getDefinition() {
		return definition;
	}

	public ADVICE_TYPES getAdviceType() {
		return this.type;
	}

	public IMethod getSource() {
		return this.source;
	}

	public IMethod getTarget() {
		return this.target;
	}

	public IResource getResource() {
		return file;
	}
}
