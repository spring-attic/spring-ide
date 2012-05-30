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

/**
 * @author Leo Dos Santos
 * @author Terry Denney
 */
public class Foo {
	
	public static Object ABCD;

	public Foo(String foo) {
	}
	
	public Foo() {
		
	}
	
	public Bar getBar() {
		return new Bar();
	}
	
	public static class Bar {
		private String foobar;
	}
	
	static Foo createFoo(String test) {
		return new Foo(test);
	}
	
	@Deprecated
	public void setZoo(String zoo) {
	}
	
}
