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
 *
 * Created on 02-Sep-2004
 */

package org.springframework.ide.eclipse.beans.ui.search.internal;

import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Torsten Juergeleit
 */
public class BeansMatch {

	private IModelElement element;

	public BeansMatch(IModelElement element) {
		this.element = element;
	}

	public final IModelElement getElement() {
		return element;
	}

	public int hashCode() {
		return element.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof BeansMatch)) {
			return false;
		}
		return ((BeansMatch) obj).getElement().equals(element);
	}
}
