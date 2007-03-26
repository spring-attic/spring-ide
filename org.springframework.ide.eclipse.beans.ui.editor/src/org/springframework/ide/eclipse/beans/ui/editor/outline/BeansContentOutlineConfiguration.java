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

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.wst.xml.ui.views.contentoutline.XMLContentOutlineConfiguration;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;
import org.springframework.ide.eclipse.beans.ui.editor.actions.LexicalSortingAction;
import org.springframework.ide.eclipse.beans.ui.editor.actions.OutlineStyleAction;

public class BeansContentOutlineConfiguration extends
		XMLContentOutlineConfiguration {

	static boolean showAttributes;

	/**
	 * Returns the bean editor plugin's preference store.
	 */
	@Override
	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	/**
	 * Adds the outline style toggle to the context menu.
	 */
	@Override
	protected IContributionItem[] createMenuContributions(TreeViewer viewer) {
		IContributionItem styleItem = new ActionContributionItem(
				new OutlineStyleAction(viewer));
		IContributionItem[] items = super.createMenuContributions(viewer);
		if (items == null) {
			items = new IContributionItem[] { styleItem };
		}
		else {
			IContributionItem[] combinedItems = new IContributionItem[items.length + 1];
			System.arraycopy(items, 0, combinedItems, 0, items.length);
			combinedItems[items.length] = styleItem;
			items = combinedItems;
		}
		return items;
	}

	/**
	 * Adds the sort toggle to the toolbar.
	 */
	@Override
	protected IContributionItem[] createToolbarContributions(TreeViewer viewer) {
		IContributionItem sortItem = new ActionContributionItem(
				new LexicalSortingAction(viewer));
		IContributionItem[] items = super.createToolbarContributions(viewer);
		if (items == null) {
			items = new IContributionItem[] { sortItem };
		}
		else {
			IContributionItem[] combinedItems = new IContributionItem[items.length + 1];
			System.arraycopy(items, 0, combinedItems, 0, items.length);
			combinedItems[items.length] = sortItem;
			items = combinedItems;
		}
		return items;
	}

	@Override
	protected void enableShowAttributes(boolean showAttributes,
			TreeViewer treeViewer) {
		BeansContentOutlineConfiguration.showAttributes = showAttributes;
	}

	/**
	 * Returns the wrapped original XML outline content provider which is only
	 * used if the outline view is non-spring style. This way the XML outline's
	 * "Show Attributes" feature doesn't interfer with a non-spring style
	 * outline view.
	 * 
	 * @see BeansOutlineLabelProvider
	 */
	@Override
	public ILabelProvider getLabelProvider(TreeViewer viewer) {
		return new DelegatingLabelProvider(super.getLabelProvider(viewer));
	}

	public static boolean isShowAttributes() {
		return showAttributes;
	}
}
