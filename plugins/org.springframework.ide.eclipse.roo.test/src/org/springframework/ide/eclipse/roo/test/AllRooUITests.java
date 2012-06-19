/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.test;

import org.springsource.ide.eclipse.commons.frameworks.test.util.UITestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * From the IDE run this suite as an "SWTBot test".
 * @author Kris De Volder
 */
public class AllRooUITests {
	public static Test suite() {
		final TestSuite suite = new TestSuite(AllRooUITests.class.getName());
		addTest(suite, RooShellTests.class);
		suite.addTestSuite(StyledTextAppenderTest.class);
		//Add more...
		return suite;
	}

	private static void addTest(TestSuite suite, Class<? extends UITestCase> test) {
		suite.addTest(UITestCase.createSuite(test));
	}
}
