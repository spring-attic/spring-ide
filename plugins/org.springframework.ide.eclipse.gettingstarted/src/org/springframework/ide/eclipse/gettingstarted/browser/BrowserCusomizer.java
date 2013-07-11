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
package org.springframework.ide.eclipse.gettingstarted.browser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.content.GettingStartedContent;
import org.springframework.ide.eclipse.gettingstarted.content.GettingStartedGuide;
import org.springframework.ide.eclipse.gettingstarted.util.URIParams;
import org.springframework.ide.eclipse.gettingstarted.wizard.GSImportWizard;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

public class BrowserCusomizer extends BrowserContext {

	private ImportJSFunction importFun;
	private Browser browser;

	/**
	 * Provides a js function that can be called by webpage inside the dash to
	 * open the STS import wizard to import a guide.
	 */
	private class ImportJSFunction extends BrowserFunction {

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

	private boolean importGuideUrl(URI uri) {
		String host = uri.getHost();
		if ("github.com".equals(host)) {
			Path path = new Path(uri.getPath());
			//String org = path.segment(0); Curently ignore the org.
			String guideName = path.segment(1);
			if (guideName !=null && guideName.startsWith("gs-")) {
				//GuideImportWizard.open(getSite().getShell(), GettingStartedContent.getInstance().getGuide(guideName));
				GettingStartedGuide guide = GettingStartedContent.getInstance().getGuide(guideName);
				if (guide!=null) {
					GSImportWizard.open(getShell(), guide);
					return true;
				}
			}
		}
		return false;
	}
	

	
	public BrowserCusomizer(Browser browser) {
		super(browser);
		addBrowserHooks(browser);
	}
	
	protected void addBrowserHooks(Browser browser) {
		browser.addLocationListener(new UrlInterceptor(browser));

		importFun = new ImportJSFunction(browser);
		
		browser.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (importFun!=null) {
					importFun.dispose();
					importFun = null;
				}
			}
		});
	}
	
	/**
	 * Not used anymore. Now only using the JavaScript function approach.
	 */
	public class UrlInterceptor extends BrowserContext implements LocationListener {
		
		public UrlInterceptor(Browser browser) {
			super(browser);
		}
		
		@Override
		public void changing(LocationEvent event) {
			System.out.println("Navigation: "+event.location);
			
			
//			//Be careful...any  exception thrown out of here have a nasty tendency to deadlock Eclipse 
//			// (By crashing native UI thread maybe?)
//			try {
//				URI uri = new URI(event.location);
//				//zip file containing a codeset single codeset:
//				// https://github.com/kdvolder/gs-one-codeset/archive/master.zip?sts_codeset=true
////				Map<String, String> params = URIParams.parse(uri);
//				IPath path = new Path(uri.getPath());
//				String host = uri.getHost();
//				
//				//link to a gs project github zip file:
//				//https://github.com/springframework-meta/gs-consuming-rest-android/archive/master.zip
//				if ("github.com".equals(host)) {
//					String fileName = path.lastSegment();
//					if ("master.zip".equals(fileName)) {
//						String projectName = path.segment(1);
//						if (projectName!=null && projectName.startsWith("gs-")) {
//							boolean doneIt = importGuideUrl(uri);
//							event.doit = !doneIt;
//						}
//					}
//				}
//			} catch (Throwable e) {
//				GettingStartedActivator.log(e);
//			}
//			
//			//Doing something special with urls that follow certain patterns
//			
//			//1: link to a gihub zip 
//			
//			
////			if (!useJavaScript) {
////				//try to detect when user clicks on guide links without help from the page.
////				try {
////					//Here we are interested only in URLs that look like a github 'guides' project.
////					//Example: https://github.com/springframework-meta/gs-messaging-redis
////					//Pattern: https://github.com/springframework-meta/gs-<name>
////					
////					URI uri = new URI(event.location);
////					boolean doneIt = importGuideUrl(uri);
////					if (doneIt) {
////						event.doit = false;
////						return;
////					}
////				} catch (URISyntaxException e) {
////					GettingStartedActivator.log(e);
////				}
////			}
//			
//			//If we get here the event didn't get handled yet.
//			
//			//We should always avoid navigation in the dashboard because... the page will
//			//then no longer display the intended content. 
//			//To avoid this open the url in another browser instead.
//			if (!allowNavigation(event.location)) {
//				event.doit = false;
//				System.out.println("Navigation intercepted: "+event.location);
//				UiUtil.openUrl(event.location);
//			}
		}

		private boolean allowNavigation(String location) {
			return true; //ok to navigate anywhere now. We have a 'home' button.
//			//We white list some urls for navigation since they are needed to allow signing it with github at the moment
//			try {
//				if (location.startsWith("https://github.com/login?return_to")) {
//					return true;
//				} else if (location.equals("https://github.com/session")) {
//					return true;
//				} else if (new URI(location).equals(new URI(GuidesDashboardPage.this.getUrl()))) {
//					return true;
//				}
//			} catch (URISyntaxException e) {
//			}
//			return false;
		}

		@Override
		public void changed(LocationEvent event) {
		}

	}
	
	
}
