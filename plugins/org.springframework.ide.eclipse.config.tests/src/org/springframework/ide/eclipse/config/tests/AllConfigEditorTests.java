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

import org.springframework.ide.eclipse.config.tests.core.ConfigCoreUtilsTest;
import org.springframework.ide.eclipse.config.tests.core.contentassist.SpringConfigContentAssistProcessorTest;
import org.springframework.ide.eclipse.config.tests.graph.AbstractConfigGraphicalEditorTest;
import org.springframework.ide.eclipse.config.tests.ui.actions.CollapseAndExpandNodeActionTest;
import org.springframework.ide.eclipse.config.tests.ui.actions.InsertAndDeleteNodeActionTest;
import org.springframework.ide.eclipse.config.tests.ui.actions.RaiseAndLowerNodeActionTest;
import org.springframework.ide.eclipse.config.tests.ui.editors.AbstractConfigDetailsPartTest;
import org.springframework.ide.eclipse.config.tests.ui.editors.AbstractConfigFormPageTest;
import org.springframework.ide.eclipse.config.tests.ui.editors.AbstractNamespaceDetailsPartTest;
import org.springframework.ide.eclipse.config.tests.ui.editors.SpringConfigEditorTest;
import org.springframework.ide.eclipse.config.tests.ui.editors.SpringConfigInputAccessorTest;
import org.springframework.ide.eclipse.config.tests.ui.editors.namespaces.NamespacesDetailsPartTest;
import org.springframework.ide.eclipse.config.tests.ui.editors.namespaces.NamespacesMasterPartTest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class AllConfigEditorTests {

	public static Test suite() {
		return suite(false);
	}

	public static Test suite(boolean heartbeat) {
		TestSuite suite = new TestSuite(AllConfigEditorTests.class.getName());
		suite.addTestSuite(ConfigCoreUtilsTest.class);
		suite.addTestSuite(AbstractConfigGraphicalEditorTest.class);
		suite.addTestSuite(CollapseAndExpandNodeActionTest.class);
		suite.addTestSuite(InsertAndDeleteNodeActionTest.class);
		suite.addTestSuite(RaiseAndLowerNodeActionTest.class);
		suite.addTestSuite(AbstractConfigDetailsPartTest.class);
		suite.addTestSuite(AbstractConfigFormPageTest.class);
		suite.addTestSuite(AbstractNamespaceDetailsPartTest.class);
		if (!heartbeat) {
			suite.addTestSuite(SpringConfigEditorTest.class);
		}
		suite.addTestSuite(SpringConfigInputAccessorTest.class);
		suite.addTestSuite(NamespacesDetailsPartTest.class);
		suite.addTestSuite(NamespacesMasterPartTest.class);
		return suite;
	}

	public static Test experimentalSuite() {
		TestSuite suite = new TestSuite(AllConfigEditorTests.class.getName());
		suite.addTestSuite(SpringConfigContentAssistProcessorTest.class);
		return suite;
	}

}
