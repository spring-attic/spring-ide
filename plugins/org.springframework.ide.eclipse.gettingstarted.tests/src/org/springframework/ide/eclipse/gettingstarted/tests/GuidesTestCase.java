/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.tests;

import junit.framework.TestCase;

import org.springframework.ide.eclipse.gettingstarted.guides.GettingStartedGuide;

/**
 * Some infrastucture shared among different dynamically generated testcases for
 * Guides.
 * 
 * @author Kris De Volder
 */
public class GuidesTestCase extends TestCase {
	
	/**
	 * The guide under test
	 */
	protected GettingStartedGuide guide;

	public GuidesTestCase(GettingStartedGuide guide) {
		super(guide.getName());
		this.guide = guide;
	}
	

}
