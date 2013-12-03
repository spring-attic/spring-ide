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
package org.springsource.ide.eclipse.commons.gettingstarted.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllGettingStartedTests {
	
	public static Test suite() throws Exception {
		MavenCacheSegragator.init();
		TestSuite suite = new TestSuite(AllGettingStartedTests.class.getName());
		
		//Guides content validation
		
		suite.addTestSuite(GuidesTests.class);
		suite.addTest(GuidesStructureTest.suite());
		suite.addTest(ZBuildGuidesTest.suite());
		
		
		//TODO: test import / build

		//Sample content validation
		//The tests below are disabled. They produce
		//seemingly random failures.
		
//		suite.addTestSuite(SampleTests.class);
//		suite.addTest(BuildSampleTest.suite());
		
		//TODO: Tutorial content validation
		
		return suite;
	}

}
