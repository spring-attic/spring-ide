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
package org.springframework.ide.eclipse.editor.support.yaml.completions;

import org.springframework.ide.eclipse.editor.support.yaml.YamlCompletionEngine;
import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YType;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeUtil;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YamlSchema;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

/**
 * A Yaml completion engine that creates proposals based on a single 'YType' which defines the
 * expected structure of the yaml document's contents.
 *
 * @author Kris De Volder
 */
public class TypeBasedYamlCompletionEngine extends YamlCompletionEngine {

	private YamlSchema schema;

	public TypeBasedYamlCompletionEngine(YamlStructureProvider structureProvider, YamlSchema schema) {
		super(structureProvider);
		this.schema = schema;
	}

	protected YType getTopLevelType() {
		return schema.getToplevelType();
	}

	protected YTypeUtil getTypeUtil() {
		return schema.getTypeUtil();
	}

	@Override
	protected YamlAssistContext getGlobalContext(YamlDocument doc) {
		return new TopLevelAssistContext() {
			@Override
			protected YamlAssistContext getDocumentContext(int documentSelector) {
				return new YTypeAssistContext(this, documentSelector, getTopLevelType(), getTypeUtil());
			}
		};
	}
}
