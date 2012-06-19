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
package org.springframework.ide.eclipse.internal.bestpractices.quickfix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Element;


/**
 * Resolution for class not found markers. Opens a dialog so that the user can
 * create the class that wasn't found.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class CreateNewClassMarkerResolution implements IMarkerResolution2 {

	private static final String MESSAGE_ATTRIBUTE_KEY = "message";

	private String descriptionClassName = "";

	public CreateNewClassMarkerResolution(IMarker marker) {
		String markerMessage = marker.getAttribute(MESSAGE_ATTRIBUTE_KEY, "");
		descriptionClassName = extractQualifiedClassName(markerMessage);
	}

	private String extractClassNameFromMessage(String message) {
		String qualifiedClassName = extractQualifiedClassName(message);
		return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1, qualifiedClassName.length());
	}

	private String extractPackageNameFromMessage(String message) {
		if (message.length() == 0) {
			return "";
		}
		String qualifiedClassName = extractQualifiedClassName(message);
		int endPos = qualifiedClassName.lastIndexOf(".");
		if (endPos < 0) {
			return "";
		}
		return qualifiedClassName.substring(0, endPos);
	}

	private String extractQualifiedClassName(String message) {
		final String startTag = "class '";
		int startPos = message.indexOf(startTag) + startTag.length() + 1;
		int endPos = message.indexOf("'", startPos);
		return message.substring(startPos, endPos);
	}

	public String getDescription() {
		return "Create class " + descriptionClassName + " declared in the bean definition";
	}

	public Image getImage() {
		return null;
	}

	public String getLabel() {
		return "Create class " + descriptionClassName;
	}

	/**
	 * Create a package fragment for the given package name. This currently
	 * guesses the package root from the available package roots. It currently
	 * selects the first package root that isn't for a jar'ed package.
	 */
	private IPackageFragmentRoot inferPackageFragmentRoot(IJavaProject javaProject, String packageName)
			throws CoreException {
		try {
			IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
			for (int i = 0; i < packageFragmentRoots.length; i++) {
				if (!(packageFragmentRoots[i] instanceof JarPackageFragmentRoot)) {
					return packageFragmentRoots[i];
				}
			}
		}
		catch (JavaModelException e) {
			throw new CoreException(e.getStatus());
		}

		return null;
	}

	public void run(IMarker marker) {
		NewClassCreationWizard wizard = new NewClassCreationWizard();
		wizard.init(JavaPlugin.getDefault().getWorkbench(), null);
		Shell shell = JavaPlugin.getActiveWorkbenchShell();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.create();
		dialog.getShell().setText("New");
		IWizardPage[] pages = wizard.getPages();
		NewTypeWizardPage page = (NewTypeWizardPage) pages[0];
		String markerMessage = marker.getAttribute(MESSAGE_ATTRIBUTE_KEY, "");

		IJavaProject javaProject = JdtUtils.getJavaProject(marker.getResource());

		IPackageFragmentRoot sourcePackageFragmentRoot = null;
		String packageName = extractPackageNameFromMessage(markerMessage);
		try {
			sourcePackageFragmentRoot = inferPackageFragmentRoot(javaProject, packageName);
			if (sourcePackageFragmentRoot != null) {
				page.setPackageFragmentRoot(sourcePackageFragmentRoot, true);
				IPackageFragment packageFragment = sourcePackageFragmentRoot.getPackageFragment(packageName);
				if (packageFragment != null) {
					page.setPackageFragment(packageFragment, true);
				}
			}
		}
		catch (CoreException e) {
			StatusHandler.log(e.getStatus());
		}

		page.setTypeName(extractClassNameFromMessage(markerMessage), true);

		if (dialog.open() == Window.OK) {
			IType createdType = (IType) wizard.getCreatedElement();
			String fullyQualifiedClassName = createdType.getFullyQualifiedName();
			updateXmlBeanClass(marker, fullyQualifiedClassName);
		}

	}

	private void updateXmlBeanClass(IMarker marker, String fullyQualifiedClassName) {
		IStructuredModel model = null;
		try {
			model = XmlQuickFixUtil.getModel(marker);
			Element beanElement = XmlQuickFixUtil.getMarkerElement(model, marker);
			beanElement.setAttribute("class", fullyQualifiedClassName);
			XmlQuickFixUtil.saveMarkedFile(marker);
		}
		catch (CoreException e) {
			StatusHandler.log(e.getStatus());
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
				model = null;
			}
		}

	}

}
