package org.springframework.ide.eclipse.quickfix.jdt.proposals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;

/**
 * Proposal for completing a class attribute (i.e. ClassName.class)
 * @author Terry Denney
 * 
 */
public class ClassCompletionProposal extends AnnotationCompletionProposal {

	private final String classValue;

	private final Annotation annotation;

	private final StringLiteral oldLiteral;

	public ClassCompletionProposal(String className, Annotation annotation, StringLiteral oldLiteral,
			JavaContentAssistInvocationContext javaContext) {
		super(className + ".class", javaContext.getCompilationUnit(), QuickfixImages.getImage(QuickfixImages.CLASS));
		this.classValue = className + ".class";
		this.annotation = annotation;
		this.oldLiteral = oldLiteral;
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		AST ast = annotation.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		StringLiteral newValue = ast.newStringLiteral();
		newValue.setLiteralValue(classValue);
		ITrackedNodePosition newValuePosition = rewrite.track(newValue);

		rewrite.replace(oldLiteral, newValue, null);
		setTrackPosition(new StringLiteralTrackedPosition(newValuePosition, newValuePosition.getStartPosition() + 1
				+ classValue.length(), 0, true));

		return rewrite;
	}
}
