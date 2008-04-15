/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.metadata;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.annotation.AnnotationMemberValuePair;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.util.ObjectUtils;

/**
 * Abstract base annotation meta data implementation.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public abstract class AbstractAnnotationMetadata implements IClassMetadata, IAdaptable,
		Serializable {
	
	private static final Set<IMethodMetadata> EMPTY_METHOD_METADATA = Collections.emptySet();

	private static final long serialVersionUID = -8338005903818492219L;

	private String beanId;

	private String handle;

	private IModelSourceLocation location;

	private Object value;

	private Set<IMethodMetadata> methodMetadata;

	public AbstractAnnotationMetadata(IBean bean, String handle, Object value,
			IModelSourceLocation location) {
		this(bean, handle, value, location, EMPTY_METHOD_METADATA);
	}

	public AbstractAnnotationMetadata(IBean bean, String handle, Object value,
			IModelSourceLocation location, Set<IMethodMetadata> methodMetadata) {
		this.handle = handle;
		this.value = value;
		this.beanId = bean.getElementID();
		this.location = location;
		this.methodMetadata = methodMetadata;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractAnnotationMetadata)) {
			return false;
		}
		AbstractAnnotationMetadata that = (AbstractAnnotationMetadata) other;
		if (!ObjectUtils.nullSafeEquals(this.value, that.value))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.handle, that.handle))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.beanId, that.beanId))
			return false;
		return ObjectUtils.nullSafeEquals(this.location, that.location);
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IBean.class) {
			return getBean();
		}
		return null;
	}

	public IBean getBean() {
		return (IBean) BeansCorePlugin.getModel().getElement(beanId);
	}

	public String getClassHandle() {
		return handle;
	}

	public IModelSourceLocation getElementSourceLocation() {
		return this.location;
	}

	public String getHandleIdentifier() {
		return beanId;
	}

	public String getKey() {
		return getClassHandle();
	}

	public Set<IMethodMetadata> getMethodMetaData() {
		return methodMetadata;
	}

	public Object getValue() {
		return value;
	}

	@SuppressWarnings("unchecked")
	public String getValueAsText() {
		if (getValue() instanceof Set) {
			StringBuilder buf = new StringBuilder();
			for (AnnotationMemberValuePair pair : (Set<AnnotationMemberValuePair>) getValue()) {
				if (pair.getName() != null) {
					buf.append(pair.getName());
					buf.append(" = ");
				}
				buf.append(pair.getValue().toString());
				buf.append(", ");
			}

			if (buf.length() > 0) {
				return buf.substring(0, buf.length() - 2) + " - ";
			}
			return "";
		}
		return "";
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(value);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(handle);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(beanId);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(location);
		return 12 * hashCode;
	}

}
