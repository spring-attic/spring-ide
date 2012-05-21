/*
 * Copyright 2011 by the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.data.beans.ui.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
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

		int type = element.getElementType();
		IProject project = element.getJavaProject().getProject();

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

		super.decorateJavaElement(element, decoration);
	}
}
