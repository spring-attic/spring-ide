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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

public class LegacyProjectsJob extends UIJob {
    
    private final boolean warnIfNone;
    private List<IProject> legacyProjects;
    private final boolean enableToggle;

    public LegacyProjectsJob(boolean warnIfNone, boolean enableToggle) {
        super("Legacy Maven Project Checker");
        this.warnIfNone = warnIfNone;
        this.enableToggle = enableToggle;
    }
    public LegacyProjectsJob(List<IProject> legacyProjects, boolean warnIfNone, boolean enableToggle) {
        this(warnIfNone, enableToggle);
        this.legacyProjects = legacyProjects;
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        monitor.beginTask("Checking for legacy Maven projects", 100);
        IStatus status = doCheck(monitor, getDisplay().getActiveShell());
        monitor.done();
        return status;
    }
    
    private  IStatus doCheck(IProgressMonitor monitor, Shell shell) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        SubMonitor sub = SubMonitor.convert(monitor, 100);
        if (legacyProjects == null) {
            legacyProjects = findLegacyProjects();
        }
        sub.worked(30);
        if (legacyProjects.size() > 0) {
            LegacyProjectConverter converter = new LegacyProjectConverter(legacyProjects);
            if (converter.askToConvert(shell, enableToggle)) {
                return converter.convert(sub.newChild(70));
            } 
        } else if (warnIfNone && !LegacyProjectChecker.NON_BLOCKING) {
            MessageDialog.openInformation(shell, "No legacy projects found", "No legacy projects found.");
        }
        return Status.OK_STATUS;
    }
    
    private List<IProject> findLegacyProjects() {
        List<IProject> legacyProjects = new ArrayList<IProject>();
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : allProjects) {
            try {
                if (project.isAccessible() && project.hasNature(IM2EConstants.OLD_NATURE)) {
                    legacyProjects.add(project);
                }
            } catch (CoreException e) {
                // shouldn't happen since we already know project is accessible
                // don't want to use the regular logging mechanism since that may 
                // load the bundle.
                e.printStackTrace();
            }
        }
        return legacyProjects;
    }


}