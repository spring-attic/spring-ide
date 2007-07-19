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
package org.springframework.ide.eclipse.aop.ui.navigator;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.springframework.ide.eclipse.aop.ui.Activator;
import org.springframework.ide.eclipse.aop.ui.navigator.action.ToggleShowBeanRefsForFileAction;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AbstractJavaElementReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdviceAopReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdviceDeclareParentAopReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdvisedAopReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdvisedDeclareParentAopReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IRevealableReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelNavigatorUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;
import org.springframework.ide.eclipse.ui.dialogs.WrappingStructuredSelection;
import org.w3c.dom.Element;

/**
 * Customized extension of the {@link CommonNavigator} that links to the current
 * selected element in the Java Editor or Spring IDE's XML Editor.
 * @author Christian Dupuis'
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AopReferenceModelNavigator extends CommonNavigator implements
		ISelectionListener, IModelChangeListener {

	public static final String ID = "org.springframework.ide.eclipse.aop.ui.navigator.aopReferenceModelNavigator";

	public static final String BEAN_REFS_FOR_FILE_ID = ID + ".beanRefsForFile";

	static {
		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
		showBeansRefsForFileEnabled = pstore.getBoolean(BEAN_REFS_FOR_FILE_ID);
	}

	private static boolean showBeansRefsForFileEnabled;

	public static int calculateExpandToLevel(Object element) {
		return AbstractTreeViewer.ALL_LEVELS;
	}

	public static Object calculateRootElement(Object element) {
		return calculateRootElement(element, showBeansRefsForFileEnabled);
	}

	public static Object calculateRootElement(Object element,
			boolean showBeansRefsForFile) {
		if (showBeansRefsForFile && element != null) {
			if (element instanceof IMethod) {
				element = ((IMethod) element).getDeclaringType();
			}
			else if (element instanceof Element) {
				element = ((Element) element).getOwnerDocument()
						.getDocumentElement();
			}
			else if (element instanceof IField) {
				element = ((IField) element).getDeclaringType();
			}
		}
		return element;
	}

	public static void expandTree(TreeItem[] items, boolean b) {
		if (items != null) {
			for (TreeItem item : items) {
				Object obj = item.getData();
				if (obj instanceof AdviceAopReferenceNode
						|| obj instanceof AdvisedAopReferenceNode
						|| obj instanceof AdviceDeclareParentAopReferenceNode
						|| obj instanceof AdvisedDeclareParentAopReferenceNode) {
					expandTree(item.getItems(), true);
				}
				else {
					expandTree(item.getItems(), b);
				}
				if (b) {
					item.setExpanded(false);
				}
			}
		}
	}

	public static void refreshViewer(TreeViewer viewer,
			final Object rootElement, Object element) {
		viewer.getTree().setRedraw(false);
		viewer.setInput(rootElement);
		// viewer.refresh();
		viewer.expandToLevel(calculateExpandToLevel(rootElement));
		expandTree(viewer.getTree().getItems(), false);
		revealSelection(viewer, element);
		viewer.getTree().setRedraw(true);
	}

	public static void revealSelection(TreeViewer viewer,
			final Object javaElement) {
		revealSelection(viewer, javaElement, showBeansRefsForFileEnabled);
	}

	public static void revealSelection(TreeViewer viewer,
			final Object javaElement, boolean showBeansRefsForFile) {
		TreeItem[] items = viewer.getTree().getItems();
		Object wr = null;

		if (javaElement instanceof IJavaElement) {
			if (showBeansRefsForFile && javaElement instanceof IMethod) {
				// we have one root element
				TreeItem[] aspectChildren = items[0].getItems();
				for (TreeItem element : aspectChildren) {
					Object obj = element.getData();
					if (obj instanceof AbstractJavaElementReferenceNode
							&& ((AbstractJavaElementReferenceNode) obj)
									.getElement().equals(javaElement)) {
						wr = obj;
						break;
					}
				}
			}
			else {
				if (items != null && items.length > 0) {
					wr = items[0].getData();
				}
			}
		}
		else if (javaElement instanceof ElementImpl) {
			if (!showBeansRefsForFile) {
				if (items != null && items.length > 0) {
					wr = items[0].getData();
				}
			}
			else {
				ElementImpl element = (ElementImpl) javaElement;
				IStructuredDocument document = element.getStructuredDocument();
				int startLine = document.getLineOfOffset(element
						.getStartOffset()) + 1;
				int endLine = document.getLineOfOffset(element.getEndOffset()) + 1;
				for (TreeItem element0 : items) {
					Object obj = element0.getData();
					if (obj instanceof IRevealableReferenceNode
							&& ((IRevealableReferenceNode) obj).getLineNumber() >= startLine
							&& ((IRevealableReferenceNode) obj).getLineNumber() <= endLine) {
						wr = obj;
						break;
					}
				}
			}
		}

		if (wr != null) {
			viewer.setSelection(new StructuredSelection(wr), true);
			viewer.reveal(wr);
		}

	}

	private Object lastElement;

	private ISelection lastSelection;

	private IWorkbenchPart lastWorkbenchPart;

	private ToggleShowBeanRefsForFileAction toggleShowBeanRefsForFileAction;

	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		getSite().getWorkbenchWindow().getSelectionService()
				.addPostSelectionListener(this);
		BeansCorePlugin.getModel().addChangeListener(this);

		makeActions();
	}

	@Override
	public void dispose() {
		super.dispose();
		BeansCorePlugin.getModel().removeChangeListener(this);
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(this);
	}

	public boolean isShowBeansRefsForFileEnabled() {
		return showBeansRefsForFileEnabled;
	}

	private void makeActions() {
		this.toggleShowBeanRefsForFileAction = new ToggleShowBeanRefsForFileAction(
				this);
		IActionBars bars = getViewSite().getActionBars();
		bars.getToolBarManager().add(toggleShowBeanRefsForFileAction);
		bars.getMenuManager().add(toggleShowBeanRefsForFileAction);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			selection = new WrappingStructuredSelection(
					(IStructuredSelection) selection);
		}
		updateTreeViewer(part, selection, true);
	}

	public void setShowBeansRefsForFileEnabled(
			boolean showBeansRefsForFileEnabled) {
		AopReferenceModelNavigator.showBeansRefsForFileEnabled = showBeansRefsForFileEnabled;

		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
		pstore.setValue(BEAN_REFS_FOR_FILE_ID, showBeansRefsForFileEnabled);
		Activator.getDefault().savePluginPreferences();

		updateTreeViewer(lastWorkbenchPart, lastSelection, false);
	}

	private void updateTreeViewer(final IWorkbenchPart part, final ISelection selection,
			final boolean ignoreSameSelection) {
		// Abort if this happens after disposes
		Control ctrl = getCommonViewer().getControl();
		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}

			// Are we in the UI thread?
		if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
			determineAndRefreshViewer(part, selection, ignoreSameSelection);
		}
		else {
			ctrl.getDisplay().asyncExec(new Runnable() {
				public void run() {

					// Abort if this happens after disposes
					Control ctrl = getCommonViewer().getControl();
					if (ctrl == null || ctrl.isDisposed()) {
						return;
					}
					determineAndRefreshViewer(part, selection,
							ignoreSameSelection);
				}
			});
		}
	}

	private void determineAndRefreshViewer(IWorkbenchPart part,
			ISelection selection, boolean ignoreSameSelection) {
		final Object element = AopReferenceModelNavigatorUtils
			.getSelectedElement(part, selection);
		if (element == null
				|| (element.equals(lastElement) && ignoreSameSelection)) {
			return;
		}
		if ((element instanceof IType || element instanceof IMethod
				|| element instanceof IField || element instanceof Element)
				&& isLinkingEnabled()) {
			refreshViewer(getCommonViewer(), calculateRootElement(element,
					showBeansRefsForFileEnabled), element);
		}
		lastElement = element;
		lastSelection = selection;
		lastWorkbenchPart = part;
	}

	/**
	 * Refreshed the view on added and changed events so that no closed project
	 * artefacts will be displayed in the references view.
	 */
	public void elementChanged(ModelChangeEvent event) {
		if (event.getType() == Type.ADDED || event.getType() == Type.REMOVED) {
			updateTreeViewer(lastWorkbenchPart, lastSelection, false);
		}
	}
}
