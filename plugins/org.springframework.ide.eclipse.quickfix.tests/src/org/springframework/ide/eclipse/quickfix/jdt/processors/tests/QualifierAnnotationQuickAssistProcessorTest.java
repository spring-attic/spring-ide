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
package org.springframework.ide.eclipse.quickfix.jdt.processors.tests;

import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.processors.QualifierAnnotationQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddQualiferCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddQualiferToMethodParamCompletionProposal;


/**
 * @author Terry Denney
 */
public class QualifierAnnotationQuickAssistProcessorTest extends AnnotationProcessorTest {

	private QualifierAnnotationQuickAssistProcessor processor;

	@Override
	protected void setUp() throws Exception {
		setUp("com.test.QualifierTest");

		processor = new QualifierAnnotationQuickAssistProcessor();
	}

	public void testFieldWithNoAutowired() throws JavaModelException {
		IField field = type.getField("testBean1");
		ISourceRange sourceRange = field.getSourceRange();
		FieldDeclaration fieldDecl = (FieldDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, field, fieldDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(fieldDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testField() throws JavaModelException {
		IField field = type.getField("testBean2");
		ISourceRange sourceRange = field.getSourceRange();
		FieldDeclaration fieldDecl = (FieldDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, field, fieldDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(fieldDecl, context);
		assertEquals("1 proposal is expected", 1, proposals.size());
		assertTrue("AddQualiferCompletionProposal expected", proposals.get(0) instanceof AddQualiferCompletionProposal);
		assertEquals("Add @Qualifier", proposals.get(0).getDisplayString());
	}

	public void testMethodWithNoAutowired() throws JavaModelException {
		IMethod method = type.getMethod("testMethod1", new String[] { "QTestClass;" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testMethodWith1Param() throws JavaModelException {
		IMethod method = type.getMethod("testMethod2", new String[] { "QTestClass;" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertEquals("1 proposal expected", 1, proposals.size());
		assertTrue("AddQualiferCompletionProposal expected",
				proposals.get(0) instanceof AddQualiferToMethodParamCompletionProposal);
		assertEquals("Add @Qualifier for testBean", proposals.get(0).getDisplayString());
	}

	public void testMethodWith2Params() throws JavaModelException {
		IMethod method = type.getMethod("testMethod3", new String[] { "QTestClass;", "QTestClass;" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertEquals("1 proposal expected", 1, proposals.size());
		assertTrue("AddQualiferCompletionProposal expected",
				proposals.get(0) instanceof AddQualiferToMethodParamCompletionProposal);
		assertEquals("Add @Qualifier for testBean1, testBean2", proposals.get(0).getDisplayString());
	}

	public void testMethodWith2ParamsOnePrimitive() throws JavaModelException {
		IMethod method = type.getMethod("testMethod4", new String[] { "QTestClass;", "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertEquals("1 proposal expected", 1, proposals.size());
		assertTrue("AddQualiferCompletionProposal expected",
				proposals.get(0) instanceof AddQualiferToMethodParamCompletionProposal);
		assertEquals("Add @Qualifier for testBean", proposals.get(0).getDisplayString());
	}

	public void testMethodWith2ParamsOneQualifier() throws JavaModelException {
		IMethod method = type.getMethod("testMethod5", new String[] { "QTestClass;", "QTestClass;" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertEquals("1 proposal expected", 1, proposals.size());
		assertTrue("AddQualiferCompletionProposal expected",
				proposals.get(0) instanceof AddQualiferToMethodParamCompletionProposal);
		assertEquals("Add @Qualifier for testBean2", proposals.get(0).getDisplayString());
	}

	public void testMethodWith2ParamsOneUnmatchedBeans() throws JavaModelException {
		IMethod method = type.getMethod("testMethod6", new String[] { "QTestClass;", "QQualifierTest;" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertEquals("1 proposal expected", 1, proposals.size());
		assertTrue("AddQualiferCompletionProposal expected",
				proposals.get(0) instanceof AddQualiferToMethodParamCompletionProposal);
		assertEquals("Add @Qualifier for param1", proposals.get(0).getDisplayString());
	}

}
