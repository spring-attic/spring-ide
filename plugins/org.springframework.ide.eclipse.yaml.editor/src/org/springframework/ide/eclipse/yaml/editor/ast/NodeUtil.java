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
package org.springframework.ide.eclipse.yaml.editor.ast;

import org.eclipse.jface.text.IRegion;
import org.yaml.snakeyaml.nodes.Node;

import org.eclipse.jface.text.Region;

/**
 * @author Kris De Volder
 */
public class NodeUtil {

	public static boolean contains(Node node, int offset) {
		return getStart(node)<=offset && offset<=getEnd(node);
	}

	public static int getStart(Node node) {
		return node.getStartMark().getIndex();
	}

	public static int getEnd(Node node) {
		return node.getEndMark().getIndex();
	}

	public static IRegion getRegion(Node node) {
		int start = getStart(node);
		int end = getEnd(node);
		return new Region(start, end-start);
	}

}
