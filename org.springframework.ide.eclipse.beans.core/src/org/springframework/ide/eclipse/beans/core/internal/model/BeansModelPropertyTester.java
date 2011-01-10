/*******************************************************************************
 * Copyright (c) 2006, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * This {@link PropertyTester} is used to check properties of the BeansCoreModel in <code><test property="..."/></code>
 * expressions.
 * <p>
 * Currently the following properties are supported:
 * <ul>
 * <li><strong>isBeansConfig</strong> checks if a given {@link IFile} is a BeansConfig file</li>
 * <li><strong>isInfrstructureBean</strong> checks if a given {@link IBean} is a
 * {@link BeanDefinition#ROLE_INFRASTRUCTURE}</li>
 * </ul>
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansModelPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IFile && "isBeansConfig".equals(property)) {
			boolean isBeansConfig = BeansCoreUtils.isBeansConfig((IFile) receiver, true);
			return expectedValue == null ? isBeansConfig : isBeansConfig == ((Boolean) expectedValue).booleanValue();
		}
		else if (receiver instanceof IBean && "isInfrstructureBean".equals(property)) {
			boolean isInfrstructureBean = ((IBean) receiver).isInfrastructure();
			return expectedValue == null ? isInfrstructureBean : isInfrstructureBean == ((Boolean) expectedValue)
					.booleanValue();
		}
		return false;
	}
}
