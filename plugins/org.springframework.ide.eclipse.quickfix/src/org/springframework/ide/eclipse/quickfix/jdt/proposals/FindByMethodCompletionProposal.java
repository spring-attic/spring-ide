package org.springframework.ide.eclipse.quickfix.jdt.proposals;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedCorrectionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.quickfix.Activator;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

public class FindByMethodCompletionProposal extends LinkedCorrectionProposal {

	private final String propertyName;

	private final Class<?> propertyClass;

	private final Class<?> domainClass;

	private final TypeDeclaration parentNode;

	private final int startOffset;

	private final int endOffset;

	private final FieldDeclaration stubFieldDecl;

	private final ICompilationUnit cu;

	public FindByMethodCompletionProposal(String propertyName, Class<?> propertyClass, Class<?> domainClass,
			TypeDeclaration parentNode, int startOffset, int endOffset, JavaContentAssistInvocationContext javaContext,
			FieldDeclaration stubFieldDecl) {
		super(getMethodSignature(propertyName, propertyClass.getSimpleName(), domainClass.getSimpleName()), javaContext
				.getCompilationUnit(), null, 0, null);
		this.propertyName = propertyName;
		this.propertyClass = propertyClass;
		this.domainClass = domainClass;
		this.parentNode = parentNode;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.stubFieldDecl = stubFieldDecl;
		this.cu = javaContext.getCompilationUnit();
	}

	private ImportRewrite getImportRewriteHelper(CompilationUnit astRoot) {
		if (getImportRewrite() == null) {
			createImportRewrite(astRoot);
		}
		return getImportRewrite();
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		AST ast = parentNode.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(parentNode);
		if (!ProposalCalculatorUtil.containsImport(cu, propertyClass.getCanonicalName())) {
			getImportRewriteHelper(astRoot).addImport(propertyClass.getCanonicalName());
		}
		if (!ProposalCalculatorUtil.containsImport(cu, domainClass.getCanonicalName())) {
			getImportRewriteHelper(astRoot).addImport(domainClass.getCanonicalName());
		}
		if (!ProposalCalculatorUtil.containsImport(cu, "java.util.List")) {
			getImportRewriteHelper(astRoot).addImport("java.util.List");
		}

		if (stubFieldDecl != null) {
			return rewrite;
		}

		MethodDeclaration methodDecl = ast.newMethodDeclaration();

		SimpleName methodNameSimpleName = ast.newSimpleName(getMethodName(propertyName));
		methodDecl.setName(methodNameSimpleName);

		SimpleName domainTypeName = ast.newSimpleName(domainClass.getSimpleName());
		SimpleType domainType = ast.newSimpleType(domainTypeName);
		SimpleName listTypeName = ast.newSimpleName("List");
		SimpleType listType = ast.newSimpleType(listTypeName);
		ParameterizedType returnType = ast.newParameterizedType(listType);

		ListRewrite returnTypeRewrite = rewrite.getListRewrite(returnType, ParameterizedType.TYPE_ARGUMENTS_PROPERTY);
		returnTypeRewrite.insertFirst(domainType, null);

		methodDecl.setReturnType2(returnType);

		ListRewrite methodParamRewrite = rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY);

		SingleVariableDeclaration paramDecl = ast.newSingleVariableDeclaration();

		SimpleName paramTypeName = ast.newSimpleName(propertyClass.getSimpleName());
		SimpleType paramType = ast.newSimpleType(paramTypeName);
		paramDecl.setType(paramType);

		SimpleName paramName = ast.newSimpleName(propertyName.toLowerCase());
		paramDecl.setName(paramName);
		methodParamRewrite.insertFirst(paramDecl, null);

		addLinkedPosition(rewrite.track(returnType), true, "returnType");
		addLinkedPosition(rewrite.track(methodNameSimpleName), false, "methodName");
		addLinkedPosition(rewrite.track(paramType), false, "paramType");
		addLinkedPosition(rewrite.track(paramName), false, "paramName");

		@SuppressWarnings("unchecked")
		List<BodyDeclaration> bodyDecls = parentNode.bodyDeclarations();
		BodyDeclaration nextSibling = null;
		boolean found = false;
		for (BodyDeclaration bodyDecl : bodyDecls) {
			nextSibling = bodyDecl;
			if (bodyDecl.getStartPosition() > startOffset) {
				found = true;
				break;
			}
		}

		ListRewrite listRewrite = rewrite.getListRewrite(parentNode, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);

		if (nextSibling == null || !found) {
			listRewrite.insertLast(methodDecl, null);
		}
		else {
			listRewrite.insertBefore(methodDecl, nextSibling, null);
		}

		return rewrite;
	}

	public static String getMethodName(String propertyName) {
		StringBuilder name = new StringBuilder("findBy");
		if (propertyName.length() > 0) {
			name.append(propertyName.substring(0, 1).toUpperCase());
		}
		if (propertyName.length() > 1) {
			name.append(propertyName.substring(1));
		}

		return name.toString();
	}

	private static String getMethodSignature(String propertyName, String propertyType, String domainType) {
		StringBuilder name = new StringBuilder();

		name.append(getMethodName(propertyName));
		name.append("(");
		name.append(propertyType);
		name.append(" ");
		name.append(propertyName.toLowerCase());
		name.append(")");
		name.append(" : ");
		name.append("List<");
		name.append(domainType);
		name.append(">");

		return name.toString();
	}

	private String getMethodString() {
		StringBuilder str = new StringBuilder();

		str.append("List<");
		str.append(domainClass.getSimpleName());
		str.append("> ");
		str.append(getMethodName(propertyName));
		str.append("(");
		str.append(propertyClass.getSimpleName());
		str.append(" ");
		str.append(propertyName.toLowerCase());
		str.append(");");

		return str.toString();
	}

	@Override
	public void apply(IDocument document) {
		try {
			if (stubFieldDecl != null) {
				document.replace(startOffset, endOffset - startOffset, getMethodString() + "\n");
			}
			else {
				document.replace(startOffset, endOffset - startOffset, "");
			}

		}
		catch (BadLocationException e) {
			StatusHandler.log(new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}

		super.apply(document);
	}
}
