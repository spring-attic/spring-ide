/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanClassRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanInitDestroyMethodRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanPropertyRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.RequiredPropertyRuleTest;
import org.springframework.ide.eclipse.core.java.IntrospectorTest;

/**
 * {@link TestSuite} for <code>beans.core</code> plugin.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class AllBeansCoreTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllBeansCoreTests.class.getName());
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(BeanClassRuleTest.class));
		suite.addTest(new TestSuite(BeanPropertyRuleTest.class));
		suite.addTest(new TestSuite(BeanInitDestroyMethodRuleTest.class));
		suite.addTest(new TestSuite(RequiredPropertyRuleTest.class));
		suite.addTest(new TestSuite(IntrospectorTest.class));
		//$JUnit-END$
		return suite;
	}

}
