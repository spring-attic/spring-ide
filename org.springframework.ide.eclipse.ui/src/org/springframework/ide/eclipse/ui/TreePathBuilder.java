/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui;

import java.util.LinkedList;

import org.eclipse.jface.viewers.TreePath;

public class TreePathBuilder {

	private final LinkedList<Object> segments = new LinkedList<Object>();
	
	public TreePathBuilder() {
	}

	public TreePathBuilder(Object segment) {
		segments.add(segment);
	}

	public TreePathBuilder(TreePathBuilder builder) {
		segments.addAll(builder.segments);
	}

	public TreePathBuilder(TreePath path) {
		for (int i = 0; i < path.getSegmentCount(); i++) {
			segments.addLast(path.getSegment(i));
		}
	}

	public void addParent(Object segment) throws IllegalArgumentException {
		if (segments.contains(segment)) {
			throw createCyclicPathException(segment);
		}
		segments.addFirst(segment);
	}

	public void addChild(Object segment) throws IllegalArgumentException {
		if (segments.contains(segment)) {
			throw createCyclicPathException(segment);
		}
		segments.addLast(segment);
	}

	public TreePath getPath() {
		return new TreePath(segments.toArray());
	}

	public TreePath getParentPath() {
		LinkedList parentSegments = new LinkedList<Object>(segments);
		parentSegments.removeLast();
		return new TreePath(parentSegments.toArray());
	}

	public Object getLastSegment() {
		return segments.getLast();
	}

	public Object getFirstSegment() {
		return segments.getFirst();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (Object segment : segments) {
			buffer.append(segment).append("::");
		}
		return buffer.toString();
	}

	protected IllegalArgumentException createCyclicPathException(
			Object segment) {
		return new IllegalArgumentException("Cyclic in path '" + this
				+ "' while adding segment '" + segment + "'");
	}
}
