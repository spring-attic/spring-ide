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
package org.springframework.ide.eclipse.quickfix.jdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * Rename participant for renaming template URI variable and path variable when
 * a method parameter is renamed
 * 
 * @author Terry Denney
 */
public class RequestMappingRenameParticipant extends RenameParticipant {

	private IFile file;

	private String oldName;

	private StringLiteral pathVariableLiteral;

	private PathVariableSourceRange methodPathVariableSourceRange;

	private PathVariableSourceRange typePathVariableSourceRange;

	private class PathVariableSourceRange {

		private final List<Integer> offsets;

		public PathVariableSourceRange() {
			offsets = new ArrayList<Integer>();
		}

		public void addOffset(int offset) {
			offsets.add(offset);
		}

		public List<Integer> getOffsets() {
			return offsets;
		}
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		return new RefactoringStatus();
	}

	private void getPositions(TextEdit edit, List<Integer> positions) {
		if (edit instanceof MultiTextEdit) {
			for (TextEdit child : ((MultiTextEdit) edit).getChildren()) {
				getPositions(child, positions);
			}
		}
		else {
			positions.add(edit.getOffset());
		}
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		RenameArguments arguments = getArguments();
		String newName = arguments.getNewName();
		MultiTextEdit multiEdit = new MultiTextEdit();

		List<Integer> offsets = new ArrayList<Integer>();
		offsets.addAll(typePathVariableSourceRange.getOffsets());
		offsets.addAll(methodPathVariableSourceRange.getOffsets());
		if (pathVariableLiteral != null) {
			offsets.add(pathVariableLiteral.getStartPosition() + 1);
		}

		// need to look at already refactored positions to make sure offsets are
		// correct
		List<Integer> refactoredPositions = new ArrayList<Integer>();
		ProcessorBasedRefactoring refactoring = getProcessor().getRefactoring();
		if (refactoring != null) {
			TextChange textChange = refactoring.getTextChange(file);
			if (textChange != null) {
				TextEdit edit = textChange.getEdit();
				getPositions(edit, refactoredPositions);
			}
		}

		int changeCount = 0;
		int newLength = newName.length();
		int oldLength = oldName.length();
		int diff = newLength - oldLength;

		for (Integer offset : offsets) {
			List<Integer> toBeRemoved = new ArrayList<Integer>();
			for (int position : refactoredPositions) {
				if (position < offset) {
					changeCount++;

					// once a refactoredPosition is accounted for, remove from
					// list so that it won't be double counted
					toBeRemoved.add(position);
				}
			}
			refactoredPositions.removeAll(toBeRemoved);

			multiEdit.addChild(new ReplaceEdit(offset + changeCount * diff, oldLength, newName));
		}

		if (multiEdit.getChildrenSize() > 0) {
			TextFileChange change = new TextFileChange("", file);
			change.setEdit(multiEdit);
			return change;
		}
		else {
			return null;
		}
	}

	private ICompilationUnit getCompilationUnit(IJavaElement element) {
		if (element == null || element instanceof ICompilationUnit) {
			return (ICompilationUnit) element;
		}
		return getCompilationUnit(element.getParent());
	}

	@Override
	public String getName() {
		return "Rename path variable referenced in @Controller class";
	}

	@SuppressWarnings("unchecked")
	private void addPathVariableSourceRanges(BodyDeclaration decl, String pathVariable,
			PathVariableSourceRange sourceRange) throws JavaModelException {
		if (decl == null) {
			return;
		}

		Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("RequestMapping", decl);
		for (Annotation annotation : annotations) {
			Expression value = null;
			if (annotation instanceof SingleMemberAnnotation) {
				value = ((SingleMemberAnnotation) annotation).getValue();
			}
			else if (annotation instanceof NormalAnnotation) {
				List<MemberValuePair> pairs = ((NormalAnnotation) annotation).values();
				for (MemberValuePair pair : pairs) {
					if ("value".equals(pair.getName().getFullyQualifiedName())) {
						value = pair.getValue();
						break;
					}
				}
			}

			if (value instanceof StringLiteral) {
				String uri = ((StringLiteral) value).getLiteralValue();

				int offset = value.getStartPosition() + 1; // skip opening quote
				while (uri != null && uri.length() > 0) {
					int index = uri.indexOf("{" + pathVariable + "}");
					if (index > 0) {
						int pathVariableLength = pathVariable.length();
						sourceRange.addOffset(offset + index + 1);
						offset += index + pathVariableLength + 2;
						uri = uri.substring(index + pathVariableLength + 2);
					}
					else {
						uri = null;
					}
				}
			}
		}
	}

	@Override
	protected boolean initialize(Object element) {
		boolean hasPathVariable = false;

		if (element instanceof ILocalVariable) {
			ILocalVariable variable = (ILocalVariable) element;
			ICompilationUnit compilationUnit = getCompilationUnit(variable);
			file = (IFile) compilationUnit.getResource();

			try {
				ISourceRange sourceRange = variable.getSourceRange();
				AssistContext assistContext = new AssistContext(compilationUnit, null, sourceRange.getOffset(),
						sourceRange.getLength());
				ASTNode node = assistContext.getCoveringNode();

				if (node instanceof SingleVariableDeclaration) {
					SingleVariableDeclaration varDecl = (SingleVariableDeclaration) node;
					Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("PathVariable", varDecl);
					SimpleName varDeclName = varDecl.getName();
					String varName = varDeclName.getFullyQualifiedName();

					String pathVariableName = varName;
					StringLiteral pathVariableLiteral = null;

					for (Annotation annotation : annotations) {
						if (annotation instanceof SingleMemberAnnotation) {
							Expression value = ((SingleMemberAnnotation) annotation).getValue();
							if (value instanceof StringLiteral) {
								pathVariableLiteral = ((StringLiteral) value);
								pathVariableName = pathVariableLiteral.getLiteralValue();
								break;
							}
						}
					}

					if (varName.equals(pathVariableName)) {
						hasPathVariable = true;
						this.oldName = varName;
						this.pathVariableLiteral = pathVariableLiteral;

						// find reference in method annotation
						MethodDeclaration methodDecl = (MethodDeclaration) ASTResolving.findAncestor(varDecl,
								ASTNode.METHOD_DECLARATION);
						methodPathVariableSourceRange = new PathVariableSourceRange();
						addPathVariableSourceRanges(methodDecl, pathVariableName, methodPathVariableSourceRange);

						// find reference in type annotation
						TypeDeclaration typeDecl = (TypeDeclaration) ASTResolving.findAncestor(varDecl,
								ASTNode.TYPE_DECLARATION);
						typePathVariableSourceRange = new PathVariableSourceRange();
						addPathVariableSourceRanges(typeDecl, pathVariableName, typePathVariableSourceRange);
					}
				}

			}
			catch (JavaModelException e) {
				StatusHandler.log(e.getStatus());
			}

		}

		return hasPathVariable;
	}

}
