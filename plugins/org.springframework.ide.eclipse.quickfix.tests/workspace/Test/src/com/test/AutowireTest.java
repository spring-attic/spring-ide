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
package com.test;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Terry Denney
 */
public class AutowireTest {

	private TestClass testBean1;
	
	@Autowired
	private TestClass testBean2;
	
	public AutowireTest() {
	}
	
	public AutowireTest(TestClass testBean) {
	}
	
	public void testMethod1(TestClass testBean) {
	}
	
	@Autowired
	public void testMethod2(TestClass testBean) {	
	}
	
	public void testMethod3() {
	}

}
