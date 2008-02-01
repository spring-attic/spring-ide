/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Utility class that tries to locate {@link IType} instances from the AJDT type
 * hierachy.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AjdtUtils {

	@SuppressWarnings("unchecked")
	public static IType getAjdtType(IProject project, String className) {
		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (javaProject != null && className != null) {
			try {
				List<AJCompilationUnit> ajcus = AJCompilationUnitManager.INSTANCE
						.getAJCompilationUnits(javaProject);
				if (ajcus != null) {
					for (AJCompilationUnit ajcu : ajcus) {
						IType[] types = ajcu.getAllTypes();
						for (IType type : types) {
							if (className.equals(type.getFullyQualifiedName())) {
								return type;
							}
						}
					}
				}
			}
			catch (CoreException e) {
				SpringCore.log(e);
			}
		}
		return null;
	}

	public static boolean isTypeAjdtElement(IType type) {
		return type instanceof IAspectJElement;
	}

	public static Set<IMethod> getDeclaredMethods(IType type) throws JavaModelException {
		Set<IMethod> methods = new HashSet<IMethod>();
		AJRelationshipType[] types = new AJRelationshipType[] { AJRelationshipManager.DECLARED_ON };
		List<AJRelationship> rels = AJModel.getInstance().getAllRelationships(
				type.getResource().getProject(), types);
		for (AJRelationship rel : rels) {
			if (rel.getTarget().equals(type)) {
				IntertypeElement iType = (IntertypeElement) rel.getSource();
				methods.add(iType);
			}
		}
		return methods;
	}
}
