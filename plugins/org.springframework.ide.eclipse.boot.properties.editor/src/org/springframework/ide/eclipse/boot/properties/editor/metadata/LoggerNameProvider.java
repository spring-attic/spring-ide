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

import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.core.search.SearchPattern;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderStrategy;

/**
 * Provides the algorithm for 'logger-name' valueProvider.
 * <p>
 * See: https://github.com/spring-projects/spring-boot/blob/master/spring-boot-docs/src/main/asciidoc/appendix-configuration-metadata.adoc
 *
 * @author Kris De Volder
 */
public class LoggerNameProvider extends JdtSearchingValueProvider {

	private  static final ValueProviderStrategy INSTANCE = new LoggerNameProvider();
	public static final Function<Map<String, Object>, ValueProviderStrategy> FACTORY = (params) -> INSTANCE;

	@Override
	protected SearchPattern toPattern(String query) {
		String wildCardedQuery = toWildCardPattern(query);
		return SearchPattern.createOrPattern(
				toTypePattern(wildCardedQuery),
				toPackagePattern(wildCardedQuery)
		);
	}

}
