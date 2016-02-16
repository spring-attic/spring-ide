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
package org.springframework.ide.eclipse.quickfix.processors.tests;

import org.springframework.ide.eclipse.quickfix.processors.NameSuggestionComparator;

import junit.framework.TestCase;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class NameSuggestionComparatorTest extends TestCase {

	public void testSimpleNameComparator() {
		String name1 = "com.test.Account";
		String name2 = "com.test.Accounts";
		String name3 = "com.test.Accountsss";
		String name4 = "com.test.Car";
		String name5 = "Account";
		
		NameSuggestionComparator comparator = new NameSuggestionComparator("Account");
		assertEquals("Expects 0 when string are equal", 0, comparator.compare(name1, name1));
		assertTrue(comparator.compare(name1, name2) > 0);
		assertTrue(comparator.compare(name2, name1) < 0);
		assertTrue(comparator.compare(name1, name3) >= 0);
		assertTrue(comparator.compare(name3, name1) <= 0);
		assertTrue(comparator.compare(name2, name3) >= 0);
		assertTrue(comparator.compare(name3, name2) <= 0);
		assertTrue(comparator.compare(name3, name4) > 0);
		assertTrue(comparator.compare(name1, name5) == 0);
	}
	
}
