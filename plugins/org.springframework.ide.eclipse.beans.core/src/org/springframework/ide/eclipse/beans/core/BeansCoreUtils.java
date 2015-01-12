/*******************************************************************************
 * Copyright (c) 2004, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * Some helper methods for the Spring IDE core model.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public class BeansCoreUtils {
	
	/**
	 * Scheduling rule for Beans model initilization job
	 */
	public static final ISchedulingRule BEANS_MODEL_INIT_RULE = lightRule("Beans Model Initialization"); //$NON-NLS-1$

	/**
	 * Returns <code>true</code> if given resource is a Spring bean factory
	 * config file.
	 * <p>
	 * Calling is method is equivalent to call
	 * {@link #isBeansConfig(IResource, true)}
	 */
	public static boolean isBeansConfig(IResource resource) {
		return isBeansConfig(resource, true);
	}

	/**
	 * Returns <code>true</code> if given resource is a Spring bean factory
	 * config file. If <code>includeImported</code> is provided as
	 * <code>true</code> the imported configs are queried as well.
	 * 
	 * @since 2.0.3
	 */
	public static boolean isBeansConfig(IResource resource, boolean includeImported) {
		if (resource instanceof IFile) {
//			return BeansCorePlugin.getModel().getConfig((IFile) resource, includeImported) != null;
			return BeansCorePlugin.getModel().isConfig((IFile) resource, includeImported);
		}
		return false;
	}
	
	private static ISchedulingRule lightRule(final String name) {
		return new ISchedulingRule() {
			
			public boolean contains(ISchedulingRule rule) {
				return rule == this;
			}

			public boolean isConflicting(ISchedulingRule rule) {
				return rule == this || rule.contains(this);
			}
			
			@Override
			public String toString() {
				return name;
			}
			
		};
	}
}
