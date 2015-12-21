/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
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

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.IAspectJElement;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Utility class that tries to locate {@link IType} instances from the AJDT type Hierarchy.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0
 */
public class AjdtUtils {

	private static final String AJDT_CLASS = "org.eclipse.contribution.jdt.IsWovenTester";

	private static final boolean IS_JDT_WEAVING_PRESENT = isJdtWeavingPresent();

	public static IType getAjdtType(IProject project, String className) {

		if (IS_JDT_WEAVING_PRESENT && JdtWeavingTester.isJdtWeavingActive()) {
			return null;
		}

		// this is only be used if JDT weaving is disabled (using somewhat old AJDT API) 
		if (project != null && className != null) {
			try {
				List<AJCompilationUnit> ajcus = AJCompilationUnitManager.INSTANCE.getCachedCUs(project);
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

	/**
	 * @since 2.6.0 
	 */
	public static IJavaElement getByHandle(String handle) {
		return AspectJCore.create(handle);
	}

	public static Set<IMethod> getDeclaredMethods(IType type) throws JavaModelException {
		Set<IMethod> methods = new HashSet<IMethod>();
		AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(type);
		if (model.hasModel()) {
			List<IJavaElement> elements = model.getRelationshipsForElement(type, AJRelationshipManager.ASPECT_DECLARATIONS);
			for (IJavaElement element : elements) {
				if (element instanceof IntertypeElement) {
					methods.add((IMethod) element);
				}
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
	
	public static boolean isTypeAjdtElement(IType type) {
		return type instanceof IAspectJElement;
	}
}
