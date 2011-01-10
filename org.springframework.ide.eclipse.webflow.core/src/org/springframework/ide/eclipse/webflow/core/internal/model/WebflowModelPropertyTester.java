/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;

/**
 * This <code>PropertyTester</code> is used to check properties of the
 * {@link IWebflowModel} in <code><test property="..."/></code> expressions.
 * <p>
 * Currently the following properties are supported:
 * <ul>
 * <li><strong>isWebflowConfig</strong> checks if a given <code>IFile</code>
 * is a {@link IWebflowConfig} file</li>
 * </ul>
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowModelPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof IFile && "isWebflowConfig".equals(property)) {
			IFile file = (IFile) receiver;
			boolean isWebflowConfig = WebflowModelUtils.isWebflowConfig(file);
			return expectedValue == null ? isWebflowConfig
					: isWebflowConfig == ((Boolean) expectedValue)
							.booleanValue();
		}
		return false;
	}
}
