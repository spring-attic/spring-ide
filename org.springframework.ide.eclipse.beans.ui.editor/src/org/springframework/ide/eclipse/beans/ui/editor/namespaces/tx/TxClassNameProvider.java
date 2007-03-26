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
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.tx;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.eclipse.beans.ui.editor.namespaces.IClassNameProvider;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.w3c.dom.Element;

public class TxClassNameProvider implements IClassNameProvider {

	private static Map<String, String> elementToClassNameMapping;

	static {
		elementToClassNameMapping = new HashMap<String, String>();
		elementToClassNameMapping.put("advice", TransactionInterceptor.class
				.getName());
	}

	public String getClassNameForElement(Element elem) {
		return elementToClassNameMapping.get(elem.getLocalName());
	}

}
