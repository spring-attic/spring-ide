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
import org.springframework.ide.eclipse.quickfix.jdt.processors.ControllerAnnotationQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddControllerCompletionProposal;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class ControllerAnnotationQuickAssistProcessorTest extends AnnotationProcessorTest {

	private ControllerAnnotationQuickAssistProcessor processor;

	@Override
	protected void setUp() throws Exception {
		setUp("com.test.ControllerTest");

		processor = new ControllerAnnotationQuickAssistProcessor();
	}

	public void testNoControllerNoRequestMappingMethod() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("noAnnotation", new String[0]);
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testNoControllerRequestMappingMethod() throws JavaModelException {
		IMethod method = type.getType("NoAnnotation").getMethod("requestMappingAnnotation", new String[0]);
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertEquals("1 proposal expected", proposals.size(), 1);
		assertTrue("AddControllerCompletionProposal expected",
				proposals.get(0) instanceof AddControllerCompletionProposal);
	}

	public void testControllerNoRequestMappingMethod() throws JavaModelException {
		IMethod method = type.getType("ControllerAnnotation").getMethod("noAnnotation", new String[0]);
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testControllerRequestMappingMethod() throws JavaModelException {
		IMethod method = type.getType("ControllerAnnotation").getMethod("requestMappingAnnotation", new String[0]);
		ISourceRange sourceRange = method.getSourceRange();
		MethodDeclaration methodDecl = (MethodDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, method, methodDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(methodDecl, context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testNoControllerNoRequestMappingMethods() throws JavaModelException {
		IType type = this.type.getType("NoAnnotationNoRequestMapping");
		ISourceRange sourceRange = type.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, type, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testNoControllerRequestMappingMethods() throws JavaModelException {
		IType type = this.type.getType("NoAnnotation");
		ISourceRange sourceRange = type.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, type, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertEquals("1 proposal expected", proposals.size(), 1);
		assertTrue("AddControllerCompletionProposal expected",
				proposals.get(0) instanceof AddControllerCompletionProposal);
	}

	public void testNoControllerRequestMappingNoRequestMappingMethods() throws JavaModelException {
		IType type = this.type.getType("RequestMappingAnnotationNoRequestMapping");
		ISourceRange sourceRange = type.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, type, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertEquals("1 proposal expected", proposals.size(), 1);
		assertTrue("AddControllerCompletionProposal expected",
				proposals.get(0) instanceof AddControllerCompletionProposal);
	}

	public void testControllerNoRequestMappingMethods() throws JavaModelException {
		IType type = this.type.getType("ControllerAnnotationNoRequestMapping");
		ISourceRange sourceRange = type.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, type, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testControllerRequestMappingMethods() throws JavaModelException {
		IType type = this.type.getType("ControllerAnnotation");
		ISourceRange sourceRange = type.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, type, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testBothNoRequestMappingMethods() throws JavaModelException {
		IType type = this.type.getType("BothAnnotationsNoRequestMapping");
		ISourceRange sourceRange = type.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, type, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

	public void testBothRequestMappingMethods() throws JavaModelException {
		IType type = this.type.getType("BothAnnotations");
		ISourceRange sourceRange = type.getSourceRange();
		TypeDeclaration typeDecl = (TypeDeclaration) getASTNode(sourceRange, type, viewer);
		IInvocationContext context = getContext(sourceRange, type, typeDecl);

		List<IJavaCompletionProposal> proposals = processor.getAssists(typeDecl.getName(), context);
		assertTrue("No proposals expected", proposals.isEmpty());
	}

}
