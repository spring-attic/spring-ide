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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.config.core.ConfigCoreUtils;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.config.core.IConfigEditorPage;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.springframework.ide.eclipse.config.core.extensions.CommonActionsExtensionPointConstants;
import org.springframework.ide.eclipse.config.core.extensions.FormPagesExtensionPointConstants;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;
import org.springframework.ide.eclipse.config.ui.extensions.ConfigUiExtensionPointReader;


/**
 * The base class for all pages of an {@link AbstractConfigEditor} that plan on
 * making changes to and receiving changes from the underlying XML model in
 * real-time. This class implements the master/details UI pattern.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
public abstract class AbstractConfigFormPage extends FormPage implements IConfigEditorPage {

	public static String PAGE_KIND = "formPage"; //$NON-NLS-1$

	private AbstractConfigEditor cEditor;

	private final AbstractConfigMasterDetailsBlock block;

	private ScrolledForm form;

	private String pId;

	private String namespaceUri;

	private final Set<IConfigurationElement> adapterDefinitions = new HashSet<IConfigurationElement>();

	private final Set<IConfigurationElement> wizardDefinitions = new HashSet<IConfigurationElement>();

	/**
	 * Constructor for pages contributed through the
	 * <code>com.springsource.sts.config.ui.formPages</code> extension point.
	 */
	public AbstractConfigFormPage() {
		this("", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Constructor for pages contributed to the editor programmatically.
	 * 
	 * @see FormPage#FormPage(org.eclipse.ui.forms.editor.FormEditor, String,
	 * String)
	 * @param editor the parent editor
	 * @param id the unique identifier
	 * @param title the page title
	 */
	public AbstractConfigFormPage(AbstractConfigEditor editor, String id, String title) {
		this(id, title);
		initialize(editor, null);
	}

	private AbstractConfigFormPage(String id, String title) {
		super(id, title);
		block = createMasterDetailsBlock();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		form = managedForm.getForm();
		form.getForm().setSeparatorVisible(true);
		form.getForm().setText(getTitle());

		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		block.createContent(managedForm);
		getSite().setSelectionProvider(getMasterPart().getViewer());
		updateHeader();
	}

	/**
	 * This method is called when the form page is created. Clients must extend
	 * {@link AbstractConfigMasterDetailsBlock} or
	 * {@link AbstractNamespaceMasterDetailsBlock} and instantiate their class
	 * in this method.
	 * 
	 * @return master/details block for this page
	 */
	protected abstract AbstractConfigMasterDetailsBlock createMasterDetailsBlock();

	@Override
	public void doSaveAs() {
		setInput(cEditor.getEditorInput());
		AbstractConfigMasterPart master = getMasterPart();
		if (master != null) {
			master.getViewer().setInput(cEditor.getDomDocument());
		}
		modelUpdated();
	}

	protected Set<IConfigurationElement> getAdapterDefinitions() {
		return adapterDefinitions;
	}

	/**
	 * Returns the the parent editor as an {@link AbstractConfigEditor} object.
	 * 
	 * @see FormPage#getEditor()
	 * @return parent editor instance
	 */
	@Override
	public AbstractConfigEditor getEditor() {
		return cEditor;
	}

	@Override
	public String getId() {
		if (pId != null) {
			return pId;
		}
		return super.getId();
	}

	/**
	 * Returns the master/details block for this page.
	 * 
	 * @return master/details block for this page
	 */
	public AbstractConfigMasterDetailsBlock getMasterDetailsBlock() {
		return block;
	}

	/**
	 * Returns the section part hosting the master content.
	 * 
	 * @return master section part
	 */
	public AbstractConfigMasterPart getMasterPart() {
		if (block != null) {
			return block.getMasterPart();
		}
		return null;
	}

	/**
	 * Returns the namespace URI associated with this page. May be null.
	 * 
	 * @return namespace URI associated with the page
	 */
	public String getNamespaceUri() {
		return namespaceUri;
	}

	public String getPageKind() {
		return PAGE_KIND;
	}

	/**
	 * Returns the namespace prefix used in the XML elements of interest to this
	 * page.
	 * 
	 * @return namespace prefix of relevant XML elements
	 */
	public String getPrefixForNamespaceUri() {
		return ConfigCoreUtils.getPrefixForNamespaceUri(cEditor.getDomDocument(), namespaceUri);
	}

	public Version getSchemaVersion() {
		return ConfigCoreUtils.getSchemaVersion(cEditor.getDomDocument(), namespaceUri);
	}

	public ScrolledForm getScrolledForm() {
		return form;
	}

	protected Set<IConfigurationElement> getWizardDefinitions() {
		return wizardDefinitions;
	}

	/**
	 * Returns a content assist processor for the XML source file.
	 * 
	 * @return content assist processor for the XML source file
	 */
	public SpringConfigContentAssistProcessor getXmlProcessor() {
		return getEditor().getXmlProcessor();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(new MultiPageEditorSite(getEditor(), this), input);
	}

	private void initAdapterDefinitions() {
		Set<IConfigurationElement> allAdapters = ConfigUiExtensionPointReader.getAdapterDefinitions();
		for (IConfigurationElement config : allAdapters) {
			String parent = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_PARENT_URI);
			String uri = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI);
			if (parent != null && parent.equals(namespaceUri) && uri != null) {
				adapterDefinitions.add(config);
			}
		}
	}

	/**
	 * Initialize the page with the parent editor as an
	 * {@link AbstractConfigEditor} instance, and with a namespace URI.
	 * 
	 * @see FormPage#initialize(org.eclipse.ui.forms.editor.FormEditor)
	 * @param editor the parent editor
	 * @param uri the namespace URI associated with the page
	 */
	public void initialize(IConfigEditor editor, String uri) {
		this.cEditor = (AbstractConfigEditor) editor;
		this.namespaceUri = uri;
		initAdapterDefinitions();
		initWizardDefinitions();
		super.initialize(this.cEditor);
	}

	private void initWizardDefinitions() {
		Set<IConfigurationElement> allWizards = ConfigUiExtensionPointReader.getWizardDefinitions();
		for (IConfigurationElement config : allWizards) {
			String uri = config.getAttribute(CommonActionsExtensionPointConstants.ATTR_NAMESPACE_URI);
			if (uri != null && uri.equals(namespaceUri)) {
				wizardDefinitions.add(config);
			}
		}
	}

	public void modelUpdated() {
		if (isActive()) {
			AbstractConfigMasterPart master = getMasterPart();
			if (master != null) {
				master.markStale();
			}
		}
	}

	public void namespacesUpdated() {
		// no-op
	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		if (active) {
			modelUpdated();
			updateHeader();
		}
	}

	@Override
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		if (cfig != null) {
			String id = cfig.getAttribute(FormPagesExtensionPointConstants.ATTR_ID);
			if (id != null && id.trim().length() > 0) {
				this.pId = id;
			}
		}
		super.setInitializationData(cfig, propertyName, data);
	}

	/**
	 * Set the selection on the master part's viewer.
	 * 
	 * @param selection the item to select
	 */
	public void setSelection(ISelection selection) {
		getSite().getSelectionProvider().setSelection(selection);
	}

	public void updateHeader() {
		cEditor.getHeaderMessage().setMessage(this);
	}
}
