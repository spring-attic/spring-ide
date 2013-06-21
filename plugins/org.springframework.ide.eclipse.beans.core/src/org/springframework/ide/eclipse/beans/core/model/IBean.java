/*******************************************************************************
 * Copyright (c) 2004, 2013 Spring IDE Developers and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *     GoPivotal, Inc.       - performance optimizations
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model;

import java.util.Collection;
import java.util.Set;

import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * Holds all data of a Spring bean.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public interface IBean extends IBeansModelElement, ISourceModelElement {
	
	/**
	 * Returns the name of the parent bean (in case of a child bean) or null
	 * (in case of a root bean).
	 */
	String getParentName();

	String getClassName();

	boolean isRootBean();

	boolean isChildBean();
	
	boolean isInnerBean();

	boolean isSingleton();

	boolean isAbstract();

	boolean isLazyInit();

	boolean isFactory();
	
	boolean isInfrastructure();
	
	/**
	 * Returns <code>true</code> if the bean id or name is generated
	 * @since 2.2.2 
	 */
	boolean isGeneratedElementName();

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

	Collection<IBeanProperty> getProperties();
	
	/**
	 * Returns <b>all</b> implemented interfaces of this bean class.
	 * @since 2.2.3 
	 */
//	Set<String> getInterfaceNames();
	
	/**
	 * Returns <b>all</b> super class of this bean class.
	 * @since 2.2.3
	 */
//	Set<String> getSuperClassNames();
	
}
