package org.springframework.ide.eclipse.quickfix.jdt.computers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.core.SourceRefElement;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.SourceViewer;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.ClassCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;

/**
 * 
 * Annotation proposal computer for classes in simple class name form (i.e.
 * ClassName.class)
 * 
 * @author Terry Denney
 * 
 */
public class SimpleClassProposalComputer extends AnnotationProposalComputer {

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		if (!(context instanceof JavaContentAssistInvocationContext)) {
			return Collections.emptyList();
		}

		JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
		ITextViewer viewer = context.getViewer();

		if (!(viewer instanceof SourceViewer)) {
			return Collections.emptyList();
		}

		SourceViewer sourceViewer = (SourceViewer) viewer;
		ICompilationUnit cu = javaContext.getCompilationUnit();

		try {
			int invocationOffset = context.getInvocationOffset();
			IJavaElement element = cu.getElementAt(invocationOffset);
			if (!(element instanceof SourceRefElement)) {
				return Collections.emptyList();
			}

			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

			ISourceRange sourceRange = ((SourceRefElement) element).getSourceRange();
			AssistContext assistContext = new AssistContext(javaContext.getCompilationUnit(), sourceViewer,
					sourceRange.getOffset(), sourceRange.getLength());
			ASTNode node = assistContext.getCoveringNode();

			if (element instanceof SourceType) {
				Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations("ComponentScan", invocationOffset,
						node);
				for (Annotation annotation : annotations) {
					if (annotation instanceof NormalAnnotation) {
						NormalAnnotation nAnnotation = (NormalAnnotation) annotation;
						@SuppressWarnings("unchecked")
						List<MemberValuePair> memberValuePairs = nAnnotation.values();
						for (MemberValuePair memberValuePair : memberValuePairs) {
							if ("basePackageClasses".equals(memberValuePair.getName().getIdentifier())) {
								Expression value = memberValuePair.getValue();
								if (value instanceof ArrayInitializer) {
									ArrayInitializer arrayInit = (ArrayInitializer) value;
									@SuppressWarnings("unchecked")
									List<Expression> expressions = arrayInit.expressions();
									for (Expression expression : expressions) {
										int expStartPos = expression.getStartPosition();
										if (expStartPos <= invocationOffset
												&& expStartPos + expression.getLength() >= invocationOffset) {
											if (expression instanceof SimpleName) {
												SimpleName simpleName = (SimpleName) expression;
												String filter = simpleName.toString().substring(0,
														invocationOffset - expStartPos);
												proposals.addAll(computeCompletionProposals((SourceType) element,
														new LocationInformation(expStartPos, expression.getLength(),
																filter, simpleName), annotation, javaContext));
											}
										}
									}
								}
							}

						}
					}
				}
			}

			return proposals;
		}
		catch (JavaModelException e) {

		}

		return Collections.emptyList();
	}

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(SourceType type, LocationInformation locationInfo,
			Annotation annotation, JavaContentAssistInvocationContext javaContext) throws JavaModelException {

		String content = locationInfo.getFilter();

		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		IPackageFragment[] packages = javaContext.getProject().getPackageFragments();
		Set<String> foundCUs = new HashSet<String>();
		for (IPackageFragment currPackage : packages) {
			ICompilationUnit[] cus = currPackage.getCompilationUnits();
			for (ICompilationUnit cu : cus) {
				String cuName = cu.getElementName();
				if (cuName.startsWith(content) && !foundCUs.contains(cuName)) {
					boolean needsImport = true;
					IPackageDeclaration[] packageDeclarations = javaContext.getCompilationUnit()
							.getPackageDeclarations();
					for (IPackageDeclaration packageDeclaration : packageDeclarations) {
						String packageName = packageDeclaration.getElementName();
						if (packageName.equals(currPackage.getElementName())) {
							needsImport = false;
						}
					}

					IImportDeclaration[] imports = cu.getImports();
					for (IImportDeclaration importDecl : imports) {
						if (importDecl.getElementName().equals(currPackage.getElementName())) {
							needsImport = false;
						}
					}

					IPackageFragment importPackage = null;
					if (needsImport) {
						importPackage = currPackage;
					}
					proposals.add(new ClassCompletionProposal(cuName.replaceAll(".java", ""), annotation, locationInfo
							.getASTNode(), importPackage, javaContext));
					foundCUs.add(cuName);
				}
			}
		}

		return proposals;
	}

}
