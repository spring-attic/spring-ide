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
package org.springframework.ide.eclipse.internal.bestpractices.quickfix;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.springframework.ide.eclipse.bestpractices.BestPracticesPluginConstants;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * Resolution for missing property markers. Creates a corresponding setter in
 * the class referenced by the bean definition.
 * @author Wesley Coelho
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class AbstractCreateMethodMarkerResolution implements IMarkerResolution2 {

	private static final String MESSAGE_ATTRIBUTE_KEY = "message";

	private String markerMessage = "";

	public AbstractCreateMethodMarkerResolution(IMarker marker) {
		markerMessage = marker.getAttribute(MESSAGE_ATTRIBUTE_KEY, "");
	}

	protected String extractQuotedString(String startTag, String message) {
		int startPos = message.indexOf(startTag) + startTag.length();
		int endPos = message.indexOf("'", startPos);
		return message.substring(startPos, endPos);
	}

	@SuppressWarnings("unchecked")
	protected List<Expression> getArguments(Expression invocationExpression) {
		MethodInvocation methodInvocation = (MethodInvocation) invocationExpression;
		List<Expression> arguments = new ArrayList<Expression>();
		for (Iterator<Expression> argumentIter = methodInvocation.arguments().iterator(); argumentIter.hasNext();) {
			Expression argumentExpression = argumentIter.next();
			arguments.add(argumentExpression);
		}
		return arguments;
	}

	public abstract String getDescription();

	public Image getImage() {
		return null;
	}

	public abstract String getLabel();

	protected String getMarkerMessage() {
		return markerMessage;
	}

	protected Expression getMockMethodInvocation() {
		ASTParser fooParser = ASTParser.newParser(AST.JLS3);
		fooParser.setKind(ASTParser.K_STATEMENTS);
		String mockMethodInvocationCode = getNewMethodName() + "(" + getNewMethodParameters() + ");";
		fooParser.setSource(mockMethodInvocationCode.toCharArray());
		fooParser.setResolveBindings(true);
		ASTNode parsedAstNode = fooParser.createAST(null);
		Block codeBlock = (Block) parsedAstNode;
		ExpressionStatement methodInvocationExpressionStatement = (ExpressionStatement) codeBlock.statements().get(0);
		return methodInvocationExpressionStatement.getExpression();
	}

	protected abstract String getNewMethodName();

	/**
	 * Override to provide parameters for the new method name. Parameters should
	 * not include types and should be as they would appear in a method call to
	 * the new method, e.g. "object" or "obj1, obj2".
	 */
	protected String getNewMethodParameters() {
		return "";
	}

	protected abstract String getTargetClass();

	protected ITypeBinding getTargetTypeBinding(IJavaProject javaProject, IType targetType) {
		ASTParser bindingParser = ASTParser.newParser(AST.JLS3);
		bindingParser.setKind(ASTParser.K_COMPILATION_UNIT);
		bindingParser.setSource(targetType.getCompilationUnit());
		bindingParser.setResolveBindings(true);
		bindingParser.setProject(javaProject);
		IBinding[] bindings = bindingParser
				.createBindings(new IJavaElement[] { targetType }, new NullProgressMonitor());
		ITypeBinding typeBinding = (ITypeBinding) bindings[0];
		return typeBinding;
	}

	public void run(IMarker marker) {

		IProject project = marker.getResource().getProject();
		IJavaProject javaProject = JavaCore.create(project);
		IType targetType = null;
		try {
			targetType = javaProject.findType(getTargetClass());
		}
		catch (JavaModelException e) {
			StatusHandler.log(e.getStatus());
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Quick Fix Error", "Could not find class '"
					+ getTargetClass() + "'");
			return;
		}
		if (targetType == null) {
			StatusHandler.log(new Status(Status.ERROR, BestPracticesPluginConstants.PLUGIN_ID, "Could not find class '"
					+ getTargetClass() + "'"));
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Quick Fix Error", "Could not find class '"
					+ getTargetClass() + "'");
			return;
		}

		Expression mockMethodInvocation = getMockMethodInvocation();

		ITypeBinding typeBinding = getTargetTypeBinding(javaProject, targetType);

		List<Expression> arguments = getArguments(mockMethodInvocation);

		Image image = JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);

		// XXX e3.4 work around changed class name in Eclipse 3.4
		ICompletionProposal newMethodCompletionProposal;
		try {
			Class<?> clazz;
			try {
				// Eclipse 3.3
				clazz = Class.forName("org.eclipse.jdt.internal.ui.text.correction.NewMethodCompletionProposal");
			}
			catch (ClassNotFoundException e) {
				// Eclipse 3.4
				clazz = Class
						.forName("org.eclipse.jdt.internal.ui.text.correction.proposals.NewMethodCorrectionProposal");
			}
			Constructor<?> constructor = clazz.getConstructor(String.class, ICompilationUnit.class, ASTNode.class,
					List.class, ITypeBinding.class, int.class, Image.class);
			newMethodCompletionProposal = (ICompletionProposal) constructor.newInstance("Add missing property",
					targetType.getCompilationUnit(), mockMethodInvocation, arguments, typeBinding, 0, image);
		}
		catch (Throwable t) {
			StatusHandler.log(new Status(IStatus.ERROR, BestPracticesPluginConstants.PLUGIN_ID,
					"Problems occured when creating completion proposal", t));
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Quick Fix Error",
					"Could not execute correction proposal");
			return;
		}

		// Get the XML document model
		IStructuredModel model = null;
		try {
			model = XmlQuickFixUtil.getModel(marker);
			// Execute Quick Fix
			newMethodCompletionProposal.apply(model.getStructuredDocument());
		}
		catch (CoreException e) {
			StatusHandler.log(e.getStatus());
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Quick Fix Error",
					"Could not find error marker");
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
				model = null;
			}
		}

	}
}
