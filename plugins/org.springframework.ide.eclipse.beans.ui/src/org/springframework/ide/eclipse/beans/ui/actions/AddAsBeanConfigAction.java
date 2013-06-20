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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.beans.core.model.generators.JavaConfigGenerator;
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
				IBeansProject springProject = BeansCorePlugin.getModel().getProject(project);
				((BeansProject) springProject).addConfig(BeansConfigId.create(type, project), IBeansConfig.Type.MANUAL);
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
		return BeansCorePlugin.getModel().getConfig(BeansConfigId.create(file)) != null;
	}

	private boolean isBeansConfig(IType type) {
		IBeansConfig config = null;
		IProject project = type.getJavaProject().getProject();
		IBeansProject beansProject = BeansCorePlugin.getModel().getProject(project);
		if (beansProject != null) {
			config = beansProject.getConfig(BeansConfigId.create(type, project));
		}
		return config != null;
	}
	
	private boolean isBeansConfigContentType(IFile file) {
		IContentTypeManager contentTypeManager = Platform.getContentTypeManager();			
		try {
			InputStream contents = file.getContents();
			IContentType contentType = contentTypeManager.findContentTypeFor(contents, file.getName());
			if (contentType != null && contentType.isKindOf(contentTypeManager.getContentType("com.springsource.sts.config.ui.beanConfigFile"))) {
				return true;
			}
		} catch (CoreException e) {
			// if something goes wrong, treats the file as non spring content type
		} catch (IOException e) {
			// if something goes wrong, treats the file as non spring content type
		}
		return false;
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
						IAnnotation annotation = type.getAnnotation("Configuration");
						if (annotation != null && annotation.exists()) {
							selectedItems.add(type);
						}
					}
				} else if (element instanceof IFile) {
					IFile file = (IFile) element;
					IProject project = file.getProject();
					if (isBeansProject(project) && !isBeansConfig(file) && isBeansConfigContentType(file)) {
						selectedItems.add(file);
					}
				} 
			}
		}
	}
	
}
