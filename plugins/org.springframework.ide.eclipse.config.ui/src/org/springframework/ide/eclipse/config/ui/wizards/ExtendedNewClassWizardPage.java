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
package org.springframework.ide.eclipse.config.ui.wizards;

import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


// Copied from PDE's JavaAttributeWizardPage
public class ExtendedNewClassWizardPage extends NewClassWizardPage {

	class InitialClassProperties {
		// populate new wizard page
		IType superClassType;

		String superClassName;

		IType interfaceType;

		String interfaceName;

		String className;

		String classArgs;

		String packageName;

		IPackageFragmentRoot packageFragmentRoot;

		IPackageFragment packageFragment;

		public InitialClassProperties() {
			this.superClassType = null;
			this.superClassName = ""; //$NON-NLS-1$
			this.interfaceName = null;
			this.interfaceType = null;
			this.className = null;
			this.classArgs = null;
			this.packageName = null;
			this.packageFragment = null;
			this.packageFragmentRoot = null;
		}
	}

	private final IProject project;

	private IJavaProject javaProject;

	private String className;

	private final InitialClassProperties initialValues;

	public ExtendedNewClassWizardPage(IProject project, String className) {
		super();
		this.className = className;
		this.project = project;
		try {
			if (project.hasNature(JavaCore.NATURE_ID)) {
				this.javaProject = JavaCore.create(project);
			}
			else {
				this.javaProject = null;
			}
		}
		catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID,
					Messages.getString("ExtendedNewClassWizardPage.ERROR_CREATING_WIZARD_PAGE"), e)); //$NON-NLS-1$
		}
		initialValues = new InitialClassProperties();
		initialValues.className = className;
	}

	private IType findTypeForName(String typeName) throws JavaModelException {
		if (typeName == null || typeName.length() == 0) {
			return null;
		}
		IType type = null;
		String fileName = typeName.replace('.', '/') + ".java"; //$NON-NLS-1$
		IJavaElement element = javaProject.findElement(new Path(fileName));
		if (element == null) {
			return null;
		}
		if (element instanceof IClassFile) {
			type = ((IClassFile) element).getType();
		}
		else if (element instanceof ICompilationUnit) {
			IType[] types = ((ICompilationUnit) element).getTypes();
			type = types[0];
		}
		return type;
	}

	public void init() {
		initializeExpectedValues();
		initializeWizardPage();
	}

	private void initializeExpectedValues() {
		// source folder name, package name, class name
		int loc = className.indexOf(":"); //$NON-NLS-1$
		if (loc != -1) {
			if (loc < className.length()) {
				initialValues.classArgs = className.substring(loc + 1, className.length());
				className = className.substring(0, loc);
			}
			if (loc > 0) {
				initialValues.className = className.substring(0, loc);
			}
			else if (loc == 0) {
				initialValues.className = ""; //$NON-NLS-1$
			}
		}

		loc = className.lastIndexOf('.');
		if (loc != -1) {
			initialValues.packageName = className.substring(0, loc);
			initialValues.className = className.substring(loc + 1);
		}
		if (javaProject == null) {
			return;
		}

		try {
			if (initialValues.packageFragmentRoot == null) {
				IPackageFragmentRoot srcEntryDft = null;
				IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
				for (IPackageFragmentRoot root : roots) {
					if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
						srcEntryDft = root;
						break;
					}
				}
				if (srcEntryDft != null) {
					initialValues.packageFragmentRoot = srcEntryDft;
				}
				else {
					initialValues.packageFragmentRoot = javaProject.getPackageFragmentRoot(javaProject.getResource());
				}
				if (initialValues.packageFragment == null && initialValues.packageFragmentRoot != null
						&& initialValues.packageName != null && initialValues.packageName.length() > 0) {
					IFolder packageFolder = project.getFolder(initialValues.packageName);
					initialValues.packageFragment = initialValues.packageFragmentRoot.getPackageFragment(packageFolder
							.getProjectRelativePath().toOSString());
				}
			}
			initialValues.superClassName = "java.lang.Object"; //$NON-NLS-1$
			initialValues.superClassType = findTypeForName(initialValues.superClassName);
		}
		catch (JavaModelException e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID,
					Messages.getString("ExtendedNewClassWizardPage.ERROR_INITIALIZING_WIZARD_PAGE"), e)); //$NON-NLS-1$
		}
	}

	private void initializeWizardPage() {
		setPackageFragmentRoot(initialValues.packageFragmentRoot, true);
		setPackageFragment(initialValues.packageFragment, true);
		setEnclosingType(null, true);
		setEnclosingTypeSelection(false, true);
		setTypeName(initialValues.className, true);
		setSuperClass(initialValues.superClassName, true);
		if (initialValues.interfaceName != null) {
			ArrayList<String> interfaces = new ArrayList<String>();
			interfaces.add(initialValues.interfaceName);
			setSuperInterfaces(interfaces, true);
		}
		boolean hasSuperClass = initialValues.superClassName != null && initialValues.superClassName.length() > 0;
		boolean hasInterface = initialValues.interfaceName != null && initialValues.interfaceName.length() > 0;
		setMethodStubSelection(false, hasSuperClass, hasInterface || hasSuperClass, true);
	}

}
