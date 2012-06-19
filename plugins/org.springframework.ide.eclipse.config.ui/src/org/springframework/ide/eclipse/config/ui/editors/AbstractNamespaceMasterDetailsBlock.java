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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.extensions.CommonActionsExtensionPointConstants;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.config.ui.wizards.AbstractConfigWizard;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * This class is an extension to {@link AbstractConfigMasterDetailsBlock} that
 * is suited to displaying a master/details representation of elements in a
 * Spring configuration file belonging to a specific Spring namespace.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.5.0
 */
@SuppressWarnings("restriction")
public abstract class AbstractNamespaceMasterDetailsBlock extends AbstractConfigMasterDetailsBlock {

	/**
	 * Constructor for details factory contributed through the
	 * <code>com.springsource.sts.config.ui.pageAdapters</code> extension point.
	 */
	public AbstractNamespaceMasterDetailsBlock() {
		this(null);
	}

	public AbstractNamespaceMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected void createMasterPart(IManagedForm managedForm, Composite parent) {
		super.createMasterPart(managedForm, parent);
		Composite container = getMasterPart().getSection().getParent();
		SectionPart templatePart = createTemplateSectionPart(container, managedForm.getToolkit(), Section.TITLE_BAR);
		if (templatePart != null) {
			managedForm.addPart(templatePart);
		}
	}

	private SectionPart createTemplateSectionPart(Composite parent, FormToolkit toolkit, int style) {
		if (getFormPage().getWizardDefinitions().size() > 0) {
			TableWrapLayout wrapperLayout = new TableWrapLayout();
			wrapperLayout.topMargin = 0;
			wrapperLayout.bottomMargin = 0;
			wrapperLayout.leftMargin = 0;
			wrapperLayout.rightMargin = 0;
			wrapperLayout.horizontalSpacing = 0;
			wrapperLayout.verticalSpacing = 0;

			Composite wrapper = toolkit.createComposite(parent);
			wrapper.setLayout(wrapperLayout);
			wrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

			// We wrap the section part inside an empty composite so that we can
			// apply TableWrapData to the section part and constrain it's
			// maximum size. Applying the TableWrapData directly to the section
			// part would otherwise cause a ClassCastException.

			SectionPart sectionPart = new SectionPart(wrapper, toolkit, style);
			Section templateSection = sectionPart.getSection();
			templateSection
					.setText(Messages.getString("AbstractConfigMasterDetailsBlock.COMMON_ACTIONS_SECTION_TITLE")); //$NON-NLS-1$

			ScrolledComposite scroll = new ScrolledComposite(templateSection, SWT.V_SCROLL);
			GC gc = new GC(scroll);
			FontMetrics fm = gc.getFontMetrics();
			int height = 10 * fm.getHeight();
			gc.dispose();

			TableWrapData data = new TableWrapData(TableWrapData.FILL_GRAB);
			data.maxHeight = scroll.computeSize(SWT.DEFAULT, height).y;
			templateSection.setLayout(new TableWrapLayout());
			templateSection.setLayoutData(data);

			TableWrapLayout layout = new TableWrapLayout();
			layout.leftMargin = 0;
			layout.rightMargin = 0;

			Composite templateClient = toolkit.createComposite(scroll);
			templateClient.setLayout(layout);
			templateClient.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			scroll.setContent(templateClient);
			templateSection.setClient(scroll);
			toolkit.adapt(scroll);

			for (final IConfigurationElement def : getFormPage().getWizardDefinitions()) {
				Hyperlink wizardLink = toolkit.createHyperlink(templateClient,
						def.getAttribute(CommonActionsExtensionPointConstants.ATTR_DESCRIPTION), SWT.WRAP);
				wizardLink.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						try {
							AbstractConfigWizard wizard = (AbstractConfigWizard) def
									.createExecutableExtension(CommonActionsExtensionPointConstants.ATTR_CLASS);
							wizard.initialize(getFormPage().getEditor().getResourceFile(), getFormPage().getEditor()
									.getDomDocument(), def
									.getAttribute(CommonActionsExtensionPointConstants.ATTR_NAMESPACE_URI));
							Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
							if (shell != null && !shell.isDisposed()) {
								WizardDialog dialog = new WizardDialog(shell, wizard);
								dialog.create();
								dialog.setBlockOnOpen(true);
								if (dialog.open() == Window.OK) {
									IDOMElement element = wizard.getNewElement();
									getMasterPart().getViewer().setSelection(new StructuredSelection(element));
								}
							}
						}
						catch (CoreException e1) {
							StatusHandler.log(new Status(
									IStatus.ERROR,
									ConfigUiPlugin.PLUGIN_ID,
									Messages.getString("AbstractConfigMasterDetailsBlock.ERROR_LOADING_WIZARD_CONTRIBUTION"), e1)); //$NON-NLS-1$
						}
					}
				});
			}

			templateClient.setSize(templateClient.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			templateClient.layout();
			return sectionPart;
		}
		return null;
	}

	public abstract AbstractConfigDetailsPart getDetailsPage(Object key);

	@Override
	public AbstractConfigDetailsPart getPage(Object key) {
		if (key instanceof IDOMElement) {
			IDOMElement element = (IDOMElement) key;
			if (getFormPage().getNamespaceUri().equals(element.getNamespaceURI())) {
				return getDetailsPage(element);
			}
			for (final IConfigurationElement config : getFormPage().getAdapterDefinitions()) {
				String uri = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI);
				if (uri.equals(element.getNamespaceURI())) {
					if (config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_DETAILS_FACTORY) == null) {
						return new AbstractNamespaceDetailsPart(getMasterPart());
					}
					else {
						try {
							Object obj = config
									.createExecutableExtension(PageAdaptersExtensionPointConstants.ATTR_DETAILS_FACTORY);
							if (obj instanceof AbstractNamespaceMasterDetailsBlock) {
								AbstractNamespaceMasterDetailsBlock factory = (AbstractNamespaceMasterDetailsBlock) obj;
								factory.setFormPage(getFormPage());
								AbstractConfigDetailsPart page = factory.getDetailsPage(element);
								page.setMasterPart(getMasterPart());
								return page;
							}
						}
						catch (CoreException e) {
							StatusHandler.log(new Status(IStatus.ERROR, ConfigUiPlugin.PLUGIN_ID, Messages
									.getString("AbstractConfigMasterDetailsBlock.ERROR_LOADING_DETAILS"), e)); //$NON-NLS-1$
						}
					}
				}
			}
		}
		return null;
	}

}
