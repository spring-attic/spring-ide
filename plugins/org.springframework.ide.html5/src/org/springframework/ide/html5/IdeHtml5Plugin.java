package org.springframework.ide.html5;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class IdeHtml5Plugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.html5"; //$NON-NLS-1$

	// The shared instance
	private static IdeHtml5Plugin plugin;

	// private GettingStartedPreferences prefs;

	// private ServiceTracker tracker;

	/**
	 * The constructor
	 */
	public IdeHtml5Plugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static IdeHtml5Plugin getDefault() {
		return plugin;
	}

	public static void warn(String msg) {
		getDefault().getLog().log(createWarningStatus(msg));
	}

	public static void error(String msg, Exception e) {
		getDefault().getLog().log(createErrorStatus(msg, e));
	}

	private static IStatus createWarningStatus(String msg) {
		return new Status(IStatus.WARNING, PLUGIN_ID, msg);
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = exception.getMessage();
		}
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

}
