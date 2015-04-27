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
package org.springframework.ide.eclipse.yaml.editor.path;


/**
 * A YamlPathSegment is a 'primitive' NodeNavigator operation.
 * More complex operations (i.e {@link YamlPath}) are composed as seqences
 * of 0 or more {@link YamlPathSegment}s.
 *
 * @author Kris De Volder
 */
public abstract class YamlPathSegment {

	public static enum YamlPathSegmentType {
		VAL_AT_KEY, //Go to value associate with given key in a map.
		KEY_AT_KEY, //Go to the key node associated with a given key in a map.
		VAL_AT_INDEX //Go to value associate with given index in a sequence
	}

	public static class AtIndex extends YamlPathSegment {

		private int index;

		public AtIndex(int index) {
			this.index = index;
		}

		public String toNavString() {
			return "["+index+"]";
		}

		public String toPropString() {
			return "["+index+"]";
		}

		@Override
		public YamlPathSegmentType getType() {
			return YamlPathSegmentType.VAL_AT_INDEX;
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
			return YamlPathSegmentType.VAL_AT_KEY;
		}

		@Override
		public Integer toIndex() {
			return null;
		}
	}


	public String toString() {
		return toNavString();
	}

	public abstract String toNavString();
	public abstract String toPropString();

	public abstract Integer toIndex();
	public abstract YamlPathSegmentType getType();

	public static YamlPathSegment valueAt(String key) {
		return new AtKey(key);
	}
	public static YamlPathSegment valueAt(int index) {
		return new AtIndex(index);
	}
	public static YamlPathSegment keyAt(String key) {
		return new AtKey(key);
	}

}
