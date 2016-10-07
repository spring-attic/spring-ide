/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.completions.YamlAssistContext;

public class ApplicationYamlAssistContextProvider implements YamlAssistContextProvider {

	private final SpringPropertyIndexProvider indexProvider;
	private final PropertyCompletionFactory completionFactory;
	private final TypeUtilProvider typeUtilProvider;
	private final RelaxedNameConfig relaxedNameConfig;

	public ApplicationYamlAssistContextProvider(
			SpringPropertyIndexProvider indexProvider,
			TypeUtilProvider typeUtilProvider,
			RelaxedNameConfig relaxedNameConfig,
			DocumentContextFinder documentContextFinder
	) {
		super();
		this.indexProvider = indexProvider;
		this.completionFactory = new PropertyCompletionFactory(documentContextFinder);
		this.typeUtilProvider = typeUtilProvider;
		this.relaxedNameConfig = relaxedNameConfig;
	}

	@Override
	public YamlAssistContext getGlobalAssistContext(YamlDocument ydoc) {
		IDocument doc = ydoc.getDocument();
		FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
		return ApplicationYamlAssistContext.global(ydoc, index, completionFactory, typeUtilProvider.getTypeUtil(doc), relaxedNameConfig);
	}
}