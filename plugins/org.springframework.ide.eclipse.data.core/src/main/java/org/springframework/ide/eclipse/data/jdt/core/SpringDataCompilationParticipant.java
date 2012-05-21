/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.data.jdt.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * 
 * @author Oliver Gierke
 */
public class SpringDataCompilationParticipant extends CompilationParticipant {

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.CompilationParticipant#isActive(org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public boolean isActive(IJavaProject project) {
		return SpringCoreUtils.isSpringProject(project.getResource());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.CompilationParticipant#reconcile(org.eclipse.jdt.core.compiler.ReconcileContext)
	 */
	@Override
	public void reconcile(ReconcileContext context) {

		try {

			CompilationUnit compilationUnit = context.getAST3();
			IType type = compilationUnit.getTypeRoot().findPrimaryType();

			// Skip non-interfaces
			if (type == null || !type.isInterface()) {
				super.reconcile(context);
				return;
			}

			RepositoryInformation information = new RepositoryInformation(type);

			if (information.isSpringDataRepository()) {
				System.out.println("Found Spring Data Repository " + type.getFullyQualifiedName());
			} else {
				return;
			}

			Class<?> domainClass = information.getManagedDomainClass();
			List<CategorizedProblem> problems = new ArrayList<CategorizedProblem>();

			for (IMethod method : type.getMethods()) {

				String methodName = method.getElementName();

				try {
					new PartTree(methodName, domainClass);
				} catch (PropertyReferenceException e) {
					problems.add(new InvalidDerivedQueryProblem(method, e.getMessage()));
				}
			}

			context.putProblems("MARKER", problems.toArray(new CategorizedProblem[problems.size()]));

		} catch (JavaModelException e) {
			SpringCore.log(e);
		}

		super.reconcile(context);
	}
}
