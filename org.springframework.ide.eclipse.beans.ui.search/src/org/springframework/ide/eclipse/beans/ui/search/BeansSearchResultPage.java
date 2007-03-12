/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.search;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.PartInitException;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchContentProvider;
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
		viewer.setLabelProvider(new BeansModelLabelProvider(true));
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
