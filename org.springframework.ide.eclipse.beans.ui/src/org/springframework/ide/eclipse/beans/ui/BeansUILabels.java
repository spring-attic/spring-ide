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

	public static final boolean isFlagged(int flags, int flag) {
		return (flags & flag) != 0;
	}

	public static boolean appendNodeName(ISourceModelElement element,
			StringBuffer buf) {
		String nodeName = ModelUtils.getNodeName(element);
		if (nodeName != null) {
			buf.append(nodeName);
			return true;
		}
		return false;
	}
}
