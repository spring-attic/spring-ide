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

import org.springframework.ide.eclipse.yaml.editor.ast.NodeUtil;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;

/**
 * A YamlPathSegment is a 'primitive' NodeNavigator operation.
 * More complex operations (i.e {@link YamlPath}) are composed as seqences
 * of 0 or more {@link YamlPathSegment}s.
 *
 * @author Kris De Volder
 */
public abstract class YamlPathSegment implements NodeNavigator {

	public static enum YamlPathSegmentType {
		AT_SCALAR_KEY //Go to value associated with a given Scalar key expressed as a String.
	}

	public static class AtScalarKey extends YamlPathSegment {

		private String key;

		public AtScalarKey(String key) {
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
			return YamlPathSegmentType.AT_SCALAR_KEY;
		}
	}


	public String toString() {
		return toNavString();
	}

	public abstract YamlPathSegmentType getType();

	public static YamlPathSegment at(String key) {
		return new AtScalarKey(key);
	}
}
