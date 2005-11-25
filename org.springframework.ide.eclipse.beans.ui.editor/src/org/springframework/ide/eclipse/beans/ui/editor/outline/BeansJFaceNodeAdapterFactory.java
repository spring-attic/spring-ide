/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.editor.outline;

import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapterFactory;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.ui.internal.contentoutline.IJFaceNodeAdapter;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeAdapterFactory;

/**
 * An adapter factory to create JFaceNodeAdapters. Use this adapter factory
 * with a JFaceAdapterContentProvider to display DOM nodes in a tree.
 */
public class BeansJFaceNodeAdapterFactory extends JFaceNodeAdapterFactory {

	protected BeansJFaceNodeAdapter singletonAdapter;

	public BeansJFaceNodeAdapterFactory() {
		this(IJFaceNodeAdapter.class, true);
	}

	public BeansJFaceNodeAdapterFactory(Object adapterKey, boolean registerAdapters) {
		super(adapterKey, registerAdapters);
	}


	/**
	 * Create a new JFace adapter for the DOM node passed in
	 */
	protected INodeAdapter createAdapter(INodeNotifier node) {
		if (singletonAdapter == null) {

			// create the JFaceNodeAdapter
			singletonAdapter = new BeansJFaceNodeAdapter(this);
			initAdapter(singletonAdapter, node);
		}
		return singletonAdapter;
	}

	public INodeAdapterFactory copy() {
		return new BeansJFaceNodeAdapterFactory();
	}
}
