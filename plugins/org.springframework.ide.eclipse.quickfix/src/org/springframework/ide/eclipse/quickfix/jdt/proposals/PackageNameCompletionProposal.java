package org.springframework.ide.eclipse.quickfix.jdt.proposals;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.internal.core.SourceRefElement;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.source.SourceViewer;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;

public class PackageNameCompletionProposal extends AnnotationCompletionProposal {

	private final String packageName;

	private Annotation annotation;

	private ASTNode oldLiteral;

	private IAnnotation iAnnotation;

	private JavaContentAssistInvocationContext javaContext;

	public PackageNameCompletionProposal(String packageName, IAnnotation a,
			JavaContentAssistInvocationContext javaContext) {
		super(packageName, javaContext.getCompilationUnit(), QuickfixImages.getImage(QuickfixImages.PACKAGE));
		this.packageName = packageName;
		this.iAnnotation = a;
		this.javaContext = javaContext;
	}

	public PackageNameCompletionProposal(String packageName, Annotation annotation, ASTNode oldNode,
			JavaContentAssistInvocationContext javaContext) {
		super(packageName, javaContext.getCompilationUnit(), QuickfixImages.getImage(QuickfixImages.PACKAGE));
		this.packageName = packageName;
		this.annotation = annotation;
		this.oldLiteral = oldNode;
	}

	private boolean isWithinRange(Expression value, int invocationOffset) {
		int startPosition = value.getStartPosition();
		int length = value.getLength();
		return startPosition < invocationOffset && startPosition + length >= invocationOffset;
	}

	private void setupASTNodes() {
		ICompilationUnit cu = javaContext.getCompilationUnit();
		SourceViewer sourceViewer = (SourceViewer) javaContext.getViewer();
		int invocationOffset = javaContext.getInvocationOffset();
		AssistContext assistContext = new AssistContext(cu, sourceViewer, invocationOffset, 0);
		ASTNode node = ((SourceRefElement) iAnnotation).findNode(assistContext.getASTRoot());
		annotation = (Annotation) node;
		if (node instanceof NormalAnnotation) {
			NormalAnnotation normalAnnotation = (NormalAnnotation) node;
			@SuppressWarnings("unchecked")
			List<MemberValuePair> pairs = normalAnnotation.values();

			for (MemberValuePair pair : pairs) {
				Expression value = pair.getValue();
				if (value instanceof StringLiteral) {
					if (isWithinRange(value, invocationOffset)) {
						oldLiteral = value;
					}
				}
				else if (value instanceof ArrayInitializer) {
					ArrayInitializer arrayInit = (ArrayInitializer) value;
					@SuppressWarnings("unchecked")
					List<Expression> expressions = arrayInit.expressions();
					for (Expression expression : expressions) {
						if (expression instanceof StringLiteral) {
							if (isWithinRange(expression, invocationOffset)) {
								oldLiteral = expression;
							}
						}
					}
				}
			}
		}
		else if (node instanceof SingleMemberAnnotation) {
			SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) node;
			Expression value = singleMemberAnnotation.getValue();
			if (isWithinRange(value, invocationOffset)) {
				oldLiteral = value;
			}
		}
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		setupASTNodes();

		AST ast = annotation.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		if (oldLiteral == null) {
			return rewrite;
		}

		StringLiteral newValue = ast.newStringLiteral();
		newValue.setLiteralValue(packageName);
		ITrackedNodePosition newValuePosition = rewrite.track(newValue);

		rewrite.replace(oldLiteral, newValue, null);
		setTrackPosition(new StringLiteralTrackedPosition(newValuePosition, newValuePosition.getStartPosition() + 1
				+ packageName.length(), 0, true));

		return rewrite;
	}
}
