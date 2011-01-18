/*******************************************************************************
 * Copyright (c) 2008, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.osgi.framework.Bundle;

/**
 * {@link IDynamicVariableResolver} that is capable to resolve
 * <code>$
 * {bundle_loc}</code> to the bundle location.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class BundleLocationVariableResolver implements IDynamicVariableResolver {
	
	public String resolveValue(IDynamicVariable variable, String argument)
			throws CoreException {
		Bundle bundle = Platform.getBundle(argument);
		if (bundle != null) {
			try {
				String path = FileLocator.toFileURL(bundle.getEntry("/")).getPath();
				if (path != null && path.endsWith(File.separator)) {
					return path.substring(0, path.length() - 1);
				}
				return path;
			}
			catch (IOException e) {
				// ignore
			}
		}
		return null;
	}

}
