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
package org.springframework.ide.eclipse.editor.support.yaml;

import org.springframework.ide.eclipse.editor.support.completions.ICompletionEngine;
import org.springframework.ide.eclipse.editor.support.yaml.structure.YamlStructureProvider;

/**
 * Abstratc superclass to make it easier to define {@link ICompletionEngine} implementation
 * for .yml file.
 *
 * @author Kris De Volder
 */
public abstract class YamlCompletionEngine implements ICompletionEngine {

	public YamlCompletionEngine(YamlStructureProvider structureProvider) {
		this.structureProvider= structureProvider;
	}

	protected YamlStructureProvider structureProvider;

}
