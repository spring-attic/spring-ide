package org.springframework.ide.eclipse.quickfix.jdt.proposals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;

public class PackageNameCompletionProposal extends AnnotationCompletionProposal {

	private final String packageName;

	private final Annotation annotation;

	public PackageNameCompletionProposal(String packageName, Annotation annotation,
			JavaContentAssistInvocationContext javaContext) {
		super(packageName, javaContext.getCompilationUnit(), QuickfixImages.getImage(QuickfixImages.PACKAGE));
		this.packageName = packageName;
		this.annotation = annotation;
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		AST ast = annotation.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		if (annotation.isSingleMemberAnnotation()) {
			SingleMemberAnnotation smAnnotation = (SingleMemberAnnotation) annotation;
			Expression oldValue = smAnnotation.getValue();

			StringLiteral newValue = ast.newStringLiteral();
			newValue.setLiteralValue(packageName);

			ITrackedNodePosition newValuePosition = rewrite.track(newValue);
			rewrite.replace(oldValue, newValue, null);

			setTrackPosition(new StringLiteralTrackedPosition(newValuePosition, newValuePosition.getStartPosition() + 1
					+ packageName.length(), 0, true));

			return rewrite;
		}

		return super.getRewrite();
	}
}
