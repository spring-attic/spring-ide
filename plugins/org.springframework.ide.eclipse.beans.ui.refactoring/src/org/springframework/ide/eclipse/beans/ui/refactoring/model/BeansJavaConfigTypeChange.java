/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.model;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;

/**
 * change to rename java-based bean configs in case the referenced Java type is moved or renamed
 * 
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansJavaConfigTypeChange extends Change {

	private IType type;
	private String newName;

	public BeansJavaConfigTypeChange(IType type, String newName) {
		this.type = type;
		this.newName = newName;
	}

	@Override
	public String getName() {
		return "Rename references to '" + type.getElementName() + "' in Spring Project Configurations";
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		if (pm.isCanceled()) {
			return null;
		}

		for (IBeansProject project : BeansCorePlugin.getModel().getProjects()) {
			boolean updated = false;
			BeansProject beansProject = (BeansProject) project;

			// Firstly rename references to config sets
			for (IBeansConfigSet configSet : beansProject.getConfigSets()) {
				Set<IBeansConfig> configs = configSet.getConfigs();
				for (IBeansConfig config : configs) {
					if (config instanceof BeansJavaConfig) {
						IType configClass = ((BeansJavaConfig) config).getConfigClass();
						if (type.equals(configClass)) {
							((BeansConfigSet) configSet).removeConfig(config.getElementName());
							((BeansConfigSet) configSet).addConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + newName);
						}
						else if (configClass != null && type.equals(configClass.getDeclaringType())) {
							beansProject.removeConfig(config.getElementName());
							String newConfigClassName = newName + "$" + configClass.getElementName();
							beansProject.addConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + newConfigClassName, IBeansConfig.Type.MANUAL);
						}
					}
				}
			}

			// Secondly rename configs
			for (IBeansConfig config : beansProject.getConfigs()) {
				if (config instanceof BeansJavaConfig) {
					IType configClass = ((BeansJavaConfig) config).getConfigClass();
					if (type.equals(configClass)) {
						beansProject.removeConfig(config.getElementName());
						beansProject.addConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + newName, IBeansConfig.Type.MANUAL);
//						removeMarkers(config);
						updated = true;
					}
					else if (configClass != null && type.equals(configClass.getDeclaringType())) {
						beansProject.removeConfig(config.getElementName());
						String newConfigClassName = newName + "$" + configClass.getElementName();
						beansProject.addConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + newConfigClassName, IBeansConfig.Type.MANUAL);
//						removeMarkers(config);
						updated = true;
					}
				}
			}

			if (updated) {
				((BeansProject) project).saveDescription();
				BeansModelLabelDecorator.update();
			}
		}
		
		
		return null;
	}

	@Override
	public Object getModifiedElement() {
		return null;
	}

}
