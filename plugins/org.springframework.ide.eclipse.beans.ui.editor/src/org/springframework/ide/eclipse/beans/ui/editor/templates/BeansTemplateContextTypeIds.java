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
package org.springframework.ide.eclipse.beans.ui.editor.templates;

import org.springframework.ide.eclipse.beans.ui.editor.Activator;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public interface BeansTemplateContextTypeIds {

	public static final String PREFIX = Activator.PLUGIN_ID
			+ ".templates.contextType.";

	public static final String ALL = PREFIX + "all";

	public static final String BEAN = PREFIX + "bean";

	public static final String PROPERTY = PREFIX + "property";
}
