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
package org.springframework.ide.eclipse.quickfix.proposals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;

/**
 * @author Terry Denney
 * @author Martin Lippert
 */
public class RemoveDeprecatedQuickFixProposal extends BeanAttributeQuickFixProposal implements ICompletionProposal {

	private IMethod method;

	private final String className;

	private String methodName;

	private final ICompilationUnit cu;

	public RemoveDeprecatedQuickFixProposal(int offset, int length, boolean missingEndQuote, String className,
			IType type) {
		super(offset, length, missingEndQuote);
		this.className = className;
		this.cu = type.getCompilationUnit();
	}

	public RemoveDeprecatedQuickFixProposal(int offset, int length, boolean missingEndQuote, String className,
			String methodName, IMethod method) {
		super(offset, length, missingEndQuote);
		this.className = className;
		this.methodName = methodName;
		this.method = method;
		this.cu = method.getCompilationUnit();
	}

	@Override
	public void applyQuickFix(IDocument document) {
		BodyDeclaration decl = getDeclaration();
		removeDeprecatedAnnotation(document, cu, decl);
	}

	// copied from
	// org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor
	@SuppressWarnings("rawtypes")
	private Annotation findAnnotation(List modifiers) {
		for (int i = 0; i < modifiers.size(); i++) {
			Object curr = modifiers.get(i);
			if (curr instanceof Annotation) {
				Annotation annot = (Annotation) curr;
				ITypeBinding binding = annot.getTypeName().resolveTypeBinding();
				if (binding != null && "java.lang.Deprecated".equals(binding.getQualifiedName())) {
					return annot;
				}
			}
		}
		return null;
	}

	private BodyDeclaration getDeclaration() {
		if (method != null) {
			return QuickfixUtils.getMethodDecl(method);
		}
		return QuickfixUtils.getTypeDecl(className, cu);
	}

	public String getDisplayString() {
		StringBuilder displayBuilder = new StringBuilder("Remove '@Deprecated' from ");
		displayBuilder.append(className);
		if (methodName != null) {
			displayBuilder.append(".");
			displayBuilder.append(methodName);
			displayBuilder.append("(..)");
		}
		return displayBuilder.toString();
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
	}

	private ITextViewer getViewer(IEditorPart editorPart) {
		if (editorPart instanceof IConfigEditor) {
			IConfigEditor configEditor = (IConfigEditor) editorPart;
			return configEditor.getTextViewer();
		}
		if (editorPart instanceof StructuredTextEditor) {
			return ((StructuredTextEditor) editorPart).getTextViewer();
		}
		if (editorPart instanceof XMLMultiPageEditorPart) {
			Object result = QuickfixReflectionUtils.callProtectedMethod(editorPart, "getTextEditor");
			return getViewer((IEditorPart) result);
		}
		if (editorPart instanceof JavaEditor) {
			return ((JavaEditor) editorPart).getViewer();
		}

		return null;
	}

	// similar to
	// org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor.removeOverrideAnnotationProposal(..)
	public void removeDeprecatedAnnotation(IDocument document, ICompilationUnit cu, BodyDeclaration decl) {
		Annotation annot = findAnnotation(decl.modifiers());
		if (annot != null) {
			ASTRewrite rewrite = ASTRewrite.create(annot.getAST());
			rewrite.remove(annot, null);

			callASTRewriteCorrectionProposal(getDisplayString(), cu, rewrite, 6, getImage(), document);

			ITextViewer viewer = getViewer(JavaPlugin.getActivePage().getActiveEditor());
			ITrackedNodePosition trackPos = rewrite.track(decl);
			if (trackPos != null && viewer != null) {
				viewer.setSelectedRange(trackPos.getStartPosition(), 0);
			}
		}
	}

	private void callASTRewriteCorrectionProposal(String displayString, ICompilationUnit cu2, ASTRewrite rewrite,
			int i, Image image, IDocument document) {
		try {
			Class<?> astRewriteClass = getASTRewriteCorrectionProposalClass();
			Constructor<?> constructor = astRewriteClass.getConstructor(String.class, ICompilationUnit.class,
					ASTRewrite.class, int.class, Image.class);
			Object astRewriteCorrectionProposal = constructor.newInstance(displayString, cu2, rewrite, i, image);
			Method applyMethod = astRewriteCorrectionProposal.getClass().getMethod("apply", IDocument.class);
			applyMethod.invoke(astRewriteCorrectionProposal, document);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Class<?> getASTRewriteCorrectionProposalClass() throws ClassNotFoundException {
		try {
			// Eclipse 3.7 and previous versions
			return this.getClass().getClassLoader()
					.loadClass("org.eclipse.jdt.internal.ui.text.correction.proposals.ASTRewriteCorrectionProposal");
		}
		catch (ClassNotFoundException e) {
			// Eclipse 3.8, 4.2 and beyond
			return this.getClass().getClassLoader()
					.loadClass("org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal");
		}
	}
}
