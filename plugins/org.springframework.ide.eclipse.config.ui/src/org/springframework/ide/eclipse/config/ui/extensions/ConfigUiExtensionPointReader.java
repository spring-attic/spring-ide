/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.extensions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.config.core.extensions.CommonActionsExtensionPointConstants;
import org.springframework.ide.eclipse.config.core.extensions.FormPagesExtensionPointConstants;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;


/**
 * This class serves to load and return all contributions to the extension
 * points defined in the <code>com.springsource.sts.config.ui</code> plug-in.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.1.0
 */
public class ConfigUiExtensionPointReader {

	private static Set<IConfigurationElement> pageDefinitions = new HashSet<IConfigurationElement>();

	private static Set<IConfigurationElement> wizardDefinitions = new HashSet<IConfigurationElement>();

	private static Set<IConfigurationElement> adapterDefinitions = new HashSet<IConfigurationElement>();

	private static boolean read;

	/**
	 * Returns all form page adapter definitions for the configuration editor.
	 * 
	 * @return set of form page adapter definitions for the configuration
	 * editor.
	 */
	public static Set<IConfigurationElement> getAdapterDefinitions() {
		if (!read) {
			readExtensionPoints();
		}
		return adapterDefinitions;
	}

	/**
	 * Returns all page definitions for the configuration editor.
	 * 
	 * @return set of page definitions for the configuration editor
	 */
	public static Set<IConfigurationElement> getPageDefinitions() {
		if (!read) {
			readExtensionPoints();
		}
		return pageDefinitions;
	}

	/**
	 * Returns all best practice wizard definitions for the configuration
	 * editor.
	 * 
	 * @return set of best practice wizard definitions for the configuration
	 * editor
	 */
	public static Set<IConfigurationElement> getWizardDefinitions() {
		if (!read) {
			readExtensionPoints();
		}
		return wizardDefinitions;
	}

	private static void readAdapterDefinitions() {
		IExtensionPoint adaptersExtPoint = Platform.getExtensionRegistry().getExtensionPoint(
				PageAdaptersExtensionPointConstants.POINT_ID);
		if (adaptersExtPoint != null) {
			for (IExtension extension : adaptersExtPoint.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					// required attributes
					String id = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_ID);
					String uri = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI);
					String parent = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_PARENT_URI);

					if (id != null && id.trim().length() > 0 && uri != null && uri.trim().length() > 0
							&& parent != null && parent.trim().length() > 0) {
						adapterDefinitions.add(config);
					}
				}
			}
		}
	}

	private static void readExtensionPoints() {
		readPageDefinitions();
		readAdapterDefinitions();
		readWizardDefinitions();
		read = true;
	}

	private static void readPageDefinitions() {
		IExtensionPoint pagesExtPoint = Platform.getExtensionRegistry().getExtensionPoint(
				FormPagesExtensionPointConstants.POINT_ID);
		if (pagesExtPoint != null) {
			for (IExtension extension : pagesExtPoint.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					// required attributes
					String id = config.getAttribute(FormPagesExtensionPointConstants.ATTR_ID);
					String name = config.getAttribute(FormPagesExtensionPointConstants.ATTR_NAME);
					String clazz = config.getAttribute(FormPagesExtensionPointConstants.ATTR_CLASS);
					String prefix = config.getAttribute(FormPagesExtensionPointConstants.ATTR_NAMESPACE_PREFIX);
					String uri = config.getAttribute(FormPagesExtensionPointConstants.ATTR_NAMESPACE_URI);

					if (id != null && id.trim().length() > 0 && name != null && name.trim().length() > 0
							&& clazz != null && clazz.trim().length() > 0 && prefix != null
							&& prefix.trim().length() > 0 && uri != null && uri.trim().length() > 0) {
						pageDefinitions.add(config);
					}
				}
			}
		}
	}

	private static void readWizardDefinitions() {
		IExtensionPoint templatesExtPoint = Platform.getExtensionRegistry().getExtensionPoint(
				CommonActionsExtensionPointConstants.POINT_ID);
		if (templatesExtPoint != null) {
			for (IExtension extension : templatesExtPoint.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					String id = config.getAttribute(CommonActionsExtensionPointConstants.ATTR_ID);
					String clazz = config.getAttribute(CommonActionsExtensionPointConstants.ATTR_CLASS);
					String desc = config.getAttribute(CommonActionsExtensionPointConstants.ATTR_DESCRIPTION);
					String uri = config.getAttribute(CommonActionsExtensionPointConstants.ATTR_NAMESPACE_URI);

					if (id != null && id.trim().length() > 0 && clazz != null && clazz.trim().length() > 0
							&& desc != null && desc.trim().length() > 0 && uri != null && uri.trim().length() > 0) {
						wizardDefinitions.add(config);
					}
				}
			}
		}
	}

}
