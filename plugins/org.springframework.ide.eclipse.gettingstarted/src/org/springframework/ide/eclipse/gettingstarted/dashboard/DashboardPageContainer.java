/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.dashboard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Container for a 'IDashboardPage' instance. Manages the life cycle of
 * a DashboardPage contents. I.e. this takes care of lazyly creating
 * the widgets on the page when it is first made visible.
 * 
 * @author Kris De Volder
 */
public class DashboardPageContainer {

	private IWorkbenchPartSite site;
	private CTabItem widget;
	private IDashboardPage page;
	private Composite composite = null;

	public DashboardPageContainer(IDashboardPage page) {
		this.page = page;
	}

	public String getName() {
		return page.getName();
	}

	public void setWidget(CTabItem widget) {
		this.widget = widget;
	}
	
	@Override
	public String toString() {
		return "DashboardPage("+getName()+")";
	}

	public void initialize(IWorkbenchPartSite site) { 
		if (composite==null && page!=null) {
			this.site = site;
			composite = new Composite(widget.getParent(), SWT.NONE);
			//To see better where the composite is on the page:
			//Color color = Display.getDefault().getSystemColor(SWT.COLOR_RED); 
			//composite.setBackground(color);
			page.createContents(site, composite);
			widget.setControl(composite);
		}
	}

	public void dispose() {
		if (page!=null) {
			page.dispose();
			page = null;
		}
	}	
}
