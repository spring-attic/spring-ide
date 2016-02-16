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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.ui.text.java.JavaTypeCompletionProposal;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.springframework.ide.eclipse.quickfix.jdt.processors.JdtQuickfixUtils;
import org.springframework.ide.eclipse.quickfix.jdt.processors.RequestMappingDialog;
import org.springframework.ide.eclipse.quickfix.jdt.processors.RequestMappingDialog.Method;


/**
 * @author Terry Denney
 */
public class AddRequestMappingCompletionProposal extends JavaTypeCompletionProposal {

	private static final String REQUEST_MAPPING_IMPORT = "org.springframework.web.bind.annotation.RequestMapping";

	private static final String REQUEST_METHOD_IMPORT = "org.springframework.web.bind.annotation.RequestMethod";

	private final ICompilationUnit cu;

	private final BodyDeclaration decl;

	public AddRequestMappingCompletionProposal(String typeName, BodyDeclaration decl, ICompilationUnit cu, int start,
			int length, int relevance) {
		super("", cu, start, length, null, getDisplayName(typeName), relevance);
		this.decl = decl;
		this.cu = cu;
	}

	@Override
	public void apply(IDocument document, char trigger, int offset) {
		applyChange(document, offset);
		// super.apply(document, trigger, offset);
	}

	@Override
	public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
		applyChange(viewer.getDocument(), offset);
		// super.apply(viewer, trigger, stateMask, offset);
	}

	private void applyChange(IDocument document, int offset) {
		try {
			if (decl instanceof MethodDeclaration) {
				applyChangeForMethod(document, offset);
			}
			else if (decl instanceof TypeDeclaration) {
				applyChangeForType(document, offset);
			}
		}
		catch (MalformedTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void applyChangeForMethod(IDocument document, int offset) throws MalformedTreeException,
			JavaModelException, IllegalArgumentException, BadLocationException {
		MultiTextEdit edit = new MultiTextEdit();
		String methodTypeString = null;

		RequestMappingDialog dialog = new RequestMappingDialog(Display.getDefault().getActiveShell());
		if (dialog.open() == Dialog.OK) {
			TextEdit importEdit = JdtQuickfixUtils.getTextEditForImport(cu, REQUEST_MAPPING_IMPORT);
			if (importEdit != null) {
				edit.addChild(importEdit);
			}

			Method methodType = dialog.getMethodType();
			if (methodType == Method.GET) {
				methodTypeString = "GET";
			}
			else if (methodType == Method.POST) {
				methodTypeString = "POST";
			}

			ASTRewrite astRewrite = ASTRewrite.create(decl.getAST());
			NormalAnnotation annotation = astRewrite.getAST().newNormalAnnotation();

			annotation.setTypeName(astRewrite.getAST().newSimpleName("RequestMapping")); //$NON-NLS-1$
			if (methodTypeString != null) {
				importEdit = JdtQuickfixUtils.getTextEditForImport(cu, REQUEST_METHOD_IMPORT);
				if (importEdit != null) {
					edit.addChild(importEdit);
				}

				ListRewrite listRewrite = astRewrite.getListRewrite(annotation, NormalAnnotation.VALUES_PROPERTY);
				AST annotationAST = annotation.getAST();
				MemberValuePair pair = annotationAST.newMemberValuePair();
				pair.setName(annotationAST.newSimpleName("method"));

				QualifiedName valueName = annotationAST.newQualifiedName(annotationAST.newSimpleName("RequestMethod"),
						annotationAST.newSimpleName(methodTypeString));
				pair.setValue(valueName);
				listRewrite.insertFirst(pair, null);
			}

			astRewrite.getListRewrite(decl, MethodDeclaration.MODIFIERS2_PROPERTY).insertFirst(annotation, null);

			edit.addChild(astRewrite.rewriteAST());

			int oldLength = document.getLength();
			// int oldCurserPos = document.getCursorPosition();
			edit.apply(document);

			int diff = document.getLength() - oldLength;

			setCursorPosition(diff + offset);
		}
	}

	private void applyChangeForType(IDocument document, int offset) throws MalformedTreeException, JavaModelException,
			IllegalArgumentException, BadLocationException {
		MultiTextEdit edit = new MultiTextEdit();

		TextEdit importEdit = JdtQuickfixUtils.getTextEditForImport(cu, REQUEST_MAPPING_IMPORT);
		if (importEdit != null) {
			edit.addChild(importEdit);
		}

		ASTRewrite astRewrite = ASTRewrite.create(decl.getAST());
		NormalAnnotation annotation = astRewrite.getAST().newNormalAnnotation();

		annotation.setTypeName(astRewrite.getAST().newSimpleName("RequestMapping"));
		astRewrite.getListRewrite(decl, TypeDeclaration.MODIFIERS2_PROPERTY).insertFirst(annotation, null);

		ITrackedNodePosition tracker = astRewrite.track(annotation.getTypeName());
		edit.addChild(astRewrite.rewriteAST());

		edit.apply(document);

		setReplacementOffset(tracker.getStartPosition() + annotation.getTypeName().getFullyQualifiedName().length() + 1);
	}

	@Override
	protected boolean updateReplacementString(IDocument document, char trigger, int offset, ImportRewrite impRewrite)
			throws CoreException, BadLocationException {
		applyChange(document, offset);
		return true;
	}

	private static StyledString getDisplayName(String typeName) {
		StyledString buf = new StyledString();
		buf.append("Add @RequestMapping annotation for '");
		buf.append(typeName);
		buf.append("'");
		return buf;
	}

}
