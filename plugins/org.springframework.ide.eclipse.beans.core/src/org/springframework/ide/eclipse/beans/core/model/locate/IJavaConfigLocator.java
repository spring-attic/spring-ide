/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.locate;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Extension interface to be implemented by third parties who want to provide auto-discovery
 * mechanisms for {@link IBeansConfig}s.
 * <p>
 * This extension to the {@link IBeansConfigLocator} interface locates JDT {@link IType}s to be
 * marked as {@link IBeansConfig}s.
 * <p>
 * Note: implementations of this interface do not need to be thread-safe.
 * 
 * @author Leo Dos Santos
 * @since 3.4.0
 */
public interface IJavaConfigLocator extends IBeansConfigLocator {

	/**
	 * Locates {@link IType} instances that should be {@link IBeansConfig} for the supplied
	 * {@link IProject}.
	 * <p>
	 * Note: Implementations of this interface are not allowed to put {@link IBeansConfig} instances
	 * in the {@link IBeansProject} themselves. This method is not allowed to return
	 * <code>null</code>.
	 * @return a set of {@link IType}
	 */
	Set<IType> locateJavaConfigs(IProject project, IProgressMonitor monitor);
	
}
