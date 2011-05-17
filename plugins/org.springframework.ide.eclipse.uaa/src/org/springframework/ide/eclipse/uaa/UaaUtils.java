/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.uaa;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Some helper methods for UAA clases
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public final class UaaUtils {

	public static final String FILE_SCHEME = "file";

	public static final String SOURCE_CONTROL_SCHEME = "sourcecontrol";

	private static boolean DOCUMENT_BUILDER_ERROR = false;

	private static final Object DOCUMENT_BUILDER_LOCK = new Object();

	public static DocumentBuilderFactory getDocumentBuilderFactory() {
		if (!DOCUMENT_BUILDER_ERROR) {
			try {
				// this might fail on IBM J9; therefore trying only once and then falling back to
				// OSGi service reference as it should be
				return DocumentBuilderFactory.newInstance();
			}
			catch (Exception e) {
				UaaPlugin.getDefault().getLog().log(new Status(IStatus.INFO, UaaPlugin.PLUGIN_ID,
					"Error creating DocumentBuilderFactory. Switching to OSGi service reference."));
				DOCUMENT_BUILDER_ERROR = true;
			}
		}

		BundleContext bundleContext = UaaPlugin.getDefault().getBundle().getBundleContext();
		ServiceReference reference = bundleContext.getServiceReference(DocumentBuilderFactory.class.getName());
		if (reference != null) {
			try {
				synchronized (DOCUMENT_BUILDER_LOCK) {
					return (DocumentBuilderFactory) bundleContext.getService(reference);
				}
			}
			catch (Exception e) {
				UaaPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, UaaPlugin.PLUGIN_ID,
					"Unable to obtain document builder factory from OSGi service registry", e));
			}
			finally {
				bundleContext.ungetService(reference);
			}
		}

		return null;
	}

	public static IJavaProject getJavaProject(IResource config) {
		return JavaCore.create(config.getProject());
	}

	public static URI getResourceURI(IResource resource) {
		if (resource != null) {
			URI uri = resource.getRawLocationURI();
			if (uri == null) {
				uri = resource.getLocationURI();
			}
			if (uri != null) {
				String scheme = uri.getScheme();
				if (FILE_SCHEME.equalsIgnoreCase(scheme)) {
					return uri;
				}
				else if (SOURCE_CONTROL_SCHEME.equals(scheme)) {
					// special case of Rational Team Concert
					IPath path = resource.getLocation();
					File file = path.toFile();
					if (file.exists()) {
						return file.toURI();
					}
				}
				else {
					IPathVariableManager variableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
					return variableManager.resolveURI(uri);
				}
			}
		}
		return null;
	}

	/**
	 * Returns the standard display to be used. The method first checks, if the thread calling this method has an
	 * associated display. If so, this display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/**
	 * Returns true if given resource's project is a Java project.
	 */
	public static boolean isJavaProject(IResource resource) {
		if (resource != null && resource.isAccessible()) {
			IProject project = resource.getProject();
			if (project != null) {
				try {
					return project.hasNature(JavaCore.NATURE_ID);
				}
				catch (CoreException e) {
				}
			}
		}
		return false;
	}

	public static void openUrl(String location) {
		openUrl(location, 0);
	}

	private static void openUrl(String location, int customFlags) {
		try {
			URL url = null;

			if (location != null) {
				url = new URL(location);
			}
			if (WebBrowserPreference.getBrowserChoice() == WebBrowserPreference.EXTERNAL) {
				try {
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
					support.getExternalBrowser().openURL(url);
				}
				catch (Exception e) {
				}
			}
			else {
				IWebBrowser browser = null;
				int flags = customFlags;
				if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()) {
					flags |= IWorkbenchBrowserSupport.AS_EDITOR | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}
				else {
					flags |= IWorkbenchBrowserSupport.AS_EXTERNAL | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}

				String generatedId = UaaPlugin.PLUGIN_ID + "-" + Calendar.getInstance().getTimeInMillis();
				browser = WorkbenchBrowserSupport.getInstance().createBrowser(flags, generatedId, null, null);
				browser.openURL(url);
			}
		}
		catch (PartInitException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Browser init error",
					"Browser could not be initiated");
		}
		catch (MalformedURLException e) {
		}
	}
}
