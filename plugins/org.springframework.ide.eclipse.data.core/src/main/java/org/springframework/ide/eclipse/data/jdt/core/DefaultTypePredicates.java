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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Simple implementation of {@link TypePredicates} to make functionality of JdtUtils injectable.
 * 
 * @author Oliver Gierke
 */
class DefaultTypePredicates implements TypePredicates {

	private final IJavaProject project;

	/**
	 * Creates a new {@link DefaultTypePredicates} instance using the given {@link IJavaProject}.
	 * 
	 * @param project
	 */
	public DefaultTypePredicates(IJavaProject project) {
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.data.jdt.core.TypePredicates#typeImplements(org.eclipse.jdt.core.IType, java.lang.String)
	 */
	public boolean typeImplements(IType type, String candidateType) {
		return JdtUtils.doesImplement(project.getResource(), type, candidateType);
	}
}
