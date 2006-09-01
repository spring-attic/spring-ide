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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;

/**
 * This <code>PropertyTester</code> is used to check properties of the
 * BeansCoreModel in <code><test property="..."/></code> expressions.
 * <p>
 * Currently the following properties are supported:
 * <ul>
 * <li><strong>isBeansConfig</strong> checks if a given <code>IFile</code>
 * is a BeansConfig file</li>
 * </ul>
 * 
 * @author Torsten Juergeleit
 */
public class BeansModelPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof IFile && "isBeansConfig".equals(property)) {
			boolean isBeansConfig = BeansCoreUtils
					.isBeansConfig((IFile) receiver);
			return expectedValue == null ? isBeansConfig :
					isBeansConfig == ((Boolean) expectedValue).booleanValue();
		}
		return false;
	}
}
