/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.ide.eclipse.core.ui.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIException;


/**
 * Toolbox for the purpose of files manipulations inside Eclipse PDE.
 * 
 * @author Pierre-Antoine Grégoire
 */
public final class FileUtils {
    private FileUtils() {
    }

    /**
     * Copy a file's content to another file.
     * 
     * @param in the file to copy from
     * @param out the file to copy to
     * @throws Exception
     */
    public static void copyFile(File in, File out) throws Exception {
        FileChannel sourceChannel = new FileInputStream(in).getChannel();
        FileChannel destinationChannel = new FileOutputStream(out).getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        sourceChannel.close();
        destinationChannel.close();
    }

    /**
     * Gets the parent project for a system file.
     * 
     * @param file a system file
     * @return the file's project's handle
     */
    public static IProject getParentProjectForFile(File file) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(file.getName());
        if (!project.exists()) {
            project = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.getAbsolutePath())).getProject();
        }
        return project;
    }

    /**
     * Gets the corresponding system file from a workspace file handle.
     * 
     * @param eclipseFile an eclipse file handle
     * @return a system file
     */
    public static File getSystemFile(IFile eclipseFile) {
        return eclipseFile.getLocation().toFile();
    }

    /**
     * Gets the corresponding system file from a workspace path.
     * 
     * @param eclipsePath an eclipse path
     * @return a system file
     */
    public static File getSystemFile(IPath eclipsePath) {
        return eclipsePath.toFile();
    }

    /**
     * Gets the corresponding system file from a workspace folder handle
     * 
     * @param eclipseFolder an eclipse folder handle
     * @return a system file
     */
    public static File getSystemFile(IFolder eclipseFolder) {
        return eclipseFolder.getLocation().toFile();
    }

    public static IProject getProject(IStructuredSelection selection) {
        IProject project = null;
        IResource tmpFile = null;
        if (selection instanceof IStructuredSelection) {
            Object selectedElement = ((IStructuredSelection) selection).getFirstElement();
            if (selectedElement instanceof IResource) {
                tmpFile = (IResource) selectedElement;
                project = tmpFile.getProject();
            }
        }
        return project;
    }

    public static IFile createOrUpdateFile(IProject project, String name, InputStream contents) {
        IFile file = null;
        try {
            file = project.getFile(name);
            if (file.exists()) {
                file.setContents(contents, true, true, null);
            } else {
                file.create(contents, true, null);
            }
        } catch (org.eclipse.core.runtime.CoreException e) {
            throw new SpringCoreUIException(e);
        }
        return file;
    }

    public static IFile getFile(IProject project, String name) {
        IFile file = null;
        file = project.getFile(name);
        if (!file.exists()) {
            file = null;
        }
        return file;
    }
}