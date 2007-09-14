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
package org.springframework.ide.eclipse.core.internal.model.validation;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.PersistablePreferenceObjectSupport;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.validation.IValidator;

/**
 * Wraps an {@link IValidator} and all the information from it's definition via
 * the corresponding extension point.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class ValidatorDefinition extends PersistablePreferenceObjectSupport {

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String DESCRIPTION_ATTRIBUTE = "description";

	private static final String ENABLED_BY_DEFAULT_ATTRIBUTE = "enabledByDefault";

	private static final String ENABLEMENT_PREFIX = "validator.enable.";

	private static final String ICON_ATTRIBUTE = "icon";

	private static final String ID_ATTRIBUTE = "id";

	private static final String MARKER_ID_ATTRIBUTE = "markerId";

	private static final String NAME_ATTRIBUTE = "name";

	private String description;

	private String iconUri;

	private String id;

	private String markerId;

	private String name;

	private String namespaceUri;

	private IValidator validator;

	public ValidatorDefinition(IConfigurationElement element)
			throws CoreException {
		init(element);
	}

	/**
	 * Delete all problem markers created by this validator in given project.
	 */
	private void cleanup(IProject project) {
		if (!isEnabled(project)) {
			if (project != null) {
				MarkerUtils.deleteMarkers(project, markerId);
			}
			// cleanup projects that use workspace properties
			else {
				Set<ISpringProject> projects = SpringCore.getModel()
						.getProjects();
				for (ISpringProject sproject : projects) {
					IProject p = sproject.getProject();
					if (!hasProjectSpecificOptions(p)) {
						MarkerUtils.deleteMarkers(p, markerId);
					}
				}
			}
		}
	}

	public String getDescription() {
		return description;
	}

	public String getIconUri() {
		return iconUri;
	}

	public String getID() {
		return id;
	}

	public String getMarkerId() {
		return markerId;
	}

	public String getName() {
		return name;
	}

	public String getNamespaceUri() {
		return namespaceUri;
	}

	@Override
	protected String getPreferenceId() {
		return ENABLEMENT_PREFIX + id;
	}

	public IValidator getValidator() {
		return validator;
	}

	private void init(IConfigurationElement element) throws CoreException {
		Object executable = element.createExecutableExtension(CLASS_ATTRIBUTE);
		if (executable instanceof IValidator) {
			validator = (IValidator) executable;
		}
		id = element.getContributor().getName() + "."
				+ element.getAttribute(ID_ATTRIBUTE);
		name = element.getAttribute(NAME_ATTRIBUTE);
		description = element.getAttribute(DESCRIPTION_ATTRIBUTE);
		iconUri = element.getAttribute(ICON_ATTRIBUTE);
		markerId = element.getContributor().getName() + "."
				+ element.getAttribute(MARKER_ID_ATTRIBUTE);
		namespaceUri = element.getDeclaringExtension().getNamespaceIdentifier();
		String enabledByDefault = element
				.getAttribute(ENABLED_BY_DEFAULT_ATTRIBUTE);
		if (enabledByDefault != null) {
			setEnabledByDefault(Boolean.valueOf(enabledByDefault));
		}
		else {
			setEnabledByDefault(true);
		}
	}

	@Override
	protected void onEnablementChanged(boolean isEnabled, IProject project) {
		cleanup(project);
	}

	@Override
	public String toString() {
		return id + " (" + validator.getClass().getName() + ")";
	}
}
