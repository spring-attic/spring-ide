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
package org.springframework.ide.eclipse.beans.ui.editor.util;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavadocContentAccess;

/**
 * @author Christian Dupuis
 */
public class BeansJavaDocUtils {

	private final IMember element;

	public BeansJavaDocUtils(IMember element) {
		this.element = element;
	}

	public String getJavaDoc() {

		try {
			StringBuffer buf = new StringBuffer();
			Reader reader = JavadocContentAccess.getHTMLContentReader(element,
					false, false);
			if (reader != null) {
				int charValue = 0;
				while ((charValue = reader.read()) != -1) {
					buf.append((char) charValue);
				}
				return buf.toString();
			}
		}
		catch (JavaModelException e) {
		}
		catch (IOException e) {
		}

		return "";
	}
}
