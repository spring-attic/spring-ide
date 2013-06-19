/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.model.generators.JavaConfigGenerator;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModel;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesProject;

/**
 * Action for removing the selected elements as beans configurations.
 * 
 * @author Leo Dos Santos
 */
public class RemoveAsBeanConfigAction extends AbstractHandler {

	private Set<IBeansConfig> selectedItems;
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		for (IBeansConfig config : selectedItems) {
			removeConfigFromProject(config);

		}
		BeansModelLabelDecorator.update();
		return null;
	}

	private IBeansConfig getConfigFromFile(IFile file) {
		return BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(file));
	}

	private IBeansConfig getConfigFromType(IType type) {
		IProject project = type.getJavaProject().getProject();
		IBeansProject beansProject = BeansCorePlugin.getModel().getProject(project);
		if (beansProject != null) {
			return  beansProject.getConfig(BeansConfigFactory.getConfigId(type, type.getJavaProject().getProject()));
		}
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return selectedItems.size() > 0;
	}
	
	public void removeConfigFromProject(IBeansConfig config) {
		if (config != null) {
			IBeansProject project = BeansModelUtils.getProject(config);
			if (project != null) {
				PropertiesProject modelProject = new PropertiesProject(new PropertiesModel(), project);
				modelProject.removeConfig(config.getId());
				modelProject.saveDescription();
			}
		}
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		selectedItems = new HashSet<IBeansConfig>();
		Object selection = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (selection instanceof StructuredSelection) {
			for (Object element : ((StructuredSelection) selection).toArray()) {
				if (element instanceof IType) {
					IBeansConfig config = getConfigFromType((IType) element);
					if (config != null) {
						selectedItems.add(config);
					}
				} else if (element instanceof IFile) {
					IBeansConfig config = getConfigFromFile((IFile) element);
					if (config != null) {
						selectedItems.add(config);
					}
				} else if (element instanceof IBeansConfig) {
					selectedItems.add((IBeansConfig) element);
				}
			}
		}		
	}
	
}
