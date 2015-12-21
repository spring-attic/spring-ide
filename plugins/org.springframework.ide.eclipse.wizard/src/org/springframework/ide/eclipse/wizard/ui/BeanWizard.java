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
package org.springframework.ide.eclipse.wizard.ui;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.ui.internal.StructuredTextViewer;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.config.core.formatting.ShallowFormatProcessorXML;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.wizard.Messages;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Node;


/**
 * Wizard for creating a new bean definiton
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeanWizard extends Wizard implements INewWizard {

	private IFile beanFile;

	private BeanWizardPage newBeanPage;

	private BeanPropertiesWizardPage propertiesPage;

	private IDOMDocument originalDocument;

	private IDOMElement newBean, existingBean;

	private boolean fileBrowsingEnabled;

	private final Set<IPropertyChangeListener> propertyListeners;

	public static String[] ATTRIBUTES = { BeansSchemaConstants.ATTR_ID, BeansSchemaConstants.ATTR_NAME,
			BeansSchemaConstants.ATTR_CLASS, BeansSchemaConstants.ATTR_PARENT };

	public static final String IGNORE_ERROR_PREFERENCE = "beanWizardIgnoreError"; //$NON-NLS-1$

	public static boolean getIgnoreError() {
		return getPreferenceStore().getBoolean(IGNORE_ERROR_PREFERENCE);
	}

	public static IPreferenceStore getPreferenceStore() {
		return WizardPlugin.getDefault().getPreferenceStore();
	}

	public BeanWizard() {
		this(Messages.getString("NewBeanWizard.TITLE"));
	}

	public BeanWizard(IDOMElement existingNode, IFile beanFile) {
		this(Messages.getString("ModifyBeanWizard.TITLE")); //$NON-NLS-1$
		this.beanFile = beanFile;
		this.existingBean = existingNode;
		this.fileBrowsingEnabled = false;

		IModelManager modelManager = StructuredModelManager.getModelManager();
		DOMModelImpl modelForRead = null;
		try {
			modelForRead = (DOMModelImpl) modelManager.getModelForRead(beanFile);
			originalDocument = modelForRead.getDocument();

			DOMModelImpl copiedModel = (DOMModelImpl) modelManager.createNewInstance(modelForRead);
			IDOMDocument copiedDocument = copiedModel.getDocument();

			newBean = (IDOMElement) copiedDocument.importNode(existingNode, true);
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, "Failed to create bean node.", e));
		}
		catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, "Failed to create bean node.", e));
		}
		finally {
			if (modelForRead != null) {
				modelForRead.releaseFromRead();
				modelForRead = null;
			}
		}
	}

	public BeanWizard(IFile beanFile) {
		this(Messages.getString("NewBeanWizard.TITLE")); //$NON-NLS-1$
		if (beanFile != null) {
			setBeanFile(beanFile);
		}
	}

	public BeanWizard(IFile beanFile, boolean fileBrowsingEnabled) {
		this(beanFile);

		if (beanFile != null) {
			this.fileBrowsingEnabled = fileBrowsingEnabled;
		}
	}

	private BeanWizard(String title) {
		setWindowTitle(title);
		setDefaultPageImageDescriptor(BeansUIImages.DESC_WIZ_CONFIG);

		this.fileBrowsingEnabled = true;

		this.propertyListeners = new HashSet<IPropertyChangeListener>();
	}

	@Override
	public void addPages() {
		super.addPages();
		newBeanPage = new BeanWizardPage("beanNewDefPage", this, fileBrowsingEnabled); //$NON-NLS-1$
		propertiesPage = new BeanPropertiesWizardPage("beanPropertiesDefPage", this); //$NON-NLS-1$
		addPage(newBeanPage);
		addPage(propertiesPage);
	}

	private void createBean() {
		DOMModelImpl model = null;
		try {
			IEditorPart editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
					beanFile);

			IModelManager modelManager = StructuredModelManager.getModelManager();
			model = (DOMModelImpl) modelManager.getModelForEdit(beanFile);
			IDOMDocument document = model.getDocument();

			model.beginRecording(this);

			Node nextSibling = null;
			IDOMElement parentNode;

			if (existingBean != null) {
				nextSibling = existingBean.getNextSibling();
				parentNode = (IDOMElement) existingBean.getParentNode();
				parentNode.removeChild(existingBean);
			}
			else {
				parentNode = (IDOMElement) document.getDocumentElement();
			}

			IDOMElement bean = (IDOMElement) document.importNode(newBean, true);
			if (nextSibling != null) {
				parentNode.insertBefore(bean, nextSibling);
			}
			else {
				parentNode.appendChild(bean);
			}

			new ShallowFormatProcessorXML().formatNode(parentNode);
			new FormatProcessorXML().formatNode(bean);

			model.endRecording(this);

			if (editor instanceof IConfigEditor) {
				IConfigEditor configEditor = (IConfigEditor) editor;
				int startOffset = bean.getStartOffset();
				int length = bean.getEndOffset() - startOffset;

				StructuredTextViewer textViewer = configEditor.getTextViewer();
				textViewer.setRangeIndication(startOffset, length, true);
				textViewer.revealRange(startOffset, length);
			}

			newBean = bean;
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, "Failed to create new bean.", e));
		}
		catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, "Failed to create new bean.", e));
		}
		finally {
			if (model != null) {
				model.releaseFromEdit();
				model = null;
			}
		}
	}

	public Button createIgnoreErrorButton(Composite container, final AbstractBeanWizardPage page) {
		final Button ignoreErrorButton = new Button(container, SWT.CHECK);
		ignoreErrorButton.setText(" " + Messages.getString("NewBeanWizardPage.IGNORE_ERRORS_MESSAGE")); //$NON-NLS-1$
		GridData buttonData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonData.verticalIndent = 10;
		ignoreErrorButton.setLayoutData(buttonData);

		boolean ignoreError = BeanWizard.getIgnoreError();
		ignoreErrorButton.setSelection(ignoreError);

		ignoreErrorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WizardPlugin.getDefault().getPreferenceStore().setValue(BeanWizard.IGNORE_ERROR_PREFERENCE,
						ignoreErrorButton.getSelection());
			}
		});

		IPropertyChangeListener listener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(IGNORE_ERROR_PREFERENCE)) {
					ignoreErrorButton.setSelection(getPreferenceStore().getBoolean(IGNORE_ERROR_PREFERENCE));
					// page.updateMessage();
				}
			}
		};
		getPreferenceStore().addPropertyChangeListener(listener);
		propertyListeners.add(listener);

		return ignoreErrorButton;
	}

	@Override
	public void dispose() {
		for (IPropertyChangeListener listener : propertyListeners) {
			getPreferenceStore().removePropertyChangeListener(listener);
		}
	}

	public IFile getBeanFile() {
		return beanFile;
	}

	public IDOMElement getNewBean() {
		return newBean;
	}

	public IDOMDocument getOriginalDocument() {
		return originalDocument;
	}

	public BeanPropertiesWizardPage getPropertiesPage() {
		return propertiesPage;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	@Override
	public boolean performFinish() {
		createBean();
		return true;
	}

	public void setBeanFile(IFile beanFile) {
		this.beanFile = beanFile;
		IModelManager modelManager = StructuredModelManager.getModelManager();
		DOMModelImpl modelForRead = null;

		try {
			modelForRead = (DOMModelImpl) modelManager.getModelForRead(beanFile);
			originalDocument = modelForRead.getDocument();

			DOMModelImpl copiedModel = (DOMModelImpl) modelManager.createNewInstance(modelForRead);
			IDOMDocument copiedDocument = copiedModel.getDocument();

			newBean = (IDOMElement) copiedDocument.createElementNS(NamespaceUtils.DEFAULT_NAMESPACE_URI,
					BeansSchemaConstants.ELEM_BEAN);
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, "Failed to create bean node.", e));
		}
		catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, "Failed to create bean node.", e));
		}
		finally {
			if (modelForRead != null) {
				modelForRead.releaseFromRead();
				modelForRead = null;
			}
		}
	}
}
