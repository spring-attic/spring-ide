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
package org.springframework.ide.eclipse.metadata.ui;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.metadata.BeansMetadataPlugin;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataReference;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.metadata.MetadataPlugin;
import org.springframework.ide.eclipse.metadata.actions.OpenInBrowserAction;
import org.springframework.ide.eclipse.metadata.actions.OpenInJavaEditorAction;
import org.springframework.ide.eclipse.metadata.actions.ToggleBreakPointAction;
import org.springframework.ide.eclipse.metadata.actions.ToggleLinkingAction;
import org.springframework.ide.eclipse.metadata.actions.ToggleOrientationAction;
import org.springframework.ide.eclipse.metadata.core.RequestMappingAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.RequestMappingMethodAnnotationMetadata;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class RequestMappingView extends ViewPart implements ISelectionListener,
		ISelectionChangedListener {

	public static int COLUMN_HANDLER_METHOD = 2;

	public static int COLUMN_REQUEST_METHOD = 1;

	public static int COLUMN_URL = 0;

	public static final String ID_VIEW = "com.springsource.sts.ide.metadata.ui.RequestMappingView"; //$NON-NLS-1$

	public static final String PREF_LINKING = "com.springsource.sts.ide.metadata.prefs.linking.RequestMappingView"; //$NON-NLS-1$

	public static final String PREF_ORIENTATION = "com.springsource.sts.ide.metadata.prefs.orientation.RequestMappingView"; //$NON-NLS-1$

	private static final String EMPTY_JAVADOC = Messages.RequestMappingView_DESCRIPTION_EMPTY_JAVADOC;

	private static final String EMPTY_MAPPINGS = Messages.RequestMappingView_DESCRIPTION_EMPTY_REQUESTMAPPINGS;

	private Set<RequestMappingAnnotationMetadata> annotations;

	private BaseSelectionListenerAction breakpointAction;

	private IBeansModelElement element;

	private HTMLTextPresenter htmlPresenter;

	private StyledText javadocText;

	private BaseSelectionListenerAction javaEditorAction;

	private RequestMappingViewLabelProvider labelProvider;

	private ToggleLinkingAction linkingAction;

	private boolean linkingEnabled;

	private TableViewer mainViewer;

	private BaseSelectionListenerAction openBrowserAction;

	private ToggleOrientationAction[] orientationActions;

	private IPreferenceStore prefStore;

	private ResourceChangeListener resourceListener;

	private SashForm splitter;

	private TextPresentation textPresentation;

	private RequestMappingViewSorter viewSorter;

	public RequestMappingView() {
		annotations = new HashSet<RequestMappingAnnotationMetadata>();
		prefStore = MetadataPlugin.getDefault().getPreferenceStore();
		resourceListener = new ResourceChangeListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				resourceListener);
		linkingEnabled = prefStore.getBoolean(PREF_LINKING);
	}

	@Override
	public void createPartControl(Composite parent) {
		splitter = new SashForm(parent, SWT.NONE);
		createRequestMappingViewer(splitter);
		createJavadocViewer(splitter);
		initializeColors();
		createActions();
		hookContextMenu();
		fillActionBars();
		getSite().setSelectionProvider(mainViewer);
		setLinkingEnabled(linkingEnabled);
		setOrientation(prefStore.getInt(PREF_ORIENTATION));
		setContentDescription(EMPTY_MAPPINGS);
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
				resourceListener);
		mainViewer.removeSelectionChangedListener(openBrowserAction);
		mainViewer.removeSelectionChangedListener(javaEditorAction);
		mainViewer.removeSelectionChangedListener(breakpointAction);
		mainViewer.removeSelectionChangedListener(this);
		getSite().getPage().removeSelectionListener(this);
		super.dispose();
	}

	public void finish(int kind, IResourceDelta delta,
			List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions,
			IProjectContributorState state, IProject project) {
		if (element == null && isLinkingEnabled()) {
			setLinkingEnabled(true);
		} else {
			IProject thisProject = null;
			if (element instanceof IBeansProject) {
				thisProject = ((IBeansProject) element).getProject();
			} else if (element != null) {
				thisProject = BeansModelUtils.getParentOfClass(element,
						IBeansProject.class).getProject();
			}
			if (thisProject != null && thisProject.equals(project)) {
				internalSetInput();
			}
		}
	}

	public IBeansModelElement getInput() {
		return element;
	}

	public boolean isLinkingEnabled() {
		return linkingEnabled;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IEditorPart) {
			doEditorActivated((IEditorPart) part);
		} else if (!(part instanceof RequestMappingView)) {
			doSelectionActivated(selection);
		}
	}

	public void selectionChanged(SelectionChangedEvent event) {
		if (event.getSelectionProvider() == mainViewer) {
			setJavadocInput(event.getSelection());
		}
	}

	@Override
	public void setFocus() {
		mainViewer.getControl().setFocus();
	}

	public void setInput(Object input) {
		if (input instanceof IBeansModelElement) {
			element = (IBeansModelElement) input;
			internalSetInput();
		}
	}

	public void setLinkingEnabled(boolean enabled) {
		linkingEnabled = enabled;
		prefStore.setValue(PREF_LINKING, enabled);

		IWorkbenchPage page = getSite().getPage();
		if (enabled) {
			page.addSelectionListener(this);
			IEditorPart editor = page.getActiveEditor();
			if (editor != null) {
				doEditorActivated(editor);
			}
		} else {
			page.removeSelectionListener(this);
		}
	}

	public void setOrientation(int orientation) {
		if (orientation != SWT.VERTICAL) {
			orientation = SWT.HORIZONTAL;
		}
		splitter.setOrientation(orientation);
		for (int i = 0; i < orientationActions.length; i++) {
			ToggleOrientationAction action = orientationActions[i];
			action.setChecked(orientation == action.getOrientation());
		}
		prefStore.setValue(PREF_ORIENTATION, orientation);
	}

	private void addAnnotationsForConfig(
			Set<RequestMappingAnnotationMetadata> newAnnotations,
			IBeansConfig config) {
		for (IBean bean : BeansModelUtils.getBeans(config)) {
			Set<IBeanMetadata> metadataSet = BeansMetadataPlugin
					.getMetadataModel().getBeanMetadata(bean);
			for (IBeanMetadata metadata : metadataSet) {
				if (metadata instanceof RequestMappingAnnotationMetadata) {
					newAnnotations
							.add((RequestMappingAnnotationMetadata) metadata);
				}
			}
		}
	}

	private void createActions() {
		openBrowserAction = new OpenInBrowserAction(this, labelProvider);
		mainViewer.addSelectionChangedListener(openBrowserAction);
		javaEditorAction = new OpenInJavaEditorAction();
		mainViewer.addSelectionChangedListener(javaEditorAction);
		breakpointAction = new ToggleBreakPointAction(this);
		mainViewer.addSelectionChangedListener(breakpointAction);
		linkingAction = new ToggleLinkingAction(this);
		orientationActions = new ToggleOrientationAction[] {
				new ToggleOrientationAction(this, SWT.HORIZONTAL),
				new ToggleOrientationAction(this, SWT.VERTICAL) };
	}

	private void createJavadocViewer(Composite parent) {
		javadocText = new StyledText(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		javadocText.setEditable(false);
		javadocText.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				setJavadocInput(mainViewer.getSelection());
			}
		});
		htmlPresenter = new HTMLTextPresenter(false);
		textPresentation = new TextPresentation();
	}

	private void createRequestMappingColumns() {
		final Table table = mainViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		String[] columnText = {
				Messages.RequestMappingView_HEADER_RESOURCE_URL,
				Messages.RequestMappingView_HEADER_REQUEST_METHOD,
				Messages.RequestMappingView_HEADER_HANDLER_METHOD };
		int[] columnWidth = { 200, 150, 300 };
		for (int i = 0; i < columnText.length; i++) {
			final TableViewerColumn column = new TableViewerColumn(mainViewer,
					SWT.NONE);
			final int columnId = i;
			column.getColumn().setText(columnText[i]);
			column.getColumn().setWidth(columnWidth[i]);
			column.getColumn().setResizable(true);
			column.getColumn().addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (column.getColumn().equals(table.getSortColumn())) {
						int direction = table.getSortDirection();
						if (direction == SWT.UP) {
							table.setSortDirection(SWT.DOWN);
							viewSorter.setSortDirection(SWT.DOWN);
						} else {
							table.setSortDirection(SWT.UP);
							viewSorter.setSortDirection(SWT.UP);
						}
					} else {
						table.setSortColumn(column.getColumn());
						viewSorter.setSortColumn(columnId);
					}
					mainViewer.refresh();
				}
			});
		}

		table.setSortColumn(table.getColumn(COLUMN_URL));
		viewSorter.setSortColumn(COLUMN_URL);
		table.setSortDirection(SWT.UP);
		viewSorter.setSortDirection(SWT.UP);
	}

	private void createRequestMappingViewer(Composite parent) {
		mainViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		labelProvider = new RequestMappingViewLabelProvider();
		viewSorter = new RequestMappingViewSorter(labelProvider);
		createRequestMappingColumns();
		mainViewer.setContentProvider(new RequestMappingViewContentProvider());
		mainViewer.setLabelProvider(labelProvider);
		mainViewer.setSorter(viewSorter);
		mainViewer.addSelectionChangedListener(this);
		mainViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				javaEditorAction.run();
			}
		});
	}

	private void doEditorActivated(IEditorPart editor) {
		final IEditorInput editorInput = editor.getEditorInput();
		if (editorInput != null) {
			Job updateFromEditor = new Job("Loading RequestMappings") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					IBeansModelElement input = getInputFromEditor(editorInput);
					if (input != null) {
						element = input;
					} else {
						element = null;
					}
					internalSetInput();
					return Status.OK_STATUS;
				}
			};
			IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getViewSite()
					.getService(IWorkbenchSiteProgressService.class);
			if (service != null) {
				service.schedule(updateFromEditor, 0L, true);
			} else {
				updateFromEditor.schedule();
			}
		}
	}

	private void doSelectionActivated(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final Object obj = ((IStructuredSelection) selection)
					.getFirstElement();
			if (obj != null) {
				Job updateFromSelection = new Job("Loading RequestMappings") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						IBeansModelElement input = getInputFromSelection(obj);
						if (input != null) {
							element = input;
						} else {
							element = null;
						}
						internalSetInput();
						return Status.OK_STATUS;
					}
				};
				IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getViewSite()
						.getService(IWorkbenchSiteProgressService.class);
				if (service != null) {
					service.schedule(updateFromSelection, 0L, true);
				} else {
					updateFromSelection.schedule();
				}
			}
		}
	}

	private void fillActionBars() {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(linkingAction);
		toolBar.add(new Separator());
		for (int i = 0; i < orientationActions.length; i++) {
			toolBar.add(orientationActions[i]);
		}
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(openBrowserAction);
		manager.add(javaEditorAction);
		manager.add(new Separator());
		manager.add(breakpointAction);
	}

	private IMember findJavaMember(Object obj) {
		IMember member = null;
		if (obj instanceof RequestMappingAnnotationMetadata) {
			RequestMappingAnnotationMetadata annotation = (RequestMappingAnnotationMetadata) obj;
			member = (IType) JavaCore.create(annotation.getClassHandle());
		} else if (obj instanceof RequestMappingMethodToClassMap) {
			RequestMappingMethodAnnotationMetadata annotation = ((RequestMappingMethodToClassMap) obj)
					.getMethodMetadata();
			member = (IMethod) JdtUtils.getByHandle(annotation
					.getHandleIdentifier());

		}
		return member;
	}

	private String generateJavadoc(IMember member) {
		try {
			Reader reader = JavadocContentAccess.getHTMLContentReader(member,
					true, false);
			if (reader != null) {
				StringBuffer sBuffer = new StringBuffer();
				char[] cBuffer = new char[1024];
				int i = 0;
				while (-1 != (i = reader.read(cBuffer))) {
					sBuffer.append(cBuffer, 0, i);
				}
				String body = sBuffer.toString();
				reader.close();
				return body;
			} else {
				return ""; //$NON-NLS-1$
			}
		} catch (JavaModelException e) {
			StatusHandler.log(new Status(IStatus.ERROR,
					MetadataPlugin.PLUGIN_ID,
					Messages.RequestMappingView_ERROR_GENERATING_JAVADOC, e));
			return ""; //$NON-NLS-1$
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR,
					MetadataPlugin.PLUGIN_ID,
					Messages.RequestMappingView_ERROR_GENERATING_JAVADOC, e));
			return ""; //$NON-NLS-1$
		}
	}

	private IBeansModelElement getInputFromEditor(IEditorInput editorInput) {
		IJavaElement javaElement = JavaUI
				.getEditorInputJavaElement(editorInput);
		IBeansModelElement modelElement = getInputFromJavaElement(javaElement);
		if (modelElement == null && editorInput instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) editorInput).getFile();
			modelElement = getInputFromFile(file);
		}
		// TODO: support IStorageEditorInput??
		return modelElement;
	}

	private IBeansModelElement getInputFromFile(IFile file) {
		IBeansModelElement modelElement = BeansCorePlugin.getModel().getConfig(
				file);
		if (modelElement == null) {
			IProject project = file.getProject();
			modelElement = BeansCorePlugin.getModel().getProject(project);
		}
		return modelElement;
	}

	private IBeansModelElement getInputFromJavaElement(IJavaElement javaElement) {
		IBeansModelElement modelElement = null;
		if (javaElement != null) {
			IJavaProject project = javaElement.getJavaProject();
			if (project != null) {
				modelElement = BeansCorePlugin.getModel().getProject(
						project.getProject());
			}
		}
		return modelElement;
	}

	private IBeansModelElement getInputFromSelection(Object obj) {
		if (obj instanceof IBeansModelElement) {
			return (IBeansModelElement) obj;
		}
		if (obj instanceof BeanMetadataReference) {
			return ((BeanMetadataReference) obj).getBeansProject();
		}
		IBeansModelElement modelElement = null;
		if (obj instanceof IJavaElement) {
			modelElement = getInputFromJavaElement((IJavaElement) obj);
		}
		if (modelElement == null && obj instanceof IFile) {
			modelElement = getInputFromFile((IFile) obj);
		}
		return modelElement;
	}

	private void hookContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ISelection selection = getViewSite().getSelectionProvider()
						.getSelection();
				if (!selection.isEmpty()) {
					RequestMappingView.this.fillContextMenu(manager);
				}
			}
		});
		Menu menu = menuManager.createContextMenu(mainViewer.getControl());
		mainViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, mainViewer);
	}

	private void initializeColors() {
		if (getSite().getShell().isDisposed()) {
			return;
		}

		Display display = getSite().getShell().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}

		javadocText.setForeground(display
				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		javadocText.setBackground(display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}

	private void internalSetInput() {
		final Set<RequestMappingAnnotationMetadata> newAnnotations = new HashSet<RequestMappingAnnotationMetadata>();
		String contentDescription = getContentDescription();
		if (element == null) {
			contentDescription = EMPTY_MAPPINGS;
		} else if (element instanceof IBeansConfig) {
			addAnnotationsForConfig(newAnnotations, (IBeansConfig) element);
			contentDescription = Messages.RequestMappingView_PREFIX_CONFIG_FILE
					+ ((IBeansConfig) element).getElementResource()
							.getFullPath().toString();
		} else if (element instanceof IBeansConfigSet) {
			for (IBeansConfig config : ((IBeansConfigSet) element).getConfigs()) {
				addAnnotationsForConfig(newAnnotations, config);
			}
			IModelElement parent = ((IBeansConfigSet) element)
					.getElementParent();
			contentDescription = Messages.RequestMappingView_PREFIX_CONFIG_SET
					+ parent.getElementName() + "/" + element.getElementName(); //$NON-NLS-1$
		} else if (element instanceof IBeansProject) {
			for (IBeansConfig config : ((IBeansProject) element).getConfigs()) {
				addAnnotationsForConfig(newAnnotations, config);
			}
			contentDescription = Messages.RequestMappingView_PREFIX_PROJECT
					+ ((IBeansProject) element).getElementName();
		}

		final String newContentDescription = contentDescription;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				setContentDescription(newContentDescription);
				if (!newAnnotations.equals(annotations)) {
					annotations = newAnnotations;
					mainViewer.setInput(annotations);
				}
			}
		});
	}

	private void setJavadocInput(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object obj = structuredSelection.getFirstElement();
			String javadocStr = null;
			IMember member = findJavaMember(obj);
			if (member == null) {
				javadocStr = EMPTY_JAVADOC;
			} else {
				javadocStr = generateJavadoc(member);
				if (javadocStr == null || javadocStr.trim().length() == 0) {
					javadocStr = EMPTY_JAVADOC;
				}
			}
			textPresentation.clear();
			Rectangle size = javadocText.getClientArea();
			javadocStr = htmlPresenter.updatePresentation(javadocText,
					javadocStr, textPresentation, size.width, size.height);
			javadocText.setText(javadocStr);
			TextPresentation.applyTextPresentation(textPresentation,
					javadocText);
		}
	}

	private class ResourceChangeListener implements IResourceChangeListener,
			IResourceDeltaVisitor {

		public void resourceChanged(IResourceChangeEvent event) {
			if (element != null) {
				if (event.getType() == IResourceChangeEvent.PRE_CLOSE
						|| event.getType() == IResourceChangeEvent.PRE_DELETE) {
					IProject project = null;
					if (event.getSource() instanceof IWorkspace) {
						project = (IProject) event.getResource();
					} else if (event.getSource() instanceof IProject) {
						project = (IProject) event.getSource();
					}

					if (project != null
							&& BeansModelUtils
									.getParentOfClass(element,
											IBeansProject.class).getProject()
									.equals(project)) {
						element = null;
						internalSetInput();
					}
				} else if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
					IResourceDelta delta = event.getDelta();
					try {
						if (delta != null) {
							delta.accept(this);
						}
					} catch (CoreException e) {
						StatusHandler
								.log(new Status(
										IStatus.ERROR,
										MetadataPlugin.PLUGIN_ID,
										Messages.RequestMappingView_ERROR_PROCESSING_RESOURCE_CHANGE));
					}
				}
			}
		}

		public boolean visit(IResourceDelta root) throws CoreException {
			IFile file = BeansModelUtils.getFile(element);
			if (element != null && root != null && file != null) {
				IResourceDelta delta = root.findMember(file.getFullPath());
				if (delta != null && delta.getKind() == IResourceDelta.REMOVED) {
					element = null;
					internalSetInput();
				}
			}
			return false;
		}

	}

}
