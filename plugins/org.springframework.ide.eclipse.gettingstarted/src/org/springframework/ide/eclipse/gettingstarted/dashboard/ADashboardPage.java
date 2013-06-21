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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Abstract superclass to help implement IDashboardPage interface.
 * This class provides some common methods one may need/want to
 * call in order to access contextual info for the page. 
 * (E.g. the 'Shell' or 'IWorkbenchPartSite' associated with the page.
 */
public abstract class ADashboardPage implements IDashboardPage {

	private IWorkbenchPartSite site;

	@Override
	public void createContents(IWorkbenchPartSite site, Composite parent) {
		this.site = site;
		createControl(parent);
	}

	protected abstract void createControl(Composite parent);

	/**
	 * Shell associated with the page. This will return null if called 
	 * before the contents of the page is created. Typically that 
	 * should be ok since the shell is usually needed to do things
	 * in response to ui events for widgets in the page.
	 */
	public Shell getShell() {
		if (site!=null) {
			return site.getShell();
		}
		return null;
	}
	
	public IWorkbenchPartSite getSite() {
		return site;
	}
	
	@Override
	public void dispose() {
		this.site = null;
	}

}
