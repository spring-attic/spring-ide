package org.springframework.ide.eclipse.beans.ui.wizards.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.core.ui.utils.PluginUtils;

public class SpringBeanWizardWorkbenchAware extends AbstractSpringBeanWizard
		implements IWorkbenchWizard {

	private IStructuredSelection selection;

	private IWorkbench workbench;

	/**
	 * @return Returns the workbench.
	 */
	protected IWorkbench getWorkbench() {
		return workbench;
	}

	/**
	 * @param workbench
	 *            The workbench to set.
	 */
	protected void setWorkbench(IWorkbench workbench) {
		this.workbench = workbench;
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}

	public WizardInitingDatas initWizardInitingDatas() {
		WizardInitingDatas initingDatas = new WizardInitingDatas();
		if (selection.getFirstElement() instanceof IResource) {
			IResource selectionFirstElement = (IResource) selection
					.getFirstElement();
			if (selectionFirstElement.isAccessible()) {
				initingDatas.setBeansProject(new BeansProject(
						selectionFirstElement.getProject()));
				if (selectionFirstElement instanceof IFile) {
					if (initingDatas.getBeansProject().getConfig(
							((IFile) selectionFirstElement).getName()) != null) {
						initingDatas.setBeansConfig(initingDatas
								.getBeansProject().getConfig(
										selectionFirstElement.getName()));
					} else {
						Object adaptedElement = PluginUtils.getAdapted(
								selectionFirstElement, IJavaElement.class);
						if (adaptedElement != null) {
							IJavaElement javaElement = (IJavaElement) adaptedElement;
							if (javaElement instanceof ICompilationUnit) {
								ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
								try {
									initingDatas
											.setType((IType) compilationUnit
													.getAllTypes()[0]);
								} catch (JavaModelException e) {
									// silence this exception
								}
							}
						}
					}
				}
			}
		}
		return initingDatas;
	}

}
