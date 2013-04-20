/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.properties.model;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * This class defines a Spring beans Java configuration.
 * @author Martin Lippert
 */
public class PropertiesJavaConfig extends BeansJavaConfig {

	public PropertiesJavaConfig(PropertiesProject project, IType configClass, String configClassName, Type type) {
		super(project, configClass, configClassName, type);
	}

	public PropertiesJavaConfig(PropertiesConfigSet configSet, IType configClass, String configClassName, Type type) {
		super((IBeansProject) configSet.getElementParent(), configClass, configClassName, type);

		// After initializing the config with the corresponding project
		// (required for retrieving the file) change the parent to the given
		// config set
		setElementParent(configSet);
	}
}
