/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.ide.eclipse.aop.ui.navigator;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.CommonNavigator;
import org.springframework.ide.eclipse.aop.ui.navigator.util.BeansAopNavigatorUtils;
import org.w3c.dom.Element;

@SuppressWarnings("restriction")
public class BeansAopNavigator
        extends CommonNavigator implements ISelectionListener {

    private Object lastElement;

    public void createPartControl(Composite aParent) {
        super.createPartControl(aParent);
        getSite().getWorkbenchWindow().getSelectionService()
                .addPostSelectionListener(this);
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        final Object element = BeansAopNavigatorUtils.getSelectedElement(part,
                selection);
        if (element != null
                && !element.equals(lastElement)
                && (element instanceof IType || element instanceof IMethod || element instanceof Element)
                && isLinkingEnabled()) {
            Control ctrl = getCommonViewer().getControl();
            // Are we in the UI thread?
            if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
                refreshViewer(getCommonViewer(), element);
            }
            else {
                ctrl.getDisplay().asyncExec(new Runnable() {
                    public void run() {

                        // Abort if this happens after disposes
                        Control ctrl = getCommonViewer().getControl();
                        if (ctrl == null || ctrl.isDisposed()) {
                            return;
                        }
                        refreshViewer(getCommonViewer(), element);
                    }
                });
            }
            lastElement = element;
        }
    }

    public static void refreshViewer(TreeViewer viewer, final Object javaElement) {
        viewer.getTree().setRedraw(false);
        viewer.setInput(javaElement);
        viewer.refresh();
        viewer.expandAll();
        revealSelection(viewer, javaElement);
        viewer.getTree().setRedraw(true);
    }

    public static void revealSelection(TreeViewer viewer,
            final Object javaElement) {
        TreeItem[] items = viewer.getTree().getItems();
        Object wr = null;
        if (items != null && items.length > 0) {
            wr = items[0].getData();
        }
        if (wr != null) {
            viewer.setSelection(new StructuredSelection(wr), true);
            viewer.reveal(wr);
        }
    }

    public void dispose() {
        super.dispose();
        getSite().getWorkbenchWindow().getSelectionService()
                .removeSelectionListener(this);
    }
}
