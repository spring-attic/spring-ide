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
package org.springframework.ide.eclipse.quickfix.jdt.proposals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;


/**
 * @author Kaitlin Duck Sherwood
 * @since 2.9
 */
public class AddAutowiredConstructorCompletionProposal extends AnnotationCompletionProposal {

	private final TypeDeclaration typeDecl;

	private final ICompilationUnit compilationUnit;

	private final IVariableBinding[] variableBindings;

	public AddAutowiredConstructorCompletionProposal(TypeDeclaration typeDecl, ICompilationUnit cu,
			IVariableBinding[] myVariables) {
		super(NLS.bind("Add {0} constructor", "@Autowired"), cu, QuickfixImages.getImage(QuickfixImages.ANNOTATION));
		this.typeDecl = typeDecl;
		this.compilationUnit = cu;
		this.variableBindings = myVariables;
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {

		CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(typeDecl);
		AST ast = astRoot.getAST();
		ASTRewrite astRewrite = ASTRewrite.create(ast);
		ImportRewrite importRewrite = StubUtility.createImportRewrite(astRoot, true);
		ListRewrite listRewriter = astRewrite.getListRewrite(typeDecl, (typeDecl).getBodyDeclarationsProperty());

		if (listRewriter != null) {
			IJavaProject javaProject = compilationUnit.getJavaProject();
			CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings(javaProject);
			MethodDeclaration methodDecl = createNewConstructor(typeDecl, ast);
			IMethodBinding constructorBinding = ASTNodes.getMethodBinding(methodDecl.getName());
			ITypeBinding typeBinding = ASTNodes.getTypeBinding(typeDecl.getName());
			ImportRewriteContext context = new ContextSensitiveImportRewriteContext(typeDecl, importRewrite);

			MethodDeclaration stub = StubUtility2.createConstructorStub(compilationUnit, astRewrite, importRewrite,
					context, typeBinding, constructorBinding, this.variableBindings, Modifier.PUBLIC, settings);
			if (stub != null) {

				IType type = null;
				// Getting the IType in question turned out to be much more
				// difficult than one would hope. At this point, it appears that
				// typeDecl "belongs to" a different compilation unit than the
				// one which we can get at from here. Thus, we have to look
				// through *this* compilation unit for the IType with the name
				// we are looking for.
				for (Object type2 : compilationUnit.getAllTypes()) {
					if (type2 instanceof IType) {

						// Allegedly, getFullyQualifiedName will return the
						// fully qualified name (e.g. a.b.c.Foo), but I have
						// seen it return a name without the package type.
						String typeName = typeDecl.getName().getFullyQualifiedName();
						String type2Name = ((IType) type2).getFullyQualifiedName();
						if (type2Name.endsWith(typeName)) {
							type = (IType) type2;
							break;
						}
					}
				}
				if (type == null) {
					return astRewrite; // this should never get hit
				}

				IJavaElement[] fields = type.getChildren();
				IJavaElement firstField;
				if (fields.length == 0) {
					firstField = null;
				}
				else {
					firstField = fields[0];
				}

				ASTNode insertion = StubUtility2.getNodeToInsertBefore(listRewriter, firstField);

				if (insertion != null && insertion.getParent() == typeDecl) {
					listRewriter.insertBefore(stub, insertion, null);
				}
				else {
					listRewriter.insertLast(stub, null);
				}

				constructorBinding = ASTNodes.getMethodBinding(stub.getName());
				addAutowiredAnnotation(javaProject, astRewrite, stub, constructorBinding);

			}
		}

		return astRewrite;
	}

	// based on StubUtility2.addOverrideAnnotation
	public void addAutowiredAnnotation(IJavaProject project, ASTRewrite rewrite, MethodDeclaration decl,
			IMethodBinding binding) {
		final Annotation marker = rewrite.getAST().newMarkerAnnotation();
		marker.setTypeName(rewrite.getAST().newSimpleName("Autowired")); //$NON-NLS-1$
		rewrite.getListRewrite(decl, MethodDeclaration.MODIFIERS2_PROPERTY).insertFirst(marker, null);
	}

	@SuppressWarnings("unchecked")
	private MethodDeclaration createNewConstructor(TypeDeclaration typeDecl2, AST ast) {
		MethodDeclaration newConstructor = ast.newMethodDeclaration();
		newConstructor.setConstructor(true);
		newConstructor.setExtraDimensions(0);
		newConstructor.setJavadoc(null);
		int modifier = Modifier.PUBLIC & Modifier.CONSTRUCTOR_INVOCATION;
		newConstructor.modifiers().addAll(ASTNodeFactory.newModifiers(ast, modifier));
		newConstructor.setName(ast.newSimpleName(typeDecl.getName().toString()));

		return newConstructor;

	}

}
