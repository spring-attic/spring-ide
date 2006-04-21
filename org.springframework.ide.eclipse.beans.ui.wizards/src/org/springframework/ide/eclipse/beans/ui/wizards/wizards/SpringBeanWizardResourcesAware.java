package org.springframework.ide.eclipse.beans.ui.wizards.wizards;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

public class SpringBeanWizardResourcesAware extends AbstractSpringBeanWizard
		implements IResourcesAwareWizard {

	private IBeansProject project;

	private IBeansConfig config;

	private IType type;

	public void init(IBeansProject project, IBeansConfig config, IType type) {
		this.project = project;
		this.config = config;
		this.type = type;
	}

	public WizardInitingDatas initWizardInitingDatas() {
		WizardInitingDatas initingDatas=new WizardInitingDatas();
		initingDatas.setBeansProject(this.project);
		initingDatas.setBeansConfig(this.config);
		initingDatas.setType(this.type);
		if (initingDatas.getBeansProject() == null) {
			if (initingDatas.getBeansConfig() != null) {
				initingDatas.setBeansProject(new BeansProject(
						initingDatas.getBeansConfig().getConfigFile()
								.getProject()));
			}
		}
		return initingDatas;
	}

}
