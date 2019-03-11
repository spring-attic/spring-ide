/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.yaml.completions;

import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.boot.properties.editor.DocumentContextFinder;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.RelaxedNameConfig;
import org.springframework.ide.eclipse.boot.properties.editor.completions.PropertyCompletionFactory;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtilProvider;
import org.springframework.ide.eclipse.editor.support.yaml.YamlAssistContextProvider;
import org.springframework.ide.eclipse.editor.support.yaml.YamlCompletionEngine;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.completions.YamlAssistContext;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

/**
 * @author Kris De Volder
 */
public class ApplicationYamlCompletionEngine {
	public static YamlCompletionEngine create(
			final SpringPropertyIndexProvider indexProvider,
			final DocumentContextFinder documentContextFinder,
			final YamlStructureProvider structureProvider,
			final TypeUtilProvider typeUtilProvider,
			final RelaxedNameConfig conf
	) {
		final PropertyCompletionFactory completionFactory = new PropertyCompletionFactory(documentContextFinder);
		YamlAssistContextProvider contextProvider = new YamlAssistContextProvider() {
			@Override
			public YamlAssistContext getGlobalAssistContext(YamlDocument ydoc) {
				IDocument doc = ydoc.getDocument();
				FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
				return ApplicationYamlAssistContext.global(index, completionFactory, typeUtilProvider.getTypeUtil(doc), conf);
			}
		};
		return new YamlCompletionEngine(structureProvider, contextProvider);
	}
}
