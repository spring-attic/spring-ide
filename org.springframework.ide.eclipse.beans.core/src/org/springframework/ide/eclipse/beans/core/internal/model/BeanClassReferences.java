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

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.util.ObjectUtils;

/**
 * Holder for information about the references from Spring beans to a bean
 * class.
 * 
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public class BeanClassReferences {

	private IType beanClass;
	private Set<IBean> beans;

	public BeanClassReferences(IType beanClass, Set<IBean> beans) {
		this.beanClass = beanClass;
		this.beans = beans;
	}

	public final IType getBeanClass() {
		return beanClass;
	}

	public final Set<IBean> getBeans() {
		return beans;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanClassReferences)) {
			return false;
		}
		BeanClassReferences that = (BeanClassReferences) other;
		if (!ObjectUtils.nullSafeEquals(this.beanClass, that.beanClass))
			return false;
		return ObjectUtils.nullSafeEquals(this.beans, that.beans);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(beanClass);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(beans);
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(": ");
		text.append(beanClass).append(" <- ").append(beans);
		return text.toString();
	}
}
