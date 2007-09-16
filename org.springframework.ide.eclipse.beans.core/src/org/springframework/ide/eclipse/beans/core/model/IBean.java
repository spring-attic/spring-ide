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
package org.springframework.ide.eclipse.beans.core.model;

import java.util.Set;

import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * Holds all data of a Spring bean.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IBean extends IBeansModelElement, ISourceModelElement {

	/**
	 * Returns the name of the parent bean (in case of a child bean) or null
	 * (in case of a root bean).
	 */
	String getParentName();

	String getClassName();

	public boolean isRootBean();

	public boolean isChildBean();

	public boolean isInnerBean();

	public boolean isSingleton();

	public boolean isAbstract();

	public boolean isLazyInit();

	public boolean isFactory();
	
	public boolean isInfrastructure();

	/**
	 * Returns array of aliases defined for this bean or <code>null</code> if
	 * no alias is defined.
	 */
	String[] getAliases();

	Set<IBeanConstructorArgument> getConstructorArguments();
	
	/**
	 * Returns a set of {@link IBeanMethodOverride}s.
	 * @since 2.0.2
	 */
	Set<IBeanMethodOverride> getMethodOverrides();

	IBeanProperty getProperty(String name);

	Set<IBeanProperty> getProperties();
}
