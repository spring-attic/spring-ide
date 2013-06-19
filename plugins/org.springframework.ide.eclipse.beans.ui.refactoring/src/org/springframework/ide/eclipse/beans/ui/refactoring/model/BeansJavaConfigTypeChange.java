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
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
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
	private IBeansModel beansModel;

	public BeansJavaConfigTypeChange(IType type, String newName) {
		this.type = type;
		this.newName = newName;
		setBeansModel(BeansCorePlugin.getModel());
	}
	
	public void setBeansModel(IBeansModel beansModel) {
		this.beansModel = beansModel;
	}

	@Override
	public String getName() {
		return "Rename references to '" + type.getElementName() + "' in Spring project configurations";
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

		for (IBeansProject project : this.beansModel.getProjects()) {
			boolean updated = false;
			BeansProject beansProject = (BeansProject) project;

			// Firstly rename references to config sets
			for (IBeansConfigSet configSet : beansProject.getConfigSets()) {
				Set<IBeansConfig> configs = configSet.getConfigs();
				for (IBeansConfig config : configs) {
					if (config instanceof BeansJavaConfig) {
						IType configClass = ((BeansJavaConfig) config).getConfigClass();
						if (type.equals(configClass)) {
							((BeansConfigSet) configSet).removeConfig(config.getId());
							((BeansConfigSet) configSet).addConfig(config.getId().newName(newName));
						}
						else if (configClass != null && type.equals(configClass.getDeclaringType())) {
							beansProject.removeConfig(config.getId());
							String newConfigClassName = newName + "$" + configClass.getElementName();
							beansProject.addConfig(config.getId().newName(newConfigClassName), IBeansConfig.Type.MANUAL);
						}
					}
				}
			}

			// Secondly rename configs
			for (IBeansConfig config : beansProject.getConfigs()) {
				if (config instanceof BeansJavaConfig) {
					IType configClass = ((BeansJavaConfig) config).getConfigClass();
					if (configClass != null && getNewConfigName(newName, type, configClass) != null) {
						beansProject.removeConfig(config.getId());
						beansProject.addConfig(config.getId().newName(getNewConfigName(newName, type, configClass)), IBeansConfig.Type.MANUAL);
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
	
	/**
	 * calculate the name of the new config class
	 * 
	 * @param newName The new name of the class (without any package or outer class information)
	 * @param type The "old" type that is renamed
	 * @param configClass The type of the config class that might need an update
	 * @return null, if not matching the config class at all, otherwise the name of the new config class (including inner- and outer-class handling)
	 */
	protected String getNewConfigName(String newName, IType type, IType configClass) {
		if (type.equals(configClass)) {
			return newName;
		}
		
		IType walkingType = configClass.getDeclaringType();
		String walkingTypeName = configClass.getElementName();
		while (walkingType != null) {
			if (type.equals(walkingType)) {
				return newName + "$" + walkingTypeName;
			}
			walkingTypeName = walkingType.getElementName() + "$" + walkingTypeName;
			walkingType = walkingType.getDeclaringType();
		}
		
		return null;
	}

	@Override
	public Object getModifiedElement() {
		return null;
	}

}
