/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import org.eclipse.core.resources.IResource;

/**
 * Utility class
 * @author Christian Dupuis
 * @since 2.0.3
 */
public abstract class ModelUtils {
	
	public static String getFilePath(IResource resource) {
		String path = resource.getFullPath().removeLastSegments(1).toString();
		StringBuilder builder = new StringBuilder();
		builder.append(resource.getName());
		if (path.length() > 1) {
			builder.append(" - ");
			builder.append(path.substring(1));
		}
		return builder.toString(); 
	}
	
}
