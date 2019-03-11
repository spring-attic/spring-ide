/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for Bug 463360 - [override method][null] generating method override should not create redundant null annotations
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.copied;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.TypeLocation;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.util.JDTUIHelperClasses;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.preferences.formatter.FormatterProfileManager;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Utilities for code generation based on AST rewrite.
 *
 * @see StubUtility
 * @see JDTUIHelperClasses
 * @since 3.1
 */
public final class StubUtility2 {

	/**
	 * Adds <code>@Override</code> annotation to <code>methodDecl</code> if not
	 * already present and if requested by code style settings or compiler
	 * errors/warnings settings.
	 *
	 * @param settings the code generation style settings, may be
	 * <code>null</code>
	 * @param project the Java project used to access the compiler settings
	 * @param rewrite the ASTRewrite
	 * @param imports the ImportRewrite
	 * @param methodDecl the method declaration to add the annotation to
	 * @param isDeclaringTypeInterface <code>true</code> if the type declaring
	 * the method overridden by <code>methodDecl</code> is an interface
	 * @param group the text edit group, may be <code>null</code>
	 */
	public static void addOverrideAnnotation(CodeGenerationSettings settings, IJavaProject project, ASTRewrite rewrite,
			ImportRewrite imports, MethodDeclaration methodDecl, boolean isDeclaringTypeInterface,
			TextEditGroup group) {
		if (!JavaModelUtil.is50OrHigher(project)) {
			return;
		}
		if (isDeclaringTypeInterface) {
			String version = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			if (JavaModelUtil.isVersionLessThan(version, JavaCore.VERSION_1_6)) {
				return; // not allowed in 1.5
			}
			if (JavaCore.DISABLED.equals(project.getOption(
					JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION_FOR_INTERFACE_METHOD_IMPLEMENTATION, true))) {
				return; // user doesn't want to use 1.6 style
			}
		}
		if ((settings != null && settings.overrideAnnotation)
				|| !JavaCore.IGNORE.equals(project.getOption(JavaCore.COMPILER_PB_MISSING_OVERRIDE_ANNOTATION, true))) {
			createOverrideAnnotation(rewrite, imports, methodDecl, group);
		}
	}

	public static void createOverrideAnnotation(ASTRewrite rewrite, ImportRewrite imports, MethodDeclaration decl,
			TextEditGroup group) {
		if (findAnnotation("java.lang.Override", decl.modifiers()) != null) { //$NON-NLS-1$
			return; // No need to add duplicate annotation
		}
		AST ast = rewrite.getAST();
		ASTNode root = decl.getRoot();
		ImportRewriteContext context = null;
		if (root instanceof CompilationUnit) {
			context = new ContextSensitiveImportRewriteContext((CompilationUnit) root, decl.getStartPosition(),
					imports);
		}
		Annotation marker = ast.newMarkerAnnotation();
		marker.setTypeName(ast.newName(imports.addImport("java.lang.Override", context))); //$NON-NLS-1$
		rewrite.getListRewrite(decl, MethodDeclaration.MODIFIERS2_PROPERTY).insertFirst(marker, group);
	}

	/* This method should work with all AST levels. */
	public static MethodDeclaration createConstructorStub(ICompilationUnit unit, ASTRewrite rewrite,
			ImportRewrite imports, ImportRewriteContext context, IMethodBinding binding, String type, int modifiers,
			boolean omitSuperForDefConst, boolean todo, CodeGenerationSettings settings) throws CoreException {
		AST ast = rewrite.getAST();
		MethodDeclaration decl = ast.newMethodDeclaration();
		decl.modifiers().addAll(ASTNodeFactory.newModifiers(ast, modifiers & ~Modifier.ABSTRACT & ~Modifier.NATIVE));
		decl.setName(ast.newSimpleName(type));
		decl.setConstructor(true);

		createTypeParameters(imports, context, ast, binding, decl);

		List<SingleVariableDeclaration> parameters = createParameters(unit.getJavaProject(), imports, context, ast,
				binding, null, decl);

		createThrownExceptions(decl, binding, imports, context, ast);

		Block body = ast.newBlock();
		decl.setBody(body);

		String delimiter = StubUtility.getLineDelimiterUsed(unit);
		String bodyStatement = ""; //$NON-NLS-1$
		if (!omitSuperForDefConst || !parameters.isEmpty()) {
			SuperConstructorInvocation invocation = ast.newSuperConstructorInvocation();
			SingleVariableDeclaration varDecl = null;
			for (Iterator<SingleVariableDeclaration> iterator = parameters.iterator(); iterator.hasNext();) {
				varDecl = iterator.next();
				invocation.arguments().add(ast.newSimpleName(varDecl.getName().getIdentifier()));
			}
			bodyStatement = ASTNodes.asFormattedString(invocation, 0, delimiter,
					FormatterProfileManager.getProjectSettings(unit.getJavaProject()));
		}

		if (todo) {
			String placeHolder = CodeGeneration.getMethodBodyContent(unit, type, binding.getName(), true, bodyStatement,
					delimiter);
			if (placeHolder != null) {
				ReturnStatement todoNode = (ReturnStatement) rewrite.createStringPlaceholder(placeHolder,
						ASTNode.RETURN_STATEMENT);
				body.statements().add(todoNode);
			}
		}
		else {
			ReturnStatement statementNode = (ReturnStatement) rewrite.createStringPlaceholder(bodyStatement,
					ASTNode.RETURN_STATEMENT);
			body.statements().add(statementNode);
		}

		if (settings != null && settings.createComments) {
			String string = CodeGeneration.getMethodComment(unit, type, decl, binding, delimiter);
			if (string != null) {
				Javadoc javadoc = (Javadoc) rewrite.createStringPlaceholder(string, ASTNode.JAVADOC);
				decl.setJavadoc(javadoc);
			}
		}
		return decl;
	}

	public static MethodDeclaration createConstructorStub(ICompilationUnit unit, ASTRewrite rewrite,
			ImportRewrite imports, ImportRewriteContext context, ITypeBinding typeBinding,
			IMethodBinding superConstructor, IVariableBinding[] variableBindings, int modifiers,
			CodeGenerationSettings settings) throws CoreException {
		AST ast = rewrite.getAST();

		MethodDeclaration decl = ast.newMethodDeclaration();
		decl.modifiers().addAll(ASTNodeFactory.newModifiers(ast, modifiers & ~Modifier.ABSTRACT & ~Modifier.NATIVE));
		decl.setName(ast.newSimpleName(typeBinding.getName()));
		decl.setConstructor(true);

		List<SingleVariableDeclaration> parameters = decl.parameters();
		if (superConstructor != null) {
			createTypeParameters(imports, context, ast, superConstructor, decl);

			createParameters(unit.getJavaProject(), imports, context, ast, superConstructor, null, decl);

			createThrownExceptions(decl, superConstructor, imports, context, ast);
		}

		Block body = ast.newBlock();
		decl.setBody(body);

		String delimiter = StubUtility.getLineDelimiterUsed(unit);

		if (superConstructor != null) {
			SuperConstructorInvocation invocation = ast.newSuperConstructorInvocation();
			SingleVariableDeclaration varDecl = null;
			for (Iterator<SingleVariableDeclaration> iterator = parameters.iterator(); iterator.hasNext();) {
				varDecl = iterator.next();
				invocation.arguments().add(ast.newSimpleName(varDecl.getName().getIdentifier()));
			}
			body.statements().add(invocation);
		}

		List<String> prohibited = new ArrayList<>();
		for (final Iterator<SingleVariableDeclaration> iterator = parameters.iterator(); iterator.hasNext();) {
			prohibited.add(iterator.next().getName().getIdentifier());
		}
		String param = null;
		List<String> list = new ArrayList<>(prohibited);
		String[] excluded = null;
		for (int i = 0; i < variableBindings.length; i++) {
			SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
			var.setType(imports.addImport(variableBindings[i].getType(), ast, context, TypeLocation.PARAMETER));
			excluded = new String[list.size()];
			list.toArray(excluded);
			param = suggestParameterName(unit, variableBindings[i], excluded);
			list.add(param);
			var.setName(ast.newSimpleName(param));
			parameters.add(var);
		}

		list = new ArrayList<>(prohibited);
		for (int i = 0; i < variableBindings.length; i++) {
			excluded = new String[list.size()];
			list.toArray(excluded);
			final String paramName = suggestParameterName(unit, variableBindings[i], excluded);
			list.add(paramName);
			final String fieldName = variableBindings[i].getName();
			Expression expression = null;
			if (paramName.equals(fieldName) || settings.useKeywordThis) {
				FieldAccess access = ast.newFieldAccess();
				access.setExpression(ast.newThisExpression());
				access.setName(ast.newSimpleName(fieldName));
				expression = access;
			}
			else {
				expression = ast.newSimpleName(fieldName);
			}
			Assignment assignment = ast.newAssignment();
			assignment.setLeftHandSide(expression);
			assignment.setRightHandSide(ast.newSimpleName(paramName));
			assignment.setOperator(Assignment.Operator.ASSIGN);
			body.statements().add(ast.newExpressionStatement(assignment));
		}

		if (settings != null && settings.createComments) {
			String string = CodeGeneration.getMethodComment(unit, typeBinding.getName(), decl, superConstructor,
					delimiter);
			if (string != null) {
				Javadoc javadoc = (Javadoc) rewrite.createStringPlaceholder(string, ASTNode.JAVADOC);
				decl.setJavadoc(javadoc);
			}
		}
		return decl;
	}

	private static void createTypeParameters(ImportRewrite imports, ImportRewriteContext context, AST ast,
			IMethodBinding binding, MethodDeclaration decl) {
		ITypeBinding[] typeParams = binding.getTypeParameters();
		List<TypeParameter> typeParameters = decl.typeParameters();
		for (int i = 0; i < typeParams.length; i++) {
			ITypeBinding curr = typeParams[i];
			TypeParameter newTypeParam = ast.newTypeParameter();
			newTypeParam.setName(ast.newSimpleName(curr.getName()));
			ITypeBinding[] typeBounds = curr.getTypeBounds();
			if (typeBounds.length != 1 || !"java.lang.Object".equals(typeBounds[0].getQualifiedName())) {//$NON-NLS-1$
				List<Type> newTypeBounds = newTypeParam.typeBounds();
				for (int k = 0; k < typeBounds.length; k++) {
					newTypeBounds.add(imports.addImport(typeBounds[k], ast, context, TypeLocation.TYPE_BOUND));
				}
			}
			typeParameters.add(newTypeParam);
		}
	}

	private static List<SingleVariableDeclaration> createParameters(IJavaProject project, ImportRewrite imports,
			ImportRewriteContext context, AST ast, IMethodBinding binding, String[] paramNames,
			MethodDeclaration decl) {
		return createParameters(project, imports, context, ast, binding, paramNames, decl, null);
	}

	private static List<SingleVariableDeclaration> createParameters(IJavaProject project, ImportRewrite imports,
			ImportRewriteContext context, AST ast, IMethodBinding binding, String[] paramNames, MethodDeclaration decl,
			EnumSet<TypeLocation> nullnessDefault) {
		boolean is50OrHigher = JavaModelUtil.is50OrHigher(project);
		List<SingleVariableDeclaration> parameters = decl.parameters();
		ITypeBinding[] params = binding.getParameterTypes();
		if (paramNames == null || paramNames.length < params.length) {
			paramNames = StubUtility.suggestArgumentNames(project, binding);
		}
		for (int i = 0; i < params.length; i++) {
			SingleVariableDeclaration var = ast.newSingleVariableDeclaration();
			ITypeBinding type = params[i];
			type = replaceWildcardsAndCaptures(type);
			if (!is50OrHigher) {
				type = type.getErasure();
				var.setType(imports.addImport(type, ast, context, TypeLocation.PARAMETER));
			}
			else if (binding.isVarargs() && type.isArray() && i == params.length - 1) {
				var.setVarargs(true);
				/*
				 * Varargs annotations are special. Example: foo(@O Object @A
				 * [] @B ... arg) => @B is not an annotation on the array
				 * dimension that constitutes the vararg. It's the type
				 * annotation of the *innermost* array dimension.
				 */
				int dimensions = type.getDimensions();
				@SuppressWarnings("unchecked")
				List<Annotation>[] dimensionAnnotations = (List<Annotation>[]) new List<?>[dimensions];
				for (int dim = 0; dim < dimensions; dim++) {
					dimensionAnnotations[dim] = new ArrayList<>();
					for (IAnnotationBinding annotation : type.getTypeAnnotations()) {
						dimensionAnnotations[dim].add(imports.addAnnotation(annotation, ast, context));
					}
					type = type.getComponentType();
				}

				Type elementType = imports.addImport(type, ast, context);
				if (dimensions == 1) {
					var.setType(elementType);
				}
				else {
					ArrayType arrayType = ast.newArrayType(elementType, dimensions - 1);
					List<Dimension> dimensionNodes = arrayType.dimensions();
					for (int dim = 0; dim < dimensions - 1; dim++) { // all
																		// except
																		// the
																		// innermost
																		// dimension
						Dimension dimension = dimensionNodes.get(dim);
						dimension.annotations().addAll(dimensionAnnotations[dim]);
					}
					var.setType(arrayType);
				}
				List<Annotation> varargTypeAnnotations = dimensionAnnotations[dimensions - 1];
				var.varargsAnnotations().addAll(varargTypeAnnotations);
			}
			else {
				var.setType(imports.addImport(type, ast, context, TypeLocation.PARAMETER));
			}
			var.setName(ast.newSimpleName(paramNames[i]));
			IAnnotationBinding[] annotations = binding.getParameterAnnotations(i);
			for (IAnnotationBinding annotation : annotations) {
				if (StubUtility2.isCopyOnInheritAnnotation(annotation.getAnnotationType(), project, nullnessDefault,
						TypeLocation.PARAMETER)) {
					var.modifiers().add(imports.addAnnotation(annotation, ast, context));
				}
			}
			parameters.add(var);
		}
		return parameters;
	}

	public static boolean isCopyOnInheritAnnotation(ITypeBinding annotationType, IJavaProject project,
			EnumSet<TypeLocation> nullnessDefault, TypeLocation typeLocation) {
		if (JavaCore.ENABLED.equals(project.getOption(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, true))) {
			return false;
		}
		if (nullnessDefault != null && Bindings.isNonNullAnnotation(annotationType, project)) {
			if (!nullnessDefault.contains(typeLocation)) {
				return true;
			}
			return false; // nonnull within the scope of @NonNullByDefault:
							// don't copy
		}
		return Bindings.isAnyNullAnnotation(annotationType, project);
	}

	/**
	 * @param decl method declaration
	 * @return thrown exception names
	 * @deprecated to avoid deprecation warnings
	 */
	@Deprecated
	private static List<Name> getThrownExceptions(MethodDeclaration decl) {
		return decl.thrownExceptions();
	}

	private static void createThrownExceptions(MethodDeclaration decl, IMethodBinding method, ImportRewrite imports,
			ImportRewriteContext context, AST ast) {
		ITypeBinding[] excTypes = method.getExceptionTypes();
		if (ast.apiLevel() >= AST.JLS8) {
			List<Type> thrownExceptions = decl.thrownExceptionTypes();
			for (int i = 0; i < excTypes.length; i++) {
				Type excType = imports.addImport(excTypes[i], ast, context, TypeLocation.EXCEPTION);
				thrownExceptions.add(excType);
			}
		}
		else {
			List<Name> thrownExceptions = getThrownExceptions(decl);
			for (int i = 0; i < excTypes.length; i++) {
				String excTypeName = imports.addImport(excTypes[i], context);
				thrownExceptions.add(ASTNodeFactory.newName(ast, excTypeName));
			}
		}
	}

	/**
	 * Evaluates the insertion position of a new node.
	 *
	 * @param listRewrite The list rewriter to which the new node will be added
	 * @param sibling The Java element before which the new element should be
	 * added.
	 * @return the AST node of the list to insert before or null to insert as
	 * last.
	 * @throws JavaModelException thrown if accessing the Java element failed
	 */

	public static ASTNode getNodeToInsertBefore(ListRewrite listRewrite, IJavaElement sibling)
			throws JavaModelException {
		if (sibling instanceof IMember) {
			ISourceRange sourceRange = ((IMember) sibling).getSourceRange();
			if (sourceRange == null) {
				return null;
			}
			int insertPos = sourceRange.getOffset();

			List<? extends ASTNode> members = listRewrite.getOriginalList();
			for (int i = 0; i < members.size(); i++) {
				ASTNode curr = members.get(i);
				if (curr.getStartPosition() >= insertPos) {
					return curr;
				}
			}
		}
		return null;
	}

	public static ITypeBinding replaceWildcardsAndCaptures(ITypeBinding type) {
		while (type.isWildcardType() || type.isCapture() || (type.isArray() && type.getElementType().isCapture())) {
			ITypeBinding bound = type.getBound();
			type = (bound != null) ? bound : type.getErasure();
		}
		return type;
	}

	private static String suggestParameterName(ICompilationUnit unit, IVariableBinding binding, String[] excluded) {
		String name = StubUtility.getBaseName(binding, unit.getJavaProject());
		return StubUtility.suggestArgumentName(unit.getJavaProject(), name, excluded);
	}

	public static Annotation findAnnotation(String qualifiedTypeName, List<IExtendedModifier> modifiers) {
		for (int i = 0; i < modifiers.size(); i++) {
			IExtendedModifier curr = modifiers.get(i);
			if (curr instanceof Annotation) {
				Annotation annot = (Annotation) curr;
				ITypeBinding binding = annot.getTypeName().resolveTypeBinding();
				if (binding != null && qualifiedTypeName.equals(binding.getQualifiedName())) {
					return annot;
				}
			}
		}
		return null;
	}

	/**
	 * Creates a new stub utility.
	 */
	private StubUtility2() {
		// Not for instantiation
	}

}
