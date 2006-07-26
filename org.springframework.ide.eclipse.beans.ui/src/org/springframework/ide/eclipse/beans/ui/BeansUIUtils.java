/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.views.properties.FilePropertySource;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.ResourcePropertySource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.properties.ChildBeanProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.ConfigSetProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.ConstructorArgumentProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.PropertyProperties;
import org.springframework.ide.eclipse.beans.ui.model.properties.RootBeanProperties;
import org.springframework.ide.eclipse.beans.ui.properties.ConfigurationPropertyPage;
import org.springframework.ide.eclipse.beans.ui.views.BeansViewLocation;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public final class BeansUIUtils {

	/**
	 * Returns edited file from given editor if it's a Spring bean config file.
	 */
	public static IFile getConfigFile(IEditorPart editor) {
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile file = ((IFileEditorInput) input).getFile();
				IBeansProject project = BeansCorePlugin.getModel().getProject(
															 file.getProject());
				if (project != null && project.hasConfig(file)) {
					return file;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a corresponding instance of <code>IPropertySource</code> for the
	 * given model element ID or null.
	 * @param id  the model element ID
	 */
	public static IPropertySource getPropertySource(String id) {
		IModelElement element = BeansCorePlugin.getModel().getElement(id);
		return (element != null ? getPropertySource(element) : null);
	}

	/**
	 * Returns a corresponding instance of <code>IPropertySource</code> for the
	 * given <code>IModelElement</code> or null.
	 */
	public static IPropertySource getPropertySource(
													   IModelElement element) {
		if (element instanceof IBeansProject) {
			return new ResourcePropertySource(
										((IBeansProject) element).getProject());
		} else if (element instanceof IBeansConfig) {
			IFile file = ((IBeansConfig) element).getConfigFile();
			if (file != null && file.exists()) {
				return new FilePropertySource(file);
			}
		} else if (element instanceof IBeansConfigSet) {
			return new ConfigSetProperties(((IBeansConfigSet) element));
			
		} else if (element instanceof IBean) {
			IBean bean = ((IBean) element);
			if (bean.isRootBean()) {
				return new RootBeanProperties(bean);
			} else if (bean.isChildBean()){
				return new ChildBeanProperties(bean);
			} else {
				// FIXME add support for factory beans
//				return new FactoryBeanProperties(bean);
			}
		} else if (element instanceof IBeanConstructorArgument) {
			return new ConstructorArgumentProperties(
											(IBeanConstructorArgument) element);
		} else if (element instanceof IBeanProperty) {
			return new PropertyProperties((IBeanProperty) element);
		}
		return null;
	}

	/**
	 * Returns an instance of <code>BeansViewLocation</code> which is
	 * initialized with information from the model element identified by the
	 * given element ID.
	 */
	public static BeansViewLocation getBeansViewLocation(
															String elementID) {
		IBeansModel model = BeansCorePlugin.getModel();
		return getBeansViewLocation(model.getElement(elementID));
	}

	/**
	 * Returns an instance of <code>BeansViewLocation</code> which is
	 * initialized with information from the given core model element.
	 */
	public static final BeansViewLocation getBeansViewLocation(
													   IModelElement element) {
		BeansViewLocation location = new BeansViewLocation();
		if (element instanceof IBeansProject) {
			location.setProjectName(element.getElementName());
		} else if (element instanceof IBeansConfigSet) {
			location.setProjectName(element.getElementParent().getElementName());
			location.setConfigSetName(element.getElementName());
		} else if (element instanceof IBeansConfig) {
			location.setProjectName(element.getElementParent().getElementName());
			location.setConfigName(element.getElementName());
		} else if (element instanceof IBean) {
			location.setProjectName(
				element.getElementParent().getElementParent().getElementName());
			location.setConfigName(element.getElementParent().getElementName());
			location.setBeanName(element.getElementName());
		} else if (element instanceof IBeanProperty) {
			location.setProjectName(element.getElementParent().
						getElementParent().getElementParent().getElementName());
			location.setConfigName(element.getElementParent().
										   getElementParent().getElementName());
			location.setBeanName(element.getElementParent().getElementName());
			location.setPropertyName(element.getElementName());
		}
		return location;
	}

	public static void showProjectPropertyPage(IProject project, int block) {
		if (project != null) {
			String title = BeansUIPlugin
					.getResourceString("PropertiesPage.title")
					+ project.getName();
			IPreferencePage page = new ConfigurationPropertyPage(project, block);
			SpringUIUtils.showPreferencePage(ConfigurationPropertyPage.ID,
					page, title);
		}
	}
}
