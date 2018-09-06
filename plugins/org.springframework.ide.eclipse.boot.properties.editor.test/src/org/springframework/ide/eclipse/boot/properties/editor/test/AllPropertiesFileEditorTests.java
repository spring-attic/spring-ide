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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Kris De Volder
 */
@RunWith(Suite.class)
@SuiteClasses({
	LoggerNameProviderTest.class,
	QuickfixCreationTests.class,
	IgnoreProblemTypeInWorkspaceQuickfixTests.class,
	IgnoreProblemTypeInProjectQuickfixTests.class,
	MetaDataManipulatorTest.class,
	SpringPropertiesEditorTests.class,
	TypeUtilTests.class,
	FuzzyMapTests.class,
	TypeParserTest.class,

	YamlASTTests.class,
	DocumentEditsTest.class,
	YamlStructureParserTest.class,
	IndexNavigatorTest.class,
	YamlEditorTests.class
})
public class AllPropertiesFileEditorTests {
}
