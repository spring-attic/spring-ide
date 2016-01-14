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
package org.springframework.ide.eclipse.editor.support.yaml.structure;

import org.springframework.ide.eclipse.editor.support.yaml.YamlDocument;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureParser.SRootNode;

/**
 * @author Kris De Volder
 */
public abstract class YamlStructureProvider {
	public abstract SRootNode getStructure(YamlDocument doc) throws Exception;

	public static final YamlStructureProvider DEFAULT = new YamlStructureProvider() {
		public SRootNode getStructure(YamlDocument doc) throws Exception {
			return new YamlStructureParser(doc).parse();
		}
	};
}
