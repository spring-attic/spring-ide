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
package org.springframework.ide.eclipse.beans.mylar.ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.mylar.monitor.ui.workbench.AbstractEditorTracker;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;

/**
 * {@link AbstractEditorTracker} extension that registers
 * {@link BeansActiveFoldingListener} for every open {@link IBeansConfig}.
 * <p>
 * {@link #editorShouldBeRegistered(XMLMultiPageEditorPart)} determines if a 
 * {@link IFileEditorInput} is actually a {@link IBeansConfig}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeansActiveFoldingEditorTracker extends AbstractEditorTracker {

	protected Map<XMLMultiPageEditorPart, BeansActiveFoldingListener> editorListenerMap = 
		new ConcurrentHashMap<XMLMultiPageEditorPart, BeansActiveFoldingListener>();

	@Override
	public void editorOpened(IEditorPart part) {
		if (part instanceof XMLMultiPageEditorPart)
			registerEditor((XMLMultiPageEditorPart) part);
	}

	@Override
	public void editorClosed(IEditorPart part) {
		if (part instanceof XMLMultiPageEditorPart)
			unregisterEditor((XMLMultiPageEditorPart) part);
	}

	public void registerEditor(XMLMultiPageEditorPart editor) {
		if (editorShouldBeRegistered(editor)) {
			if (editorListenerMap.containsKey(editor)) {
				return;
			}
			else {
				BeansActiveFoldingListener listener = new BeansActiveFoldingListener(
						editor);
				editorListenerMap.put(editor, listener);
			}
		}
	}

	/**
	 * Make sure that only {@link IBeansConfig} files are registered
	 */
	protected boolean editorShouldBeRegistered(XMLMultiPageEditorPart editor) {
		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput != null && editorInput instanceof IFileEditorInput) {
			IResource resource = ((IFileEditorInput) editorInput).getFile();
			return BeansCoreUtils.isBeansConfig(resource);
		}
		return false;
	}

	public void unregisterEditor(XMLMultiPageEditorPart editor) {
		BeansActiveFoldingListener listener = editorListenerMap.get(editor);
		if (listener != null) {
			listener.dispose();
		}
		editorListenerMap.remove(editor);
	}

	@Override
	protected void editorBroughtToTop(IEditorPart part) {
		// ignore
	}

}