/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.dashboard;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * A page that can be added to the (new) Dashboard should implement this
 * interface.
 * 
 * @author Kris De Volder
 */
public interface IDashboardPage {

	/**
	 * Name of the page. Displayed as label on a Tab.
	 */
	public String getName();
	
	/**
	 * Called to create the widgetry on the page. Note that this is only
	 * called when the page is first made visible (user clicks on the Tab).
	 */
	public void createContents(IWorkbenchPartSite site, Composite parent);

	/**
	 * Called when the dashboard is closed and all pages get disposed.
	 */
	public void dispose();
	
}
