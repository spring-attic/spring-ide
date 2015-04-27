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

import java.util.List;

import org.springframework.ide.eclipse.yaml.editor.ast.NodeRef.SeqRef;
import org.springframework.ide.eclipse.yaml.editor.ast.NodeRef.TupleValueRef;
import org.springframework.ide.eclipse.yaml.editor.path.YamlPath;

/**
 * A path is just a List<NodeRef> this class contains a few
 * utilities to manipulate / access parts of paths.
 *
 * @author Kris De Volder
 */
public class PathUtil {

	public static NodeRef<?> getLast(List<NodeRef<?>> path) {
		if (path!=null && !path.isEmpty()) {
			return path.get(path.size()-1);
		}
		return null;
	}

	/**
	 * Convert a ast path into a String that can be used for property lookup in
	 * a property index. Not all paths can be converted (for example navigating
	 * into a 'complex' key can not be represented by an equivalent property name.
	 * <p>
	 * If a path can not be converted as a whole then null is returned.
	 */
	public static String toPropertyPrefixString(List<NodeRef<?>> path) {
		YamlPath yamlPath = YamlPath.fromASTPath(path);
		return yamlPath.toPropString();
	}

	private static void addDotMaybe(StringBuilder buf) {
		if (buf.length()>0) {
			buf.append(".");
		}
	}

}
