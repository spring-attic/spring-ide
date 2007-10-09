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
package org.springframework.ide.eclipse.aop.ui.matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.PartInitException;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.matcher.internal.PointcutMatcherContentProvider;
import org.springframework.ide.eclipse.aop.ui.matcher.internal.PointcutMatcherLabelProvider;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Text search result page that is used to display pointcut matches
 * @author Christian Dupuis
 * @since 2.0.2
 * @see PointcutMatcherSearchPage
 */
public class PointcutMatcherResultPage extends AbstractTextSearchViewPage {

	private PointcutMatcherContentProvider provider;

	public PointcutMatcherResultPage() {
		super();
		setID(PointcutMatcherResultPage.class.getName());
	}

	protected void clear() {
		provider.clear();
	}

	protected void configureTableViewer(TableViewer viewer) {
		viewer.setUseHashlookup(true);
		viewer.setLabelProvider(new PointcutMatcherLabelProvider(this));
		viewer.setContentProvider(getContentProvider());
	}
	
	protected void configureTreeViewer(TreeViewer viewer) {
		viewer.setUseHashlookup(true);
		viewer.setLabelProvider(new PointcutMatcherLabelProvider(this));
		viewer.setContentProvider(getContentProvider());
	}

	protected void elementsChanged(Object[] objects) {
		if (provider != null) {
			provider.elementsChanged(objects);
		}
	}

	private PointcutMatcherContentProvider getContentProvider() {
		if (this.provider == null) {
			this.provider = new PointcutMatcherContentProvider(this);
		}
		return this.provider;
	}

	protected void handleOpen(OpenEvent event) {
		Viewer viewer= event.getViewer();
		ISelection sel= event.getSelection();
		if (viewer instanceof TreeViewer && sel instanceof IStructuredSelection) {
			IStructuredSelection selection= (IStructuredSelection) sel;
			Object element = selection.getFirstElement();
			if (getDisplayedMatches(element).length != 0) {
				super.handleOpen(event);
			}
			else {
				showElement(true, element);
			}
		}
	}

	private void showElement(boolean activate, Object element) {
		if (element instanceof IAopReference) {
			SpringUIUtils.openInEditor(((IAopReference) element).getTarget());
		}
		else if (element instanceof IResourceModelElement) {
			BeansUIUtils.openInEditor((IResourceModelElement) element, activate);
		}
		else if (element instanceof IFile) {
			SpringUIUtils.openInEditor((IFile) element, 1, activate);
		}
	}
	
	@Override
	protected void showMatch(Match match, int currentOffset, int currentLength,
			boolean activate) throws PartInitException {
		Object element = match.getElement();
		showElement(activate, element);
	}
}
