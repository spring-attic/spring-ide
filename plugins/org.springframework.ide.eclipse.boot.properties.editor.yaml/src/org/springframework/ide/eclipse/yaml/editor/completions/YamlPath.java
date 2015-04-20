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
package org.springframework.ide.eclipse.yaml.editor.completions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.yaml.editor.ast.path.NodeNavigator;
import org.springframework.ide.eclipse.yaml.editor.ast.path.YamlPathSegment;
import org.yaml.snakeyaml.nodes.Node;

public class YamlPath implements NodeNavigator {

	public static final YamlPath EMPTY = new YamlPath();
	private final YamlPathSegment[] segments;

	public YamlPath(List<YamlPathSegment> segments) {
		this.segments = segments.toArray(new YamlPathSegment[segments.size()]);
	}

	public YamlPath() {
		this.segments = new YamlPathSegment[0];
	}

	@Override
	public Node apply(Node node) {
		for (YamlPathSegment s : segments) {
			node = s.apply(node);
		}
		return node;
	}

	/**
	 * Turns a path into a property String.
	 */
	@Override
	public String toPropString() {
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (YamlPathSegment s : segments) {
			if (first) {
				buf.append(s.toPropString());
			} else {
				buf.append(s.toNavString());
			}
			first = false;
		}
		return buf.toString();
	}

	@Override
	public String toNavString() {
		StringBuilder buf = new StringBuilder();
		for (YamlPathSegment s : segments) {
			buf.append(s.toNavString());
		}
		return buf.toString();
	}

	public YamlPathSegment[] getSegments() {
		return segments;
	}

	/**
	 * Parse a YamlPath from a dotted property name. The segments are obtained
	 * by spliting the name at each dot.
	 */
	public static YamlPath fromProperty(String propName) {
		ArrayList<YamlPathSegment> segments = new ArrayList<YamlPathSegment>();
		for (String s : propName.split("\\.")) {
			segments.add(YamlPathSegment.at(s));
		}
		return new YamlPath(segments);
	}
}
