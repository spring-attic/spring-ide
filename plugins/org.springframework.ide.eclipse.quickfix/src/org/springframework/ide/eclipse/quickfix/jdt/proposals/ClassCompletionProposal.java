package org.springframework.ide.eclipse.quickfix.jdt.proposals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.springframework.ide.eclipse.quickfix.QuickfixImages;

/**
 * Proposal for completing a class attribute (i.e. ClassName.class)
 * @author Terry Denney
 * 
 */
public class ClassCompletionProposal extends AnnotationCompletionProposal {

	private final String className;

	private final Annotation annotation;

	private final ASTNode oldASTnode;

	private final IPackageFragment packageFragment;

	public ClassCompletionProposal(String className, Annotation annotation, ASTNode oldASTNode,
			IPackageFragment packageFragment, JavaContentAssistInvocationContext javaContext) {
		super(className + ".class", javaContext.getCompilationUnit(), QuickfixImages.getImage(QuickfixImages.CLASS));
		this.className = className;
		this.annotation = annotation;
		this.oldASTnode = oldASTNode;
		this.packageFragment = packageFragment;
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		AST ast = annotation.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		SimpleName typeName = ast.newSimpleName(className);
		SimpleType type = ast.newSimpleType(typeName);
		TypeLiteral typeLiteral = ast.newTypeLiteral();
		typeLiteral.setType(type);
		final ITrackedNodePosition newValuePosition = rewrite.track(typeLiteral);

		rewrite.replace(oldASTnode, typeLiteral, null);

		if (packageFragment != null) {
			ImportRewrite importRewrite = createImportRewrite(ASTResolving.findParentCompilationUnit(oldASTnode));
			importRewrite.addImport(packageFragment.getElementName() + "." + className);
		}

		setTrackPosition(new ITrackedNodePosition() {

			public int getStartPosition() {
				return newValuePosition.getStartPosition() + newValuePosition.getLength();
			}

			public int getLength() {
				return 0;
			}
		});

		return rewrite;
	}
}
