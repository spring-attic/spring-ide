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
package org.springframework.ide.eclipse.boot.dash.model.requestmappings;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Uiltily methods for extracting info out of the 'toString' values produced by java.lang.reflect.Method
 * objects.
 *
 * @author Kris De Volder
 */
public class JLRMethodParser {

	private static final Set<String> MODIFIERS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"public", "protected", "private", "abstract",
		    "static", "final", "synchronized", "native", "strictfp"
	)));


	public static String getFQClassName(String methodString) {
		if (methodString!=null) {
			// Example: public java.lang.Object org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint.invoke(java.security.Principal)
			// Example: java.util.Collection<demo.Reservation> demo.ReservationRestController.reservations()
			// public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)'

			//The spaces inside generics will mess this klunky parser up. So get rid of those first:
			methodString = methodString.replaceAll(",\\s", ",");
			String[] pieces = methodString.split("\\s");
			int modifiersEnd = 0;
			while (modifiersEnd<pieces.length && isModifier(pieces[modifiersEnd])) {
				modifiersEnd++;
			}
			if (pieces.length>=modifiersEnd+2) {
				methodString = pieces[modifiersEnd+1];
				int methodNameEnd = methodString.indexOf('(');
				if (methodNameEnd>=0) {
					int methodNameStart = methodString.lastIndexOf('.', methodNameEnd);
					if (methodNameStart>=0) {
						return methodString.substring(0, methodNameStart);
					}
				}
			}
		}
		return null;
	}

	private static boolean isModifier(String string) {
		return MODIFIERS.contains(string);
	}



}
