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
package org.springframework.ide.eclipse.quickfix.ui.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Terry Denney
 */
public class AllQuickfixUiTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllQuickfixUiTests.class.getName());
		suite.addTestSuite(ConstructorArgUiTest.class);
		suite.addTestSuite(PropertyAttributeUiTest.class);
		suite.addTestSuite(BeanReferenceAttributeUiTest.class);
		suite.addTestSuite(MethodAttributeUiTest.class);
		suite.addTestSuite(ClassAttributeUiTest.class);
		return suite;
	}

}
