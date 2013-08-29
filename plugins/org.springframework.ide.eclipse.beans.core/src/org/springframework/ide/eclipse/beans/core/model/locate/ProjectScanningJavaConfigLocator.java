/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.locate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springsource.ide.eclipse.commons.core.JdtUtils;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

/**
 * @author Leo Dos Santos
 */
public class ProjectScanningJavaConfigLocator implements IJavaConfigLocator {
	
	public Set<IFile> locateBeansConfigs(IProject project, IProgressMonitor progressMonitor) {
		return Collections.emptySet();
	}

	public Set<IType> locateJavaConfigs(IProject project, IProgressMonitor monitor) {
		Set<IType> types = new HashSet<IType>();
		IJavaProject javaProj = JdtUtils.getJavaProject(project);
		if (javaProj != null) {
			IJavaSearchScope sources = SearchEngine.createJavaSearchScope(new IJavaElement[] { javaProj },
					IJavaSearchScope.SOURCES);
			Set<IType> candidates = org.springframework.ide.eclipse.core.java.JdtUtils.searchForJavaConfigs(sources);
			for (IType candidate : candidates) {
				if (!candidate.getElementName().contains("Test")) {
					types.add(candidate);
				}
			}
		}
		return types;
	}

	public boolean isBeansConfig(IFile file) {
		return false;
	}

	public boolean supports(IProject project) {
		return SpringCoreUtils.isSpringProject(project);
	}

	public boolean requiresRefresh(IFile file) {
		return false;
	}

	public String getBeansConfigSetName(Set<IFile> files) {
		return null;
	}

	public void configureBeansConfigSet(IBeansConfigSet configSet) {
		// no-op
	}

}
