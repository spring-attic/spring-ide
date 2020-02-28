/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.internal.BufferedResourceNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;

public class LocalFile {

	private final BufferedResourceNode wrappedResource;
	private String label;
	private boolean editable;
	private final IProject project;

	public LocalFile(IProject project, String resourceName, boolean editable) {
		IFile localFile = project.getProject().getFile(resourceName);
		this.project = project;
		this.wrappedResource = new BufferedResourceNode(localFile);
		this.label = "Local file in " + project.getName() + ": " + wrappedResource.getName();
		this.editable = editable;
	}

	public void commit(IProgressMonitor monitor) throws CoreException {
		wrappedResource.commit(monitor);
	}

	public String getLabel() {
		return label;
	}

	public IProject getProject() {
		return project;
	}

	public Image getImage() {
		return wrappedResource.getImage();
	}

	public boolean isEditable() {
		return editable;
	}

	public ResourceNode getWrappedResource() {
		return wrappedResource;
	}

}
