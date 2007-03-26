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
package org.springframework.ide.eclipse.webflow.ui.properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.springframework.ide.eclipse.webflow.ui.Activator;

/**
 * Viewer filter for Spring config file selection dialogs.
 * Spring config files are files with file extension 'xml'.
 * The filter is not case sensitive.
 * Folders are only shown if, searched recursivly, contain a config file.
 */
public class FileFilter extends ViewerFilter {

    /**
     * 
     */
    private String[] allowedFileExtensions;

    /**
     * Creates new instance of filter.
     * 
     * @param allowedFileExtensions list of file extension the filter has to
     * recognize or <code>null</code> if all files are allowed
     */
    public FileFilter(String[] allowedFileExtensions) {
        this.allowedFileExtensions = allowedFileExtensions;
    }

    /**
     * 
     */
    public FileFilter() {
        this.allowedFileExtensions = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
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
                Activator.log(e.getStatus());
            }
        }
        return false;
    }

    /**
     * 
     * 
     * @param file 
     * 
     * @return 
     */
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
