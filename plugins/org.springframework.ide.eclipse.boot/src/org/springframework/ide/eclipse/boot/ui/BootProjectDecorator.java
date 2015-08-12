/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;

/**
 * Decorates a Spring Boot Project with a '[devtools]' text decoration if the
 * project has spring-boot-devtools as a dependency on its classpath.
 *
 * @author Kris De Volder
 */
public class BootProjectDecorator implements ILightweightLabelDecorator {

	public BootProjectDecorator() {
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		//System.out.println("Decorating "+element);
		IProject project = getProject(element);
//		decoration.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA));
		if (project!=null) {
			if (BootPropertyTester.isBootProject(project)) {
				decoration.addSuffix(" [boot]");
				if (hasDevtools(project)) {
					decoration.addSuffix(" [devtools]");
				}
			}
		}
	}

	private boolean hasDevtools(IProject p) {
		try {
			if (p!=null) {
				IJavaProject jp = JavaCore.create(p);
				IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
				if (classpath!=null) {
					for (IClasspathEntry e : classpath) {
						if (isDevtoolsJar(e)) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return false;
	}

	public static boolean isDevtoolsJar(IClasspathEntry e) {
		if (e.getEntryKind()==IClasspathEntry.CPE_LIBRARY) {
			IPath path = e.getPath();
			String name = path.lastSegment();
			return name.endsWith(".jar") && name.startsWith("spring-boot-devtools");
		}
		return false;
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
