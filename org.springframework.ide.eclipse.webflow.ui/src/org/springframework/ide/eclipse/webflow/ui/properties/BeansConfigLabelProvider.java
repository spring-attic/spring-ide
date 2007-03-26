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
package org.springframework.ide.eclipse.webflow.ui.properties;

import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansConfigLabelProvider extends BeansModelLabelProvider {

	public BeansConfigLabelProvider() {
		super(true);
	}

	public String getText(Object obj) {
		if (obj instanceof IBeansConfig) {
			return super.getText(((IBeansConfig) obj).getElementResource());
		}
		else {
			return super.getText(obj);
		}
	}
}
