/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.properties;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.properties.XMLPropertySource;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class SpringConfigPropertySourceProvider implements IPropertySourceProvider {

	public IPropertySource getPropertySource(Object object) {
		if (object instanceof IDOMNode) {
			return new XMLPropertySource((IDOMNode) object);
		}
		else if (object instanceof IAdaptable) {
			Object adapter = ((IAdaptable) object).getAdapter(IPropertySource.class);
			if (adapter instanceof IPropertySource) {
				return (IPropertySource) adapter;
			}
		}
		return null;
	}

}
