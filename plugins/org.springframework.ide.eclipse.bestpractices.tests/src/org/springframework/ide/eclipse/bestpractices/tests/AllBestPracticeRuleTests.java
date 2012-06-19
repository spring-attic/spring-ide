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

package org.springframework.ide.eclipse.bestpractices.tests;

import org.springframework.ide.eclipse.bestpractices.tests.ruletests.AvoidDriverManagerDataSourceRuleTest;
import org.springframework.ide.eclipse.bestpractices.tests.ruletests.ImportElementsAtTopRuleTest;
import org.springframework.ide.eclipse.bestpractices.tests.ruletests.ParentBeanSpecifiesAbstractClassRuleTest;
import org.springframework.ide.eclipse.bestpractices.tests.ruletests.RefElementRuleTest;
import org.springframework.ide.eclipse.bestpractices.tests.ruletests.TooManyBeansInFileRuleTest;
import org.springframework.ide.eclipse.bestpractices.tests.ruletests.UnnecessaryValueElementRuleTest;
import org.springframework.ide.eclipse.bestpractices.tests.ruletests.UseBeanInheritanceRuleTest;
import org.springframework.ide.eclipse.bestpractices.tests.ruletests.UseDedicatedNamespacesRuleTest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * {@link TestSuite} for the <code>com.springsource.sts.bestpractices</code> plugin.
 * @author Wesley Coelho
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class AllBestPracticeRuleTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllBestPracticeRuleTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTestSuite(ImportElementsAtTopRuleTest.class);
		suite.addTestSuite(ParentBeanSpecifiesAbstractClassRuleTest.class);
		suite.addTestSuite(TooManyBeansInFileRuleTest.class);
		suite.addTestSuite(RefElementRuleTest.class);
		suite.addTestSuite(UnnecessaryValueElementRuleTest.class);
		suite.addTestSuite(UseBeanInheritanceRuleTest.class);
		suite.addTestSuite(AvoidDriverManagerDataSourceRuleTest.class);
		suite.addTestSuite(UseDedicatedNamespacesRuleTest.class);
		//$JUnit-END$
		return suite;
	}

}
