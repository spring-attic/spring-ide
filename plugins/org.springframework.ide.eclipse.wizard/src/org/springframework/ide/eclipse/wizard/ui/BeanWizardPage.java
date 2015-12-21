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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.config.core.contentassist.XmlBackedContentProposalAdapter;
import org.springframework.ide.eclipse.config.core.contentassist.XmlBackedContentProposalProvider;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.quickfix.ContentAssistProposalWrapper;
import org.springframework.ide.eclipse.wizard.Messages;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.core.WizardBeanReferenceContentProposalProvider;
import org.springframework.ide.eclipse.wizard.core.WizardClassContentProposalProvider;
import org.springframework.ide.eclipse.wizard.core.WizardContentAssistConverter;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;


/**
 * Wizard page for creating or modifying a bean definition
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeanWizardPage extends AbstractBeanWizardPage {

	private Label idLabel, nameLabel, parentLabel;

	private Hyperlink classLink;

	private Text idText, nameText, classText, parentText, fileText;

	private Button ignoreErrorButton, fileBrowseButton;

	private static final String DEFAULT_MESSAGE = Messages.getString("NewBeanWizardPage.TITLE_DESCRIPTION"); //$NON-NLS-1$

	private String fileErrorMessage, classErrorMessage, parentErrorMessage;

	private final Set<XmlBackedContentProposalAdapter> contentProposalAdapters;

	private WizardClassContentProposalProvider classProposalProvider;

	private WizardBeanReferenceContentProposalProvider parentProposalProvider;

	private final boolean fileBrowsingEnabled;

	public BeanWizardPage(String pageName, BeanWizard wizard, boolean fileBrowsingEnabled) {
		super(pageName, wizard);
		setTitle(Messages.getString("NewBeanWizardPage.TITLE")); //$NON-NLS-1$
		if (wizard.getBeanFile() == null) {
			setDescription(Messages.getString("NewBeanWizardPage.ENTER_BEAN_FILE")); //$NON-NLS-1$
		}
		else {
			setDescription(DEFAULT_MESSAGE);
		}

		this.wizard = wizard;
		this.fileBrowsingEnabled = fileBrowsingEnabled;
		this.contentProposalAdapters = new HashSet<XmlBackedContentProposalAdapter>();
	}

	private void addListener(final Text text, final String attributeName) {
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateAttribute(attributeName, text.getText());
			}
		});
	}

	@Override
	public boolean canFlipToNextPage() {
		return wizard.getBeanFile() != null
				&& (BeanWizard.getIgnoreError() || classText.getText().length() > 0 || parentText.getText().length() > 0);
	}

	private void createAttribute(String attributeName, Hyperlink link, Text text) {
		link.setText(getDisplayText(attributeName) + ":"); //$NON-NLS-1$
		link.setUnderlined(true);
		link.setLayoutData(new GridData());

		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				IProject project = BeanWizardPage.this.wizard.getBeanFile().getProject();
				String className = classText.getText();

				try {
					if (project.hasNature(JavaCore.NATURE_ID)) {
						IJavaProject javaProject = JavaCore.create(project);
						IJavaElement result = null;
						if (className.length() > 0) {
							result = javaProject.findType(className);
						}

						if (result != null) {
							JavaUI.openInEditor(result);
							return;
						}

						NewClassWizardPage page = new NewClassWizardPage();

						int index = className.lastIndexOf(".");
						if (index > 0) {
							String packageName = className.substring(0, index);
							className = className.substring(index + 1);

							IPackageFragment[] packageFragments = javaProject.getPackageFragments();
							for (IPackageFragment packageFragment : packageFragments) {
								if (packageFragment.getElementName().equals(packageName)) {
									page.setPackageFragment(packageFragment, true);

									IPackageFragmentRoot[] packageFragmentRoots = javaProject
											.getAllPackageFragmentRoots();
									for (IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
										if (packageFragmentRoot.getPath().isPrefixOf(packageFragment.getPath())) {
											page.setPackageFragmentRoot(packageFragmentRoot, true);
											break;
										}
									}
									break;
								}
							}
						}

						page.setTypeName(className, false);

						NewClassCreationWizard wizard = new NewClassCreationWizard(page, true);
						IWorkbench workbench = PlatformUI.getWorkbench();
						wizard.init(workbench, null);

						Shell shell = workbench.getActiveWorkbenchWindow().getShell();
						WizardDialog dialog = new WizardDialog(shell, wizard);
						dialog.create();
						dialog.getShell().setText("New Class");

						dialog.setBlockOnOpen(true);
						if (dialog.open() == Window.OK) {
							validateAttribute(BeansSchemaConstants.ATTR_CLASS, className);
						}

					}
				}
				catch (CoreException ex) {

				}

			}
		});

		createAttrbute(attributeName, text);
	}

	private void createAttribute(String attributeName, Label label, Text text) {
		label.setText(getDisplayText(attributeName) + ":"); //$NON-NLS-1$
		label.setLayoutData(new GridData());

		createAttrbute(attributeName, text);
	}

	private void createAttrbute(String attributeName, Text text) {
		text.setEditable(true);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		IDOMElement newBean = wizard.getNewBean();
		String attributeValue = null;
		if (newBean != null) {
			attributeValue = newBean.getAttribute(attributeName);
		}

		if (attributeValue != null) {
			text.setText(attributeValue);
		}

		XmlBackedContentProposalProvider proposalProvider = null;

		IFile beanFile = wizard.getBeanFile();
		Document originalDocument = wizard.getOriginalDocument();
		if (attributeName.equals(BeansSchemaConstants.ATTR_CLASS)) {
			classProposalProvider = new WizardClassContentProposalProvider(newBean, attributeName, beanFile,
					originalDocument);
			proposalProvider = classProposalProvider;
		}
		else if (attributeName.equals(BeansSchemaConstants.ATTR_PARENT)) {
			parentProposalProvider = new WizardBeanReferenceContentProposalProvider(newBean, attributeName, beanFile,
					originalDocument);
			proposalProvider = parentProposalProvider;
		}

		if (proposalProvider != null) {
			contentProposalAdapters.add(new XmlBackedContentProposalAdapter(text, new TextContentAdapter(),
					proposalProvider));
		}

		addListener(text, attributeName);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createFileComposite(composite);

		Composite container = new Composite(composite, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		container.setLayout(layout);

		idLabel = new Label(container, SWT.NONE);
		idText = new Text(container, SWT.BORDER);
		createAttribute(BeansSchemaConstants.ATTR_ID, idLabel, idText);

		nameLabel = new Label(container, SWT.NONE);
		nameText = new Text(container, SWT.BORDER);
		createAttribute(BeansSchemaConstants.ATTR_NAME, nameLabel, nameText);

		classLink = new Hyperlink(container, SWT.UNDERLINE_LINK);

		Composite classContainer = new Composite(container, SWT.NONE);
		GridLayout classContainerLayout = new GridLayout(2, false);
		classContainer.setLayout(classContainerLayout);
		classContainerLayout.marginHeight = 0;
		classContainerLayout.marginWidth = 0;
		classContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		classText = new Text(classContainer, SWT.BORDER);
		createAttribute(BeansSchemaConstants.ATTR_CLASS, classLink, classText);

		Button browseButton = new Button(classContainer, SWT.NONE);
		browseButton.setText(Messages.getString("NewBeanWizardPage.BROWSE_BUTTON_LABEL")); //$NON-NLS-1$
		browseButton.setLayoutData(new GridData());
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filter = classText.getText();
				if (filter == null) {
					filter = ""; //$NON-NLS-1$
				}

				filter = filter.replace('$', '.');
				try {
					int scope = IJavaElementSearchConstants.CONSIDER_CLASSES;

					SelectionDialog dialog = JavaUI.createTypeDialog(getShell(), PlatformUI.getWorkbench()
							.getProgressService(), null, scope, false, filter);
					dialog.setTitle(Messages.getString("NewBeanWizardPage.SELECT_TYPE_DIALOG_TITLE")); //$NON-NLS-1$

					if (dialog.open() == Window.OK) {
						IType type = (IType) dialog.getResult()[0];
						String newValue = type.getFullyQualifiedName('$');
						classText.setText(newValue);
						updateAttribute(BeansSchemaConstants.ATTR_CLASS, newValue);
					}
				}
				catch (JavaModelException ex) {
					StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, "Failed to select type.", ex));
				}

			}
		});

		parentLabel = new Label(container, SWT.NONE);
		parentText = new Text(container, SWT.BORDER);
		createAttribute(BeansSchemaConstants.ATTR_PARENT, parentLabel, parentText);

		Composite buttonComposite = new Composite(container, SWT.NONE);

		GridLayout buttonCompositeLayout = new GridLayout();
		buttonCompositeLayout.marginWidth = 0;
		buttonCompositeLayout.marginHeight = 0;
		buttonComposite.setLayout(buttonCompositeLayout);

		GridData buttonCompositeData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonCompositeData.horizontalSpan = 2;
		buttonComposite.setLayoutData(buttonCompositeData);

		ignoreErrorButton = wizard.createIgnoreErrorButton(buttonComposite, this);
		ignoreErrorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getWizard().getContainer().updateButtons();
				updateMessage();
			}
		});

		updateFieldsEnablement();

		setControl(composite);
	}

	private void createFileComposite(Composite parent) {
		Composite fileComposite = new Composite(parent, SWT.NONE);

		fileComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		GridLayout compositeLayout = new GridLayout(2, false);
		compositeLayout.marginBottom = 10;
		fileComposite.setLayout(compositeLayout);

		Label fileLabel = new Label(fileComposite, SWT.NONE);
		fileLabel.setText(Messages.getString("NewBeanWizardPage.BEAN_FILE_LABEL")); //$NON-NLS-1$
		GridData labelData = new GridData(SWT.FILL, SWT.FILL, false, false);
		labelData.horizontalSpan = 2;
		fileLabel.setLayoutData(labelData);

		fileText = new Text(fileComposite, SWT.BORDER);
		fileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fileText.setEditable(true);
		IFile beanFile = wizard.getBeanFile();
		if (beanFile != null) {
			fileText.setText(beanFile.getFullPath().toOSString());
		}

		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String filePath = fileText.getText();
				if (filePath.length() == 0) {
					fileErrorMessage = Messages.getString("NewBeanWizardPage.EMPTY_BEAN_FILE"); //$NON-NLS-1$
				}
				else {
					IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(filePath));
					validateFile(resource);
				}
			}
		});

		fileBrowseButton = new Button(fileComposite, SWT.PUSH);
		fileBrowseButton.setText(Messages.getString("NewBeanWizardPage.BROWSE_BEAN_FILE")); //$NON-NLS-1$
		fileBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		fileBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				BeanFileSelectionDialog dialog = new BeanFileSelectionDialog(getShell(), false, ResourcesPlugin
						.getWorkspace().getRoot(), IResource.FILE);
				dialog.setTitle(Messages.getString("NewBeanWizardPage.BEAN_SELECTION_DIALOG_TITLE")); //$NON-NLS-1$
				int status = dialog.open();
				if (status == Dialog.OK) {
					Object selection = dialog.getFirstResult();
					if (selection != null && selection instanceof IFile) {
						IFile beanFile = (IFile) selection;
						fileText.setText(beanFile.getFullPath().toOSString());

						validateFile(beanFile);
					}
				}
			}
		});

		fileText.setEnabled(fileBrowsingEnabled);
		fileBrowseButton.setEnabled(fileBrowsingEnabled);
	}

	public String getClassName() {
		return getInputText(classText);
	}

	private String getDisplayText(String attributeName) {
		return StringUtils.capitalize(attributeName);
	}

	public String getId() {
		return getInputText(idText);
	}

	private String getInputText(Text text) {
		if (text != null) {
			String inputText = text.getText();
			if (inputText != null) {
				return inputText;
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getName() {
		return getInputText(nameText);
	}

	@Override
	public boolean isPageComplete() {
		if (wizard.getBeanFile() == null) {
			return false;
		}

		if (getErrorMessage() != null && !BeanWizard.getIgnoreError()) {
			return false;
		}

		return super.isPageComplete();
	}

	private void updateAttribute(String attributeName, String value) {
		IDOMElement newBean = wizard.getNewBean();

		if (value.length() > 0) {
			if (newBean.getAttributeNode(attributeName) != null) {
				newBean.setAttribute(attributeName, value);
			}
			else {
				List<Attr> attributes = new ArrayList<Attr>();
				boolean found = false;
				for (String currAttributeName : BeanWizard.ATTRIBUTES) {
					if (currAttributeName == attributeName) {
						found = true;
					}
					else if (found) {
						Attr attributeNode = newBean.getAttributeNode(currAttributeName);
						if (attributeNode != null) {
							attributes.add(attributeNode);
							newBean.removeAttributeNode(attributeNode);
						}
					}
				}
				newBean.setAttribute(attributeName, value);

				for (Attr attribute : attributes) {
					newBean.setAttributeNode(attribute);
				}
			}
		}
		else {
			newBean.removeAttribute(attributeName);
		}

		validateAttribute(attributeName, value);
	}

	private void updateFieldsEnablement() {
		boolean enabled = fileErrorMessage == null && wizard.getBeanFile() != null;
		idText.setEnabled(enabled);
		nameText.setEnabled(enabled);
		classText.setEnabled(enabled);
		parentText.setEnabled(enabled);
		ignoreErrorButton.setEnabled(enabled);
	}

	@Override
	public void updateMessage() {
		if (fileErrorMessage != null) {
			setDialogMessage(fileErrorMessage, false);
		}
		else if (classErrorMessage != null) {
			setDialogMessage(classErrorMessage, true);
		}
		else if (parentErrorMessage != null) {
			setDialogMessage(parentErrorMessage, true);
		}
		else {
			setMessage(DEFAULT_MESSAGE);
		}

		getWizard().getContainer().updateButtons();
	}

	private void validateAttribute(String attributeName, String value) {
		IDOMElement newBean = wizard.getNewBean();
		WizardContentAssistConverter contentAssistConverter = new WizardContentAssistConverter(newBean,
				newBean.getAttributeNode(attributeName), wizard.getBeanFile(), wizard.getOriginalDocument());

		if (attributeName.equals(BeansSchemaConstants.ATTR_CLASS)) {
			if (value.length() > 0) {
				Set<ContentAssistProposalWrapper> classAttributeProposals = contentAssistConverter
						.getClassAttributeProposals(value, true);
				if (classAttributeProposals.isEmpty()) {
					classErrorMessage = Messages.getString("NewBeanWizardPage.UNKNOW_CLASS_MESSAGE"); //$NON-NLS-1$
				}
				else {
					classErrorMessage = null;
				}
			}
			else {
				classErrorMessage = null;
			}
		}
		else if (attributeName.equals(BeansSchemaConstants.ATTR_PARENT)) {
			if (value.length() > 0) {
				Set<ContentAssistProposalWrapper> referenceableBeanDescriptions = contentAssistConverter
						.getReferenceableBeanDescriptions(value, true);
				if (referenceableBeanDescriptions.isEmpty()) {
					parentErrorMessage = Messages.getString("NewBeanWizardPage.UNKNOWN_PARENT_MESSAGE"); //$NON-NLS-1$
				}
				else {
					parentErrorMessage = null;
				}
			}
			else {
				parentErrorMessage = null;
			}
		}

		updateMessage();
	}

	private void validateFields() {
		validateAttribute(BeansSchemaConstants.ATTR_CLASS, classText.getText());
		validateAttribute(BeansSchemaConstants.ATTR_PARENT, parentText.getText());
	}

	private void validateFile(IResource beanFile) {
		if (beanFile != null && BeansCoreUtils.isBeansConfig(beanFile)) {
			wizard.setBeanFile((IFile) beanFile);
			fileErrorMessage = null;
			validateFields();

			classProposalProvider.setFile((IFile) beanFile);
			parentProposalProvider.setFile((IFile) beanFile);

			classProposalProvider.setDocument(wizard.getOriginalDocument());
			parentProposalProvider.setDocument(wizard.getOriginalDocument());

			for (XmlBackedContentProposalAdapter contentProposalAdapter : contentProposalAdapters) {
				contentProposalAdapter.update(wizard.getNewBean());
			}
		}
		else {
			fileErrorMessage = Messages.getString("NewBeanWizardPage.BEAN_FILE_ERROR"); //$NON-NLS-1$
		}

		updateMessage();
		updateFieldsEnablement();
	}

}
