/*
 * Copyright 2002-2004 the original author or authors.
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
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.BeansModelChangedEvent;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelChangedListener;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * This decorator adds an overlay image to all projects with Spring Beans
 * project nature, Spring beans config files and Java source / class files which
 * are used as Spring bean classes. This decoration is refreshed on every
 * modification to the Spring Beans model. Therefore the decorator adds a
 * change listener to the beans model.
 * 
 * @author Torsten Juergeleit
 * @see org.springframework.ide.eclipse.beans.core.model.IBeansModelChangedListener
 */
public class BeansUILabelDecorator extends LabelProvider
										 implements ILightweightLabelDecorator {
	public static final String DECORATOR_ID = BeansUIPlugin.PLUGIN_ID +
														 ".beansLabelDecorator";
	private IBeansModelChangedListener listener;

	public BeansUILabelDecorator() {
		listener = new IBeansModelChangedListener() {
			public void elementChanged(BeansModelChangedEvent event) {
				update();
			}
		};
		BeansCorePlugin.getModel().addChangeListener(listener);
	}

	public void decorate(Object element, IDecoration decoration) {
		IBeansModel model = BeansCorePlugin.getModel();
		if (element instanceof IProject) {
			if (model.hasProject((IProject) element)) {
				decoration.addOverlay(BeansUIImages.DESC_OVR_SPRING);
			}
		} else if (element instanceof IFile) {
			if (model.getConfig((IFile) element) != null) {
				decoration.addOverlay(BeansUIImages.DESC_OVR_SPRING);
			}
		} else if (element instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) element;
			int type = javaElement.getElementType();
			if (type == IJavaElement.COMPILATION_UNIT ||
											  type == IJavaElement.CLASS_FILE) {
				IBeansProject project = model.getProject(
									 javaElement.getJavaProject().getProject());
				if (project != null) {
					try {
						if (type == IJavaElement.COMPILATION_UNIT) {
							IType[] javaTypes = ((ICompilationUnit)
														javaElement).getTypes();
							for (int i = 0; i < javaTypes.length; i++) {
								IType javaType = javaTypes[i];
								if (project.isBeanClass(
											javaType.getFullyQualifiedName())) {
									decoration.addOverlay(
												 BeansUIImages.DESC_OVR_SPRING);
									break;
								}
							}
						} else {
							IType javaType = ((IClassFile)
														 javaElement).getType();
							if (project.isBeanClass(
											javaType.getFullyQualifiedName())) {
								decoration.addOverlay(
												 BeansUIImages.DESC_OVR_SPRING);
							}
						}
					} catch (JavaModelException e) {
						// Ignore
					}
				}
			}
		}
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void dispose() {
		BeansCorePlugin.getModel().removeChangeListener(listener);
	}

	public static final void update() {
		BeansUIUtils.getStandardDisplay().asyncExec(new Runnable() {		
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				workbench.getDecoratorManager().update(DECORATOR_ID);
			}
		});
	}
}
