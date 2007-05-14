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
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.util.ObjectUtils;

/**
 * This class holds the data of a Spring {@link ComponentDefinition} defined via
 * an XML namespace.
 * 
 * @author Torsten Juergeleit
 */
public class BeansComponent extends AbstractBeansModelElement implements
		IBeansComponent {

	/** List of all beans which are defined within this component */
	private Set<IBean> beans = new LinkedHashSet<IBean>();

	/** List of all inner components which are defined within this component */
	private Set<IBeansComponent> components =
			new LinkedHashSet<IBeansComponent>();

	public BeansComponent(IModelElement parent,
			ComponentDefinition definition) {
		super(parent, definition.getName(), definition);
	}

	public int getElementType() {
		return IBeansModelElementTypes.COMPONENT_TYPE;
	}

	@Override
	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(beans);
		children.addAll(components);
		return children.toArray(new IModelElement[children.size()]);
	}

	public void addBean(IBean bean) {
		beans.add(bean);
	}

	public Set<IBean> getBeans() {
		return Collections.unmodifiableSet(beans);
	}

	public void addComponent(IBeansComponent component) {
		components.add(component);
	}

	public Set<IBeansComponent> getComponents() {
		return Collections.unmodifiableSet(components);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansComponent)) {
			return false;
		}
		BeansComponent that = (BeansComponent) other;
		if (!ObjectUtils.nullSafeEquals(this.beans, that.beans))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.components, that.components))
			return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(beans);
		hashCode = getElementType() * hashCode
				+ ObjectUtils.nullSafeHashCode(components);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": beans=");
		text.append(beans);
		text.append(", components=");
		text.append(components);
		return text.toString();
	}
	
	@Override
	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// Visit only this element
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {
			
			for (IBeansComponent carg : getComponents()) {
				carg.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			for (IBean bean : getBeans()) {
				bean.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}
}
