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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @author Terry Denney
 * @since 2.6
 */
public class RequestMappingVariableCompletionProposal extends AnnotationCompletionProposal {

	private final MethodDeclaration decl;

	private final SingleVariableDeclaration param;

	private final String variableName;

	private StringLiteral oldTemplate;

	private String newTemplateString;

	private int cursorOffset;

	public RequestMappingVariableCompletionProposal(SingleVariableDeclaration param, int startPos, int length,
			Annotation annotation, MethodDeclaration decl, JavaContentAssistInvocationContext javaContext) {
		this(param, param.getName().getFullyQualifiedName(), startPos, length, annotation, decl, javaContext);
	}

	public RequestMappingVariableCompletionProposal(SingleVariableDeclaration param, String variableName, int startPos,
			int length, Annotation annotation, MethodDeclaration decl, JavaContentAssistInvocationContext javaContext) {
		super("", javaContext.getCompilationUnit(), QuickfixImages.getImage(QuickfixImages.LOCAL_VARIABLE));
		this.param = param;
		this.variableName = variableName;
		this.decl = decl;

		setUpProposal(startPos, length, annotation);

		// give higher priority to method parameter that has @PathVariable annotation
		if (param != null && ProposalCalculatorUtil.hasAnnotation("PathVariable", param)) {
			setRelevance(100);
		}
	}

	@SuppressWarnings("unchecked")
	private void setUpProposal(int startPos, int length, Annotation annotation) {
		String typeName = ProposalCalculatorUtil.getTypeName(param.getType());

		StringBuilder displayName = new StringBuilder();
		displayName.append(variableName);
		displayName.append(": ");
		displayName.append(typeName);

		displayName.append(" - ");
		displayName.append(decl.getName().getFullyQualifiedName());
		displayName.append("(");
		List<SingleVariableDeclaration> params = decl.parameters();
		for (int i = 0; i < params.size(); i++) {
			if (i > 0) {
				displayName.append(", ");
			}
			SingleVariableDeclaration currentParam = params.get(i);
			displayName.append(ProposalCalculatorUtil.getTypeName(currentParam.getType()));
			displayName.append(" ");

			if (currentParam == param) {
				displayName.append(variableName);
			}
			else {
				displayName.append(currentParam.getName().getFullyQualifiedName());
			}
		}
		displayName.append(")");

		setDisplayName(displayName.toString());

		if (annotation instanceof NormalAnnotation) {
			List<MemberValuePair> pairs = ((NormalAnnotation) annotation).values();
			for (MemberValuePair pair : pairs) {
				Expression expression = pair.getValue();
				int valueStartPos = expression.getStartPosition();
				int valueLength = expression.getLength();
				if (valueStartPos <= startPos && valueStartPos + valueLength >= startPos + length) {
					setUpProposal(pair.getValue(), startPos, length);
					break;
				}
			}
		}
		else if (annotation instanceof SingleMemberAnnotation) {
			setUpProposal(((SingleMemberAnnotation) annotation).getValue(), startPos, length);
		}
	}

	private void setUpProposal(Expression expression, int startPos, int length) {
		int valueStartPos = expression.getStartPosition();
		int valueLength = expression.getLength();
		if (expression instanceof StringLiteral) {
			valueStartPos++; // skip over open quotes
			valueLength -= 2; // skip over quotes

			StringLiteral stringLiteral = (StringLiteral) expression;
			this.oldTemplate = stringLiteral;

			int lengthDiff = valueLength - length;
			int offset = startPos - valueStartPos;
			int remainingLength = lengthDiff - offset;

			StringBuffer buffer = new StringBuffer();
			String literalValue = stringLiteral.getLiteralValue();
			buffer.append(literalValue.substring(0, startPos - valueStartPos));
			buffer.append(variableName);
			buffer.append(literalValue.substring(valueLength - remainingLength));

			this.cursorOffset = startPos - valueStartPos;

			newTemplateString = buffer.toString();
		}
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		AST ast = decl.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		if (newTemplateString == null || oldTemplate == null) {
			return rewrite;
		}

		boolean isLinked = false;
		if (param != null && !ProposalCalculatorUtil.hasAnnotation("PathVariable", param)) {
			String requestMappingTypeName = PathVariable.class.getCanonicalName();
			if (!ProposalCalculatorUtil.containsImport(getCompilationUnit(), requestMappingTypeName)) {
				CompilationUnit astRoot = ASTResolving.findParentCompilationUnit(decl);
				ImportRewrite importRewrite = createImportRewrite(astRoot);
				importRewrite.addImport(requestMappingTypeName);
			}

			SingleMemberAnnotation annotation = ast.newSingleMemberAnnotation();
			annotation.setTypeName(ast.newSimpleName("PathVariable"));
			StringLiteral pathVariableName = ast.newStringLiteral();
			pathVariableName.setLiteralValue(variableName);
			ITrackedNodePosition trackPathVariable = rewrite.track(pathVariableName);
			addLinkedPosition(new StringLiteralTrackedPosition(trackPathVariable), true, "PathVariable");
			annotation.setValue(pathVariableName);
			isLinked = true;

			ChildListPropertyDescriptor property;

			property = SingleVariableDeclaration.MODIFIERS2_PROPERTY;

			rewrite.getListRewrite(param, property).insertLast(annotation, null);
		}

		StringLiteral newTemplate = ast.newStringLiteral();
		newTemplate.setLiteralValue(newTemplateString);
		ITrackedNodePosition trackTemplateVariable = rewrite.track(newTemplate);
		rewrite.replace(oldTemplate, newTemplate, null);

		if (isLinked) {
			addLinkedPosition(new StringLiteralTrackedPosition(trackTemplateVariable, cursorOffset, variableName
					.length(), false), false, "PathVariable");
		}
		else {
			setTrackPosition(new StringLiteralTrackedPosition(trackTemplateVariable, cursorOffset
					+ variableName.length(), 0, true));
		}

		return rewrite;
	}

}
