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

/**
 * Defines constants which are used to refer to values in the plugin's
 * preference bundle.
 * @author Torsten Juergeleit
 */
public interface IPreferencesConstants {
	String PREFIX = BeansUIPlugin.PLUGIN_ID + ".";

	String VIEW_SORT = PREFIX + "view.outline.sort";
	String VIEW_LINK = PREFIX + "view.outline.link";
}
