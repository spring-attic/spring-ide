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

import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapterFactory;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeAdapterFactory;

/**
 * An adapter factory to create JFaceNodeAdapters. Use this adapter factory with
 * a JFaceAdapterContentProvider to display DOM nodes in a tree.
 */
@SuppressWarnings("restriction")
public class BeansJFaceNodeAdapterFactory extends JFaceNodeAdapterFactory {

	public BeansJFaceNodeAdapterFactory() {
		this(IJFaceNodeAdapter.class, true);
	}

	public BeansJFaceNodeAdapterFactory(Object adapterKey,
			boolean registerAdapters) {
		super(adapterKey, registerAdapters);
	}

	/**
	 * Create a new JFace adapter for the DOM node passed in.
	 */
	@Override
	protected INodeAdapter createAdapter(INodeNotifier node) {
		if (singletonAdapter == null) {
			singletonAdapter = new BeansJFaceNodeAdapter(this);
			initAdapter(singletonAdapter, node);
		}
		return singletonAdapter;
	}

	@Override
	public INodeAdapterFactory copy() {
		return new BeansJFaceNodeAdapterFactory();
	}
}
