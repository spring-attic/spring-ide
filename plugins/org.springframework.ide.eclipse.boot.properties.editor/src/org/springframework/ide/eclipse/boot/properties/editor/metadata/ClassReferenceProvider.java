/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.metadata;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderFactory;
import org.springframework.ide.eclipse.boot.properties.editor.util.FluxJdtSearch;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.boot.util.StringUtil;

/**
 * Provides the algorithm for 'class-reference' valueProvider.
 * <p>
 * See: https://github.com/spring-projects/spring-boot/blob/master/spring-boot-docs/src/main/asciidoc/appendix-configuration-metadata.adoc
 *
 * @author Kris De Volder
 */
public class ClassReferenceProvider extends JdtSearchingValueProvider {

	private static final ClassReferenceProvider UNTARGETTED_INSTANCE = new ClassReferenceProvider(null);

	public static final ValueProviderFactory FACTORY = (params) -> {
		if (params!=null) {
			Object obj = params.get("target");
			if (obj instanceof String) {
				String target = (String) obj;
				if (StringUtil.hasText(target)) {
					return new ClassReferenceProvider(target);
				}
			}
		}
		return UNTARGETTED_INSTANCE;
	};

	/**
	 * Optional, fully qualified name of the 'target' type. Suggested hints should be a subtype of this type.
	 */
	private String target;

	private ClassReferenceProvider(String target) {
		this.target = target;
	}

	@Override
	protected SearchPattern toPattern(String query) {
		return toTypePattern(toWildCardPattern(query));
	}

	public IJavaSearchScope getScope(IJavaProject project) throws JavaModelException {
		if (target!=null) {
			IType type = getTargetType(project);
			if (type!=null) {
				boolean onlySubtypes = true;
				boolean includeFocusType = false;
				WorkingCopyOwner owner = null;
				return SearchEngine.createStrictHierarchyScope(project, type, onlySubtypes, includeFocusType, owner);
			}
			return null; //target type not on classpath so... can't search (and arguably if type isn't on CP
						// neither should any of its subtypes... so searching is a bit pointless).
						// scope = null will cause FluxJdtSearch to quickly return zero results.
		}
		return FluxJdtSearch.searchScope(project);
	}

	private IType getTargetType(IJavaProject project) {
		try {
			if (target!=null) {
				return project.findType(target);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

}
