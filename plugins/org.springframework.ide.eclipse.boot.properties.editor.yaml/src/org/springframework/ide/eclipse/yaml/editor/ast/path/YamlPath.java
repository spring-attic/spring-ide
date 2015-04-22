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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.ide.eclipse.yaml.editor.completions.YamlNavigable;
import org.yaml.snakeyaml.nodes.Node;

/**
 * @author Kris De Volder
 */
public class YamlPath implements NodeNavigator {

	public static final YamlPath EMPTY = new YamlPath();
	private final YamlPathSegment[] segments;

	public YamlPath(List<YamlPathSegment> segments) {
		this.segments = segments.toArray(new YamlPathSegment[segments.size()]);
	}

	public YamlPath() {
		this.segments = new YamlPathSegment[0];
	}

	public YamlPath(YamlPathSegment[] segments) {
		this.segments = segments;
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

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("YamlPath(");
		boolean first = true;
		for (YamlPathSegment s : segments) {
			if (!first) {
				buf.append(", ");
			}
			buf.append(s);
			first = false;
		}
		buf.append(")");
		return buf.toString();
	}

	public int size() {
		return segments.length;
	}

	public YamlPathSegment getSegment(int segment) {
		return segments[segment];
	}

	public YamlPath append(YamlPathSegment s) {
		YamlPathSegment[] newPath = Arrays.copyOf(segments, segments.length+1);
		newPath[segments.length] = s;
		return new YamlPath(newPath);
	}

	public <T extends YamlNavigable<T>> T traverse(T startNode) throws Exception {
		T node = startNode;
		for (YamlPathSegment s : segments) {
			if (node==null) {
				return null;
			}
			node = node.traverse(s);
		}
		return node;
	}

	public YamlPath dropFirst(int dropCount) {
		if (dropCount>=size()) {
			return EMPTY;
		}
		if (dropCount==0) {
			return this;
		}
		YamlPathSegment[] newPath = new YamlPathSegment[segments.length-dropCount];
		for (int i = 0; i < newPath.length; i++) {
			newPath[i] = segments[i+dropCount];
		}
		return new YamlPath(newPath);
	}

	public boolean isEmpty() {
		return segments.length==0;
	}

	public YamlPath tail() {
		return dropFirst(1);
	}

}
