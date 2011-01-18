/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa;

import org.eclipse.ui.IStartup;

/**
 * {@link IStartup} implementation just to trigger loading of the UAA bundle.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class Startup implements IStartup {

	/**
	 * {@inheritDoc}
	 */
	public void earlyStartup() {
		// nothing to do
	}

}
