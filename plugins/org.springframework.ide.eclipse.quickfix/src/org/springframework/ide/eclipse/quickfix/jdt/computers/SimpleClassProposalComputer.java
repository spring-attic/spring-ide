package org.springframework.ide.eclipse.quickfix.jdt.computers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
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
	protected List<ICompletionProposal> computeCompletionProposals(SourceType type, String value,
			IAnnotation annotation, JavaContentAssistInvocationContext javaContext) throws JavaModelException {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		IPackageFragment[] packages = javaContext.getProject().getPackageFragments();
		Set<String> foundCUs = new HashSet<String>();
		for (IPackageFragment currPackage : packages) {
			ICompilationUnit[] cus = currPackage.getCompilationUnits();
			for (ICompilationUnit cu : cus) {
				String cuName = cu.getElementName();
				if (cuName.toLowerCase().startsWith(value.toLowerCase()) && !foundCUs.contains(cuName)) {
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
					proposals.add(new ClassCompletionProposal(cuName.replaceAll(".java", ""), annotation,
							importPackage, javaContext));
					foundCUs.add(cuName);
				}
			}
		}

		return proposals;
	}

}
