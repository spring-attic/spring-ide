/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.beans.ui.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IDecoration;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.data.SpringDataUtils;

/**
 * Decorates the actual repository interfaces with the Spring bean marker.
 * 
 * @author Oliver Gierke
 */
public class RepositoriesModelLabelDecorator extends BeansModelLabelDecorator {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator#decorateJavaElement(org.eclipse.jdt.core.IJavaElement, org.eclipse.jface.viewers.IDecoration)
	 */
	@Override
	protected void decorateJavaElement(IJavaElement element, IDecoration decoration) {
		
		IJavaProject javaProject = element.getJavaProject();
		if (javaProject != null) {
			int type = element.getElementType();
			IProject project = javaProject.getProject();
	
			try {
	
				if (type == IJavaElement.CLASS_FILE) {
	
					// Decorate Java class file
					IType javaType = ((IClassFile) element).getType();
	
					if (SpringDataUtils.hasRepositoryBeanFor(project, javaType)) {
						decoration.addOverlay(BeansUIImages.DESC_OVR_SPRING);
					}
	
				} else if (type == IJavaElement.COMPILATION_UNIT) {
	
					// Decorate Java source file
					for (IType javaType : ((ICompilationUnit) element).getTypes()) {
						if (SpringDataUtils.hasRepositoryBeanFor(project, javaType)) {
							decoration.addOverlay(BeansUIImages.DESC_OVR_SPRING);
							break;
						}
					}
				}
	
			} catch (JavaModelException e) {
				// ignore
			}
		}

		super.decorateJavaElement(element, decoration);
	}
}
