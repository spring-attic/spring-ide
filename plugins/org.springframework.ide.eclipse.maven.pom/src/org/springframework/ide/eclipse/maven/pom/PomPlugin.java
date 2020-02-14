package org.springframework.ide.eclipse.maven.pom;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PomPlugin extends AbstractUIPlugin {

	final public static boolean DEBUG_MODE = true;

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.maven.pom"; //$NON-NLS-1$
	
	public static final String POM_STRUCTURE_ADDITIONS_COMPARE_SETTING = "Pom-Structure-Additions-Only";


	private static PomPlugin plugin;

	public PomPlugin() {
		plugin = this;
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Internal Error", e)); //$NON-NLS-1$
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static PomPlugin getDefault() {
		return plugin;
	}
	
	public static String getPluginId() {
		return getDefault().getBundle().getSymbolicName();
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

}
