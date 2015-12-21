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
package org.springframework.ide.eclipse.quickfix.processors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.ui.text.correction.NameMatcher;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.springframework.ide.eclipse.quickfix.Activator;
import org.springframework.ide.eclipse.quickfix.proposals.CreateNewClassQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.proposals.RenameToSimilarNameQuickFixProposal;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * Quick assist processor for bean class attribute in beans XML editor.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class ClassAttributeQuickAssistProcessor extends BeanQuickAssistProcessor {

	private class SimilarCUFindingVisitor implements IResourceVisitor {

		private final Set<String> suggestedClassNames = new HashSet<String>();

		private final String toMatch;

		private SimilarCUFindingVisitor(String toMatch) {
			this.toMatch = toMatch;
		}

		public List<String> getSuggestedClassNames() {
			List<String> result = new ArrayList<String>(suggestedClassNames);
			Collections.sort(result, new NameSuggestionComparator(toMatch));
			return result;
		}

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				String fileExtension = file.getFullPath().getFileExtension();
				if (fileExtension == null) {
					return false;
				}

				if (fileExtension.equals("java")) {
					ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);

					if (cu instanceof CompilationUnit) {
						CompilationUnit c = (CompilationUnit) cu;
						IType[] types = c.getAllTypes();
						for (IType type : types) {
							if (NameMatcher.isSimilarName(type.getElementName(), toMatch)) {
								suggestedClassNames.add(type.getFullyQualifiedName());
							}
						}
					}
					return false;
				}
				else if (fileExtension.equals("class")) {
					IClassFile classFile = JavaCore.createClassFileFrom(file);
					IType type = classFile.getType();
					if (type != null) {
						if (NameMatcher.isSimilarName(type.getElementName(), toMatch)) {
							suggestedClassNames.add(type.getFullyQualifiedName());
						}
					}
					return false;
				}
				return true;
			}
			return true;
		}

		public void visitJar(JarPackageFragmentRoot jarRoot) throws CoreException {
			IJavaElement[] children = jarRoot.getChildren();
			for (IJavaElement child : children) {
				if (child instanceof PackageFragment) {
					visitPackageFragment((PackageFragment) child);
				}
			}
		}

		public void visitPackageFragment(PackageFragment fragment) throws JavaModelException {
			IJavaElement[] grandChildren = fragment.getChildren();
			for (IJavaElement grandChild : grandChildren) {
				if (grandChild instanceof ClassFile) {
					ClassFile classFile = (ClassFile) grandChild;
					IType type = classFile.getType();
					if (NameMatcher.isSimilarName(type.getElementName(), toMatch)) {
						suggestedClassNames.add(type.getFullyQualifiedName());
					}
				}
			}

		}

	}

	private final Set<String> propertyNames;

	private final int numConstructorArgs;

	private final IJavaProject javaProject;

	public ClassAttributeQuickAssistProcessor(int offset, int length, String text, IProject project,
			boolean missingEndQuote, Set<String> propertyNames, int numConstructorArgs) {
		super(offset, length, text, missingEndQuote);

		this.propertyNames = propertyNames;
		this.numConstructorArgs = numConstructorArgs;

		this.javaProject = JavaCore.create(project).getJavaProject();
	}

	public ICompletionProposal[] computeQuickAssistProposals(IQuickAssistInvocationContext invocationContext) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		// find similar classes and add as proposals
		String className;
		int lastDotPos = text.lastIndexOf(".");
		if (lastDotPos < 0) {
			className = text;
		}
		else {
			className = text.substring(lastDotPos + 1);
		}

		try {
			SimilarCUFindingVisitor visitor = new SimilarCUFindingVisitor(className);
			IPackageFragmentRoot[] fragmentRoots = javaProject.getAllPackageFragmentRoots();
			for (IPackageFragmentRoot fragmentRoot : fragmentRoots) {
				if (fragmentRoot instanceof JarPackageFragmentRoot) {
					visitor.visitJar((JarPackageFragmentRoot) fragmentRoot);
				}
				IResource resource = fragmentRoot.getResource();
				if (resource != null) {
					resource.accept(visitor);
				}
			}
			List<String> suggestedClassNames = visitor.getSuggestedClassNames();
			for (String suggestedClassName : suggestedClassNames) {
				proposals.add(new RenameToSimilarNameQuickFixProposal(suggestedClassName, offset, length,
						missingEndQuote));
			}

			proposals.add(new CreateNewClassQuickFixProposal(offset, length, text, missingEndQuote, javaProject,
					propertyNames, numConstructorArgs));

			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		}
		catch (CoreException e1) {
			StatusHandler.log(new Status(Status.ERROR, Activator.PLUGIN_ID, "Cound not compute proposals."));
		}

		return new ICompletionProposal[0];
	}

}
