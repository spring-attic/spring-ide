package org.springframework.ide.eclipse.quickfix.jdt.computers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.SourceViewer;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.PackageNameCompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;

/**
 * 
 * Annotation proposal computer for package name
 * 
 * @author Terry Denney
 * 
 */
public class PackageNameProposalComputer extends AnnotationProposalComputer {

	@Override
	protected List<ICompletionProposal> computeCompletionProposals(SourceType type, IAnnotation a,
			JavaContentAssistInvocationContext javaContext) throws JavaModelException {

		ITextViewer viewer = javaContext.getViewer();
		if (viewer instanceof SourceViewer) {
			List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

			SourceViewer sourceViewer = (SourceViewer) viewer;
			ISourceRange sourceRange = type.getSourceRange();
			AssistContext assistContext = new AssistContext(javaContext.getCompilationUnit(), sourceViewer,
					sourceRange.getOffset(), sourceRange.getLength());

			ASTNode node = assistContext.getCoveringNode();
			if (node == null) {
				node = assistContext.getCoveredNode();
			}

			int invocationOffset = javaContext.getInvocationOffset();

			Set<Annotation> annotations = ProposalCalculatorUtil.findAnnotations(a.getElementName(),
					javaContext.getInvocationOffset(), node);

			for (Annotation annotation : annotations) {
				LocationInformation info = getLocationSourceRange(annotation, sourceViewer, invocationOffset, "value");
				String content = info.getFilter();

				IPackageFragment[] packages = javaContext.getProject().getPackageFragments();
				Set<String> foundPackages = new HashSet<String>();
				for (IPackageFragment currPackage : packages) {
					String packageName = currPackage.getElementName();
					if (packageName.startsWith(content) && !foundPackages.contains(packageName)) {
						proposals.add(new PackageNameCompletionProposal(packageName, annotation, javaContext));
						foundPackages.add(packageName);
					}
				}
			}

			return proposals;
		}

		return Collections.emptyList();
	}

}
