/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.outline;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.wst.sse.ui.views.contentoutline.ContentOutlineConfiguration;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeContentProvider;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorPlugin;
import org.springframework.ide.eclipse.beans.ui.editor.actions.LexicalSortingAction;

public class BeansContentOutlineConfiguration
										  extends ContentOutlineConfiguration {
	private IContentProvider contentProvider = null;
	private ILabelProvider labelProvider;
	
	protected IPreferenceStore getPreferenceStore() {
		return BeansEditorPlugin.getDefault().getPreferenceStore();
	}

	public IContentProvider getContentProvider(TreeViewer viewer) {
		if (contentProvider == null) {
			contentProvider = new JFaceNodeContentProvider();
		}
		return contentProvider;
	}

	public ILabelProvider getLabelProvider(TreeViewer viewer) {
		if (labelProvider == null) {
			labelProvider = new JFaceNodeLabelProvider();
		}
		return labelProvider;
	}

	protected IContributionItem[] createToolbarContributions(
														   TreeViewer viewer) {
		IContributionItem sortItem = new ActionContributionItem(
											 new LexicalSortingAction(viewer));
		IContributionItem[] items = super.createToolbarContributions(viewer);
		if (items == null) {
			items = new IContributionItem[] { sortItem };
		} else {
			IContributionItem[] combinedItems = new IContributionItem[
			                                                 items.length + 1];
			System.arraycopy(items, 0, combinedItems, 0, items.length);
			combinedItems[items.length] = sortItem;
			items = combinedItems;
		}
		return items;
	}
}
