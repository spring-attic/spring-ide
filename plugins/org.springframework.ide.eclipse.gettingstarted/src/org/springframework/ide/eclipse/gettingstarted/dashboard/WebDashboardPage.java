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

import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springsource.ide.eclipse.dashboard.ui.AbstractDashboardPage;

import static org.eclipse.ui.browser.IWorkbenchBrowserSupport.*;

/**
 * A DashBoard page that displays the contents of a webpage.
 * 
 * @author Kris De Volder
 */
public class WebDashboardPage extends AbstractDashboardPage implements IExecutableExtension {

	/**
	 * Using this ID ensures we only open one 'slave' browser when opening links from within
	 * a dashboard page.
	 */
	public static final String DASHBOARD_SLAVE_BROWSER_ID = WebDashboardPage.class.getName()+".SLAVE";
	
	private static int idCounter = 0;

	/**
	 * The URL that will be displayed in this Dashboard webpage.
	 */
	private String url;

	public WebDashboardPage() {
		//It seems we are forced to pass an id and a title although looks like this
		// stuff isn't used. Morever we don't really know what it will be yet... that
		// info is passed in later by 'setInitializationData'
		super(generateId(), "Generic Web Page");
	}
	
	private static synchronized String generateId() {
		return WebDashboardPage.class.getName() + (idCounter++);
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) {
		super.setInitializationData(cfig, propertyName, data);
		if (data!=null) {
			if (data instanceof String) {
				this.url = (String) data;
			} else if (data instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) data;
				this.url = map.get("url");
			}
		}
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
//		FormToolkit toolkit = managedForm.getToolkit();
		ScrolledForm form = managedForm.getForm();
		
		//toolkit.decorateFormHeading(form.getForm()); TODO: what's this for? 

		//IPreferenceStore prefStore = IdeUiPlugin.getDefault().getPreferenceStore();
		
		FillLayout layout = new FillLayout();

		Composite body = form.getBody();
		body.setLayout(layout);
		
		Browser browser = new Browser(body, SWT.NONE);
		if (url!=null) {
			browser.setUrl(url);
		} else {
			browser.setText("<h1>URL not set</h1>" +
					"<p>Url should be provided via the setInitializationData method</p>"
			);
		}
		addBrowserHooks(browser);
//		searchBox.setFocus();
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
		return url;
	}
	
	/**
	 * Change the url this dashboard page that will show when it is opened. 
	 * This must be called before the page is opened.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

}
