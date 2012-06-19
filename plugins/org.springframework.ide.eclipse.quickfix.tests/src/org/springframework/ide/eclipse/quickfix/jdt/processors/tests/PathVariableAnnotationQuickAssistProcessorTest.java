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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.processors.PathVariableAnnotationQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddPathVariableCompletionProposal;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class PathVariableAnnotationQuickAssistProcessorTest extends AnnotationProcessorTest {

	private PathVariableAnnotationQuickAssistProcessor processor;

	@Override
	protected void setUp() throws Exception {
		setUp("com.test.PathVariableTest");

		processor = new PathVariableAnnotationQuickAssistProcessor();
	}

	public void testMethodNoAnnotation() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("noAnnotation", new String[] { "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testMethodNoVariableNoPathVariable() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("noVariableNoPathVariable", new String[] { "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testMethodNoVariablePathVariable() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("noVariablePathVariable", new String[] { "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testMethod1VariableWithPathVariable() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("variablePathVariable", new String[] { "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testMethod1VariableWithPathVariableRegEx() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("variablePathVariableRegEx", new String[] { "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testMethod1VariableNoPathVariable() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("variableNoPathVariable", new String[] { "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertEquals("1 proposal expected", 1, proposals.size());
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(0) instanceof AddPathVariableCompletionProposal);
	}

	public void testMethod1VariableNoPathVariableRegEx() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("variableNoPathVariableRegEx", new String[] { "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertEquals("1 proposal expected", 1, proposals.size());
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(0) instanceof AddPathVariableCompletionProposal);
	}

	public void testMethod2VariablesWithPathVariables() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("variablesPathVariables", new String[] { "I", "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testMethod2Variables1PathVariable() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("variablesPathVariable", new String[] { "I", "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertEquals("1 proposal expected", 2, proposals.size());
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(0) instanceof AddPathVariableCompletionProposal);
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(1) instanceof AddPathVariableCompletionProposal);
	}

	public void testMethod2VariablesNoPathVariable() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("variablesNoPathVariable", new String[] { "I", "I" });
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertEquals("2 proposal expected", 4, proposals.size());
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(0) instanceof AddPathVariableCompletionProposal);
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(1) instanceof AddPathVariableCompletionProposal);
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(2) instanceof AddPathVariableCompletionProposal);
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(3) instanceof AddPathVariableCompletionProposal);
	}

	public void testTypeNoAnnotation() throws JavaModelException {
		IType t = type.getType("NoAnnotation");
		ISourceRange sourceRange = t.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, t, viewer);
		IInvocationContext context = getContext(sourceRange, t, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testTypeNoVariable() throws JavaModelException {
		IType t = type.getType("NoVariable");
		ISourceRange sourceRange = t.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, t, viewer);
		IInvocationContext context = getContext(sourceRange, t, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testType1VariableWithPathVariable() throws JavaModelException {
		IType t = type.getType("VariableWithPathVariable");
		ISourceRange sourceRange = t.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, t, viewer);
		IInvocationContext context = getContext(sourceRange, t, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testType1VariableWithNoPathVariable() throws JavaModelException {
		IType t = type.getType("VariableNoPathVariable");
		ISourceRange sourceRange = t.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, t, viewer);
		IInvocationContext context = getContext(sourceRange, t, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertEquals("1 proposal expected", 1, proposals.size());
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(0) instanceof AddPathVariableCompletionProposal);
	}

	public void testType1VariableWithNoPathVariables() throws JavaModelException {
		IType t = type.getType("VariableNoPathVariables");
		ISourceRange sourceRange = t.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, t, viewer);
		IInvocationContext context = getContext(sourceRange, t, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertEquals("1 proposal expected", 2, proposals.size());
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(0) instanceof AddPathVariableCompletionProposal);
		assertTrue("AddPathVariableCompletionProposal expected",
				proposals.get(1) instanceof AddPathVariableCompletionProposal);
	}

}
