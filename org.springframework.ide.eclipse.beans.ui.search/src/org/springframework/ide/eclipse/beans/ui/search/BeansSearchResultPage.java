/*
 * Copyright 2002-2006 the original author or authors.
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

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.beans.ui.BeansModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.search.internal.BeansSearchContentProvider;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author David Watkins
 * @author Torsten Juergeleit
 */
public class BeansSearchResultPage extends AbstractTextSearchViewPage {

	private ISearchResultListener resultListener;

	private IDoubleClickListener doubleClickListener;

	private BeansSearchContentProvider provider;

	private TreeViewer viewer;

	public BeansSearchResultPage() {
		super(AbstractTextSearchViewPage.FLAG_LAYOUT_TREE);
		setID(BeansSearchResultPage.class.getName());
		resultListener = new ISearchResultListener() {
			public void searchResultChanged(SearchResultEvent e) {
				handleSearchResultsChanged(e);
			}
		};
		doubleClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				Object obj = selection.getFirstElement();
				if (obj instanceof ISourceModelElement) {
					SpringUIUtils.openInEditor((ISourceModelElement) obj);
				}
			}
		};
	}

	protected void configureTableViewer(TableViewer viewer) {
		throw new UnsupportedOperationException(
				"Why do you want a table viewer?");
	}

	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setUseHashlookup(true);
		viewer.setLabelProvider(new DecoratingLabelProvider(
				new BeansModelLabelProvider(), PlatformUI.getWorkbench()
						.getDecoratorManager().getLabelDecorator()));
		BeansSearchContentProvider provider = new BeansSearchContentProvider();
		viewer.setContentProvider(provider);
		this.viewer = viewer;
		this.provider = provider;
	}

	protected TableViewer createTableViewer(Composite parent) {
		TableViewer viewer = super.createTableViewer(parent);
		viewer.addDoubleClickListener(doubleClickListener);
		return viewer;
	}

	protected TreeViewer createTreeViewer(Composite parent) {
		TreeViewer viewer = super.createTreeViewer(parent);
		viewer.addDoubleClickListener(doubleClickListener);
		return viewer;
	}

	public String getLabel() {
		// TODO read from resource
		return "Bean Search Result";
	}

	protected void elementsChanged(Object[] objects) {
		if (provider != null) {
			provider.elementsChanged(objects);
		}
	}

	private synchronized void handleSearchResultsChanged(
			final SearchResultEvent e) {
		if (e instanceof MatchEvent) {
			MatchEvent me = (MatchEvent) e;
			// FIXME
			// postUpdate(me.getMatches());
		} else if (e instanceof RemoveAllEvent) {
			clear();
		}
		// FIXME
		// viewer.refresh();
	}

	public void setInput(ISearchResult search, Object viewState) {
		super.setInput(search, viewState);
		if (search != null) {
			search.addListener(resultListener);
		}
	}

	protected void clear() {
		// FIXME
		// provider.elementsChanged(new Object[] {});
	}
}
