package org.springframework.ide.eclipse.beans.ui.wizards.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ProjectNode;
import org.springframework.ide.eclipse.core.ui.utils.PluginUtils;

public class SpringBeanWizardModelAware extends AbstractSpringBeanWizard
		implements IBeanModelAwareWizard {

	private ProjectNode projectNode;

	private ConfigNode configNode;

	private IResource[] resources;

	public void init(ProjectNode projectNode, ConfigNode configNode,
			IResource[] resources) {
		this.projectNode = projectNode;
		this.configNode = configNode;
		this.resources = resources;
	}

	public WizardInitingDatas initWizardInitingDatas() {
		WizardInitingDatas initingDatas = new WizardInitingDatas();
		IResource selectionFirstElement = resources[0];
		if (selectionFirstElement != null) {
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
		} else {
			if (projectNode != null) {
				initingDatas.setBeansProject(projectNode.getProject());
			}
			if (configNode != null) {
				initingDatas.setBeansConfig(configNode.getConfig());
			}
		}
		return initingDatas;
	}

}
