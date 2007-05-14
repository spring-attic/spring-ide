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
package org.springframework.ide.eclipse.beans.ui.search;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.PartInitException;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchContentProvider;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchLabelProvider;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * {@link ISearchResultPage} which displays the results from searching the
 * {@link IBeansModel}.
 * 
 * @author Torsten Juergeleit
 */
public class BeansSearchResultPage extends AbstractTextSearchViewPage {

	private BeansSearchContentProvider provider;

	public BeansSearchResultPage() {
		super(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		setID(BeansSearchResultPage.class.getName());
	}

	protected void configureTableViewer(TableViewer viewer) {
		throw new IllegalStateException("Doesn't support flat mode.");
	}

	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setUseHashlookup(true);
		viewer.setLabelProvider(new BeansSearchLabelProvider(true));
		BeansSearchContentProvider provider = new BeansSearchContentProvider();
		viewer.setContentProvider(provider);
		this.provider = provider;
	}

	protected void elementsChanged(Object[] objects) {
		if (provider != null) {
			provider.elementsChanged(objects);
		}
	}

	protected void clear() {
		provider.clear();
	}

	@Override
	protected void showMatch(Match match, int currentOffset, int currentLength,
			boolean activate) throws PartInitException {
		Object element = match.getElement();
		if (element instanceof ISourceModelElement) {
			SpringUIUtils.openInEditor((ISourceModelElement) element, activate);
		}
	}
}
