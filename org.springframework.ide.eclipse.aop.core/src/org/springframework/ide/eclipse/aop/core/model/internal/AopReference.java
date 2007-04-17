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
package org.springframework.ide.eclipse.aop.core.model.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMember;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.util.ObjectUtils;

public class AopReference implements IAopReference {

	private ADVICE_TYPES type;

	private IMember source;

	private IMember target;

	private IAspectDefinition definition;

	private IResource file;

	private String bean;

	public AopReference(ADVICE_TYPES type, IMember source, IMember target,
			IAspectDefinition def, IResource file, IBean bean) {
		this.type = type;
		this.source = source;
		this.target = target;
		this.definition = def;
		this.file = file;
		this.bean = bean.getElementID();
	}

	public IAspectDefinition getDefinition() {
		return definition;
	}

	public ADVICE_TYPES getAdviceType() {
		return this.type;
	}

	public IMember getSource() {
		return this.source;
	}

	public IMember getTarget() {
		return this.target;
	}

	public IResource getResource() {
		return file;
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

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(source);
		hashCode = 21 + ObjectUtils.nullSafeHashCode(target);
		hashCode = 24 + ObjectUtils.nullSafeHashCode(file);
		hashCode = 12 + ObjectUtils.nullSafeHashCode(definition
				.getAspectLineNumber());
		return hashCode;
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

	public String getTargetBeanId() {
		return this.bean;
	}
}
