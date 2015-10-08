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

/**
 * @author Kris De Volder
 */
public class AllPropertiesFileEditorTests {

	public static Test suite() throws Exception {
		TestSuite suite = new TestSuite(AllPropertiesFileEditorTests.class.getName());
		suite.addTestSuite(QuickfixCreationTests.class);
		suite.addTestSuite(IgnoreProblemTypeInWorkspaceQuickfixTests.class);
		suite.addTestSuite(IgnoreProblemTypeInProjectQuickfixTests.class);
		suite.addTestSuite(StringUtilTests.class);
		suite.addTestSuite(MetaDataManipulatorTest.class);
		suite.addTestSuite(SpringPropertiesEditorTests.class);
		suite.addTestSuite(TypeUtilTests.class);
		suite.addTestSuite(FuzzyMapTests.class);
		suite.addTestSuite(TypeParserTest.class);

		suite.addTestSuite(YamlASTTests.class);
		suite.addTestSuite(DocumentEditsTest.class);
		suite.addTestSuite(YamlStructureParserTest.class);
		suite.addTestSuite(IndexNavigatorTest.class);
		suite.addTestSuite(YamlEditorTests.class);
		return suite;
	}

}
