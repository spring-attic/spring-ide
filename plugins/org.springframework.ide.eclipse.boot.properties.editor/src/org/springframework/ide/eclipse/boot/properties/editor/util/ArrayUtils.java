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
package org.springframework.ide.eclipse.boot.properties.editor.util;

/**
 * @author Kris De Volder
 */
public class ArrayUtils {

	public static <T> boolean hasElements(T[] arr) {
		return arr!=null && arr.length>0;
	}

	public static <T> T lastElement(T[] arr) {
		if (hasElements(arr)) {
			return arr[arr.length-1];
		}
		return null;
	}

}
