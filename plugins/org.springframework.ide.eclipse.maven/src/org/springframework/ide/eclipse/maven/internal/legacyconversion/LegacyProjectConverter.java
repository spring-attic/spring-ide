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

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.maven.MavenCorePlugin;


/**
 * Converts legacy maven projects to the new m2e
 * @author Andrew Eisenberg
 * @since 2.8.0
 */
class LegacyProjectConverter implements IM2EConstants {
    
    private final List<IProject> allLegacyProjects;
    private IProject[] selectedLegacyProjects;

    public LegacyProjectConverter(List<IProject> legacyProjects) {
        this.allLegacyProjects = legacyProjects;
    }
    
    
    public boolean askToConvert(Shell shell, boolean enableToggle) {
        if (LegacyProjectChecker.NON_BLOCKING) {
            return true;
        }
        
        selectedLegacyProjects = ListMessageDialog.openViewer(shell, allLegacyProjects.toArray(new IProject[0]));
        return selectedLegacyProjects != null;
    }

    public IStatus convert(IProgressMonitor monitor) {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        SubMonitor sub = SubMonitor.convert(monitor, selectedLegacyProjects.length);
        IStatus[] statuses = new IStatus[selectedLegacyProjects.length];
        int i = 0;
        for (IProject project : selectedLegacyProjects) {
            if (project.isAccessible()) {
                sub.subTask("Converting " + project.getName());
                if (sub.isCanceled()) {
                    throw new OperationCanceledException();
                }
                statuses[i++] = convert(project, monitor);
            } else {
                // project was closed before job started.
                statuses[i++] = Status.OK_STATUS;
            }
            sub.worked(1);
        }
        
        
        return new MultiStatus(MavenCorePlugin.PLUGIN_ID, 0, statuses, "Result of converting legacy maven projects", null);
    }


    private IStatus convert(IProject project, IProgressMonitor monitor) {
        SubMonitor sub = SubMonitor.convert(monitor, 1);
        // grab project rule
        Job.getJobManager().beginRule(ResourcesPlugin.getWorkspace().getRoot(), sub);
        try {
            // nature
            IProjectDescription description = project.getDescription();
            String[] ids = description.getNatureIds();
            List<String> newIds = new ArrayList<String>(ids.length);
            for (int i = 0; i < ids.length; i++) {
                if (!ids[i].equals(OLD_NATURE) && !ids[i].equals(NEW_NATURE)) {
                    newIds.add(ids[i]);
                }
            }
            newIds.add(NEW_NATURE);
            description.setNatureIds(newIds.toArray(new String[0]));
            
            // builder
            ICommand[] commands = description.getBuildSpec();
            List<ICommand> newCommands = new ArrayList<ICommand>();
            for (int i = 0; i < commands.length; i++) {
                if (commands[i].getBuilderName().equals(NEW_BUILDER)) {
                    continue;
                }
                if (commands[i].getBuilderName().equals(OLD_BUILDER)) {
                    commands[i].setBuilderName(NEW_BUILDER);
                }
                newCommands.add(commands[i]);
            }
            description.setBuildSpec(newCommands.toArray(new ICommand[0]));
            
            project.setDescription(description, sub);
        } catch (Exception e) {
            return new Status(IStatus.ERROR, MavenCorePlugin.PLUGIN_ID, "Failed to convert " + project.getName(), e);
        } finally {
            // release rule
            Job.getJobManager().endRule(ResourcesPlugin.getWorkspace().getRoot());
        }

        try {
            // classpath container
            IJavaProject javaProject = JavaCore.create(project);
            IClasspathEntry[] classpath = javaProject.getRawClasspath();
            List<IClasspathEntry> newClasspath = new ArrayList<IClasspathEntry>();
            for (int i = 0; i < classpath.length; i++) {
                if (classpath[i].getPath().toString().equals(NEW_CONTAINER)) {
                    continue;
                }
                if (classpath[i].getPath().toString().equals(OLD_CONTAINER)) {
                    classpath[i] = JavaCore.newContainerEntry(new Path(NEW_CONTAINER), classpath[i].getAccessRules(), classpath[i].getExtraAttributes(), classpath[i].isExported());
                }
                newClasspath.add(classpath[i]);
            }
            javaProject.setRawClasspath(newClasspath.toArray(new IClasspathEntry[0]), sub);
        } catch (Exception e) {
            return new Status(IStatus.ERROR, MavenCorePlugin.PLUGIN_ID, "Failed to convert " + project.getName(), e);
        }
            
        sub.worked(1);
        return new Status(IStatus.OK, MavenCorePlugin.PLUGIN_ID, "Converted " + project.getName());
    }


    /**
     * @return
     */
    private String createMessage() {
        StringBuilder sb = new StringBuilder();
        if (allLegacyProjects.size() > 1) {
            sb.append("The following legacy Maven projects have been found:\n");
        } else {
            sb.append("The following legacy Maven project has been found:\n");
        }
        for (IProject project : allLegacyProjects) {
            sb.append("\t" + project.getName() + "\n");
        }
        if (allLegacyProjects.size() > 1) {
            sb.append("\n** These projects will not compile until they are upgraded to M2E version 1.0. **\n\n");
        } else {
            sb.append("\n** This project will not compile until it is upgraded to M2E version 1.0. **\n\n");
        }
        sb.append("Do you want to upgrade now?\n" +
                "You can choose to upgrade later by going to:\n" +
                "Project -> Configure -> Convert legacy Maven projects...");
        return sb.toString();
    }
}
