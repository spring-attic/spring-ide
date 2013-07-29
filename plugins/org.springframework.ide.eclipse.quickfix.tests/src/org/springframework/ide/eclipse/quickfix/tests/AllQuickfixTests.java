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
import org.springframework.ide.eclipse.quickfix.hyperlinks.tests.AutowireHyperlinkProviderTest;
import org.springframework.ide.eclipse.quickfix.processors.tests.NameSuggestionComparatorTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.AddConfigSetQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.AddConstructorArgQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.AddConstructorParamQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.AddToConfigSetQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.CreateConstructorQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.CreateImportQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.CreateNewBeanQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.CreateNewClassQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.CreateNewMethodQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.QuickfixReflectionUtilsTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.RemoveConstructorArgQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.RemoveConstructorParamQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.RenamePropertyQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.proposals.tests.RenameToSimilarNameQuickFixProposalTest;
import org.springframework.ide.eclipse.quickfix.validator.tests.BeanReferenceAttributeValidationTest;
import org.springframework.ide.eclipse.quickfix.validator.tests.ClassAttributeValidationTest;
import org.springframework.ide.eclipse.quickfix.validator.tests.ConstructorArgNameValidationTest;
import org.springframework.ide.eclipse.quickfix.validator.tests.FactoryBeanAttributeValidationTest;
import org.springframework.ide.eclipse.quickfix.validator.tests.FactoryMethodAttributeValidationTest;
import org.springframework.ide.eclipse.quickfix.validator.tests.MethodAttributeValidationTest;
import org.springframework.ide.eclipse.quickfix.validator.tests.NamespaceElementsValidationTest;
import org.springframework.ide.eclipse.quickfix.validator.tests.PlaceholderTest;
import org.springframework.ide.eclipse.quickfix.validator.tests.PropertyAttributeValidationTest;

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class AllQuickfixTests {

	public static Test suite() {
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.ajdt.ui");
		store.setValue("promptForAutoOpenCrossReference", false);

		TestSuite suite = new TestSuite(AllQuickfixTests.class.getName());
		// $JUnit-BEGIN$

		suite.addTest(new TestSuite(ReflectionTests.class));
		suite.addTest(new TestSuite(QuickfixReflectionUtilsTest.class));
		suite.addTest(new TestSuite(QuickfixUtilsTest.class));

		// suite.addTest(new
		// TestSuite(AutowireClassAttributeValidationTest.class));
		// TODO: remove since autowire setup is incorrect in test

		// TODO: removed till alias attribute validation is fully implemented
		// suite.addTest(new TestSuite(AliasAttributeValidationTest.class));

		suite.addTest(new TestSuite(FactoryBeanAttributeValidationTest.class));
		suite.addTest(new TestSuite(NamespaceElementsValidationTest.class));
		suite.addTest(new TestSuite(BeanReferenceAttributeValidationTest.class));
		suite.addTest(new TestSuite(ClassAttributeValidationTest.class));
		suite.addTest(new TestSuite(MethodAttributeValidationTest.class));
		suite.addTest(new TestSuite(PlaceholderTest.class));
		suite.addTest(new TestSuite(FactoryMethodAttributeValidationTest.class));
		suite.addTest(new TestSuite(PropertyAttributeValidationTest.class));
		suite.addTest(new TestSuite(ConstructorArgNameValidationTest.class));

		suite.addTest(new TestSuite(AddConstructorArgQuickFixProposalTest.class));
		suite.addTest(new TestSuite(AddConstructorParamQuickFixProposalTest.class));
		suite.addTest(new TestSuite(CreateConstructorQuickFixProposalTest.class));
		suite.addTest(new TestSuite(CreateNewBeanQuickFixProposalTest.class));
		suite.addTest(new TestSuite(CreateNewClassQuickFixProposalTest.class));
		suite.addTest(new TestSuite(CreateNewMethodQuickFixProposalTest.class));
		suite.addTest(new TestSuite(RemoveConstructorArgQuickFixProposalTest.class));
		suite.addTest(new TestSuite(RemoveConstructorParamQuickFixProposalTest.class));
		suite.addTest(new TestSuite(RenamePropertyQuickFixProposalTest.class));
		suite.addTest(new TestSuite(RenameToSimilarNameQuickFixProposalTest.class));
		suite.addTest(new TestSuite(CreateImportQuickFixProposalTest.class));
		suite.addTest(new TestSuite(AddToConfigSetQuickFixProposalTest.class));
		suite.addTest(new TestSuite(AddConfigSetQuickFixProposalTest.class));

		suite.addTest(new TestSuite(NameSuggestionComparatorTest.class));

		suite.addTest(AllJDTQuickfixTests.suite());

		suite.addTest(new TestSuite(AutowireHyperlinkProviderTest.class));

		// $JUnit-END$
		return suite;
	}

}
