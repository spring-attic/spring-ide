package org.springframework.ide.eclipse.aop.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.aop.core.model.IAopModel;
import org.springframework.ide.eclipse.aop.core.model.internal.AopModel;

/**
 * The activator class controls the plug-in life cycle
 */
public class BeansWeavingPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.aop.ui";

	// The shared instance
	private static BeansWeavingPlugin plugin;
	
	/**
	 * The constructor
	 */
	public BeansWeavingPlugin() {
		plugin = this;
	}
	
    private static AopModel model;
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
        model = new AopModel();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
	public static BeansWeavingPlugin getDefault() {
		return plugin;
	}
    
    public static IAopModel getModel() {
        return model;
    }

}
