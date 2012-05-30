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
package org.springframework.ide.eclipse.maven.internal.legacyconversion;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.springframework.ide.eclipse.maven.MavenCorePlugin;


/**
 * 
 * @author Andrew Eisenberg
 * @since 2.9.0
 */
public class ListMessageDialog extends MessageDialogWithToggle implements IM2EConstants {
    class TableContentProvider implements IStructuredContentProvider {

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof IProject[]) {
                return (IProject[]) inputElement;
            }
            return null;
        }
        
    }
    private static final String PREFERENCE_QUESTION = "Don't show this dialog again.";
    private static final String TITLE = "Should convert legacy Maven projects?";
    private static final String DIALOG_TEXT = 
            "The following legacy Maven projects have been found.\n" +
    		"Do you want them to be automatically upgraded to the new version of M2E (Maven-Eclipse integration)?\n" +
    		"** These projects will not compile until they are upgraded to M2E version 1.0. **" +
    		"You can choose to upgrade later by going to:\n" +
            "Project -> Configure -> Convert legacy Maven projects...";

    private final IProject[] legacyProjects;
    private IProject[] checkedLegacyProjects;
    
    private CheckboxTableViewer viewer;

    /**
     * Opens the legacy maven project conversion dialog focusing on the selected projects
     * @param legacyProjects
     * @return
     */
    public static IProject[] openViewer(Shell shell, IProject[] legacyProjects) {
        ListMessageDialog dialog = new ListMessageDialog(shell, legacyProjects);
        int res = dialog.open();
        MavenCorePlugin.getDefault().getPreferenceStore().setValue(DONT_AUTO_CHECK, dialog.getToggleState());
        if (res == IDialogConstants.YES_ID) {
            return dialog.getAllChecked();
        } else {
            return null;
        }
    }
    
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.YES_ID) {
            Object[] checkedElements = viewer.getCheckedElements();
            checkedLegacyProjects = new IProject[checkedElements.length];
            System.arraycopy(checkedElements, 0, checkedLegacyProjects, 0, checkedElements.length);
        }
        super.buttonPressed(buttonId);
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }

    public ListMessageDialog(Shell shell, IProject[] legacyProjects) {
        super(shell, TITLE, null, DIALOG_TEXT, QUESTION, new String[] { IDialogConstants.YES_LABEL,
                IDialogConstants.NO_LABEL }, 0, PREFERENCE_QUESTION, 
                MavenCorePlugin.getDefault().getPreferenceStore().getBoolean(DONT_AUTO_CHECK));
        this.legacyProjects = legacyProjects;
    }

    protected Control createCustomArea(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns = 2;
        ((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
        
        viewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 150;
        gd.verticalSpan = 2;
        viewer.getTable().setLayoutData(gd);
        viewer.setContentProvider(new TableContentProvider());
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setInput(legacyProjects);
        viewer.setAllChecked(true);
        applyDialogFont(viewer.getControl());
        createButton(parent, "Select all", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(true);
            }
        });
        createButton(parent, "Select none", new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(false);
            }
        });
        return viewer.getControl();
    }
    
    
    protected Button createButton(Composite parent, String label, SelectionListener listener) {
        Button button= new Button(parent, SWT.PUSH);
        button.setFont(parent.getFont());
        button.setText(label);
        button.addSelectionListener(listener);
        GridData gd= new GridData();
        gd.horizontalAlignment= GridData.FILL;
        gd.grabExcessHorizontalSpace= false;
        gd.verticalAlignment= GridData.BEGINNING;
        gd.widthHint = 100;

        button.setLayoutData(gd);

        return button;
    }
    
    IProject[] getAllChecked() {
        return checkedLegacyProjects;
    }
}
