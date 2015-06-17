/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.livexp;

import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * Represents a selection of zero or more elements of type T in some UI component.
 *
 * @author Kris De Volder
 */
public final class MultiSelection<T> {

	public static <T> MultiSelection<T> empty(Class<T> type) {
		return new MultiSelection<T>(type, LiveSets.emptySet(type));
	}

	public static <T> MultiSelection<T> union(MultiSelection<T> a, MultiSelection<T> b) {
		Assert.isLegal(a.getElementType().equals(b.getElementType()));
		return from(a.getElementType(), LiveSets.union(a.getElements(), b.getElements()));
	}

	private final Class<T> elementType;
	private final LiveExpression<Set<T>> elements;

	public MultiSelection(Class<T> elementType, LiveExpression<Set<T>> elements) {
		this.elementType = elementType;
		this.elements = elements;
	}

	public Class<T> getElementType() {
		return elementType;
	}

	/**
	 * Convert a selection of one type into a selection of a different
	 * type. The conversion only succeeds if the target-type is assignment
	 * compatible with the source type.
	 *
	 * @return Converted selection or null if the conversion is not legal.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <U> MultiSelection<U> as(Class<U> toElementType) {
		if (toElementType.isAssignableFrom(elementType)) {
			return new MultiSelection<U>(toElementType, ((LiveExpression) elements));
		} else {
			return null;
		}
	}

	/**
	 * Convert a selection of one type into a selection of a different
	 * type. The conversion only succeeds if the target-type is assignment
	 * compatible with the source type.
	 *
	 * @return Converted selection
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <U> MultiSelection<U> cast(Class<U> toElementType) throws ClassCastException {
		MultiSelection<U> converted = as(toElementType);
		if (converted==null) {
			throw new ClassCastException(getElementType().getName()+" => "+toElementType.getName());
		}
		return converted;
	}


	public LiveExpression<Set<T>> getElements() {
		return elements;
	}

	public static <T> MultiSelection<T> from(Class<T> type, LiveExpression<Set<T>> elements) {
		return new MultiSelection<T>(type, elements);
	}

	public Set<T> getValue() {
		return getElements().getValue();
	}

	/**
	 * @return The only element in the selection, if exactly one element is selected; or null
	 * otherwise.
	 */
	public T getSingle() {
		Set<T> es = getValue();
		if (es.size()==1) {
			for (T t : es) {
				return t;
			}
		}
		return null;
	}

}
