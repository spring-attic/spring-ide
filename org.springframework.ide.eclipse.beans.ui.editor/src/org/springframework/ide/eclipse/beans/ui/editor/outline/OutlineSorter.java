/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.editor.outline;

import org.eclipse.jface.viewers.ViewerSorter;
import org.w3c.dom.Node;

public class OutlineSorter extends ViewerSorter {

	// Categories
	public static final int IMPORT = 1;
	public static final int ALIAS = 2;
	public static final int BEAN = 3;
	public static final int CONSTRUCTOR = 4;
	public static final int PROPERTY = 5;
	public static final int VALUE = 6;

	public int category(Object element) {
		String nodeName = ((Node) element).getNodeName();
		if ("import".equals(nodeName)) {
	        return IMPORT;
	    } else if ("alias".equals(nodeName)) {
	        return ALIAS;
	    } else if ("bean".equals(nodeName)) {
	        return BEAN;
	    } else if ("constructor-arg".equals(nodeName)) {
	        return CONSTRUCTOR;
	    } else if ("property".equals(nodeName)) {
	        return PROPERTY;
	    } else if ("value".equals(nodeName)) {
	        return VALUE;
	    }
	    return 0;
	}
}
