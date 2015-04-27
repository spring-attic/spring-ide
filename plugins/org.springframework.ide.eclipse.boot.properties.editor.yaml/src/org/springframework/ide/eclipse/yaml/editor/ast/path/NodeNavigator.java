/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.yaml.editor.ast.path;

import org.springframework.ide.eclipse.yaml.editor.completions.YamlNavigable;
import org.yaml.snakeyaml.nodes.Node;

/**
 * A NodeNavigator is an operation that goes from a 'current'
 * node to a related node. As such it can be represented
 * abstractly as a function Node -> Node.
 *
 * TODO: remove this. Should use {@link YamlNavigable} instead
 *
 * @author Kris De Volder
 */
public interface NodeNavigator {
	Node apply(Node node);
	String toNavString();
	String toPropString();
}
