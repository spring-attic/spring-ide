/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

/**
 * A FactoryWithParam creates objects of a given type, given some other object
 * as a parameter.
 *
 * @author Kris De Volder
 *
 * @param <P> Type of parameter
 * @param <T> Type of object created
 */
public interface FactoryWithParam<P, T> {

	/**
	 * Some factories keep a 'cache' of objects so they can to
	 * reuse the same object instead of creating many object representing
	 * the same entity.
	 * <p>
	 * Such factories should implement this method to allow clients to
	 * indicate when they are done with a given element, and so the
	 * factory in turn can remove the object from its cache to avoid
	 * memory leaks.
	 * <p>
	 * Note: it might seem more logical to pass in the actual object here, instead
	 * of the paramter used to create it, but it is typically easier for
	 * the factory to this function (no need to search map by value instead of key).
	 */
	void disposed(P delegate);



}
