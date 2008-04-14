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
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.io.Serializable;

import org.springframework.beans.factory.config.BeanReference;

/**
 * {@link Serializable} implementation of the {@link BeanReference} interface.
 * @author Christian Dupuis
 * @since 2.0.5
 */
@SuppressWarnings("serial")
public class SerializableRuntimeBeanReference implements BeanReference, Serializable {

	private String beanName;

	private boolean toParent;

	private Object source;

	/**
	 * @param beanName the beanName to set
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * @param toParent the toParent to set
	 */
	public void setToParent(boolean toParent) {
		this.toParent = toParent;
	}

	public String getBeanName() {
		return this.beanName;
	}

	/**
	 * Return whether this is an explicit reference to a bean in the parent factory.
	 */
	public boolean isToParent() {
		return this.toParent;
	}

	/**
	 * Set the configuration source <code>Object</code> for this metadata element.
	 * <p>
	 * The exact type of the object will depend on the configuration mechanism used.
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	public Object getSource() {
		return this.source;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SerializableRuntimeBeanReference)) {
			return false;
		}
		SerializableRuntimeBeanReference that = (SerializableRuntimeBeanReference) other;
		return (this.beanName.equals(that.beanName) && this.toParent == that.toParent);
	}

	@Override
	public int hashCode() {
		int result = this.beanName.hashCode();
		result = 29 * result + (this.toParent ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return '<' + getBeanName() + '>';
	}
}
