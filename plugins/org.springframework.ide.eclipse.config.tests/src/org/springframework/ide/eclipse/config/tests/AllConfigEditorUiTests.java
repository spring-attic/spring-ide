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
package org.springframework.ide.eclipse.config.tests;

import org.springframework.ide.eclipse.config.tests.graph.parts.ActivityDiagramPartUiTest;
import org.springframework.ide.eclipse.config.tests.graph.parts.ActivityPartUiTest;
import org.springframework.ide.eclipse.config.tests.graph.parts.SimpleActivityWithContainerPartUiTest;
import org.springframework.ide.eclipse.config.tests.graph.parts.StructuredActivityPartUiTest;
import org.springframework.ide.eclipse.config.tests.graph.parts.TransitionPartUiTest;
import org.springframework.ide.eclipse.config.tests.ui.editors.AbstractNamespaceDetailsPartUiTest;
import org.springframework.ide.eclipse.config.tests.ui.editors.AbstractNamespaceMasterPartUiTest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Leo Dos Santos
 */
public class AllConfigEditorUiTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllConfigEditorUiTests.class.getName());
		suite.addTestSuite(ActivityDiagramPartUiTest.class);
		suite.addTestSuite(ActivityPartUiTest.class);
		suite.addTestSuite(SimpleActivityWithContainerPartUiTest.class);
		suite.addTestSuite(StructuredActivityPartUiTest.class);
		suite.addTestSuite(TransitionPartUiTest.class);
		suite.addTestSuite(AbstractNamespaceDetailsPartUiTest.class);
		suite.addTestSuite(AbstractNamespaceMasterPartUiTest.class);
		return suite;
	}

}
