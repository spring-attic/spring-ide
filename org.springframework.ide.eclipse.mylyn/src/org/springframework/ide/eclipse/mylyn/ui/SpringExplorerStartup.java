/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.mylyn.ui;

import org.eclipse.mylyn.context.ui.IContextUiStartup;

/**
 * @author Leo Dos Santos
 * @since 2.2.7
 */
public class SpringExplorerStartup implements IContextUiStartup {

	public void lazyStartup() {
		// No-op. Forces the bundle to load so that the Spring Explorer focus
		// action has proper enablement state.
	}

}
