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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.content.GettingStartedContent;
import org.springframework.ide.eclipse.gettingstarted.github.GithubClient;
import org.springframework.ide.eclipse.gettingstarted.github.auth.Credentials;
import org.springframework.ide.eclipse.gettingstarted.guides.wizard.GuideImportWizard;

/**
 * Dahsboard page that shows a list of getting started guides. Actually this is just
 * a page showing a browser pointed at some url containing a list of guides.
 * 
 * @author Kris De Volder
 */
public class GuidesDashboardPage extends WebDashboardPage {

	/**
	 * If this is set to true then we don't simply intercept links that look like
	 * pointers to gs guides on github. Instead we expect the page itself to call a
	 * function that we are providing. This allows more flexibility to the
	 * web designer to integrate the STS import support into their UI the way
	 * they want to. The UI could show buttons to import a project. These buttons
	 * could be shown only when in dash (i.e. when the function we provide is defined
	 * the buttons should automatically appear in the page).
	 * <p> 
	 * To enable this add 'parameters' to the xml element in the plugin that
	 * configures this pase as described here in the JavaDoc for
	 * {@link IExecutableExtension}
	 */
	private boolean useJavaScript = false;
	
	/**
	 * Provides a js function that can be called by webpage inside the dash to
	 * open the STS import wizard to import a guide.
	 */
	public class ImportJSFunction extends BrowserFunction {

		public ImportJSFunction(Browser browser) {
			super(browser, "sts_import");
		}
		
		@Override
		public Object function(Object[] arguments) {
			String type = (String) arguments[0];
			if ("guide".equals(type)) {
				String url = (String) arguments[1];
				try {
					return (Boolean)importGuideUrl(new URI(url));
				} catch (URISyntaxException e) {
					return false;
				}
			} else {
				throw new Error("Unknown type of import: "+type);
			}
		}

	}
	
	@Override
	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) {
		// TODO Auto-generated method stub
		super.setInitializationData(cfig, propertyName, data);
		if (data!=null && data instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, String> map = (Map<String, String>) data;
			String useJsString = map.get("useJavaScript");
			if (useJsString!=null) {
				setUseJavaScript(Boolean.valueOf(useJsString));
			}
		}
	}
	
	/**
	 * Enable 'use java script' option. This will provide a 'sts_import_guide' javascript function
	 * to the webpage instead of intercepting links that look like they are pointing a github
	 * guides project.
	 */
	public void setUseJavaScript(boolean enable) {
		useJavaScript = enable;
	}

	private boolean importGuideUrl(URI uri) {
		String host = uri.getHost();
		if ("github.com".equals(host)) {
			Path path = new Path(uri.getPath());
			String org = path.segment(0);
			if ("springframework-meta".equals(org)) {
				String guideName = path.segment(1);
				if (guideName !=null && guideName.startsWith("gs-")) {
					GuideImportWizard.open(getSite().getShell(), GettingStartedContent.getInstance().getGuide(guideName));
					return true;
				}
			}
		}
		return false;
	}
	
	public class GuidesUrlInterceptor implements LocationListener {

		@Override
		public void changing(LocationEvent event) {
			if (!useJavaScript) {
				//try to detect when user clicks on guide links without help from the page.
				try {
					//Here we are interested only in URLs that look like a githubg 'guides' project.
					//Example: https://github.com/springframework-meta/gs-messaging-redis
					//Pattern: https://github.com/springframework-meta/gs-<name>
					
					URI uri = new URI(event.location);
					boolean doneIt = importGuideUrl(uri);
					if (doneIt) {
						event.doit = false;
						return;
					}
				} catch (URISyntaxException e) {
					GettingStartedActivator.log(e);
				}
			}
			
			//If we get here the event didn't get handled yet.
			
			//We should always avoid navigation in the dashboard because... the page will
			//then no longer display the intended content. 
			//To avoid this open the url in another browser instead.
			if (!allowNavigation(event.location)) {
				event.doit = false;
				System.out.println("Navigation intercepted: "+event.location);
				WebDashboardPage.openUrl(event.location);
			}
		}

		private boolean allowNavigation(String location) {
			//We white list some urls for navigation since they are needed to allow signing it with github at the moment
			try {
				if (location.startsWith("https://github.com/login?return_to")) {
					return true;
				} else if (location.equals("https://github.com/session")) {
					return true;
				} else if (new URI(location).equals(new URI(GuidesDashboardPage.this.getUrl()))) {
					return true;
				}
			} catch (URISyntaxException e) {
			}
			return false;
		}

		@Override
		public void changed(LocationEvent event) {
		}

	}

	private LocationListener urlHandler = new GuidesUrlInterceptor();

	@Override
	protected void addBrowserHooks(Browser browser) {
		super.addBrowserHooks(browser);
		Credentials credentials = GithubClient.createDefaultCredentials();
		credentials.apply(browser);
		browser.addLocationListener(urlHandler);

		if (useJavaScript) {
			final BrowserFunction importFun = new ImportJSFunction(browser);
			//TODO: do we need to dispose this function? In the sample snippet code this
			// is done on a completed progress event. But I think that means the function can only
			// be called while the page is loading. We wanna be able to call this function 
			// when user clicks on an import button.
			
			browser.addProgressListener(new ProgressAdapter() {
				@Override
				public void completed(ProgressEvent event) {
//					importGuideFun.dispose();
					System.out.println("completed event");
				}
			});
			
			
		}
		
	}

}
