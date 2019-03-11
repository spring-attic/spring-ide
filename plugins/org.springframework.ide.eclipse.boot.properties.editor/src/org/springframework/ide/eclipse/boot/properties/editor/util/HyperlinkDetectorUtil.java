/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.util;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

public class HyperlinkDetectorUtil {

	public static IHyperlinkDetector[] merge(IHyperlinkDetector[] a, IHyperlinkDetector b) {
		//This should really be a generic method working on array of any type, but it seems
		// impossible / hard to implement because of how generic arrays work in Java.
		// (i.e. if argument 'a' and b arenull then there is really no way to create an array of
		// its type, as there's no way to obtain its runtime type without an instance.

		if (a==null || a.length==0) {
			return new IHyperlinkDetector[] {b};
		}
		if (b==null) {
			return a;
		}
		IHyperlinkDetector[] merged = new IHyperlinkDetector[a.length+1];
		System.arraycopy(a, 0, merged, 0, a.length);
		merged[a.length] = b;
		return merged;
	}

}
