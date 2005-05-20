/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.ui.properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.springframework.ide.eclipse.web.flow.ui.WebFlowUIPlugin;

/**
 * Viewer filter for Spring config file selection dialogs.
 * Spring config files are files with file extension 'xml'.
 * The filter is not case sensitive.
 * Folders are only shown if, searched recursivly, contain a config file.
 */
public class FileFilter extends ViewerFilter {

    private String[] allowedFileExtensions;

    /**
     * Creates new instance of filter.
     * 
     * @param allowedFileExtensions list of file extension the filter has to
     *			recognize or <code>null</code> if all files are allowed 
     */
    public FileFilter(String[] allowedFileExtensions) {
        this.allowedFileExtensions = allowedFileExtensions;
    }

    public FileFilter() {
        this.allowedFileExtensions = null;
    }

    public boolean select(Viewer viewer, Object parent, Object element) {
        if (element instanceof IFile) {
            return hasAllowedFileExtension((IFile) element);
        }
        else if (element instanceof IContainer) { // IProject, IFolder
            try {
                IResource[] resources = ((IContainer) element).members();
                for (int i = 0; i < resources.length; i++) {
                    // recursive! Only show containers that contain a configs
                    if (select(viewer, parent, resources[i])) {
                        return true;
                    }
                }
            }
            catch (CoreException e) {
                WebFlowUIPlugin.log(e.getStatus());
            }
        }
        return false;
    }

    private boolean hasAllowedFileExtension(IFile file) {
        if (allowedFileExtensions == null) {
            return true;
        }
        String extension = file.getFileExtension();
        if (extension != null) {
            for (int i = 0; i < allowedFileExtensions.length; i++) {
                if (extension.equalsIgnoreCase(allowedFileExtensions[i])) {
                    return true;
                }
            }
        }
        return false;
    }
}
