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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;
import org.springframework.ide.eclipse.config.ui.actions.CollapseNodeAction;
import org.springframework.ide.eclipse.config.ui.actions.DeleteNodeAction;
import org.springframework.ide.eclipse.config.ui.actions.ExpandNodeAction;
import org.springframework.ide.eclipse.config.ui.actions.InsertNodeAction;
import org.springframework.ide.eclipse.config.ui.actions.LowerNodeAction;
import org.springframework.ide.eclipse.config.ui.actions.RaiseNodeAction;
import org.springframework.ide.eclipse.wizard.ui.BeanWizardDialog;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * This class is an extension to {@link AbstractConfigMasterPart} that is suited
 * to displaying a structured overview of elements in a Spring configuration
 * file belonging to a specific Spring namespace.
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 * @see AbstractConfigMasterPart
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractNamespaceMasterPart extends AbstractConfigMasterPart {

	private class SelectionChangedListener implements ISelectionChangedListener {

		public void selectionChanged(SelectionChangedEvent event) {
			if (upButton != null && downButton != null) {
				ISelection selection = treeViewer.getSelection();
				int count = treeViewer.getTree().getSelectionCount();
				if (selection.isEmpty() || count > 1) {
					upButton.setEnabled(false);
					downButton.setEnabled(false);
				}
				else {
					upButton.setEnabled(true);
					downButton.setEnabled(true);
				}
			}
		}

	}

	private TreeViewer treeViewer;

	private SelectionChangedListener selectionListener;

	private Button newBeanButton;

	private Button upButton;

	private Button downButton;

	private static String SCHEMA_URI_PREFIX = "http://www.springframework.org/schema/"; //$NON-NLS-1$

	private final SpringConfigContentAssistProcessor xmlProcessor;

	/**
	 * Constructs a master part with a reference to its container page and its
	 * parent composite.
	 * 
	 * @param page the hosting form page
	 * @param parent the parent composite
	 */
	public AbstractNamespaceMasterPart(AbstractConfigFormPage page, Composite parent) {
		super(page, parent);
		xmlProcessor = page.getXmlProcessor();
	}

	@Override
	protected void createButtons(Composite client) {
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 100;

		newBeanButton = toolkit.createButton(client,
				Messages.getString("AbstractNamespaceMasterPart.NEW_BEAN_BUTTON"), SWT.FLAT); //$NON-NLS-1$
		newBeanButton.setLayoutData(data);
		newBeanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IFile sourceFile = getConfigEditor().getResourceFile();
				Shell shell = getFormPage().getSite().getShell();
				if (shell != null && !shell.isDisposed()) {
					BeanWizardDialog dialog = BeanWizardDialog.createBeanWizardDialog(shell, sourceFile, false);
					dialog.create();
					dialog.setBlockOnOpen(true);
					if (dialog.open() == Window.OK) {
						IDOMElement element = dialog.getNewBean();
						getViewer().setSelection(new StructuredSelection(element));
					}
				}
			}
		});

		upButton = toolkit.createButton(client,
				Messages.getString("AbstractNamespaceMasterPart.MOVE_UP_BUTTON"), SWT.FLAT); //$NON-NLS-1$
		upButton.setLayoutData(data);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredTextViewer textView = getConfigEditor().getTextViewer();
				Action action = new RaiseNodeAction(treeViewer, xmlProcessor, textView);
				action.run();
			}
		});
		upButton.setEnabled(false);

		downButton = toolkit.createButton(client,
				Messages.getString("AbstractNamespaceMasterPart.MOVE_DOWN_BUTTON"), SWT.FLAT); //$NON-NLS-1$
		downButton.setLayoutData(data);
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredTextViewer textView = getConfigEditor().getTextViewer();
				Action action = new LowerNodeAction(treeViewer, xmlProcessor, textView);
				action.run();
			}
		});
		downButton.setEnabled(false);
	}

	/**
	 * This method is called automatically when a context menu is invoked on the
	 * master viewer when the viewer is empty. Clients may override to add new
	 * actions to the menu.
	 * 
	 * @param manager the menu manager on the master viewer
	 * @param doc document object model of the XML source file
	 */
	protected abstract void createEmptyDocumentActions(IMenuManager manager, IDOMDocument doc);

	/**
	 * This method is called automatically when a context menu is invoked on the
	 * master viewer, and adds new actions to create child nodes and delete the
	 * selected node. Clients may extend to add additional actions to the menu.
	 * 
	 * @param manager the menu manager on the master viewer
	 * @param parent the selected XML node
	 */
	protected void createNodeInsertActions(IMenuManager manager, IDOMElement parent) {
		StructuredTextViewer textView = getConfigEditor().getTextViewer();
		Map<String, List<String>> childMap = getChildNames(parent);
		if (childMap.keySet().size() > 1) {
			for (String prefix : childMap.keySet()) {
				MenuManager subManager;
				String label;
				if (prefix.trim().equals("")) { //$NON-NLS-1$
					String uri = ConfigCoreUtils.getDefaultNamespaceUri(getConfigEditor().getDomDocument());
					if (uri == null) {
						uri = NamespaceUtils.DEFAULT_NAMESPACE_URI;
					}
					int pos = uri.indexOf(SCHEMA_URI_PREFIX);
					if (pos > -1) {
						label = uri.substring(pos + SCHEMA_URI_PREFIX.length(), uri.length());
					}
					else {
						label = Messages.getString("AbstractNamespaceMasterPart.DEFAULT_NAMESPACE_SUBMENU"); //$NON-NLS-1$
					}
					subManager = new MenuManager(label);
				}
				else {
					subManager = new MenuManager(prefix);
				}
				for (String childName : childMap.get(prefix)) {
					subManager.add(new InsertNodeAction(treeViewer, xmlProcessor, textView, childName));
				}
				manager.add(subManager);
			}
		}
		else {
			for (String prefix : childMap.keySet()) {
				for (String childName : childMap.get(prefix)) {
					manager.add(new InsertNodeAction(treeViewer, xmlProcessor, textView, childName));
				}
			}
		}
	}

	@Override
	protected ColumnViewer createViewer(Composite client) {
		FilteredTree filter = new FilteredTree(client, SWT.MULTI | SWT.BORDER, new PatternFilter(), true);
		treeViewer = filter.getViewer();
		return treeViewer;
	}

	/**
	 * This method is called automatically when the master part is created. This
	 * implementation returns a {@link SpringConfigContentProvider}, but clients
	 * may override to return their own content provider.
	 * 
	 * @return content provider for the master viewer
	 */
	@Override
	protected SpringConfigContentProvider createViewerContentProvider() {
		return new SpringConfigContentProvider(getFormPage());
	}

	/**
	 * This method is called automatically when the master part is created. This
	 * implementation returns a {@link SpringConfigLabelProvider}, but clients
	 * may override to return their own label provider.
	 * 
	 * @return label provider for the master viewer
	 */
	@Override
	protected AbstractConfigLabelProvider createViewerLabelProvider() {
		return new SpringConfigLabelProvider();
	}

	@Override
	public void dispose() {
		if (treeViewer != null && selectionListener != null) {
			treeViewer.removeSelectionChangedListener(selectionListener);
		}
		super.dispose();
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
		StructuredTextViewer textView = getConfigEditor().getTextViewer();
		if (!selection.isEmpty()) {
			Object obj = selection.getFirstElement();
			if (obj != null && obj instanceof IDOMElement) {
				IDOMElement node = (IDOMElement) obj;
				createNodeInsertActions(manager, node);
				manager.add(new Separator());
				manager.add(new DeleteNodeAction(textView, node));
			}
		}
		else {
			IDOMDocument doc = getConfigEditor().getDomDocument();
			if (doc != null) {
				createEmptyDocumentActions(manager, doc);
			}
		}
	}

	/**
	 * Returns a map of namespace prefixes to node names that can be added as
	 * children to the given parent. Clients may override if necessary.
	 * 
	 * @see SpringConfigContentProvider#getChildNames(String)
	 * @param parent the parent element
	 * @return map of namespace prefixes to child names for the given parent
	 */
	protected Map<String, List<String>> getChildNames(IDOMElement parent) {
		SpringConfigContentAssistProcessor proc = getFormPage().getXmlProcessor();
		Node grandParent = parent.getParentNode();
		Map<String, List<String>> childMap = new TreeMap<String, List<String>>();
		if (proc != null) {
			List<String> list = proc.getChildNames(parent);
			String uri = getFormPage().getNamespaceUri();
			for (String name : list) {
				String prefix = ""; //$NON-NLS-1$
				List<String> names;
				int pos = name.indexOf(':');

				if (pos > -1) {
					prefix = name.substring(0, pos);
				}

				if (childMap.containsKey(prefix)) {
					names = childMap.get(prefix);
				}
				else {
					names = new ArrayList<String>();
				}

				if (grandParent instanceof Document && uri != null) {
					String prefixForUri = getFormPage().getPrefixForNamespaceUri();
					if (prefix.equals(prefixForUri) || isAdapterNamespacePrefix(prefix)) {
						names.add(name);
						childMap.put(prefix, names);
					}
				}
				else {
					names.add(name);
					childMap.put(prefix, names);
				}
			}
		}
		return childMap;
	}

	/**
	 * This method is called automatically when the master part is created. This
	 * implementation returns a generic description, but clients may override to
	 * return their own description.
	 * 
	 * @return master part description
	 */
	@Override
	protected String getSectionDescription() {
		return Messages.getString("AbstractNamespaceMasterPart.SECTION_DESCRIPTION"); //$NON-NLS-1$
	}

	private boolean isAdapterNamespacePrefix(String prefix) {
		if (prefix != null) {
			for (IConfigurationElement config : getFormPage().getAdapterDefinitions()) {
				String adapterPrefix = ConfigCoreUtils.getPrefixForNamespaceUri(getConfigEditor().getDomDocument(),
						config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI));
				if (adapterPrefix != null && adapterPrefix.equals(prefix)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void postCreateContents() {
		if (treeViewer != null) {
			selectionListener = new SelectionChangedListener();
			treeViewer.addSelectionChangedListener(selectionListener);
			ToolBarManager manager = getToolBarManager();
			manager.add(new CollapseNodeAction(treeViewer, xmlProcessor));
			manager.add(new ExpandNodeAction(treeViewer, xmlProcessor));
			treeViewer.expandToLevel(2);
		}
	}

	@Override
	public void refresh() {
		if (newBeanButton != null) {
			IDOMDocument document = getConfigEditor().getDomDocument();
			if (document == null || document.getDocumentElement() == null) {
				newBeanButton.setEnabled(false);
			}
			else {
				newBeanButton.setEnabled(true);
			}
		}
		super.refresh();
	}

}
