package org.springframework.ide.eclipse.boot.validation;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class BootValidationActivator implements BundleActivator {
	
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		BootValidationActivator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		BootValidationActivator.context = null;
	}

	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.validation";
//	public static final String NATURE_ID = PLUGIN_ID+".springbootnature";
	public static final String BUILDER_ID = PLUGIN_ID+".springbootbuilder";

}
