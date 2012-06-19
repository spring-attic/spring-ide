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
package org.springframework.ide.eclipse.quickfix;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ModifierChangeCorrectionProposal;
import org.eclipse.jdt.internal.ui.text.correction.proposals.NewVariableCorrectionProposal;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.proposals.CreateNewMethodQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.proposals.QuickfixReflectionUtils;
import org.springframework.ide.eclipse.quickfix.validator.BeanValidator;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;


/**
 * Utility class with helper methods for calculating quick fixes for bean
 * configuration files.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class QuickfixUtils {

	private static class ASTFinder extends ASTVisitor {

		private MethodInvocation methodInvo;

		private ClassInstanceCreation constInvo;

		private Assignment assignment;

		private MethodDeclaration methodDecl;

		private IMethod methodToMatch;

		private String classNameToMatch;

		private TypeDeclaration typeDecl;

		private ASTFinder() {

		}

		private ASTFinder(IMethod methodToMatch) {
			this.methodToMatch = methodToMatch;
		}

		private ASTFinder(String classNameToMatch) {
			int pos = classNameToMatch.lastIndexOf(".");
			if (pos > 0) {
				this.classNameToMatch = classNameToMatch.substring(pos + 1);
			}
			else {
				this.classNameToMatch = classNameToMatch;
			}
		}

		private ClassInstanceCreation getConstructorInvocation() {
			return constInvo;
		}

		private SimpleName getFieldAccess() {
			if (assignment == null) {
				return null;
			}
			Expression lhs = assignment.getLeftHandSide();
			if (lhs instanceof SimpleName) {
				return (SimpleName) lhs;
			}

			if (lhs instanceof QualifiedName) {
				QualifiedName qn = (QualifiedName) lhs;
				return qn.getName();
			}
			return null;
		}

		private MethodDeclaration getMethodDeclaration() {
			return methodDecl;
		}

		private MethodInvocation getMethodInvocation() {
			return methodInvo;
		}

		private TypeDeclaration getTypeDeclaration() {
			return typeDecl;
		}

		@Override
		public boolean visit(Assignment node) {
			this.assignment = node;
			return false;
		}

		@Override
		public boolean visit(ClassInstanceCreation node) {
			this.constInvo = node;
			return false;
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			if (methodToMatch != null) {
				if (node.getName().getFullyQualifiedName().equals(methodToMatch.getElementName())
						&& node.parameters().size() == methodToMatch.getNumberOfParameters()) {
					this.methodDecl = node;
					return false;
				}
			}

			return super.visit(node);
		}

		@Override
		public boolean visit(MethodInvocation node) {
			this.methodInvo = node;
			return false;
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			if (node.getName().getFullyQualifiedName().equals(classNameToMatch)) {
				this.typeDecl = node;
				return false;
			}
			return true;
		}
	}

	// private static QuickfixSupport nodeQuickfixSupport;

	private static QuickfixSupport attributeQuickfixSupport;

	private static ASTParser createASTParser(IJavaProject javaProject, ICompilationUnit cu, int kind) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(kind);
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.setProject(javaProject);
		return parser;
	}

	public static void createConstructor(IDocument document, IType targetType, List<String> constructorArgClassNames,
			IJavaProject javaProject) {
		ICompilationUnit cu = targetType.getCompilationUnit();
		if (cu == null) {
			return;
		}

		ITypeBinding typeBinding = QuickfixUtils.getTargetTypeBinding(javaProject, targetType);

		String className = targetType.getFullyQualifiedName();
		ClassInstanceCreation invocationNode = QuickfixUtils.getMockConstructorInvocation(className,
				constructorArgClassNames.toArray(new String[constructorArgClassNames.size()]));
		List<Expression> arguments = QuickfixUtils.getArguments(invocationNode);
		Object constructorProposal = QuickfixReflectionUtils.createNewMethodProposal(targetType.getElementName(), cu,
				invocationNode, arguments, typeBinding, 0, null);
		QuickfixReflectionUtils.applyProposal(constructorProposal, document);
	};

	public static ModifierChangeCorrectionProposal createModifierChangeCorrectionProposal(String className,
			String fieldName, IJavaProject javaProject, String displayString, boolean isStatic) {
		IType type = JdtUtils.getJavaType(javaProject.getProject(), className);
		IField field = type.getField(fieldName);
		IBinding binding = getBinding(javaProject, field, type.getCompilationUnit(), ASTParser.K_COMPILATION_UNIT);
		SimpleName fieldNameNode = getMockFieldAccess(className, fieldName, isStatic);
		return new ModifierChangeCorrectionProposal(fieldName, type.getCompilationUnit(), binding, fieldNameNode,
				Modifier.STATIC, 0, 5, null);
	}

	public static NewVariableCorrectionProposal createNewVariableCorrectionProposal(String className, String fieldName,
			IJavaProject javaProject, String displayString, boolean isStatic) {
		IType type = JdtUtils.getJavaType(javaProject.getProject(), className);
		ITypeBinding typeBinding = getTargetTypeBinding(javaProject, type);
		SimpleName fieldNameNode = getMockFieldAccess(className, fieldName, isStatic);
		return new NewVariableCorrectionProposal(displayString, type.getCompilationUnit(),
				NewVariableCorrectionProposal.FIELD, fieldNameNode, typeBinding, 100,
				JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC));

	}

	@SuppressWarnings("unchecked")
	public static List<Expression> getArguments(ClassInstanceCreation constructorInvocation) {
		List<Expression> arguments = new ArrayList<Expression>();
		for (Iterator<Expression> argumentIter = constructorInvocation.arguments().iterator(); argumentIter.hasNext();) {
			Expression argumentExpression = argumentIter.next();
			arguments.add(argumentExpression);
		}
		return arguments;
	}

	@SuppressWarnings("unchecked")
	public static List<Expression> getArguments(MethodInvocation methodInvocation) {
		List<Expression> arguments = new ArrayList<Expression>();
		for (Iterator<Expression> argumentIter = methodInvocation.arguments().iterator(); argumentIter.hasNext();) {
			Expression argumentExpression = argumentIter.next();
			arguments.add(argumentExpression);
		}
		return arguments;
	}

	private static synchronized QuickfixSupport getAttributeQuickfixSupport() {
		if (attributeQuickfixSupport == null) {
			attributeQuickfixSupport = new AttributeQuickfixSupport();
		}
		return attributeQuickfixSupport;
	}

	private static IBinding getBinding(IJavaProject javaProject, IJavaElement javaElement, ICompilationUnit cu, int kind) {
		ASTParser bindingParser = createASTParser(javaProject, cu, kind);
		IBinding[] bindings = bindingParser.createBindings(new IJavaElement[] { javaElement },
				new NullProgressMonitor());
		if (bindings != null && bindings.length > 0) {
			return bindings[0];
		}
		return null;
	}

	public static CompilationUnit getCompilationUnitAST(IJavaProject javaProject, ICompilationUnit cu) {
		ASTParser parser = createASTParser(javaProject, cu, ASTParser.K_COMPILATION_UNIT);
		ASTNode ast = parser.createAST(null);
		if (ast instanceof CompilationUnit) {
			return (CompilationUnit) ast;
		}
		return null;
	}

	public static String getConfigName(IResource resource) {
		return resource.getProjectRelativePath().toString();
	}

	public static IDocument getDocument(IMarker marker) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		IEditorPart editor = null;
		if (marker.getResource() instanceof IFile) {
			FileEditorInput fileInput = new FileEditorInput((IFile) marker.getResource());
			editor = page.findEditor(fileInput);

			if (editor == null) {
				try {
					editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
							(IFile) marker.getResource());
				}
				catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (editor instanceof IConfigEditor) {
				IConfigEditor configEditor = (IConfigEditor) editor;
				return configEditor.getTextViewer().getDocument();
			}
			else if (editor instanceof MultiPageEditorPart) {
				MultiPageEditorPart multiEditor = (MultiPageEditorPart) editor;
				IEditorPart[] editors = multiEditor.findEditors(fileInput);
				for (IEditorPart e : editors) {
					if (e instanceof StructuredTextEditor) {
						return ((StructuredTextEditor) e).getDocumentProvider().getDocument(fileInput);
					}
				}
			}
			else if (editor instanceof AbstractDecoratedTextEditor) {
				return ((AbstractDecoratedTextEditor) editor).getDocumentProvider().getDocument(fileInput);
			}
		}
		return null;
	}

	public static IDOMNode getEnclosingBeanNode(IDOMNode node) {
		if (node == null) {
			return null;
		}

		String localName = node.getLocalName();
		if (localName != null && localName.equals(BeansSchemaConstants.ELEM_BEAN)) {
			return node;
		}

		Node parentNode = node.getParentNode();
		if (parentNode instanceof IDOMNode) {
			return getEnclosingBeanNode((IDOMNode) parentNode);
		}
		return null;
	}

	public static IMethodBinding getMethodBinding(IJavaProject javaProject, IMethod method) {
		IBinding binding = getBinding(javaProject, method, method.getCompilationUnit(),
				ASTParser.K_CLASS_BODY_DECLARATIONS);
		if (binding != null && binding instanceof IMethodBinding) {
			return (IMethodBinding) binding;
		}
		return null;
	}

	public static MethodDeclaration getMethodDecl(IMethod method) {
		ASTParser parser = createASTParser(method.getJavaProject(), method.getCompilationUnit(),
				ASTParser.K_COMPILATION_UNIT);
		ASTNode ast = parser.createAST(null);
		if (ast instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) ast;
			ASTFinder visitor = new ASTFinder(method);
			cu.accept(visitor);
			return visitor.getMethodDeclaration();
		}
		return null;
	}

	public static ClassInstanceCreation getMockConstructorInvocation(String className, String[] argTypes) {
		String varSetup = "";
		String methodArgs = "";

		for (int i = 0; i < argTypes.length; i++) {
			String argType = argTypes[i];
			varSetup += argType + " arg" + i + ";";

			if (i > 0) {
				methodArgs += ",";
			}
			methodArgs += "arg" + i;
		}

		String mockConstructorCode = varSetup + "new " + className + "(" + methodArgs + ");";
		return getParsedExpressionFinder(mockConstructorCode, false).getConstructorInvocation();
	}

	public static SimpleName getMockFieldAccess(String className, String fieldName, boolean isStatic) {
		String code = fieldName + "=null;";
		if (isStatic) {
			code = "public static void stub() {" + code + "}";
		}

		return getParsedExpressionFinder(code, isStatic).getFieldAccess();
	}

	public static MethodInvocation getMockMethodInvocation(String methodName, String[] argTypes, String returnType,
			boolean isStatic) {

		String varSetup = "";
		String methodArgs = "";
		for (int i = 0; i < argTypes.length; i++) {
			String argType = argTypes[i];
			varSetup += argType + " arg" + i + ";";

			if (i > 0) {
				methodArgs += ",";
			}
			methodArgs += "arg" + i;
		}

		String mockMethodInvocationCode = methodName + "(" + methodArgs + ");";
		if (returnType != null) {
			mockMethodInvocationCode = varSetup + returnType + " xxx = " + mockMethodInvocationCode;
		}

		if (isStatic) {
			mockMethodInvocationCode = "static {" + mockMethodInvocationCode + "}";
		}

		return getParsedExpressionFinder(mockMethodInvocationCode, isStatic).getMethodInvocation();
	}

	public static CreateNewMethodQuickFixProposal getNewMethodQuickFixProposal(String methodName, String returnType,
			String[] methodParamTypes, IJavaProject javaProject, String className, int offset, int length, String text,
			boolean missingEndQuote, boolean isStatic, String elementType) {
		IType targetType = null;
		try {
			targetType = javaProject.findType(className);
		}
		catch (JavaModelException e) {
			StatusHandler.log(e.getStatus());
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Quick Asssit Error",
					"Could not find class '" + className + "'");
			return null;
		}

		if (targetType == null) {
			StatusHandler
					.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "Could not find class '" + className + "'"));
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Quick Fix Error", "Could not find class '"
					+ className + "'");
			return null;
		}

		MethodInvocation mockMethodInvocation = getMockMethodInvocation(methodName, methodParamTypes, returnType,
				isStatic);
		if (mockMethodInvocation == null) {
			return null;
		}

		ITypeBinding typeBinding = getTargetTypeBinding(javaProject, targetType);

		List<Expression> arguments = getArguments(mockMethodInvocation);

		ICompilationUnit cu = targetType.getCompilationUnit();

		if (cu == null || typeBinding == null || arguments == null) {
			return null;
		}

		return new CreateNewMethodQuickFixProposal(offset, length, "Add missing " + elementType + " \'" + text
				+ "\' in class \'" + className + "\'", cu, mockMethodInvocation, arguments, typeBinding, 0,
				missingEndQuote);
	}

	private static ASTFinder getParsedExpressionFinder(String toBeParsed, boolean isStatic) {
		ASTParser fooParser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
		if (isStatic) {
			fooParser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		}
		else {
			fooParser.setKind(ASTParser.K_STATEMENTS);
		}
		fooParser.setSource(toBeParsed.toCharArray());
		fooParser.setResolveBindings(true);
		ASTNode parsedAstNode = fooParser.createAST(null);

		ASTFinder visitor = new ASTFinder();
		parsedAstNode.accept(visitor);
		return visitor;
	}

	public static ITypeBinding getTargetTypeBinding(IJavaProject javaProject, IType targetType) {
		IBinding binding = getBinding(javaProject, targetType, targetType.getCompilationUnit(),
				ASTParser.K_COMPILATION_UNIT);
		if (binding != null && binding instanceof ITypeBinding) {
			return (ITypeBinding) binding;
		}
		return null;
	}

	public static BodyDeclaration getTypeDecl(String className, ICompilationUnit compUnit) {
		ASTParser parser = createASTParser(compUnit.getJavaProject(), compUnit, ASTParser.K_COMPILATION_UNIT);
		ASTNode ast = parser.createAST(null);
		if (ast instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) ast;
			ASTFinder visitor = new ASTFinder(className);
			cu.accept(visitor);
			return visitor.getTypeDeclaration();
		}
		return null;
	}

	private static Set<BeanValidator> getValidators(Node node, Attr attr) {
		return getAttributeQuickfixSupport().getQuickfixValidators(node, attr);
	}

	public static boolean validateAttribute(IBeansConfig config, IResourceModelElement contextElement,
			AttrImpl attrImpl, IDOMNode parent, IReporter reporter, boolean reportError,
			BeansEditorValidator editorValidator) {
		Set<BeanValidator> beanValidators = getValidators(parent, attrImpl);
		boolean errorFound = false;
		for (BeanValidator beanValidator : beanValidators) {
			errorFound |= beanValidator.validateAttributeWithConfig(config, contextElement, attrImpl, parent, reporter,
					reportError, editorValidator);
		}

		return errorFound;
	}

}
