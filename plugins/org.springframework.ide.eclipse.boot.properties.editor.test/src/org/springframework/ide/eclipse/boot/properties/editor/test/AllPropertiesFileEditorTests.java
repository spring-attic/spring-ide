/*******************************************************************************
 * Copyright (c) 2014-2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllPropertiesFileEditorTests {


	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(AllPropertiesFileEditorTests.class.getName());

		suite.addTestSuite(SpringPropertiesEditorTests.class);
		suite.addTestSuite(TypeUtilTests.class);
		suite.addTestSuite(FuzzyMapTests.class);
		suite.addTestSuite(TypeParserTest.class);

		return suite;
	}

}
