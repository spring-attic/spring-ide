/*
 * Copyright 2004 DekaBank Deutsche Girozentrale. All rights reserved.
 */
package org.springframework.ide.eclipse.aop.ui.navigator;

import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
import org.eclipse.contribution.xref.internal.ui.utils.XRefUIUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.CommonNavigator;
import org.springframework.ide.eclipse.aop.ui.navigator.util.JavaElementWrapper;

@SuppressWarnings("restriction")
public class BeansAopNavigator
        extends CommonNavigator implements ISelectionListener {

    private IJavaElement lastJavaElement;

    public void createPartControl(Composite aParent) {
        super.createPartControl(aParent);
        getSite().getWorkbenchWindow().getSelectionService()
                .addPostSelectionListener(this);
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        final IJavaElement javaElement = XRefUIUtils.getSelectedJavaElement(
                part, selection);
        if (javaElement != null
                && !javaElement.equals(lastJavaElement)
                && (javaElement instanceof IType || javaElement instanceof IMethod)) {
            Control ctrl = getCommonViewer().getControl();
            // Are we in the UI thread?
            if (ctrl.getDisplay().getThread() == Thread.currentThread()) {
                getCommonViewer().setInput(javaElement);
                getCommonViewer().refresh();
                getCommonViewer().expandAll();
                revealSelection(javaElement);
            }
            else {
                ctrl.getDisplay().asyncExec(new Runnable() {
                    public void run() {

                        // Abort if this happens after disposes
                        Control ctrl = getCommonViewer().getControl();
                        if (ctrl == null || ctrl.isDisposed()) {
                            return;
                        }
                        getCommonViewer().setInput(javaElement);
                        getCommonViewer().refresh();
                        getCommonViewer().expandAll();
                        revealSelection(javaElement);
                    }
                });
            }
            lastJavaElement = javaElement;
        }
    }

    private void revealSelection(final IJavaElement javaElement) {
        TreeItem[] items = getCommonViewer().getTree().getItems();
        JavaElementWrapper wr = null;
        for (TreeItem item : items) {
            Object obj = item.getData();
            if (obj instanceof JavaElementWrapper
                    && javaElement.equals(((JavaElementWrapper) item
                            .getData()).getJavaElement())) {
                wr = (JavaElementWrapper) item.getData();
            }
        }

        if (wr != null) {
            getCommonViewer().setSelection(new StructuredSelection(wr),
                    true);
            getCommonViewer().reveal(wr);
        }
    }

    public void dispose() {
        super.dispose();
        getSite().getWorkbenchWindow().getSelectionService()
                .removeSelectionListener(this);
    }
}
