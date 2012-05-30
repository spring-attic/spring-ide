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
package org.springframework.ide.eclipse.quickfix.proposals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.quickfix.QuickfixUtils;


/**
 * Quick fix proposal for creating a new class
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0
 */
public class CreateNewClassQuickFixProposal extends BeanAttributeQuickFixProposal {

	private String className, packageName;

	private IPackageFragmentRoot sourceRoot;

	private final Set<String> properties;

	private final int numConstructorArgs;

	private final IJavaProject javaProject;

	private final boolean allowUserChanges;

	private IType enclosingType;

	public CreateNewClassQuickFixProposal(int offset, int length, String text, boolean missingEndQuote,
			IJavaProject javaProject, Set<String> properties, int numConstructorArgs) {
		this(offset, length, text, missingEndQuote, javaProject, properties, numConstructorArgs, true);
	}

	public CreateNewClassQuickFixProposal(int offset, int length, String text, boolean missingEndQuote,
			IJavaProject javaProject, Set<String> properties, int numConstructorArgs, boolean allowUserChanges) {
		super(offset, length, missingEndQuote);
		this.properties = properties;
		this.numConstructorArgs = numConstructorArgs;
		this.javaProject = javaProject;
		this.allowUserChanges = allowUserChanges;

		int classNameOffset = text.lastIndexOf("$");
		int packageEnd;

		if (classNameOffset >= 0) {
			String enclosingClassName = text.substring(0, classNameOffset);
			enclosingType = JdtUtils.getJavaType(javaProject.getProject(), enclosingClassName);

			packageEnd = enclosingClassName.lastIndexOf(".");
		}
		else {
			classNameOffset = text.lastIndexOf(".");
			packageEnd = classNameOffset;
		}

		if (classNameOffset < 0) {
			className = text;
		}
		else {
			className = text.substring(classNameOffset + 1);
		}

		if (packageEnd >= 0) {
			packageName = text.substring(0, packageEnd);
		}
		else {
			packageName = "";
		}

		String packageFragmentName = null;
		if (enclosingType != null) {
			packageFragmentName = enclosingType.getPackageFragment().getElementName();
		}

		IPackageFragmentRoot[] allPackageFragmentRoots;
		try {
			allPackageFragmentRoots = javaProject.getAllPackageFragmentRoots();
			if (allPackageFragmentRoots != null && allPackageFragmentRoots.length > 0) {
				for (IPackageFragmentRoot packageFragmentRoot : allPackageFragmentRoots) {
					if (!(packageFragmentRoot instanceof JarPackageFragmentRoot)) {
						if (packageFragmentName != null) {
							if (packageFragmentRoot.getPackageFragment(packageFragmentName) == null) {
								continue;
							}
						}
						sourceRoot = packageFragmentRoot;
						break;
					}
				}
			}
		}
		catch (JavaModelException e) {
		}

	}

	private IJavaElement applyQuickFix() {
		IPackageFragment packageFragment = null;
		if (packageName != null && packageName.length() > 0) {
			packageFragment = findPackageFragment(packageName);
		}

		NewClassWizardPage page = new NewClassWizardPage();
		page.setTypeName(className, false);

		if (packageFragment != null) {
			page.setPackageFragment(packageFragment, true);
		}
		else if (sourceRoot != null) {
			page.setPackageFragment(sourceRoot.getPackageFragment(packageName), true);
		}

		if (sourceRoot != null) {
			page.setPackageFragmentRoot(sourceRoot, true);
		}

		if (enclosingType != null) {
			page.setEnclosingType(enclosingType, false);
			page.setEnclosingTypeSelection(true, false);
		}

		NewClassCreationWizard wizard = new NewClassCreationWizard(page, true);
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, null);

		Shell shell = workbench.getActiveWorkbenchWindow().getShell();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.getShell().setText("New Class");

		if (allowUserChanges) {
			dialog.setBlockOnOpen(true);
			if (dialog.open() != Window.OK) {
				return null;
			}
		}
		else {
			wizard.performFinish();
		}

		return wizard.getCreatedElement();
	}

	@Override
	public void applyQuickFix(IDocument document) {
		if (packageName == null) {
			String text;
			try {
				text = document.get(getOffset(), getLength());

				int lastDotPos = text.lastIndexOf(".");
				if (lastDotPos < 0) {
					packageName = "";
				}
				else {
					packageName = text.substring(0, lastDotPos);
				}
			}
			catch (BadLocationException e) {
			}
		}

		IJavaElement createdElement = applyQuickFix();
		if (createdElement instanceof IType) {
			IType targetType = (IType) createdElement;
			createProperties(document, targetType);
			ArrayList<String> constructorArgClassNames = new ArrayList<String>();
			for (int i = 0; i < numConstructorArgs; i++) {
				constructorArgClassNames.add("Object");
			}
			QuickfixUtils.createConstructor(document, targetType, constructorArgClassNames, javaProject);
		}
	}

	private void createProperties(IDocument document, IType targetType) {
		ICompilationUnit cu = targetType.getCompilationUnit();

		if (cu == null) {
			return;
		}

		for (String property : properties) {
			createProperty(property, cu, document, targetType);
		}
	}

	private void createProperty(String property, ICompilationUnit cu, IDocument document, IType targetType) {
		MethodInvocation expr = QuickfixUtils.getMockMethodInvocation(property, new String[0], "void", false);
		SimpleName simpleName = expr.getName();
		ITypeBinding typeBinding = QuickfixUtils.getTargetTypeBinding(javaProject, targetType);
		Object fieldProposal = QuickfixReflectionUtils.createNewFieldProposal(property, cu, simpleName, typeBinding, 0,
				null);
		QuickfixReflectionUtils.applyProposal(fieldProposal, document);
		String methodName = "set" + StringUtils.capitalize(property);
		MethodInvocation invocationNode = QuickfixUtils.getMockMethodInvocation(methodName, new String[] { "Object" },
				"void", false);
		List<Expression> arguments = QuickfixUtils.getArguments(invocationNode);
		Object setterProposal = QuickfixReflectionUtils.createNewMethodProposal(methodName, cu, invocationNode,
				arguments, typeBinding, 0, null);
		QuickfixReflectionUtils.applyProposal(setterProposal, document);
	}

	private IPackageFragment findPackageFragment(String packageName) {
		final List<IPackageFragment> results = new ArrayList<IPackageFragment>();

		SearchRequestor collector = new SearchRequestor() {

			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				Object element = match.getElement();
				if (element instanceof IPackageFragment) {
					IPackageFragment packageFragment = (IPackageFragment) element;
					if (!packageFragment.isReadOnly()) {
						results.add(packageFragment);
					}
				}
			}
		};

		SearchEngine engine = new SearchEngine();
		SearchPattern pattern = SearchPattern.createPattern(packageName, IJavaSearchConstants.PACKAGE,
				IJavaSearchConstants.ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH);
		try {
			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
					SearchEngine.createWorkspaceScope(), collector, new NullProgressMonitor());
		}
		catch (CoreException e) {
		}

		if (results.size() > 0) {
			return results.get(0);
		}
		return null;
	}

	public String getDisplayString() {
		return "Create class \'" + className + "\'";
	}

	public Image getImage() {
		return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_CLASS);
	}

	@Override
	public void run(IMarker marker) {
		applyQuickFix();
	}

}
