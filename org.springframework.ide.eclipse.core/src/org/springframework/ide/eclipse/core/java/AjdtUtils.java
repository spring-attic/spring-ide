/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import java.util.Collections;
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
 * Utility class that tries to locate {@link IType} instances from the AJDT type Hierarchy.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("deprecation")
public class AjdtUtils {

	private static final String AJDT_CLASS = "org.eclipse.contribution.jdt.IsWovenTester";

	private static final boolean IS_JDT_WEAVING_PRESENT = isJdtWeavingPresent();

	@SuppressWarnings("unchecked")
	public static IType getAjdtType(IProject project, String className) {

		if (IS_JDT_WEAVING_PRESENT && JdtWeavingTester.isJdtWeavingActive()) {
			return null;
		}

		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (javaProject != null && className != null) {
			try {
				List<AJCompilationUnit> ajcus = AJCompilationUnitManager.INSTANCE.getAJCompilationUnits(javaProject);
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

	@SuppressWarnings( { "unchecked" })
	public static Set<IMethod> getDeclaredMethods(IType type) throws JavaModelException {

		if (IS_JDT_WEAVING_PRESENT && JdtWeavingTester.isJdtWeavingActive()) {
			return Collections.emptySet();
		}

		Set<IMethod> methods = new HashSet<IMethod>();
		AJRelationshipType[] types = new AJRelationshipType[] { AJRelationshipManager.DECLARED_ON };
		List<AJRelationship> rels = AJModel.getInstance().getAllRelationships(type.getResource().getProject(), types);
		for (AJRelationship rel : rels) {
			if (rel.getTarget().equals(type)) {
				IntertypeElement iType = (IntertypeElement) rel.getSource();
				methods.add(iType);
			}
		}
		return methods;
	}

	public static boolean isJdtWeavingPresent() {
		try {
			Class.forName(AJDT_CLASS);
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}
}
