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

package org.springframework.ide.eclipse.beans.ui.search.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.search.BeansSearchPlugin;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Torsten Juergeleit
 */
public class BeansClassQuery extends AbstractBeansQuery {

	private String className;

	public BeansClassQuery(BeansSearchScope scope, String className) {
		super(scope);
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public String getLabel() {
		Object[] args = new Object[] { className,
									   getSearchScope().getDescription() };
		return BeansSearchPlugin.getFormattedMessage("ClassSearch.label",
													 args);
	}

	protected boolean doesMatch(IModelElement element,
								IProgressMonitor monitor) {
		if (element instanceof IBean) {
			IBean bean = (IBean) element;

			// Compare given class name with bean's one
			if (className.equals(bean.getClassName())) {
				return true;
			}
		}
		return false;
	}
}
