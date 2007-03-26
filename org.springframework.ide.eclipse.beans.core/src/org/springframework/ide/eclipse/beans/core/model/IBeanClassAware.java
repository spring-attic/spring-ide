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

/**
 * Interface to be implemented by model elements that know about
 * {@link IBean}s and their full-qualified class name.
 * 
 * @author Torsten Juergeleit
 */
public interface IBeanClassAware {

	/**
	 * Returns <code>true</code> if given full qualified class name is a bean
	 * class used within this Beans model element. 
	 */
	boolean isBeanClass(String className);

	Set<String> getBeanClasses();

	/**
	 * Returns a list of beans which are using the given class as their bean
	 * class.
	 * @param className  full qualified name of bean class
	 */
	Set<IBean> getBeans(String className);
}
