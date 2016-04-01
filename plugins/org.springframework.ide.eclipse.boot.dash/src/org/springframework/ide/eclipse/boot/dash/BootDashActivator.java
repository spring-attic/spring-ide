/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.DefaultBootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class BootDashActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.dash"; //$NON-NLS-1$

	public static final String DT_ICON_ID = "devttools";
	public static final String MANIFEST_ICON = "manifest";
	public static final String CLOUD_ICON = "cloud";
	public static final String REFRESH_ICON = "refresh";
	public static final String SERVICE_ICON = "service";
	public static final String BOOT_ICON = "boot";
	public static final String CHECK_ICON = "check";
	public static final String CHECK_GREYSCALE_ICON = "check-greyscale";


	// The shared instance
	private static BootDashActivator plugin;

	private BootDashViewModel model;

	/**
	 * The constructor
	 */
	public BootDashActivator() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BootDashActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Deprecated use {@link ExceptionUtil} methods instead.
	 */
	@Deprecated
	public static IStatus createErrorStatus(Throwable exception) {
		String message = exception.getMessage();
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	/**
	 * Deprecated use {@link ExceptionUtil} methods instead.
	 */
	@Deprecated
	public static IStatus createErrorStatus(Throwable exception, String message) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	/**
	 * Deprecated. Use {@link ExceptionUtil}.coreException instead.
	 */
	@Deprecated
	public static CoreException asCoreException(String message) {
		return new CoreException(createErrorStatus(null, message));
	}

	public static void log(Throwable e) {
		try {
			getDefault().getLog().log(createErrorStatus(e));
		} catch (NullPointerException npe) {
			//Can happen if errors are trying to be logged during Eclipse's shutdown
			e.printStackTrace();
		}
	}

	public static void logWarning(String message) {
		if (message == null) {
			message = "";
		}
		getDefault().getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, 0, message, null));
	}

	public BootDashViewModel getModel() {
		if (model==null) {
			DefaultBootDashModelContext context = new DefaultBootDashModelContext();
			model = new BootDashViewModel(context,
					RunTargetTypes.LOCAL,
					new CloudFoundryRunTargetType(context, CloudFoundryClientFactory.DEFAULT)
					// RunTargetTypes.LATTICE
			);
//			DebugSelectionListener debugSelectionListener = new DebugSelectionListener(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService());
//			model.addDisposableChild(debugSelectionListener);
		}
		return model;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(DT_ICON_ID, getImageDescriptor("/icons/DT.png"));
		reg.put(CLOUD_ICON, getImageDescriptor("/icons/cloud_obj.png"));
		reg.put(MANIFEST_ICON, getImageDescriptor("icons/selectmanifest.gif"));
		reg.put(REFRESH_ICON, getImageDescriptor("/icons/refresh.gif"));
		reg.put(SERVICE_ICON, getImageDescriptor("icons/service.png"));
		reg.put(BOOT_ICON, getImageDescriptor("icons/boot-icon.png"));
		reg.put(CHECK_ICON, getImageDescriptor("icons/check.png"));
		reg.put(CHECK_GREYSCALE_ICON, getImageDescriptor("icons/check_greyedout.png"));
	}

}
