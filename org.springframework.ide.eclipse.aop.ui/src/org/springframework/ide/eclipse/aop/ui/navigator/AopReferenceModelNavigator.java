/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.aop.ui.navigator;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
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
import org.w3c.dom.Element;

@SuppressWarnings("restriction")
public class AopReferenceModelNavigator
        extends CommonNavigator implements ISelectionListener {

    public static final String ID = "org.springframework.ide.eclipse.aop.ui.navigator.AopReferenceModelNavigator";

    public static final String BEAN_REFS_FOR_FILE_ID = ID + ".beanRefsForFile";

    private static boolean showBeansRefsForFileEnabled;

    private ToggleShowBeanRefsForFileAction toggleShowBeanRefsForFileAction;

    static {
        IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
        showBeansRefsForFileEnabled = pstore.getBoolean(BEAN_REFS_FOR_FILE_ID);
    }

    public static int calculateExpandToLevel(Object element) {
        return AbstractTreeViewer.ALL_LEVELS;
    }

    public static Object calculateRootElement(Object element) {
        if (showBeansRefsForFileEnabled && element != null) {
            if (element instanceof IMethod) {
                element = ((IMethod) element).getDeclaringType();
            }
            else if (element instanceof Element) {
                element = ((Element) element).getOwnerDocument().getDocumentElement();
            }
            else if (element instanceof IField) {
                element = ((IField) element).getDeclaringType();
            }
        }
        return element;
    }

    public static void refreshViewer(TreeViewer viewer, final Object rootElement, Object element) {
        viewer.getTree().setRedraw(false);
        viewer.setInput(rootElement);
        viewer.refresh();
        viewer.expandToLevel(calculateExpandToLevel(rootElement));
        expandTree(viewer.getTree().getItems(), false);
        revealSelection(viewer, element);
        viewer.getTree().setRedraw(true);
    }

    private static void expandTree(TreeItem[] items, boolean b) {
        if (items != null) {
            for (TreeItem item : items) {
                Object obj = item.getData();
                if (obj instanceof AdviceAopReferenceNode || obj instanceof AdvisedAopReferenceNode
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

    public static void revealSelection(TreeViewer viewer, final Object javaElement) {
        TreeItem[] items = viewer.getTree().getItems();
        Object wr = null;

        if (javaElement instanceof IJavaElement) {
            if (showBeansRefsForFileEnabled && javaElement instanceof IMethod) {
                // we have one root element
                TreeItem[] aspectChildren = items[0].getItems();
                for (int i = 0; i < aspectChildren.length; i++) {
                    Object obj = aspectChildren[i].getData();
                    if (obj instanceof AbstractJavaElementReferenceNode
                            && ((AbstractJavaElementReferenceNode) obj).getElement().equals(
                                    javaElement)) {
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
            if (!showBeansRefsForFileEnabled) {
                if (items != null && items.length > 0) {
                    wr = items[0].getData();
                }
            }
            else {
                ElementImpl element = (ElementImpl) javaElement;
                IStructuredDocument document = element.getStructuredDocument();
                int startLine = document.getLineOfOffset(element.getStartOffset()) + 1;
                int endLine = document.getLineOfOffset(element.getEndOffset()) + 1;
                for (int i = 0; i < items.length; i++) {
                    Object obj = items[i].getData();
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

    public void createPartControl(Composite aParent) {
        super.createPartControl(aParent);
        getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);

        makeActions();
    }

    private void makeActions() {
        this.toggleShowBeanRefsForFileAction = new ToggleShowBeanRefsForFileAction(this);
        IActionBars bars = getViewSite().getActionBars();
        bars.getToolBarManager().add(toggleShowBeanRefsForFileAction);
        bars.getMenuManager().add(toggleShowBeanRefsForFileAction);
    }

    public void dispose() {
        super.dispose();
        getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
    }

    public boolean isShowBeansRefsForFileEnabled() {
        return showBeansRefsForFileEnabled;
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        updateTreeViewer(part, selection, true);
    }

    public void setShowBeansRefsForFileEnabled(boolean showBeansRefsForFileEnabled) {
        AopReferenceModelNavigator.showBeansRefsForFileEnabled = showBeansRefsForFileEnabled;

        IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
        pstore.setValue(BEAN_REFS_FOR_FILE_ID, showBeansRefsForFileEnabled);
        Activator.getDefault().savePluginPreferences();

        updateTreeViewer(lastWorkbenchPart, lastSelection, false);
    }

    private void updateTreeViewer(IWorkbenchPart part, ISelection selection,
            boolean ignoreSameSelection) {
        final Object element = AopReferenceModelNavigatorUtils.getSelectedElement(part, selection);
        if (element == null || (element.equals(lastElement) && ignoreSameSelection)) {
            return;
        }
        if ((element instanceof IType || element instanceof IMethod || element instanceof IField || element instanceof Element)
                && isLinkingEnabled()) {

            final Object rootElement = calculateRootElement(element);

            Control ctrl = getCommonViewer().getControl();
            // Are we in the UI thread?
            if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
                refreshViewer(getCommonViewer(), rootElement, element);
            }
            else {
                ctrl.getDisplay().asyncExec(new Runnable() {
                    public void run() {

                        // Abort if this happens after disposes
                        Control ctrl = getCommonViewer().getControl();
                        if (ctrl == null || ctrl.isDisposed()) {
                            return;
                        }
                        refreshViewer(getCommonViewer(), rootElement, element);
                    }
                });
            }
            lastElement = element;
            lastSelection = selection;
            lastWorkbenchPart = part;
        }
    }
}
