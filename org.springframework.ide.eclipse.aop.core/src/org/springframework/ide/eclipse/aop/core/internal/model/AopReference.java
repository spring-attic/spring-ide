/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.util.ObjectUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReference implements IAopReference, IAdaptable,
		IPersistableElement {

	private String bean;

	private IAspectDefinition definition;

	private IResource file;

	private IMember source;

	private IMember target;

	private ADVICE_TYPES type;

	public AopReference(ADVICE_TYPES type, IMember source, IMember target,
			IAspectDefinition def, IResource file, IBean bean) {
		this(type, source, target, def, file, bean.getElementID());
	}

	public AopReference(ADVICE_TYPES type, IMember source, IMember target,
			IAspectDefinition def, IResource file, String beanId) {
		this.type = type;
		this.source = source;
		this.target = target;
		this.definition = def;
		this.file = file;
		this.bean = beanId;
	}

	public AopReference(ADVICE_TYPES type, IMember source, IMember target,
			IResource file, String beanId) {
		this(type, source, target, null, file, beanId);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AopReference) {
			AopReference other = (AopReference) obj;
			return getTarget().equals(other.getTarget())
					&& ((getSource() == null && other.getSource() == null) || (getSource() != null && getSource()
							.equals(other.getSource())))
					&& getResource().equals(other.getResource())
					&& getDefinition().getAspectLineNumber() == other
							.getDefinition().getAspectLineNumber();
		}
		return false;
	}

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPersistableElement.class)) {
			return this;
		}
		return null;
	}

	public ADVICE_TYPES getAdviceType() {
		return this.type;
	}

	public IAspectDefinition getDefinition() {
		return definition;
	}

	public String getFactoryId() {
		return AopReferenceElementFactory.FACTORY_ID;
	}

	public IResource getResource() {
		return file;
	}

	public IMember getSource() {
		return this.source;
	}

	public IMember getTarget() {
		return this.target;
	}

	public String getTargetBeanId() {
		return this.bean;
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(source);
		hashCode = 21 + ObjectUtils.nullSafeHashCode(target);
		hashCode = 24 + ObjectUtils.nullSafeHashCode(file);
		hashCode = 12 + ObjectUtils.nullSafeHashCode(definition
				.getAspectLineNumber());
		return hashCode;
	}

	public void saveState(IMemento memento) {
		memento.putString(AopReferenceElementFactory.ADVICE_TYPE_ATTRIBUTE,
				this.type.toString());
		if (this.source != null) {
			memento.putString(AopReferenceElementFactory.SOURCE_ATTRIBUTE,
					this.source.getHandleIdentifier());
		}
		if (this.target != null) {
			memento.putString(AopReferenceElementFactory.TARGET_ATTRIBUTE,
					this.target.getHandleIdentifier());
		}
		if (this.file != null) {
			memento.putString(AopReferenceElementFactory.FILE_ATTRIBUTE,
					this.file.getFullPath().toString());
		}
		memento.putString(AopReferenceElementFactory.BEAN_ATTRIBUTE, this.bean);
	}

	public void setDefinition(IAspectDefinition definition) {
		this.definition = definition;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Bean definition [");
		buf.append(this.bean);
		buf.append("] advise target [");
		buf.append(this.target);
		buf.append("] advise source [");
		buf.append(this.source);
		buf.append(" ] aspect definition [");
		buf.append(this.definition);
		buf.append("]");
		return buf.toString();
	}
}
