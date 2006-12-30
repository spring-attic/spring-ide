/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui;

import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class provides constants and helper methods for the beans UI.
 * 
 * @author Torsten Juergeleit
 */
public class BeansUILabels {

	/** String for separating post qualified names (" - ") */
	public final static String CONCAT_STRING = " - ";

	/** String for separating list items (", ") */
	public final static String LIST_DELIMITER_STRING = ", ";

	/** String for ellipsis ("...") */
	public final static String ELLIPSIS_STRING = "...";

	/** Add full path name and concat string */
	public final static int PREPEND_PATH = 1 << 0;

	/** Add concat string and full path name */
	public static final int APPEND_PATH = 1 << 1;

	/** Add element description insted of element name */
	public static final int DESCRIPTION = 1 << 2;

	protected static final boolean isFlagged(int flags, int flag) {
		return (flags & flag) != 0;
	}

	protected static boolean appendNodeName(ISourceModelElement element,
			StringBuffer buf) {
		String nodeName = ModelUtils.getNodeName(element);
		if (nodeName != null) {
			buf.append('<').append(nodeName).append("/>");
			return true;
		}
		return false;
	}
}
