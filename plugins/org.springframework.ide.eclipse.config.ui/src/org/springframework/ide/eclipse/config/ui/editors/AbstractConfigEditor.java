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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.provisional.contenttype.ContentTypeIdForXML;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.mylyn.ui.IBeansXmlEditor;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.config.core.IConfigEditorPage;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.springframework.ide.eclipse.config.core.extensions.FormPagesExtensionPointConstants;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;
import org.springframework.ide.eclipse.config.core.preferences.SpringConfigPreferenceConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.config.ui.extensions.ConfigUiExtensionPointReader;
import org.springframework.ide.eclipse.config.ui.properties.AbstractConfigPropertySection;
import org.springframework.ide.eclipse.config.ui.properties.SpringConfigPropertySheetPage;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.UiStatusHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @since 2.3.4
 */
@SuppressWarnings("restriction")
public abstract class AbstractConfigEditor extends FormEditor implements IBeansXmlEditor, IConfigEditor, IGotoMarker,
		IModelChangeListener, IPropertyChangeListener, ITabbedPropertySheetPageContributor, ITextListener {

	private class ResourceChangeListener implements IResourceChangeListener, IResourceDeltaVisitor {

		public void resourceChanged(final IResourceChangeEvent event) {
			if (event.getType() == IResourceChangeEvent.PRE_CLOSE || event.getType() == IResourceChangeEvent.PRE_DELETE) {
				IProject project = null;
				if (event.getSource() instanceof IWorkspace) {
					project = (IProject) event.getResource();
				}
				else if (event.getSource() instanceof IProject) {
					project = (IProject) event.getSource();
				}

				if (project != null) {
					IResource file = project.findMember(getResourceFile().getFullPath());
					if (file != null) {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								getSite().getPage().closeEditor(AbstractConfigEditor.this, true);
							}
						});
					}
				}
			}
			else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				IResourceDelta delta = event.getDelta();
				try {
					if (delta != null) {
						delta.accept(this);
					}
				}
				catch (CoreException e) {
					StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
							.getString("SpringConfigEditor.ERROR_PROCESSING_RESOURCE_CHANGE"), e)); //$NON-NLS-1$
				}
			}
		}

		public boolean visit(IResourceDelta root) throws CoreException {
			if (root != null && getResourceFile() != null) {
				final IResourceDelta delta = root.findMember(getResourceFile().getFullPath());
				if (delta != null && delta.getKind() == IResourceDelta.REMOVED) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							getSite().getPage().closeEditor(AbstractConfigEditor.this, true);
						}
					});
				}
			}
			return false;
		}

	}

	private static String KEY_ACTIVE_PAGE = "activePage"; //$NON-NLS-1$

	private static String KEY_PAGE_ID = "id"; //$NON-NLS-1$

	private static String KEY_PAGE_URI = "uri"; //$NON-NLS-1$

	private static String KEY_PAGE_KIND = "kind"; //$NON-NLS-1$

	private static String KEY_PAGE_INDEX = "index"; //$NON-NLS-1$

	/** The text editor used by the source page. */
	private StructuredTextEditor sourceEditor;

	private final SpringConfigContentAssistProcessor xmlProcessor;

	private final SpringConfigContentOutline contentOutline;

	private final SpringConfigHeaderMessage headerMessage;

	private Set<String> selectedNamespaceUris;

	private IFileEditorInput fileInput;

	private IFile resource;

	private final ResourceChangeListener resourceListener;

	private IDOMModel model;

	private IDOMDocument domDocument;

	private final IPreferenceStore prefStore;

	private String activePageKey;

	private final Set<AbstractConfigPropertySection> propertiesPages;

	private boolean isGraphDemo = false;

	public AbstractConfigEditor() {
		propertiesPages = new HashSet<AbstractConfigPropertySection>();
		xmlProcessor = new SpringConfigContentAssistProcessor();
		headerMessage = new SpringConfigHeaderMessage(this);
		contentOutline = new SpringConfigContentOutline(this);
		resourceListener = new ResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceListener);
		BeansCorePlugin.getModel().addChangeListener(this);
		prefStore = ConfigUiPlugin.getDefault().getPreferenceStore();
		prefStore.addPropertyChangeListener(this);
		enableGraphDemo();
	}

	protected void addConfigEditorPage(IConfigEditorPage page, String pageText) {
		try {
			boolean added = false;
			Iterator iter = pages.iterator();

			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof IConfigEditorPage) {
					int i = pages.indexOf(obj);
					if (pageText.compareTo(getPageText(i)) < 0) {
						if (page instanceof IFormPage) {
							addPage(i, (IFormPage) page);
						}
						else {
							addPage(i, page, getEditorInput());
						}
						setPageText(i, pageText);
						added = true;
						break;
					}
				}
			}

			if (!added) {
				int i;
				if (page instanceof IFormPage) {
					i = addPage((IFormPage) page);
				}
				else {
					i = addPage(page, getEditorInput());
				}
				setPageText(i, pageText);
			}
		}
		catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
					.getString("SpringConfigEditor.ERROR_CREATING_EDITOR_PAGE"), e)); //$NON-NLS-1$
		}
	}

	@Override
	protected void addPages() {
		createSourcePage();
		createPreNamespacePages();
		createNamespacePages();
		loadActivePage();
	}

	public void addPropertiesPage(AbstractConfigPropertySection propertiesPage) {
		propertiesPages.add(propertiesPage);
	}

	public boolean containsNamespaceUri(String uri) {
		return selectedNamespaceUris.contains(uri);
	}

	private IConfigEditorPage createConfigEditorPage(IConfigurationElement definition, String property) {
		try {
			Object obj = definition.createExecutableExtension(property);
			if (obj instanceof IConfigEditorPage) {
				IConfigEditorPage page = (IConfigEditorPage) obj;
				page.initialize(this, definition.getAttribute(FormPagesExtensionPointConstants.ATTR_NAMESPACE_URI));
				return page;
			}
		}
		catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
					.getString("SpringConfigEditor.ERROR_INITIALIZING_EDITOR_PAGE"), e)); //$NON-NLS-1$
		}
		return null;
	}

	private void createNamespacePages() {
		selectedNamespaceUris = readNamespacesFromFile();
		updateNamespacePages();
	}

	protected abstract void createPreNamespacePages();

	@Override
	protected IEditorSite createSite(IEditorPart page) {
		IEditorSite site = null;
		if (page == sourceEditor) {
			site = new MultiPageEditorSite(this, page) {
				@Override
				public IEditorActionBarContributor getActionBarContributor() {
					IEditorActionBarContributor contributor = super.getActionBarContributor();
					IEditorActionBarContributor multiContributor = AbstractConfigEditor.this.getEditorSite()
							.getActionBarContributor();
					if (multiContributor instanceof SpringConfigEditorContributor) {
						contributor = ((SpringConfigEditorContributor) multiContributor).sourceViewerActionContributor;
					}
					return contributor;
				}

				@Override
				public String getId() {
					// Sets this ID so nested editor is configured for XML
					// source
					return ContentTypeIdForXML.ContentTypeID_XML + ".source"; //$NON-NLS-1$
				}
			};
		}
		else {
			site = super.createSite(page);
		}
		return site;
	}

	/**
	 * Creates the first page of the multi-page editor, which contains a text
	 * editor.
	 */
	private void createSourcePage() {
		try {
			sourceEditor = new StructuredTextEditor() {
				@Override
				public void selectAndReveal(int start, int length) {
					setActiveEditor(sourceEditor);
					super.selectAndReveal(start, length);
				}
			};

			int index = addPage(sourceEditor, getEditorInput());
			setPageText(index, Messages.getString("SpringConfigEditor.SOURCE_TAB")); //$NON-NLS-1$
			setPartName(sourceEditor.getTitle());
			sourceEditor.setEditorPart(this);
			sourceEditor.getTextViewer().addTextListener(this);
		}
		catch (PartInitException e) {
			UiStatusHandler.logAndDisplay(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
					.getString("SpringConfigEditor.ERROR_CREATING_SOURCE_PAGE"), e)); //$NON-NLS-1$
		}
	}

	/**
	 * The <code>MultiPageEditorPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	@Override
	public void dispose() {
		xmlProcessor.release();
		sourceEditor.getTextViewer().removeTextListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceListener);
		BeansCorePlugin.getModel().removeChangeListener(this);
		prefStore.removePropertyChangeListener(this);
		releaseModel();
		super.dispose();
	}

	/**
	 * Saves the multi-page editor's document.
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		sourceEditor.doSave(monitor);
		for (Object obj : pages) {
			if (obj instanceof IConfigEditorPage) {
				IConfigEditorPage page = (IConfigEditorPage) obj;
				page.doSave(monitor);
			}
		}
	}

	/**
	 * Saves the multi-page editor's document as another file. Also updates the
	 * text for page 0's tab, and updates this multi-page editor's input to
	 * correspond to the nested editor's.
	 */
	@Override
	public void doSaveAs() {
		releaseModel();
		sourceEditor.doSaveAs();
		setPartName(sourceEditor.getTitle());
		setInput(sourceEditor.getEditorInput());
		for (Object obj : pages) {
			if (obj instanceof IConfigEditorPage) {
				IConfigEditorPage page = (IConfigEditorPage) obj;
				page.doSaveAs();
			}
		}
	}

	public void elementChanged(ModelChangeEvent event) {
		Object source = event.getSource();
		if (source instanceof BeansConfig) {
			IResource resource = ((BeansConfig) source).getElementResource();
			if (resource != null && resource.equals(this.resource)) {
				headerMessage.updateMessage();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (pages != null) {
							for (Object obj : pages) {
								if (obj instanceof AbstractConfigFormPage) {
									AbstractConfigFormPage page = (AbstractConfigFormPage) obj;
									page.updateHeader();
								}
							}
						}
					}
				});
			}
		}
	}

	private void enableGraphDemo() {
		List<String> commandLineArgs = Arrays.asList(Platform.getCommandLineArgs());
		if (commandLineArgs.contains("-graphdemo")) { //$NON-NLS-1$
			isGraphDemo = true;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (IPropertySheetPage.class == adapter) {
			return new SpringConfigPropertySheetPage(this);
		}
		if (IContentOutlinePage.class == adapter) {
			return contentOutline;
		}
		if (ITextEditor.class == adapter) {
			return sourceEditor;
		}

		Object result = super.getAdapter(adapter);
		if (result == null && sourceEditor != null) {
			result = sourceEditor.getAdapter(adapter);
		}
		return result;
	}

	public Set<IConfigurationElement> getAdapterDefinitions() {
		return ConfigUiExtensionPointReader.getAdapterDefinitions();
	}

	public SpringConfigContentOutline getContentOutline() {
		return contentOutline;
	}

	public String getContributorId() {
		return "com.springsource.sts.config.ui.editors.AbstractConfigEditor"; //$NON-NLS-1$
	}

	/**
	 * Returns the {@link IDOMDocument} representation of the XML source file.
	 * 
	 * @return document object model of the XML source file
	 */
	public IDOMDocument getDomDocument() {
		return domDocument;
	}

	/**
	 * Returns the {@link AbstractConfigFormPage} with the given id, or null if
	 * no such page exists.
	 * 
	 * @param id the id of the form page to search for
	 * @return the form page with the given id
	 */
	public AbstractConfigFormPage getFormPage(String id) {
		if (id != null && !id.trim().equals("")) { //$NON-NLS-1$
			for (Object obj : pages) {
				if (obj instanceof AbstractConfigFormPage) {
					AbstractConfigFormPage page = (AbstractConfigFormPage) obj;
					if (id.equals(page.getId())) {
						return page;
					}
				}
			}
		}
		return null;
	}

	public AbstractConfigFormPage getFormPageForAdapterUri(String uri) {
		AbstractConfigFormPage page = getFormPageForUri(uri);
		if (page != null) {
			return page;
		}
		if (uri != null && !uri.trim().equals("")) { //$NON-NLS-1$
			for (Object obj : pages) {
				if (obj instanceof AbstractConfigFormPage) {
					page = (AbstractConfigFormPage) obj;
					for (IConfigurationElement config : page.getAdapterDefinitions()) {
						if (uri.equals(config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI))) {
							return page;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link AbstractConfigFormPage} for the given URI, or null if
	 * no such page exists.
	 * 
	 * @param uri the URI of the form page to search for
	 * @return the form page for the given URI
	 */
	public AbstractConfigFormPage getFormPageForUri(String uri) {
		if (uri != null && !uri.trim().equals("")) { //$NON-NLS-1$
			for (Object obj : pages) {
				if (obj instanceof AbstractConfigFormPage) {
					AbstractConfigFormPage page = (AbstractConfigFormPage) obj;
					if (uri.equals(page.getNamespaceUri())) {
						return page;
					}

				}
			}
		}
		return null;
	}

	public AbstractConfigGraphicalEditor getGraphicalEditorForAdapterUri(String uri) {
		AbstractConfigGraphicalEditor page = getGraphicalEditorForUri(uri);
		if (page != null) {
			return page;
		}
		if (uri != null && !uri.trim().equals("")) { //$NON-NLS-1$
			for (Object obj : pages) {
				if (obj instanceof AbstractConfigGraphicalEditor) {
					page = (AbstractConfigGraphicalEditor) obj;
					for (IConfigurationElement config : page.getAdapterDefinitions()) {
						if (uri.equals(config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI))) {
							return page;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link AbstractConfigGraphicalEditor} for the given URI, or
	 * null if no such page exists.
	 * 
	 * @param uri the URI of the graphical editor to search for
	 * @return the graphical editor for the given URI
	 */
	public AbstractConfigGraphicalEditor getGraphicalEditorForUri(String uri) {
		if (uri != null && !uri.trim().equals("")) { //$NON-NLS-1$
			for (Object obj : pages) {
				if (obj instanceof AbstractConfigGraphicalEditor) {
					AbstractConfigGraphicalEditor page = (AbstractConfigGraphicalEditor) obj;
					if (uri.equals(page.getNamespaceUri())) {
						return page;
					}
					for (IConfigurationElement config : page.getAdapterDefinitions()) {
						if (uri.equals(config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI))) {
							return page;
						}
					}
				}
			}
		}
		return null;
	}

	public SpringConfigHeaderMessage getHeaderMessage() {
		return headerMessage;
	}

	public Set<IConfigurationElement> getPageDefinitions() {
		return ConfigUiExtensionPointReader.getPageDefinitions();
	}

	public IPreferenceStore getPreferenceStore() {
		return prefStore;
	}

	/**
	 * Returns the {@link IFile} of the XML source.
	 * 
	 * @return resource file of the XML source
	 */
	public IFile getResourceFile() {
		return resource;
	}

	/**
	 * Returns the page for the XML source editor.
	 * 
	 * @return XML source editor page
	 */
	public StructuredTextEditor getSourcePage() {
		return sourceEditor;
	}

	/**
	 * Returns the text viewer for the XML source editor.
	 * 
	 * @return XML source textviewer
	 */
	public StructuredTextViewer getTextViewer() {
		return sourceEditor.getTextViewer();
	}

	public Set<IConfigurationElement> getWizardDefinitions() {
		return ConfigUiExtensionPointReader.getWizardDefinitions();
	}

	/**
	 * Returns a content assist processor for the XML source file.
	 * 
	 * @return content assist processor for the XML source file
	 */
	public SpringConfigContentAssistProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	public void gotoMarker(IMarker marker) {
		setActiveEditor(sourceEditor);
		IDE.gotoMarker(sourceEditor, marker);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput)) {
			throw new PartInitException(Messages.getString("SpringConfigEditor.EDITOR_INPUT_ERROR")); //$NON-NLS-1$
		}
		super.init(site, editorInput);
	}

	private boolean isNamespaceChange() {
		Set<String> namespaceUris = selectedNamespaceUris;
		selectedNamespaceUris = readNamespacesFromFile();
		return !namespaceUris.equals(selectedNamespaceUris);
	}

	@Override
	public boolean isSaveAsAllowed() {
		return (sourceEditor != null && sourceEditor.isSaveAsAllowed());
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		if (sourceEditor != null) {
			return sourceEditor.isSaveOnCloseNeeded();
		}
		return super.isSaveOnCloseNeeded();
	}

	private void loadActivePage() {
		// set the active editor in the action bar contributor first
		// before setActivePage calls action bar contributor's
		// setActivePage (https://bugs.eclipse.org/bugs/show_bug.cgi?id=141013 -
		// remove when https://bugs.eclipse.org/bugs/show_bug.cgi?id=151488 is
		// fixed)
		IEditorActionBarContributor contributor = getEditorSite().getActionBarContributor();
		if (contributor != null && contributor instanceof SpringConfigEditorContributor) {
			((SpringConfigEditorContributor) contributor).setActiveEditor(this);
		}
		loadActivePagePreference();
	}

	private void loadActivePagePreference() {
		IConfigEditorPage activePage = null;
		Integer index = null;
		String xml = prefStore.getString(activePageKey);

		if (xml != null && xml.length() > 0) {
			try {
				IMemento memento = XMLMemento.createReadRoot(new StringReader(xml));
				String id = memento.getString(KEY_PAGE_ID);
				String uri = memento.getString(KEY_PAGE_URI);
				String kind = memento.getString(KEY_PAGE_KIND);
				index = memento.getInteger(KEY_PAGE_INDEX);

				if (uri != null) {
					if (AbstractConfigFormPage.PAGE_KIND.equals(kind)) {
						activePage = getFormPageForUri(uri);
					}
					else if (AbstractConfigGraphicalEditor.PAGE_KIND.equals(kind)) {
						activePage = getGraphicalEditorForUri(uri);
					}
				}

				if (activePage == null && id != null) {
					activePage = getFormPage(id);
				}
			}
			catch (WorkbenchException e) {
				StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
						.getString("SpringConfigEditor.ERROR_LOADING_ACTIVE_PAGE"), e)); //$NON-NLS-1$
				setActiveEditor(sourceEditor);
			}
		}

		setActivePageFromPreference(index, activePage);
	}

	private void notifyModelChanged(boolean updateNamespaces) {
		for (Object obj : pages) {
			if (obj instanceof IConfigEditorPage) {
				IConfigEditorPage page = (IConfigEditorPage) obj;
				page.modelUpdated();
				if (updateNamespaces) {
					page.namespacesUpdated();
				}
			}
		}
		for (AbstractConfigPropertySection propertiesPage : propertiesPages) {
			propertiesPage.refresh();
		}
	}

	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		saveActivePagePreference(newPageIndex);
		updateContentOutline(newPageIndex);
	}

	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (SpringConfigPreferenceConstants.PREF_ENABLE_GEF_PAGES.equals(property)) {
			updateNamespacePages();
		}
	}

	private Set<String> readNamespacesFromFile() {
		Set<String> namespaceUris = new HashSet<String>();
		if (domDocument != null) {
			Element rootElement = domDocument.getDocumentElement();
			if (rootElement != null) {
				NamedNodeMap beanAttributes = rootElement.getAttributes();
				for (int i = 0; i < beanAttributes.getLength(); i++) {
					Node currAttr = beanAttributes.item(i);
					String currAttrName = currAttr.getNodeName().trim();
					String currAttrValue = currAttr.getNodeValue().trim();
					if (currAttrName.startsWith(ConfigCoreUtils.ATTR_DEFAULT_NAMESPACE)) {
						namespaceUris.add(currAttrValue);
					}
				}
			}
			List<String> schemaInfo = ConfigCoreUtils.parseSchemaLocationAttr(domDocument);
			if (schemaInfo != null) {
				Iterator<String> iterator = schemaInfo.iterator();
				while (iterator.hasNext()) {
					String currSchema = iterator.next();
					if (!namespaceUris.contains(currSchema)) {
						namespaceUris.add(currSchema);
					}
					if (iterator.hasNext()) {
						iterator.next();
					}
				}
			}
		}
		return namespaceUris;
	}

	private void releaseModel() {
		if (model != null) {
			model.releaseFromEdit();
			model = null;
		}
	}

	private void removeFormPage(String uri) {
		AbstractConfigFormPage page = getFormPageForUri(uri);
		if (page != null) {
			removePage(pages.indexOf(page));
		}
	}

	private void removeGraphicalEditor(String uri) {
		AbstractConfigGraphicalEditor gEditor = getGraphicalEditorForUri(uri);
		if (gEditor != null) {
			removePage(pages.indexOf(gEditor));
		}
	}

	public void removePropertiesPage(AbstractConfigPropertySection propertiesPage) {
		propertiesPages.remove(propertiesPage);
	}

	private void resolveModel() {
		resource = fileInput.getFile();
		IStructuredModel structModel = StructuredModelManager.getModelManager().getExistingModelForEdit(resource);
		if (structModel == null) {
			try {
				structModel = StructuredModelManager.getModelManager().getModelForEdit(resource);
			}
			catch (IOException e) {
				StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
						.getString("SpringConfigEditor.ERROR_LOADING_EDITOR_MODEL"), e)); //$NON-NLS-1$
			}
			catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
						.getString("SpringConfigEditor.ERROR_LOADING_EDITOR_MODEL"), e)); //$NON-NLS-1$
			}
		}

		if (structModel != null) {
			if (structModel instanceof IDOMModel) {
				releaseModel();
				model = (IDOMModel) structModel;
				domDocument = model.getDocument();
			}
			else {
				structModel.releaseFromEdit();
			}
		}
		headerMessage.updateMessage();
	}

	public void revealElement(Node element) {
		String uri = element.getNamespaceURI();
		if (uri != null) {
			AbstractConfigFormPage page = getFormPageForUri(uri);
			if (page != null) {
				setActivePage(page.getId());
				page.setSelection(new StructuredSelection(element));
			}
		}
	}

	private void saveActivePagePreference(int newPageIndex) {
		Object activePage = pages.get(newPageIndex);
		String xml = null;
		XMLMemento memento = XMLMemento.createWriteRoot(KEY_ACTIVE_PAGE);

		if (activePage instanceof IFormPage) {
			IFormPage formPage = (IFormPage) activePage;
			memento.putString(KEY_PAGE_ID, formPage.getId());
		}

		if (activePage instanceof IConfigEditorPage) {
			IConfigEditorPage configPage = (IConfigEditorPage) activePage;
			memento.putString(KEY_PAGE_URI, configPage.getNamespaceUri());
			memento.putString(KEY_PAGE_KIND, configPage.getPageKind());
		}
		memento.putInteger(KEY_PAGE_INDEX, newPageIndex);

		try {
			StringWriter writer = new StringWriter();
			memento.save(writer);
			xml = writer.getBuffer().toString();
			prefStore.setValue(activePageKey, xml);
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
					.getString("SpringConfigEditor.ERROR_SAVING_ACTIVE_PAGE"))); //$NON-NLS-1$
		}
	}

	protected void setActivePageFromPreference(Integer index, IConfigEditorPage activePage) {
		if (activePage != null) {
			setActivePage(pages.indexOf(activePage));
		}
		else {
			setActiveEditor(sourceEditor);
		}
	}

	@Override
	protected void setInput(IEditorInput input) {
		super.setInput(input);
		setInputHelper(input);
	}

	private void setInputHelper(IEditorInput input) {
		fileInput = (IFileEditorInput) input;
		resolveModel();
		activePageKey = SpringConfigPreferenceConstants.PREF_ACTIVE_PAGE_INDEX
				+ ":" + resource.getFullPath().toString(); //$NON-NLS-1$
	}

	@Override
	protected void setInputWithNotify(IEditorInput input) {
		super.setInputWithNotify(input);
		setInputHelper(input);
	}

	public void textChanged(TextEvent event) {
		if ((event.getText() != null || event.getReplacedText() != null) && event.getDocumentEvent() != null
				&& event.getDocumentEvent().getText() != null) {
			boolean updateNamespaces = isNamespaceChange();
			if (updateNamespaces) {
				updateNamespacePages();
			}
			notifyModelChanged(updateNamespaces);
		}
	}

	protected void updateContentOutline(int newPageIndex) {
		if (contentOutline != null) {
			IContentOutlinePage outline = null;
			Object activePage = pages.get(newPageIndex);
			if (activePage instanceof AbstractConfigGraphicalEditor) {
				outline = (IContentOutlinePage) ((AbstractConfigGraphicalEditor) activePage)
						.getAdapter(IContentOutlinePage.class);
			}
			else {
				outline = (IContentOutlinePage) sourceEditor.getAdapter(IContentOutlinePage.class);
			}
			if (outline != null) {
				contentOutline.setActiveOutline(outline);
			}
		}
	}

	protected void updateNamespacePages() {
		boolean gefEnabled = prefStore.getBoolean(SpringConfigPreferenceConstants.PREF_ENABLE_GEF_PAGES);
		Set<IConfigurationElement> pageDefinitions = ConfigUiExtensionPointReader.getPageDefinitions();
		for (IConfigurationElement def : pageDefinitions) {
			String uri = def.getAttribute(FormPagesExtensionPointConstants.ATTR_NAMESPACE_URI);
			if (containsNamespaceUri(uri)) {
				String prefix = def.getAttribute(FormPagesExtensionPointConstants.ATTR_NAMESPACE_PREFIX);
				if (getFormPageForUri(uri) == null) {
					IConfigEditorPage page = createConfigEditorPage(def, FormPagesExtensionPointConstants.ATTR_CLASS);
					addConfigEditorPage(page, prefix);
				}

				if (gefEnabled) {
					if (getGraphicalEditorForUri(uri) == null) {
						String graph = def.getAttribute(FormPagesExtensionPointConstants.ATTR_GRAPH);
						if (graph != null && graph.trim().length() > 0) {
							String graphPrefix = prefix.concat("-graph"); //$NON-NLS-1$
							IConfigEditorPage gEditor = createConfigEditorPage(def,
									FormPagesExtensionPointConstants.ATTR_GRAPH);
							addConfigEditorPage(gEditor, graphPrefix);
						}
					}
				}
				else {
					removeGraphicalEditor(uri);
				}
			}
			else {
				removeFormPage(uri);
				removeGraphicalEditor(uri);
			}
		}
	}

}
