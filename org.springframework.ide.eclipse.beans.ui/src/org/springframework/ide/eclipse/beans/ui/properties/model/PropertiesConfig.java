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
package org.springframework.ide.eclipse.beans.ui.properties.model;

import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * This class defines a Spring beans configuration.
 * 
 * @author Torsten Juergeleit
 */
public class PropertiesConfig extends BeansConfig {

	public PropertiesConfig(PropertiesConfigSet configSet, String configName) {
		super((IBeansProject) configSet.getElementParent(), configName);
		setElementParent(configSet);
	}
}
