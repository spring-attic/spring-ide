/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.livexp;

import java.util.Collections;
import java.util.Set;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * This is like LiveSet, but it can only be 'observed' not mutated.
 * <p>
 * Note, this should really only be used to create instances of LiveExpression<Set<?>> by calling
 * 'new'. You should not use this type in method signatures and the like. ObservableSet<T> really
 * should be an 'alias' for LiveExpression<Set<T>> but instead it is a sub-type. Methods that
 * explicitly use ObservableSet<T> in their signatures are therfore almost certainly being too specific
 * about what they expect / return.
 */
public abstract class ObservableSet<T> extends LiveExpression<Set<T>> {

	@SuppressWarnings("unchecked")
	public ObservableSet() {
		super(Collections.EMPTY_SET);
	}

}
