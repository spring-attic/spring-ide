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

import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.util.ObjectUtils;

/**
 * This class defines an alias within a Spring beans configuration.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeanAlias extends AbstractBeansModelElement implements IBeanAlias {

	private String beanName;

	public BeanAlias(IBeansConfig config, AliasDefinition definition) {
		super(config, definition.getAlias(), definition);
		beanName = definition.getBeanName();
	}

	public int getElementType() {
		return IBeansModelElementTypes.ALIAS_TYPE;
	}

	public String getBeanName() {
		return beanName;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanAlias)) {
			return false;
		}
		BeanAlias that = (BeanAlias) other;
		if (!ObjectUtils.nullSafeEquals(this.beanName, that.beanName)) return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(beanName);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(super.toString());
		text.append(": name=");
		text.append(beanName);
		return text.toString();
	}
}
