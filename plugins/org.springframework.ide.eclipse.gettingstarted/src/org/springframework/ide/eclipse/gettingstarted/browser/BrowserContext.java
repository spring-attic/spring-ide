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

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Shell;

/**
 * Covenient class mostly meant to be subclassed to implement something that 
 * is created in the context of a Browser. 
 * 
 * @author Kris De Volder
 */
public class BrowserContext {

	private Browser browser;
	
	public BrowserContext(Browser browser) {
		this.browser = browser;
		
	}
	
	protected Shell getShell() {
		if (browser!=null) {
			return browser.getShell();
		}
		return null;
	}
	
	public Browser getBrowser() {
		return browser;
	}

}
