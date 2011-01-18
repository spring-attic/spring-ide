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

import org.springframework.ide.eclipse.uaa.IUaa;

/**
 * Implementations of this interface represent monitors that capture usage data within Eclipse.
 * <p>
 * Note: product and feature usage should be reported against the {@link UaaManager} instance provided with to the
 * {@link #startMonitoring(UaaManager)} method.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public interface IUsageMonitor {

	/**
	 * Called to notify that monitoring should begin now.
	 */
	void startMonitoring(IUaa manager);

	/**
	 * Called to notify that monitoring should be stopped.
	 * <p>
	 * All registered listeners and other resources <b>must</b> be clean when this method is being called.
	 */
	void stopMonitoring();

}
