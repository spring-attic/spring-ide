/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import java.util.function.Supplier;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.springframework.ide.eclipse.beans.ui.live.model.AbstractLiveBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeanType;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.live.tree.ContextGroupedBeansContentProvider;
import org.springframework.ide.eclipse.beans.ui.live.tree.LiveBeansTreeLabelProvider;
import org.springframework.ide.eclipse.beans.ui.live.utils.LiveBeanUtil;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Failable;
import org.springframework.ide.eclipse.boot.dash.model.MissingLiveInfoMessages;
import org.springsource.ide.eclipse.commons.livexp.ui.util.TreeElementWrappingContentProvider;
import org.springsource.ide.eclipse.commons.livexp.ui.util.TreeElementWrappingContentProvider.TreeNode;

/**
 * Live beans property section
 *
 * @author Alex Boyko
 *
 */
public class BeansPropertiesSection extends AbstractBdePropertiesSection {

	private TabbedPropertySheetPage page;
	private SearchableTreeControl searchableTree;
	private Failable<LiveBeansModel> beans;


	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		page = aTabbedPropertySheetPage;

		ITreeContentProvider treeContent = new TreeElementWrappingContentProvider(new BeansContentProvider(ContextGroupedBeansContentProvider.INSTANCE));
		LabelProvider labelProvider = LiveBeansTreeLabelProvider.INSTANCE;

		searchableTree = new SearchableTreeControl(getWidgetFactory(), getNoContentMessage());
		searchableTree.createControls(parent, page, treeContent, labelProvider);

		searchableTree.getTreeViewer().addDoubleClickListener(new DoubleClickListener());
	}

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		// BootDashElement should be input rather than the LiveBeansModel. Due to
		// polling the model often to show changes in the model it's best to refresh the
		// tree viewer rather then set the whole input that would remove the selection
		// and collapse expanded nodes
		searchableTree.getTreeViewer().setInput(getBootDashElement());
	}

	@Override
	public void refresh() {
		this.beans = null;
		BootDashElement bde = getBootDashElement();
		if (bde != null) {
			this.beans = bde.getLiveBeans();
		}

		searchableTree.refresh();
	}

	private Supplier<String> getNoContentMessage() {
		return () -> {
			BootDashElement bde = getBootDashElement();
			if (bde == null) {
				return MissingLiveInfoMessages.noSelectionMessage("Beans");
			} else if (beans != null && beans.hasFailed()) {
				return beans.getErrorMessage();
			} else {
				return null;
			}
		};
	}

	private class BeansContentProvider implements ITreeContentProvider {

		private ITreeContentProvider delegateContentProvider;

		BeansContentProvider(ITreeContentProvider delegate) {
			this.delegateContentProvider = delegate;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof BootDashElement) {
				return delegateContentProvider.getElements(beans == null ? null : beans.getValue());
			}
			return new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			return delegateContentProvider.getChildren(parentElement);
		}

		@Override
		public Object getParent(Object element) {
			return delegateContentProvider.getParent(element);
		}

		@Override
		public boolean hasChildren(Object element) {
			return delegateContentProvider.hasChildren(element);
		}
	}

	private class DoubleClickListener implements IDoubleClickListener {

		@Override
		public void doubleClick(DoubleClickEvent event) {
			ISelection sel = event.getSelection();
			if (sel instanceof IStructuredSelection) {
				IStructuredSelection structuredSel = (IStructuredSelection) sel;
				Object firstElement = structuredSel.getFirstElement();
				if (firstElement instanceof TreeNode) {
					TreeNode node = (TreeNode) firstElement;
					Object wrappedValue = node.getWrappedValue();
					// NOTE: navigate to bean type and navigate to resource definition are NOT
					// always the same. Be sure to use the correct one given a tree node.
					// For nodes that indicate bean Type, use "to type" navigation
					// For all other nodes, use "to resource definition" navigation
					if (wrappedValue instanceof AbstractLiveBeansModelElement) {
						 LiveBeanUtil.navigateToResource((AbstractLiveBeansModelElement) wrappedValue);
					} else if (wrappedValue instanceof LiveBeanType) {
						 LiveBeanUtil.navigateToType(((LiveBeanType) wrappedValue).getBean());
					}
				}
			}
		}
	}
}
