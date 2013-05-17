package org.springframework.ide.eclipse.quickfix.jdt.proposals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;

public class PackageNameCompletionProposal extends AnnotationCompletionProposal {

	private final String packageName;

	private final Annotation annotation;

	private final ASTNode oldLiteral;

	public PackageNameCompletionProposal(String packageName, Annotation annotation, ASTNode oldNode,
			JavaContentAssistInvocationContext javaContext) {
		super(packageName, javaContext.getCompilationUnit(), QuickfixImages.getImage(QuickfixImages.PACKAGE));
		this.packageName = packageName;
		this.annotation = annotation;
		this.oldLiteral = oldNode;
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		AST ast = annotation.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		StringLiteral newValue = ast.newStringLiteral();
		newValue.setLiteralValue(packageName);
		ITrackedNodePosition newValuePosition = rewrite.track(newValue);

		rewrite.replace(oldLiteral, newValue, null);
		setTrackPosition(new StringLiteralTrackedPosition(newValuePosition, newValuePosition.getStartPosition() + 1
				+ packageName.length(), 0, true));

		return rewrite;
	}
}
