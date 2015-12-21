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
package org.springframework.ide.eclipse.quickfix.proposals.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;
import org.springframework.ide.eclipse.quickfix.proposals.QuickfixReflectionUtils;
import org.springframework.ide.eclipse.quickfix.tests.AbstractQuickfixTestCase;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class QuickfixReflectionUtilsTest extends AbstractQuickfixTestCase {

	private IJavaProject javaProject;
	
	private ITypeBinding objectBinding, targetTypeBinding;
	
	private IType targetType;
	
	private ICompilationUnit targetCU;
	
	private static final String ACCOUNT_CLASS_NAME = "com.test.Account", OBJECT = "java.lang.Object";
	
	@Override
	protected void setUp() throws Exception {
		copyProjectCreateDocument("src/class-attribute.xml");
		
		javaProject = JavaCore.create(project);
		objectBinding = QuickfixUtils.getTargetTypeBinding(javaProject, javaProject.findType(OBJECT));
		targetType = javaProject.findType(ACCOUNT_CLASS_NAME);
		targetCU = targetType.getCompilationUnit();
		targetTypeBinding = QuickfixUtils.getTargetTypeBinding(javaProject, targetType);
	}
	
	private Object createChangeMethodSignatureProposal() throws JavaModelException {
		Set<IMethod> allConstructors = Introspector.findAllConstructors(targetType);
		IMethod constructor = null;
		for(IMethod c: allConstructors) {
			if (c.getDeclaringType().equals(targetType) && c.getNumberOfParameters() == 0) {
				constructor = c;
				break;
			}
		}
		
		ClassInstanceCreation invocationNode = QuickfixUtils.getMockConstructorInvocation(ACCOUNT_CLASS_NAME, new String[0]);
		IMethodBinding methodBinding = QuickfixUtils.getMethodBinding(javaProject, constructor);
		Object[] changeDesc = QuickfixReflectionUtils.createChangeDescriptionArray(1);
		
		changeDesc[0] = QuickfixReflectionUtils.createInsertDescription(targetTypeBinding, "obj");
		return QuickfixReflectionUtils.createChangeMethodSignatureProposal("", targetCU, invocationNode, methodBinding, changeDesc, 5, null);
	}

	private Object createNewFieldProposal() {
		MethodInvocation expr = QuickfixUtils.getMockMethodInvocation("property", new String[0], "void", false);
		SimpleName simpleName = expr.getName();
		
		return QuickfixReflectionUtils.createNewFieldProposal("", targetCU, simpleName, targetTypeBinding, 5, null);
	}

	private Object createNewMethodProposal() {
		ClassInstanceCreation invocationNode = QuickfixUtils.getMockConstructorInvocation(ACCOUNT_CLASS_NAME, new String[0]);
		List<Expression> arguments = new ArrayList<Expression>();
		return QuickfixReflectionUtils.createNewMethodProposal("", targetCU, invocationNode, arguments, targetTypeBinding, 5, null);
	}

	public void testApplyProposal() throws Exception {
		assertTrue(QuickfixReflectionUtils.applyProposal(createChangeMethodSignatureProposal(), document));
		assertTrue(QuickfixReflectionUtils.applyProposal(createNewFieldProposal(), document));
		assertTrue(QuickfixReflectionUtils.applyProposal(createNewMethodProposal(), document));
	}
	
	private void testArray(Object obj, int length) {
		assertNotNull(obj);
		assertTrue("Expects array", obj instanceof Object[]);
		Object[] array = (Object[]) obj;
		assertEquals("Expect array to be of lengh " + length, length, array.length);
	}
	
	public void testCreateChangeDescriptionArray() {
		testArray(QuickfixReflectionUtils.createChangeDescriptionArray(0), 0);
		testArray(QuickfixReflectionUtils.createChangeDescriptionArray(1), 1);
		testArray(QuickfixReflectionUtils.createChangeDescriptionArray(5), 5);
	}
	
	public void testCreateInsertDescription() {
		Object desc = QuickfixReflectionUtils.createInsertDescription(objectBinding, "obj");
		assertNotNull(desc);
	}
	
	
	public void testCreateChangeMethodSignatureProposal() throws Exception {
		Object proposal = createChangeMethodSignatureProposal();
		assertNotNull(proposal);
	}
	
	public void testCreateNewFieldProposal() {
		Object proposal = createNewFieldProposal();
		assertNotNull(proposal);
	}
	
	public void testCreateNewMethodProposal() {
		Object proposal = createNewMethodProposal();
		assertNotNull(proposal);
	}
	
	public void testCreateRemoveDescription() {
		Object desc = QuickfixReflectionUtils.createRemoveDescription();
		assertNotNull(desc);
	}
	
}
