/*******************************************************************************
 * Copyright (c) 2017 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     DISID Corporation, S.L - Spring Roo maintainer
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal.listener;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;

/**
 * Decorates a Spring Roo Project with a '[roo]' text decoration if the
 * project has spring-roo nature
 * 
 * Copied from BootProjectDecorator.java class
 * https://github.com/spring-projects/spring-ide/blob/master/plugins/org.springframework.ide.eclipse.boot/src/org/springframework/ide/eclipse/boot/ui/BootProjectDecorator.java
 *
 * @author Juan Carlos García
 */
public class RooProjectDecorator implements ILightweightLabelDecorator {

	public RooProjectDecorator() {
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		IProject project = getProject(element);
		if (project != null) {
			try {
				if(project.getNature(RooCoreActivator.NATURE_ID) != null){
					decoration.addSuffix(" [roo] ");
				}
			}
			catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private IProject getProject(Object element) {
		if (element instanceof IProject) {
			return (IProject) element;
		} else if (element instanceof IJavaProject) {
			return ((IJavaProject) element).getProject();
		}
		return null;
	}
	
}
