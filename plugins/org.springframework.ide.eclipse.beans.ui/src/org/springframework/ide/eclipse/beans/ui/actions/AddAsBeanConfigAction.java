/*******************************************************************************
 *  Copyright (c) 2012 - 2013 GoPivotal, Inc.
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
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;

/**
 * Action for adding the selected elements as beans configurations.
 * 
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @author Tomasz Zarna
 */
public class AddAsBeanConfigAction extends AbstractHandler {
	
	private Set<Object> selectedItems;
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		for (Object element : selectedItems) {
			if (element instanceof IType) {
				IType type = (IType) element;
				IProject project = type.getJavaProject().getProject();
				String typeName = BeansConfigFactory.JAVA_CONFIG_TYPE + type.getFullyQualifiedName();
				IBeansProject springProject = BeansCorePlugin.getModel().getProject(project);
				((BeansProject) springProject).addConfig(typeName, IBeansConfig.Type.MANUAL);
				((BeansProject) springProject).saveDescription();
			} else if (element instanceof IFile) {
				IFile file = (IFile) element;
				IProject project = file.getProject();
				IBeansProject springProject = BeansCorePlugin.getModel().getProject(project);
				((BeansProject) springProject).addConfig(file, IBeansConfig.Type.MANUAL);
				((BeansProject) springProject).saveDescription();
			}
		}
		BeansModelLabelDecorator.update();
		return null;
	}

	private boolean isBeansConfig(IFile file) {
		return BeansCorePlugin.getModel().getConfig(file) != null;
	}

	private boolean isBeansConfig(IType type) {
		IBeansConfig config = null;
		IProject project = type.getJavaProject().getProject();
		IBeansProject beansProject = BeansCorePlugin.getModel().getProject(project);
		if (beansProject != null) {
			config = beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + type.getFullyQualifiedName());
		}
		return config != null;
	}
	
	private boolean isBeansProject(IProject project) {
		return BeansCorePlugin.getModel().getProject(project) != null;
	}
	
	@Override
	public boolean isEnabled() {
		return selectedItems.size() > 0;
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		selectedItems = new HashSet<Object>();
		Object selection = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (selection instanceof StructuredSelection) {
			for (Object element : ((StructuredSelection) selection).toArray()) {
				if (element instanceof IType) {
					IType type = (IType) element;
					IProject project = type.getJavaProject().getProject();
					if (isBeansProject(project) && !isBeansConfig(type)) {
						IAnnotation configurationAnnotation = type.getAnnotation("Configuration");
						IAnnotation componentAnnotation = type.getAnnotation("Component");
						IAnnotation beanAnnotation = type.getAnnotation("Bean");
						IAnnotation importAnnotation = type.getAnnotation("Import");
						if ((configurationAnnotation != null && configurationAnnotation.exists()) 
							|| (componentAnnotation != null && componentAnnotation.exists())
							|| (beanAnnotation != null && beanAnnotation.exists())
							|| (importAnnotation != null && importAnnotation.exists())) {
							selectedItems.add(type);
						}
					}
				} else if (element instanceof IFile) {
					IFile file = (IFile) element;
					IProject project = file.getProject();
					if (isBeansProject(project) && !isBeansConfig(file) && BeansUIUtils.isBeansConfigContentType(file)) {
						selectedItems.add(file);
					}
				} 
			}
		}
	}
	
}
