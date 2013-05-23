/*******************************************************************************
 *  Copyright (c) 2012, 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.springframework.ide.eclipse.wizard.WizardImages;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ITemplateElement;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springframework.ide.eclipse.wizard.template.infrastructure.TemplateCategory;
import org.springframework.ide.eclipse.wizard.template.util.TemplatesPreferencePage;
import org.springframework.ide.eclipse.wizard.template.util.TemplatesPreferencesModel;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;
import org.springsource.ide.eclipse.commons.content.core.ContentLocation;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;
import org.springsource.ide.eclipse.commons.content.core.util.ContentUtil;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;
import org.springsource.ide.eclipse.commons.content.core.util.IContentConstants;
import org.springsource.ide.eclipse.commons.ui.StsUiImages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Part that displays templates available when creating a project. Templates are
 * displayed in the part viewer based on metadata, but the actual contents of
 * the template are not download unless explicitly requested through the
 * download API. Selecting templates in the part viewer does not automatically
 * download the contents.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Kaitlin Duck Sherwood
 * @author Nieraj Singh
 */
public class TemplateSelectionPart {

	public static final String SIMPLE_PROJECTS_CATEGORY = "Simple Projects";

	private Template selectedTemplate;

	private final List<Template> templates;

	private final NewSpringProjectWizard wizard;

	private Label descriptionLabel;

	private StyledText descriptionText;

	private TreeViewer treeViewer;

	private Label legendImage;

	private Label legendText;

	private PropertyChangeListener contentManagerListener;

	private Button refreshButton;

	private final IWizardPageStatusHandler statusHandler;

	public TemplateSelectionPart(NewSpringProjectWizard wizard, IWizardPageStatusHandler statusHandler) {
		this.wizard = wizard;
		templates = new ArrayList<Template>();
		this.statusHandler = statusHandler;
	}

	public Control createControl(Composite parent) {

		initializeTemplates();

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(container, SWT.NONE);
		label.setText("Templates:"); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Tree tree = new Tree(container, SWT.FULL_SELECTION | SWT.BORDER);
		tree.setLinesVisible(false);
		tree.setHeaderVisible(false);
		tree.setEnabled(true);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		treeViewer = new TreeViewer(tree);

		treeViewer.setLabelProvider(new ILabelProvider() {

			public void removeListener(ILabelProviderListener listener) {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void dispose() {
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public String getText(Object element) {
				if (element instanceof ITemplateElement) {
					return ((ITemplateElement) element).getName();
				}
				return null;
			}

			public Image getImage(Object element) {
				if (element instanceof Template) {
					Template template = (Template) element;
					Image templateImage = WizardImages.getImage(WizardImages.TEMPLATE_ICON);
					if (template.getItem().isLocal() && !template.getItem().isNewerVersionAvailable()) {
						return templateImage;
					}
					return WizardImages.getImage(new DecorationOverlayIcon(templateImage, new ImageDescriptor[] {
							StsUiImages.DOWNLOAD_OVERLAY, null, null, null, null }));
				}

				if (element instanceof TemplateCategory) {
					return WizardImages.getImage(WizardImages.TEMPLATE_CATEGORY_ICON);
				}
				return null;
			}
		});

		treeViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof TemplateCategory && e2 instanceof Template) {
					return -1;
				}
				if (e1 instanceof Template && e2 instanceof TemplateCategory) {
					return 1;
				}

				if (e1 instanceof ITemplateElement && e2 instanceof ITemplateElement) {
					ITemplateElement t1 = (ITemplateElement) e1;
					ITemplateElement t2 = (ITemplateElement) e2;
					// Special Case, Simple Projects category is placed above
					// others
					if (t1.getName().equals(SIMPLE_PROJECTS_CATEGORY)) {
						return -1;
					}
					else if (t2.getName().equals(SIMPLE_PROJECTS_CATEGORY)) {
						return 1;
					}
					else {
						return t1.getName().compareTo(t2.getName());
					}
				}
				return super.compare(viewer, e1, e2);
			}
		});

		treeViewer.setContentProvider(new TemplateContentProvider());

		treeViewer.setInput(templates);

		Composite legendContainer = new Composite(container, SWT.NONE);
		legendContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout headerLayout = new GridLayout(3, false);
		headerLayout.marginWidth = 0;
		headerLayout.marginHeight = 0;
		legendContainer.setLayout(headerLayout);

		Composite legendComposite = new Composite(legendContainer, SWT.NONE);
		GridLayout legendLayout = new GridLayout(2, false);
		legendLayout.verticalSpacing = 0;
		legendLayout.marginHeight = 0;
		legendLayout.marginBottom = 0;

		int legendControlVerticalIndent = 5;

		legendComposite.setLayout(legendLayout);
		legendComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		legendImage = new Label(legendComposite, SWT.NONE);

		GridDataFactory.fillDefaults().grab(false, false).indent(0, legendControlVerticalIndent).applyTo(legendImage);

		legendImage.setImage(WizardImages.getImage(StsUiImages.DOWNLOAD_OVERLAY));

		legendText = new Label(legendComposite, SWT.NONE);
		legendText.setText("requires downloading");

		GridDataFactory.fillDefaults().grab(false, false).indent(0, legendControlVerticalIndent).applyTo(legendText);

		Hyperlink hyperlink = new Hyperlink(legendContainer, SWT.WRAP);
		GridDataFactory.fillDefaults().grab(false, false).indent(0, legendControlVerticalIndent).applyTo(hyperlink);
		hyperlink.setText(NLS.bind("Configure templates...", null));
		Color blue = new Color(null, 0, 0, 255);
		hyperlink.setForeground(blue);
		hyperlink.setUnderlined(true);
		blue.dispose();
		hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent event) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null,
						TemplatesPreferencePage.EXAMPLE_PREFERENCES_PAGE_ID, null, null);
				refreshButton.setEnabled(false);

				dialog.open();
				downloadDescriptors();

				refreshButton.setEnabled(!isRefreshing());

			}
		});

		refreshButton = new Button(legendContainer, SWT.PUSH);
		refreshButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		refreshButton.setText("Refresh");
		refreshButton.setEnabled(!isRefreshing());

		// refreshButton.setImage(WizardImages.getImage(WizardImages.REFRESH_ICON));

		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// under most circumstances, we don't want to download templates
				// if there has not been any change. However, when the user
				// presses Refresh, they really do want to see something happen.
				ContentPlugin.getDefault().getManager().setDirty();

				downloadDescriptors();
			}

		});

		Composite descriptionComposite = new Composite(container, SWT.NONE);
		descriptionComposite.setLayout(new GridLayout());
		descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText("Description:"); //$NON-NLS-1$
		descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		descriptionText = new StyledText(descriptionComposite, SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
		// descriptionText.setAlwaysShowScrollBars(false);
		Display display = Display.getCurrent();
		if (display != null) {
			descriptionText.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		}

		GridData descriptionData = new GridData(SWT.FILL, SWT.FILL, true, true);
		descriptionData.widthHint = 200;
		descriptionData.heightHint = 80;
		descriptionText.setLayoutData(descriptionData);

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = treeViewer.getSelection();
				if (selection instanceof TreeSelection) {
					Object element = ((TreeSelection) selection).getFirstElement();
					if (element instanceof Template) {
						setSeletectedTemplate((Template) element);
					}
					else if (element instanceof TemplateCategory) {
						// Note: ONLY clear the selected template if the
						// selection is a template category.
						// Do not clear the template if there is an empty
						// selection in the tree, as there
						// are times when the viewer
						// will set an empty selection in the tree due to a
						// refresh operation, and if
						// a template was already selected prior to that refresh
						// request, it should not be
						// overwritten with null as other operations will
						// require that the selected template not be changed
						setSeletectedTemplate(null);
					}
				}
			}

		});

		this.contentManagerListener = new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent arg0) {
				initializeTemplates();

				// switch to UI thread
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						refreshPage();
					}
				});
			}

		};

		ContentManager manager = ContentPlugin.getDefault().getManager();
		manager.addListener(contentManagerListener);

		// Add the simple project template descriptors from the wizard bundle.
		ContentLocation location = WizardPlugin.getDefault().getTemplateContentLocation();

		// It only gets added once if already present
		manager.addContentLocation(location);

		downloadDescriptors();

		return container;

	}

	/**
	 * Refreshes list of descriptors, and only refreshes the subset of
	 * descriptors in the preference store if changes have been made in the
	 * preference store (i.e., the content manager indicates dirty)
	 */
	protected void downloadDescriptors() {
		try {
			wizard.getContainer().run(true, true, new DownloadDescriptorJob());
		}
		catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				setError(((CoreException) t).getStatus());
			}
			else {
				setError(
						"Failed to download descriptors"
								+ ((t != null && t.getMessage() != null) ? " due to " + t.getMessage() : ""), t);
			}
		}
		catch (InterruptedException e) {
			setError("Download of descriptors interrupted. Please try again.", e);

		}
	}

	protected void setSeletectedTemplate(Template template) {
		selectedTemplate = template;
		if (selectedTemplate != null) {
			setDescription(selectedTemplate);
		}
		handleChanged();
	}

	public boolean isValid() {
		return getStatus().getSeverity() != IStatus.ERROR && getTemplate() != null;
	}

	public IStatus getStatus() {
		if (selectedTemplate != null) {

			String warning = getWarning(selectedTemplate);
			if (warning != null) {
				return new Status(IStatus.WARNING, WizardPlugin.PLUGIN_ID, warning);
			}
			else {
				return Status.OK_STATUS;
			}
		}
		else {
			// Do not treat as an error. That way the template requirement
			// message will show in the wizard, but not as an error.
			return new Status(IStatus.OK, WizardPlugin.PLUGIN_ID, "Please select a template.");
		}
	}

	protected void setError(String error, Throwable t) {
		if (statusHandler != null) {
			statusHandler.setStatus(new Status(IStatus.OK, WizardPlugin.PLUGIN_ID, error, t), false);
		}
	}

	protected void setError(IStatus error) {
		if (statusHandler != null) {
			statusHandler.setStatus(error, false);
		}
	}

	protected void handleChanged() {
		IStatus status = getStatus();
		if (statusHandler != null) {
			statusHandler.setStatus(status, true);
		}
	}

	public Template getTemplate() {
		return selectedTemplate;
	}

	public boolean hasSelection() {
		return selectedTemplate != null;
	}

	private void initializeTemplates() {
		templates.clear();

		TemplatesPreferencesModel model = TemplatesPreferencesModel.getInstance();
		Collection<ContentItem> items = ContentPlugin.getDefault().getManager()
				.getItemsByKind(ContentManager.KIND_TEMPLATE);

		List<ContentItem> sortedItems = new ArrayList<ContentItem>();
		sortedItems.addAll(items);
		Collections.sort(sortedItems, new Comparator<ContentItem>() {
			public int compare(ContentItem o1, ContentItem o2) {
				Descriptor descriptor1 = o1.getRemoteDescriptor();
				if (descriptor1 == null) {
					descriptor1 = o1.getLocalDescriptor();
				}

				Descriptor descriptor2 = o2.getRemoteDescriptor();
				if (descriptor2 == null) {
					descriptor2 = o2.getLocalDescriptor();
				}
				return descriptor1.getVersion().compareTo(descriptor2.getVersion()) * -1;
			}
		});

		Set<String> templateIds = new HashSet<String>();
		for (ContentItem item : sortedItems) {
			String templateId = item.getId();
			if (!templateIds.contains(templateId)) {
				Template template = null;
				// Handle the special cases: Simple Java and Simple Maven, as
				// they are not true templates.
				if (TemplateConstants.SIMPLE_JAVA_TEMPLATE_ID.equals(templateId)) {
					template = new SimpleProject(item, null, true);
				}
				else if (TemplateConstants.SIMPLE_MAVEN_TEMPLATE_ID.equals(templateId)) {
					template = new SimpleProject(item, null, false);
				}
				else {
					template = new Template(item, null);
				}

				templates.add(template);
				templateIds.add(templateId);
			}
		}

		if (model.shouldShowSelfHostedProjects()) {

			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject project : projects) {
				IFile templateFile = project.getFile(IContentConstants.TEMPLATE_DATA_FILE_NAME);
				IFile wizardFile = project.getFile(IContentConstants.WIZARD_DATA_FILE_NAME);
				if (templateFile.exists() && wizardFile.exists()) {
					File file = templateFile.getLocation().toFile();
					try {
						DocumentBuilder documentBuilder = ContentUtil.createDocumentBuilder();
						Document document = documentBuilder.parse(file);
						Element rootNode = document.getDocumentElement();
						if (rootNode != null) {
							NodeList children = rootNode.getChildNodes();
							for (int i = 0; i < children.getLength(); i++) {
								Node childNode = children.item(i);
								if (childNode.getNodeType() == Node.ELEMENT_NODE) {
									if ("descriptor".equals(childNode.getNodeName())) {
										Descriptor descriptor = Descriptor.read(childNode);
										ContentItem item = new ContentItem(descriptor.getId(), project);
										item.setLocalDescriptor(descriptor);
										descriptor.setUrl(project.getName());
										ImageDescriptor icon = null;
										Template template = new Template(item, icon);
										templates.add(template);
									}
								}
							}
						}
					}
					catch (CoreException e) {
						String message = NLS.bind("Error getting and parsing descriptors file in background {0}",
								e.getMessage());
						MessageDialog.openWarning(wizard.getShell(), "Warning", message);
					}
					catch (SAXException e) {
						String message = NLS.bind("Error parsing tmp descriptors file at {0} in background.\n{1}",
								file, e.getMessage());
						MessageDialog.openWarning(wizard.getShell(), "Warning", message);
					}
					catch (IOException e) {
						String message = NLS.bind("IO error on file at {0} opened in background.\n{1}", file,
								e.getMessage());
						MessageDialog.openWarning(wizard.getShell(), "Warning", message);
					}

				}
			}
		}
		Collections.sort(templates, new Comparator<Template>() {
			public int compare(Template t1, Template t2) {
				return t1.getName().compareTo(t2.getName());
			}
		});
	}

	private void refreshPage() {

		treeViewer.refresh(true);

		boolean needsDownload = false;
		for (Template template : templates) {
			if (!template.getItem().isLocal()) {
				needsDownload = true;
				break;
			}
		}

		legendImage.setVisible(needsDownload);
		legendText.setVisible(needsDownload);
		descriptionText.setText(""); //$NON-NLS-1$
		refreshButton.setEnabled(true);
	}

	private void setDescription(Template template) {
		String description = null;

		if (template != null) {
			description = template.getDescription();
			if (template.getItem().getRemoteDescriptor() != null) {
				description += "\n\nURL:" + template.getItem().getRemoteDescriptor().getUrl();

			}
		}

		if (description != null) {
			descriptionText.setText(description);
		}
		else {
			descriptionText.setText(""); //$NON-NLS-1$
		}
		descriptionText.redraw();
	}

	private String getWarning(Template template) {
		String requiredBundleStr = null;
		ContentItem contentItem = template.getItem();
		if (contentItem.getLocalDescriptor() != null) {
			requiredBundleStr = contentItem.getLocalDescriptor().getRequiresBundle();
		}
		if (requiredBundleStr == null && contentItem.getRemoteDescriptor() != null) {
			requiredBundleStr = contentItem.getRemoteDescriptor().getRequiresBundle();
		}

		StringBuilder missingBundleStr = new StringBuilder();
		if (requiredBundleStr != null) {
			String[] requiredBundles = requiredBundleStr.split(",");
			for (String requiredBundle : requiredBundles) {
				if (Platform.getBundle(requiredBundle.trim()) == null) {
					if (missingBundleStr.length() > 0) {
						missingBundleStr.append(", ");
					}
					missingBundleStr.append(requiredBundle.trim());
				}
			}
		}

		if (missingBundleStr.length() > 0) {
			String message = NLS.bind("To ensure project compiles properly, please install bundle(s) {0}.",
					missingBundleStr);
			return message;
		}
		return null;
	}

	public void dispose() {
		Assert.isNotNull(contentManagerListener);
		ContentPlugin.getDefault().getManager().removeListener(contentManagerListener);

	}

	private boolean isRefreshing() {
		return ContentPlugin.getDefault().getManager().isRefreshing();
	}

}
