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
package org.springframework.ide.eclipse.boot.dash.model;

/**
 * Helper methods for creating common filters.
 *
 * @author Kris De Volder
 */
public class Filters {

	@SuppressWarnings("rawtypes")
	private static final Filter ACCEPT_ALL = new Filter() {
		public boolean accept(Object t) {
			return true;
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> Filter<T> acceptAll() {
		return ACCEPT_ALL;
	}

}
