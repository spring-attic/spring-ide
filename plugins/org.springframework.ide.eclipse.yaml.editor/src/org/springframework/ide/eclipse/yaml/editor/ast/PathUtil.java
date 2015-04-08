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
import org.springframework.ide.eclipse.yaml.editor.ast.NodeRef.TupleKeyRef;
import org.springframework.ide.eclipse.yaml.editor.ast.NodeRef.TupleValueRef;

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
	 * @return true if path is a path pointing to node representing the 'key'
	 * of a map.
	 */
	public static boolean pointsToKey(List<NodeRef<?>> path) {
		NodeRef<?> last = getLast(path);
		if (last!=null) {
			return last instanceof TupleKeyRef;
		}
		return false;
	}

	/**
	 * Convert a ast path into a String that can be used for property lookup in
	 * a property index. Not all paths can be converted (for example navigating
	 * into a 'complex' key can not be represented by an equivalent property name.
	 * <p>
	 * If a path can not be converted as a whole then null is returned.
	 */
	public static String toPropertyPrefixString(List<NodeRef<?>> path) {
		final StringBuilder buf = new StringBuilder();
		int last = path.size()-1;
		for (int i = 0; i <= last; i++) {
			NodeRef<?> nodeRef = path.get(i);
			switch (nodeRef.getKind()) {
			case ROOT:
				break;
			case SEQ:
				buf.append("[");
				buf.append(((SeqRef)nodeRef).getIndex());
				buf.append("]");
				break;
			case KEY:
				{
					boolean handled = false;
					if (i==last) {
						String keyName = NodeUtil.asScalar(nodeRef.get());
						if (keyName!=null) {
							addDotMaybe(buf);
							buf.append(keyName);
							handled = true;
						}
					}
					if (!handled) {
						return null;
					}
				}
				break;
			case VAL:
				{
					boolean handled = false;
					String keyName = NodeUtil.asScalar(((TupleValueRef)nodeRef).getTuple().getKeyNode());
					if (keyName!=null) {
						addDotMaybe(buf);
						buf.append(keyName);
						handled = true;
					}
					if (!handled) {
						return null;
					}
				}
				break;
			default:
				throw new Error("Missing case");
			}
		} //end for loop
		return buf.toString();
	}

	private static void addDotMaybe(StringBuilder buf) {
		if (buf.length()>0) {
			buf.append(".");
		}
	}

}
