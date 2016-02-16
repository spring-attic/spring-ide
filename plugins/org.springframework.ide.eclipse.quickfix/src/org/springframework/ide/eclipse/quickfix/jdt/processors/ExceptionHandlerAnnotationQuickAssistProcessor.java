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
package org.springframework.ide.eclipse.quickfix.jdt.processors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.AddExceptionHandlerCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class ExceptionHandlerAnnotationQuickAssistProcessor extends AbstractAnnotationQuickAssistProcessor {

	private List<Class<?>> exceptions;

	@Override
	public String getAnnotationName() {
		return "ExceptionHandler";
	}

	@Override
	public boolean isQuickfixAvailable(FieldDeclaration fieldDecl, IInvocationContext context) {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isQuickfixAvailable(MethodDeclaration methodDecl, IInvocationContext context) {
		TypeDeclaration typeDecl = getSurroundingTypeDecl(methodDecl);
		if (typeDecl != null && ProposalCalculatorUtil.hasAnnotation("Controller", typeDecl)) {
			if (!ProposalCalculatorUtil.hasAnnotation("ExceptionHandler", methodDecl)) {
				List<SingleVariableDeclaration> params = methodDecl.parameters();
				ClassLoader classLoader = JdtUtils.getProjectClassLoaderSupport(
						context.getCompilationUnit().getJavaProject().getProject(), BeansCorePlugin.getClassLoader())
						.getProjectClassLoader();
				Class<?> exceptionType;
				try {
					exceptionType = classLoader.loadClass(Exception.class.getCanonicalName());
				}
				catch (ClassNotFoundException e) {
					return false;
				}

				for (SingleVariableDeclaration param : params) {
					ITypeBinding paramTypeBinding = param.getType().resolveBinding();
					String paramTypeName = paramTypeBinding.getQualifiedName();
					Class<?> paramClass;
					try {
						paramClass = classLoader.loadClass(paramTypeName);
					}
					catch (ClassNotFoundException e) {
						continue;
					}

					if (exceptionType.isAssignableFrom(paramClass)) {
						exceptions.add(paramClass);
					}
				}
			}
		}
		return exceptions.size() > 0;
	}

	@Override
	public boolean isQuickfixAvailable(TypeDeclaration typeDecl, IInvocationContext context) {
		return false;
	}

	@Override
	public List<IJavaCompletionProposal> getAssistsForMethod(MethodDeclaration methodDecl, SimpleName name,
			ICompilationUnit cu) {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
		List<String> exceptionNames = groupExceptions(exceptions);
		proposals.add(new AddExceptionHandlerCompletionProposal(methodDecl, exceptionNames, cu));
		return proposals;
	}

	private static List<String> groupExceptions(List<Class<?>> exceptions) {
		List<Class<?>> storedExceptions = new ArrayList<Class<?>>();

		for (Class<?> newException : exceptions) {
			boolean isSubclass = false;

			// check if newException is subtype of any stored exceptions
			for (Class<?> exception : storedExceptions) {

				// if exception is equals or super class of new Exception,
				// ignore newException
				if (exception.isAssignableFrom(newException)) {
					isSubclass = true;
					break;
				}
			}

			if (!isSubclass) {
				// check if newException is a superclass of any stored
				// exceptions
				List<Class<?>> toBeRemoved = new ArrayList<Class<?>>();
				for (Class<?> exception : storedExceptions) {
					if (newException.isAssignableFrom(exception)) {
						toBeRemoved.add(exception);
					}
				}

				storedExceptions.removeAll(toBeRemoved);
				storedExceptions.add(newException);
			}
		}

		List<String> result = new ArrayList<String>();
		for (Class<?> exception : storedExceptions) {
			result.add(exception.getSimpleName());
		}
		return result;
	}

	@Override
	protected void setUpFields() {
		exceptions = new ArrayList<Class<?>>();
	}

}
