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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIException;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin;


/**
 * A utility class for Plugin-related resources handling.
 * @author Pierre-Antoine Grégoire
 */
public final class PluginUtils {

    private PluginUtils() {
    }

    public static IWorkspace getCurrentWorkspace() {
        IWorkspace returnIWorkspace = ResourcesPlugin.getWorkspace();
        return returnIWorkspace;
    }

    public static IWorkbench getWorkbench() {
        IWorkbench returnIWorkbench = SpringCoreUIPlugin.getDefault().getWorkbench();
        return returnIWorkbench;
    }

    public static Object getAdapted(Object toAdapt, Class targetClass) {
    	Object result=null;
    	if(toAdapt instanceof IAdaptable) {
    		result=((IAdaptable) toAdapt).getAdapter(targetClass);
    	}
        return result;
    }

    public static File getFileFromBundle(Bundle bundle, String relativePath) {
        File result = null;
        try {
            URL eclipseURL = Platform.find(bundle, new Path(relativePath));
            if (eclipseURL != null) {
                URL url = Platform.resolve(eclipseURL);
                // patch for spaces in the URL (Microsoft paths)
                String urlPath = StringUtils.replace(url.toExternalForm(), " ", "%20");
                result = new File(new URI(urlPath));
            } else {
                throw new SpringCoreUIException("Impossible to find resource with name \"" + relativePath + "\" in bundle " + bundle.getSymbolicName());
            }
        } catch (URISyntaxException use) {
            throw new SpringCoreUIException("Bad URI Syntax", use);
        } catch (IOException ioe) {
            throw new SpringCoreUIException("IO Problem", ioe);
        }
        return result;
    }

    public static InputStream getStream(Plugin plugin, String relativePath) {
        InputStream result = null;
        try {
            result = plugin.openStream(new Path(relativePath));
        } catch (IOException ioe) {
            throw new SpringCoreUIException("IO Problem", ioe);
        }
        return result;
    }

    public static Shell getActiveShell(AbstractUIPlugin plugin) {
        if (plugin == null)
            return null;
        IWorkbench workBench = plugin.getWorkbench();
        if (workBench == null)
            return null;
        IWorkbenchWindow workBenchWindow = workBench.getActiveWorkbenchWindow();
        if (workBenchWindow == null)
            return null;
        return workBenchWindow.getShell();
    }

    public static URL getPluginInstallationURL(Plugin plugin) {
        URL result = null;
        try {
            result = Platform.resolve(Platform.find(plugin.getBundle(), new Path("")));
        } catch (IOException ioe) {
            throw new SpringCoreUIException("IO Problem", ioe);
        }
        return result;
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

}