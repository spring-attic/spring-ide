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

import java.util.List;

import org.springframework.ide.eclipse.yaml.editor.ast.NodeUtil;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;

/**
 * A YamlPathSegment is a 'primitive' NodeNavigator operation.
 * More complex operations (i.e {@link YamlPath}) are composed as seqences
 * of 0 or more {@link YamlPathSegment}s.
 *
 * @author Kris De Volder
 */
public abstract class YamlPathSegment implements NodeNavigator {

	public static enum YamlPathSegmentType {
		AT_KEY, //Go to value associate with given key in a map.
		AT_INDEX //Go to value associate with given index in a sequence
	}

	public static class AtIndex extends YamlPathSegment {

		private int index;

		public AtIndex(int index) {
			this.index = index;
		}

		@Override
		public Node apply(Node node) {
			if (node instanceof SequenceNode) {
				List<Node> children = ((SequenceNode) node).getValue();
				if (index>=0 && index<children.size()) {
					return children.get(index);
				}
			}
			return null;
		}

		@Override
		public String toNavString() {
			return "["+index+"]";
		}

		@Override
		public String toPropString() {
			return "["+index+"]";
		}

		@Override
		public YamlPathSegmentType getType() {
			return YamlPathSegmentType.AT_INDEX;
		}

		@Override
		public Integer toIndex() {
			return index;
		}
	}

	public static class AtKey extends YamlPathSegment {

		private String key;

		public AtKey(String key) {
			this.key = key;
		}

		@Override
		public Node apply(Node node) {
			if (node instanceof MappingNode) {
				MappingNode map = (MappingNode) node;
				for (NodeTuple e : map.getValue()) {
					String k = NodeUtil.asScalar(e.getKeyNode());
					if (k!=null && k.equals(key)) {
						return e.getValueNode();
					}
				}
			}
			return null;
		}

		@Override
		public String toNavString() {
			if (key.indexOf('.')>=0) {
				//TODO: what if key contains '[' or ']'??
				return "["+key+"]";
			}
			return "."+key;
		}

		@Override
		public String toPropString() {
			//Don't start with a '.' if trying to build a 'self contained' expression.
			return key;
		}

		@Override
		public YamlPathSegmentType getType() {
			return YamlPathSegmentType.AT_KEY;
		}

		@Override
		public Integer toIndex() {
			return null;
		}
	}


	public String toString() {
		return toNavString();
	}

	public abstract Integer toIndex();
	public abstract YamlPathSegmentType getType();

	public static YamlPathSegment at(String key) {
		return new AtKey(key);
	}
	public static YamlPathSegment at(int index) {
		return new AtIndex(index);
	}
}
