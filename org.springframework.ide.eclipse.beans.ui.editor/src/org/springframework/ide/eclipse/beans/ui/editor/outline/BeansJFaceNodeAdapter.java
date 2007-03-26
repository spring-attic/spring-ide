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
package org.springframework.ide.eclipse.beans.ui.editor.outline;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeAdapter;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeAdapterFactory;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;
import org.springframework.ide.eclipse.beans.ui.editor.IPreferencesConstants;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.w3c.dom.Node;

/**
 * Adapts a Spring beans node to a JFace viewer.
 */
@SuppressWarnings("restriction")
public class BeansJFaceNodeAdapter extends JFaceNodeAdapter {

	public static final Class<?> ADAPTER_KEY = IJFaceNodeAdapter.class;

	public BeansJFaceNodeAdapter(JFaceNodeAdapterFactory adapterFactory) {
		super(adapterFactory);
	}

	@Override
	public Object[] getChildren(Object object) {
		if (BeansEditorUtils.isSpringStyleOutline()) {
			Preferences prefs = Activator.getDefault().getPluginPreferences();
			boolean sort = prefs.getBoolean(IPreferencesConstants.OUTLINE_SORT);

			Node node = (Node) object;

			List<Node> children = new ArrayList<Node>();
			for (Node child = node.getFirstChild(); child != null; child = child
					.getNextSibling()) {
				Node n = child;
				if (n.getNodeType() != Node.TEXT_NODE) {
					if (n.getNodeType() == Node.COMMENT_NODE) {
						if (!sort) {
							children.add(n);
						}
					}
					else {
						children.add(n);
					}
				}
			}
			return children.toArray();
		}
		else {
			return super.getChildren(object);
		}
	}

	@Override
	public Object getParent(Object object) {
		if (BeansEditorUtils.isSpringStyleOutline()) {
			Node node = (Node) object;
			return node.getParentNode();
		}
		else {
			return super.getParent(object);
		}
	}

	@Override
	public boolean hasChildren(Object object) {
		if (BeansEditorUtils.isSpringStyleOutline()) {
			Node node = (Node) object;
			for (Node child = node.getFirstChild(); child != null; child = child
					.getNextSibling()) {
				if (child.getNodeType() != Node.TEXT_NODE)
					return true;
			}
		}
		else {
			return super.hasChildren(object);
		}
		return false;
	}

	/**
	 * Allowing the INodeAdapter to compare itself against the type allows it to
	 * return true in more than one case.
	 */
	@Override
	public boolean isAdapterForType(Object type) {
		return type.equals(ADAPTER_KEY);
	}

	@Override
	public Object[] getElements(Object node) {
		if (BeansEditorUtils.isSpringStyleOutline()) {
			return getChildren(node);
		}
		else {
			return super.getElements(node);
		}
	}
}
