/*******************************************************************************
 * Copyright (c) 2006 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.Set;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

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
 * @author Leo Dos Santos
 */
public class BeansModelPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IFile && "isBeansConfig".equals(property)) {
			boolean isBeansConfig = BeansCoreUtils.isBeansConfig((IFile) receiver, true);
			return expectedValue == null ? isBeansConfig : isBeansConfig == ((Boolean) expectedValue).booleanValue();
		} else if (receiver instanceof IBean && "isInfrstructureBean".equals(property)) {
			boolean isInfrstructureBean = ((IBean) receiver).isInfrastructure();
			return expectedValue == null ? isInfrstructureBean : isInfrstructureBean == ((Boolean) expectedValue)
					.booleanValue();
		} else if (receiver instanceof IBeansConfig && "canAddToConfigSet".equals(property)) {
			 IBeansConfig config = (IBeansConfig) receiver;
			 boolean canAddToConfigSet = hasAvailableConfigSet(config);
			 return expectedValue == null ? canAddToConfigSet : canAddToConfigSet == ((Boolean) expectedValue);
		} else if (receiver instanceof IBeansConfigSet && "isEmptyConfigSet".equals(property)) {
			IBeansConfigSet configSet = (IBeansConfigSet) receiver;
			boolean isEmpty = configSet.getConfigs().isEmpty();
			return expectedValue == null ? isEmpty : isEmpty == ((Boolean) expectedValue);
		}
		return false;
	}
	
	private boolean hasAvailableConfigSet(IBeansConfig config) {
		IBeansProject project = BeansModelUtils.getProject(config);
		Set<IBeansConfigSet> configSets = project.getConfigSets();
		for (IBeansConfigSet configSet : configSets) {
			if (!configSet.hasConfig(config.getElementName())) {
				return true;
			}
		}
		return false;
	}
	
}
