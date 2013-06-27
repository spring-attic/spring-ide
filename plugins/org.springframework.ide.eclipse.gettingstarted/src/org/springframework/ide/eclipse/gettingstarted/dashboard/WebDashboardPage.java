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

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.gettingstarted.browser.BrowserFactory;
import org.springframework.ide.eclipse.gettingstarted.browser.STSBrowserViewer;


//Note: some complications on Linux systems because of problems satisfying
// the requirements for SWT browser component to work.
//For Ubuntu 12.04 some usefull info here.
//Maybe this can be somehow fixed by us packaging a compatible xulrunner
//and STS.ini file?

// http://askubuntu.com/questions/125980/how-do-i-install-xulrunner-in-12-04

/**
 * A DashBoard page that displays the contents of a webpage.
 * 
 * @author Kris De Volder
 */
public class WebDashboardPage extends ADashboardPage /* implements IExecutableExtension*/ {

	/**
	 * Using this ID ensures we only open one 'slave' browser when opening links from within
	 * a dashboard page.
	 */
	public static final String DASHBOARD_SLAVE_BROWSER_ID = WebDashboardPage.class.getName()+".SLAVE";

	/**
	 * The URL that will be displayed in this Dashboard webpage.
	 */
	private String homeUrl;

	private String name;

	private Shell shell;

	/**
	 * Constructor for when this class is used as n {@link IExecutableExtension}. In that case
	 * setInitializationData method will be called with infos from plugin.xml to fill
	 * in the fields.
	 */
	public WebDashboardPage() {
	}
	
	public WebDashboardPage(String name, String homeUrl) {
		this.name = name;
		this.homeUrl = homeUrl;
	}

	
//	@Override
//	public void setInitializationData(IConfigurationElement cfig,
//			String propertyName, Object data) {
//		if (data!=null && data instanceof Map) {
//			@SuppressWarnings("unchecked")
//			Map<String, String> map = (Map<String, String>) data;
//			this.name = map.get("name");
//			this.homeUrl = map.get("url");
//		}
//		Assert.isNotNull(this.name, "A name must be provided as initialization data for WebDashboardPage");
//		Assert.isNotNull(this.homeUrl, "A url must be provided as initialization data for WebDashboardPage");
//	}
	
	@Override
	public void createControl(Composite parent) {
		this.shell = parent.getShell();
		parent.setLayout(new FillLayout());
		STSBrowserViewer browserViewer = BrowserFactory.create(parent);
		Browser browser = browserViewer.getBrowser();
		if (homeUrl!=null) {
			browserViewer.setHomeUrl(homeUrl);
			browserViewer.setURL(homeUrl);
		} else {
			browser.setText("<h1>URL not set</h1>" +
					"<p>Url should be provided via the setInitializationData method</p>"
			);
		}
		addBrowserHooks(browser);
	}

	/**
	 * Subclasses may override this if they want to customize the browser (e.g. add listeners to
	 * handle certain urls specially.
	 */
	protected void addBrowserHooks(Browser browser) {
	}

	/**
	 * The url of the landing page this dashboard page will show when it is opened.
	 */
	public String getUrl() {
		return homeUrl;
	}
	
	/**
	 * Change the url this dashboard page will show when it is first opened,
	 * or when the user clicks on the 'home' icon. 
	 */
	public void setHomeUrl(String url) {
		this.homeUrl = url;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String string) {
		this.name = string;
	}


	
	public Shell getShell() {
		return shell;
	}

}
 