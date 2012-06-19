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
package org.springframework.ide.eclipse.quickfix.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.springframework.ide.eclipse.quickfix.jdt.processors.tests.AutowiredAnnotationQuickAssistProcessorTest;
import org.springframework.ide.eclipse.quickfix.jdt.processors.tests.ControllerAnnotationQuickAssistProcessorTest;
import org.springframework.ide.eclipse.quickfix.jdt.processors.tests.PathVariableAnnotationQuickAssistProcessorTest;
import org.springframework.ide.eclipse.quickfix.jdt.processors.tests.QualifierAnnotationQuickAssistProcessorTest;
import org.springframework.ide.eclipse.quickfix.processors.tests.AddAutowiredConstructorTest;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class AllJDTQuickfixTests {

	public static Test suite() {
		IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), "org.eclipse.ajdt.ui");
		store.setValue("promptForAutoOpenCrossReference", false);

		TestSuite suite = new TestSuite(AllJDTQuickfixTests.class.getName());
		// $JUnit-BEGIN$

		suite.addTest(new TestSuite(AutowiredAnnotationQuickAssistProcessorTest.class));
		suite.addTest(new TestSuite(QualifierAnnotationQuickAssistProcessorTest.class));
		suite.addTest(new TestSuite(ControllerAnnotationQuickAssistProcessorTest.class));
		suite.addTest(new TestSuite(PathVariableAnnotationQuickAssistProcessorTest.class));
		suite.addTest(new TestSuite(AddAutowiredConstructorTest.class));
		suite.addTest(new TestSuite(ConfigurationLocationProposalComputerTest.class));

		// $JUnit-END$
		return suite;
	}

}
