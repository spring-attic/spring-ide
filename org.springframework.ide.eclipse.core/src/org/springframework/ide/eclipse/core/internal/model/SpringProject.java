/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.internal.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringModel;
import org.springframework.ide.eclipse.core.model.ISpringModelElementTypes;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.util.ObjectUtils;

/**
 * This class holds information for a Spring project.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class SpringProject extends AbstractResourceModelElement implements
		ISpringProject {

	private IProject project;

	public SpringProject(ISpringModel model, IProject project) {
		super(model, project.getName());
		this.project = project;
	}

	public int getElementType() {
		return ISpringModelElementTypes.PROJECT_TYPE;
	}

	public IResource getElementResource() {
		return project;
	}

	public boolean isElementArchived() {
		return false;
	}

	public IProject getProject() {
		return project;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SpringProject)) {
			return false;
		}
		SpringProject that = (SpringProject) other;
		if (!ObjectUtils.nullSafeEquals(this.project, that.project))
			return false;
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(project);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		return "Project=" + getElementName();
	}

	public boolean isExternal() {
		return false;
	}
}
