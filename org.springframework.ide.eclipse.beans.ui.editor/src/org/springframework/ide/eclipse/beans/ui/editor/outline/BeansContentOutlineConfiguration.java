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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.eclipse.wst.xml.ui.views.contentoutline.XMLContentOutlineConfiguration;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorPlugin;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.actions.LexicalSortingAction;
import org.springframework.ide.eclipse.beans.ui.editor.actions.OutlineStyleAction;

public class BeansContentOutlineConfiguration
									   extends XMLContentOutlineConfiguration {
	/**
	 * Returns the bean editor plugin's preference store.
	 */
	protected IPreferenceStore getPreferenceStore() {
		return BeansEditorPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Adds the outline style toggle to the context menu.
	 */
	protected IContributionItem[] createMenuContributions(TreeViewer viewer) {
		IContributionItem styleItem = new ActionContributionItem(
				new OutlineStyleAction(viewer));
		IContributionItem[] items = super.createMenuContributions(viewer);
		if (items == null) {
			items = new IContributionItem[] { styleItem };
		} else {
			IContributionItem[] combinedItems = new IContributionItem[
			                                                 items.length + 1];
			System.arraycopy(items, 0, combinedItems, 0, items.length);
			combinedItems[items.length] = styleItem;
			items = combinedItems;
		}
		return items;
	}

	/**
	 * Adds the sort toggle to the toolbar.
	 */
	protected IContributionItem[] createToolbarContributions(TreeViewer viewer) {
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

	/**
	 * Returns the wrapped original XML outline content provider which is only
	 * used if the outline view is non-spring style. This way the XML outline's
	 * "Show First Attribute" feature doesn't interfer with a non-spring style
	 * outline view.
	 * @see BeansOutlineLabelProvider
	 */
	public ILabelProvider getLabelProvider(TreeViewer viewer) {
		return new BeansOutlineLabelProvider(super.getLabelProvider(viewer));
	}

	private final static class BeansOutlineLabelProvider extends JFaceNodeLabelProvider {

		private ILabelProvider xmlProvider;

		public BeansOutlineLabelProvider(ILabelProvider superProvider) {
			this.xmlProvider = superProvider;
		}

		public String getText(Object o) {
			if (BeansEditorUtils.isSpringStyleOutline()) {
				return super.getText(o);
			}
			return xmlProvider.getText(o);
		}
	}
}
