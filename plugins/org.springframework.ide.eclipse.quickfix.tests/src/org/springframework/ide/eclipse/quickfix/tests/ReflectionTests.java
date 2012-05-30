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

import junit.framework.TestCase;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;


/**
 * Tests reflections used in quickfix
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class ReflectionTests extends TestCase {

	private void testCorrectConstructorInvocation(String className, String[] argTypes) {
		ClassInstanceCreation invocation = QuickfixUtils.getMockConstructorInvocation(className, argTypes);

		Type type = invocation.getType();
		assertTrue("Expects invocation type to be a SimpleType", type instanceof SimpleType);

		SimpleType simpleType = (SimpleType) type;
		assertEquals("Expects invocation class name = " + className, className, simpleType.getName()
				.getFullyQualifiedName());

		assertEquals("Expects number of arguments = " + argTypes.length, argTypes.length, invocation.arguments().size());
	}

	private void testCorrectMethodInvocation(String methodName, String returnType, String[] argTypes, boolean isStatic) {
		MethodInvocation invocation = QuickfixUtils.getMockMethodInvocation(methodName, argTypes, returnType, isStatic);

		assertEquals("Expects method name = " + methodName, methodName, invocation.getName().getIdentifier());
		assertEquals("Expects number of arguments = " + argTypes.length, argTypes.length, invocation.arguments().size());

		assertEquals("Expects static = " + isStatic, isStatic, ASTResolving.isInStaticContext(invocation));
	}

	public void testGetMockConstructorInvocation() {
		String className = "com.test.Account";
		String[] argTypes = new String[0];

		testCorrectConstructorInvocation(className, argTypes);

		argTypes = new String[] { "Object" };
		testCorrectConstructorInvocation(className, argTypes);
	}

	public void testGetMockMethodInvocation() {
		String methodName = "getBalance";
		String returnType = "int";
		String[] argTypes = new String[0];

		testCorrectMethodInvocation(methodName, returnType, argTypes, true);
		testCorrectMethodInvocation(methodName, returnType, argTypes, false);

		argTypes = new String[] { "Object" };
		testCorrectMethodInvocation(methodName, returnType, argTypes, true);
		testCorrectMethodInvocation(methodName, returnType, argTypes, false);
	}

}
