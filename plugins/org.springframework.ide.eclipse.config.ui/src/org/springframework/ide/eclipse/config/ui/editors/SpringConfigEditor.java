/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditorInput;
import org.springframework.ide.eclipse.config.core.IConfigEditorPage;
import org.springframework.ide.eclipse.config.core.preferences.SpringConfigPreferenceConstants;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.config.ui.editors.namespaces.NamespacesFormPage;
import org.springframework.ide.eclipse.config.ui.editors.overview.OverviewFormPage;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Node;

/**
 * This class is a multi-page form editor for editing Spring configuration
 * files. Every {@code SpringConfigEditor} hosts one page for the XML source
 * file, one form page for adding & removing Spring namespaces, and an
 * additional form page for each namespace in the configuration. The editor also
 * hosts a graphical overview of the configuration if the source file has been
 * configured with the Spring nature.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
public class SpringConfigEditor extends AbstractConfigEditor implements IPropertyListener {

	public static final String ID_EDITOR = "com.springsource.sts.config.ui.editors.SpringConfigEditor"; //$NON-NLS-1$

	private NamespacesFormPage namespacesPage;

	private OverviewFormPage overviewPage;

	private SpringConfigGraphPage graphPage;

	private boolean requiresDeferredBeanGraph = false;

	public SpringConfigEditor() {
		super();
		addPropertyListener(this);
	}

	private void addBeanGraphPage(IBeansConfig config) {
		GraphEditorInput graphInput = null;
		try {
			graphInput = new GraphEditorInput(config.getElementID());
		}
		catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
					.getString("SpringConfigEditor.ERROR_CREATING_BEANS_GRAPH"), e)); //$NON-NLS-1$
		}

		if (graphInput != null) {
			try {
				graphPage = new SpringConfigGraphPage(this);
				int graphIndex = addPage(graphPage, graphInput);
				setPageText(graphIndex, Messages.getString("SpringConfigEditor.GRAPH_TAB")); //$NON-NLS-1$
			}
			catch (PartInitException e) {
				StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
						.getString("SpringConfigEditor.ERROR_CREATING_BEANS_GRAPH"), e)); //$NON-NLS-1$
			}
		}
	}

	private void createBeanGraphPage() {
		boolean gefEnabled = getPreferenceStore().getBoolean(SpringConfigPreferenceConstants.PREF_ENABLE_GEF_PAGES);
		if (gefEnabled) {
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigId.create(getResourceFile()));
			if (config != null) {
				addBeanGraphPage(config);
			}
			else {
				requiresDeferredBeanGraph = true;
			}
		}
	}

	protected void createDeferredBeanGraphPage() {
		boolean gefEnabled = getPreferenceStore().getBoolean(SpringConfigPreferenceConstants.PREF_ENABLE_GEF_PAGES);
		if (!gefEnabled) {
			requiresDeferredBeanGraph = false;
		}
		if (requiresDeferredBeanGraph) {
			final IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigId.create(getResourceFile()));
			if (config != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						addBeanGraphPage(config);
					}
				});
				requiresDeferredBeanGraph = false;
			}
		}
	}

	@Override
	protected void createPreNamespacePages() {
		namespacesPage = new NamespacesFormPage(this);
		overviewPage = new OverviewFormPage(this);
		addConfigEditorPage(namespacesPage, Messages.getString("SpringConfigEditor.NAMESPACES_TAB")); //$NON-NLS-1$
		addConfigEditorPage(overviewPage, Messages.getString("SpringConfigEditor.OVERVIEW_TAB")); //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		removePropertyListener(this);
		super.dispose();
	}

	public SpringConfigGraphPage getBeansGraphPage() {
		return graphPage;
	}

	public void propertyChanged(Object source, int propId) {
		if (propId == IEditorPart.PROP_DIRTY && graphPage != null) {
			graphPage.updateHeader();
		}
	}

	private void removeBeanGraphPage() {
		if (graphPage != null && pages.contains(graphPage)) {
			removePage(pages.indexOf(graphPage));
			graphPage = null;
		}
	}

	@Override
	public void revealElement(Node element) {
		String uri = element.getNamespaceURI();
		if (uri != null) {
			AbstractConfigFormPage page = getFormPageForUri(uri);
			if (page == null) {
				page = overviewPage;
			}
			setActivePage(page.getId());
			page.setSelection(new StructuredSelection(element));
		}
	}

	@Override
	protected void setActivePageFromPreference(Integer index, IConfigEditorPage activePage) {
		if (activePage == null && index != null && graphPage != null && index.intValue() >= pages.indexOf(graphPage)) {
			setActiveEditor(graphPage);
		}
		else {
			super.setActivePageFromPreference(index, activePage);
		}
	}

	@Override
	protected void updateContentOutline(int newPageIndex) {
		if (getContentOutline() != null) {
			IContentOutlinePage outline = null;
			Object activePage = pages.get(newPageIndex);
			if (activePage instanceof SpringConfigGraphPage) {
				outline = (IContentOutlinePage) graphPage.getAdapter(IContentOutlinePage.class);
			}
			if (outline != null) {
				getContentOutline().setActiveOutline(outline);
			}
			else {
				super.updateContentOutline(newPageIndex);
			}
		}
	}

	@Override
	protected void updateNamespacePages() {
		removeBeanGraphPage();
		super.updateNamespacePages();
		createBeanGraphPage();
	}

}
