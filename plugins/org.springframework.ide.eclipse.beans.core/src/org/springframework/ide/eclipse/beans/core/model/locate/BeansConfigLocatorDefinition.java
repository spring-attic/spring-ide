/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.locate;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.core.PersistablePreferenceObjectSupport;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Definition wrapper around an {@link IBeansConfigLocator}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeansConfigLocatorDefinition extends PersistablePreferenceObjectSupport {

	private static final Long DEFAULT_ORDER = 10L;

	private static final String LOCATOR_PREFIX = "locator.enable.";

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String DESCRIPTION_ATTRIBUTE = "description";

	private static final String ENABLED_BY_DEFAULT_ATTRIBUTE = "enabledByDefault";

	private static final String ID_ATTRIBUTE = "id";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String ORDER_ATTRIBUTE = "order";

	private String description;

	private String id;

	private String name;

	private String namespaceUri;

	private Long order;

	private IBeansConfigLocator beansConfigLocator;

	public BeansConfigLocatorDefinition(IConfigurationElement element) throws CoreException {
		init(element);
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNamespaceUri() {
		return namespaceUri;
	}

	@Override
	protected String getPreferenceId() {
		return LOCATOR_PREFIX + this.namespaceUri + "." + this.id;
	}

	public IBeansConfigLocator getBeansConfigLocator() {
		return beansConfigLocator;
	}

	public Long getOrder() {
		return order;
	}

	private void init(IConfigurationElement element) throws CoreException {
		Object builder = element.createExecutableExtension(CLASS_ATTRIBUTE);
		if (builder instanceof IBeansConfigLocator) {
			beansConfigLocator = (IBeansConfigLocator) builder;
		}
		this.namespaceUri = element.getDeclaringExtension().getNamespaceIdentifier();
		this.id = element.getAttribute(ID_ATTRIBUTE);
		this.name = element.getAttribute(NAME_ATTRIBUTE);
		this.description = element.getAttribute(DESCRIPTION_ATTRIBUTE);
		String orderString = element.getAttribute(ORDER_ATTRIBUTE);
		if (StringUtils.hasText(orderString)) {
			this.order = Long.valueOf(orderString);
		}
		else {
			this.order = DEFAULT_ORDER;
		}
		String enabledByDefault = element.getAttribute(ENABLED_BY_DEFAULT_ATTRIBUTE);
		if (enabledByDefault != null) {
			setEnabledByDefault(Boolean.valueOf(enabledByDefault));
		}
		else {
			setEnabledByDefault(true);
		}
	}

	@Override
	protected void onEnablementChanged(boolean isEnabled, IProject project) {
		BeansProject beansProject = (BeansProject) BeansCorePlugin.getModel().getProject(project);
		if (beansProject != null) {
			if (!isEnabled) {
				beansProject.removeAutoDetectedConfigs(getNamespaceUri() + "." + getId());
			}
			else {
				beansProject.reset();
			}
			((BeansModel) beansProject.getElementParent()).notifyListeners(beansProject,
					ModelChangeEvent.Type.CHANGED);
		}
	}

	@Override
	public String toString() {
		return id + " (" + beansConfigLocator.getClass().getName() + ")";
	}

	@Override
	protected boolean hasProjectSpecificOptions(IProject project) {
		return project != null;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansConfigLocatorDefinition)) {
			return false;
		}
		BeansConfigLocatorDefinition that = (BeansConfigLocatorDefinition) other;
		if (!ObjectUtils.nullSafeEquals(this.id, that.id))
			return false;
		return ObjectUtils.nullSafeEquals(this.namespaceUri, that.namespaceUri);
	}
	
	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(namespaceUri);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(id);
		return 12 * hashCode;
	}

}
