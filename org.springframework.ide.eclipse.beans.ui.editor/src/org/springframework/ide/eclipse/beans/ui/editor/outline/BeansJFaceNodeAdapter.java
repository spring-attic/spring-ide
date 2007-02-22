/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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

	public Object getParent(Object object) {
		if (BeansEditorUtils.isSpringStyleOutline()) {
			Node node = (Node) object;
			return node.getParentNode();
		}
		else {
			return super.getParent(object);
		}
	}

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
	public boolean isAdapterForType(Object type) {
		return type.equals(ADAPTER_KEY);
	}

	public Object[] getElements(Object node) {
		if (BeansEditorUtils.isSpringStyleOutline()) {
			return getChildren(node);
		}
		else {
			return super.getElements(node);
		}
	}
}
