/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes;

import org.eclipse.core.runtime.Assert;

/**
 * Represents a component of a RouteBinding with either a specific
 * or 'randomized' value. E.g a Randomized `port` specifies either
 * a specific port number, or otherwise it specifies that the port should
 * be assigned randomly by the cf-client.
 */
public class Randomized<T> {

	private T value; //When null, it means 'choose randomly'

	private Randomized(T value) {
		this.value = value;
	}

	public boolean isRandomized() {
		return value==null;
	}

	public T getValue() {
		Assert.isLegal(value!=null);
		return value;
	}

	public static <T> Randomized<T> random() {
		return new Randomized<>(null);
	}

	public static <T> Randomized<T> justOrNull(T value) {
		if (value!=null) {
			return new Randomized<>(value);
		}
		return null;
	}

	@Override
	public String toString() {
		if (isRandomized()) {
			return "?";
		} else {
			return value.toString();
		}
	}
}
