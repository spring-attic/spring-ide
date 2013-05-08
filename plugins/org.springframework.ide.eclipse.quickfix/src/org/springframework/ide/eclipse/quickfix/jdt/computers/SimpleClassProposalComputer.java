package org.springframework.ide.eclipse.quickfix.jdt.computers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.springframework.ide.eclipse.quickfix.jdt.proposals.ClassCompletionProposal;

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
					proposals.add(new ClassCompletionProposal(cuName, annotation, locationInfo.getStringLiteral(),
							javaContext));
					foundCUs.add(cuName);
				}
			}
		}

		return proposals;
	}

}
