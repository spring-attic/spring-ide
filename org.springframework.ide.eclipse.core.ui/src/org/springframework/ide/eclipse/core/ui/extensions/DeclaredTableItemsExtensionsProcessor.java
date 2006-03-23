package org.springframework.ide.eclipse.core.ui.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.WorkbenchException;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin;
import org.springframework.ide.eclipse.core.ui.properties.ITabItemDefinition;
import org.springframework.ide.eclipse.core.ui.properties.PropertyPageTabItems;

/**
 * This class processes the extensions defined in other plugins in order to add them to the common preferences.
 * 
 * @author pagregoire
 */
public final class DeclaredTableItemsExtensionsProcessor {

	private DeclaredTableItemsExtensionsProcessor() {
	}

	/**
	 * The fully-qualified name of the functions extension-point for this plug-in.
	 */
	private static final String EXTENSION_POINT = "org.springframework.ide.eclipse.core.ui.extensions.PropertyPageTabItems";

	/**
	 * Name of the XML attribute designating a function's name.
	 */
	private static final String TAB_NAME_ATTRIBUTE = "name";

	/**
	 * Name of the XML attribute designating the fully-qualified name of the implementation class of a function.
	 */
	private static final String CLASS_ATTRIBUTE = "class";

	private static boolean processed = false;

	/**
	 * Perform initial extension processing for the members of the <code>functions</code> extension-point. Make calls to the user interface module to add the functions of an extension to the UI functions grid. For each function, a virtual proxy callback object is created and handed to the user interface module. The proxy class is a nested top-level class and is therefore known at compile time. The actual (real) callback objects configured into extensions are instantiated and initialized in a lazy fashion by the proxy callback objects.
	 * 
	 * @param grid
	 *            The UI functions grid exposing the functions configured into <code>functions</code> extensions.
	 * 
	 */
	public static void process() throws WorkbenchException, CoreException {
		if (!processed) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			// IExtensionPoint[] extensionPoints = registry.getExtensionPoints();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT);
			if (extensionPoint == null) {
				throw new WorkbenchException("unable to resolve extension-point: " + EXTENSION_POINT);
			}
			IConfigurationElement[] members = extensionPoint.getConfigurationElements();

			// For each service:
			for (int m = 0; m < members.length; m++) {
				IConfigurationElement member = members[m];
				// Get the label of the extender plugin and the ID of the extension.
				String pluginLabel = (String) SpringCoreUIPlugin.getDefault().getBundle().getHeaders().get(org.osgi.framework.Constants.BUNDLE_NAME);
				if (pluginLabel == null) {
					pluginLabel = "[unnamed plugin]";
				}
				// Get the name of the operation implemented by the service.
				// The operation name is a service attribute in the extension's XML specification.
				String tabName = member.getAttribute(TAB_NAME_ATTRIBUTE);
				if (tabName == null) {
					tabName = "[unnamed function]";
				}
				Object callback = null;
				callback = member.createExecutableExtension(CLASS_ATTRIBUTE);
				if (callback == null) {
					throw new WorkbenchException("Impossible to load this extension :" + CLASS_ATTRIBUTE);
				}
				if (callback instanceof ITabItemDefinition) {
					ITabItemDefinition tabItem = (ITabItemDefinition) callback;
					PropertyPageTabItems.addTabItem(tabItem);
				}
			}
			processed = true;
		}
	}

}