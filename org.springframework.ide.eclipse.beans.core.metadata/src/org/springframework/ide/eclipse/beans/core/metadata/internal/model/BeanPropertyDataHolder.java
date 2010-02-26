/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.metadata.internal.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanProperty;
import org.springframework.ide.eclipse.beans.core.internal.model.SerializableRuntimeBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;

/**
 * Internal class to hold {@link IBeanProperty}s for a certain {@link IBean} identified by its
 * elementId.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanPropertyDataHolder implements Serializable {

	private static final long serialVersionUID = 3826359277035252154L;

	private long lastModified = -1;

	private Set<PropertyValue> beanProperties;

	private String elemenetId;

	public Set<IBeanProperty> getBeanProperties() {
		IBean bean = (IBean) BeansCorePlugin.getModel().getElement(getElemenetId());
		Set<IBeanProperty> properties = new LinkedHashSet<IBeanProperty>();
		for (PropertyValue property : beanProperties) {
			RuntimeBeanReference rbr = new RuntimeBeanReference(
					((SerializableRuntimeBeanReference) property.getValue()).getBeanName());
			rbr.setSource(((SerializableRuntimeBeanReference) property.getValue()).getSource());
			PropertyValue pv = new PropertyValue(property.getName(), rbr);
			pv.setSource(((SerializableRuntimeBeanReference) property.getValue()).getSource());
			BeanProperty beanProperty = new BeanProperty(bean, pv);
			properties.add(beanProperty);
		}
		return properties;
	}

	public String getElemenetId() {
		return elemenetId;
	}

	public void setBeanProperties(Set<IBeanProperty> properties) {
		beanProperties = new LinkedHashSet<PropertyValue>();
		for (IBeanProperty property : properties) {
			if (property.getValue() instanceof IBeanReference) {
				IBeanReference reference = (IBeanReference) property.getValue();
				SerializableRuntimeBeanReference rbr = new SerializableRuntimeBeanReference();
				rbr.setBeanName(reference.getBeanName());
				rbr.setToParent(false);
				rbr.setSource(property.getElementSourceLocation());
				beanProperties.add(new PropertyValue(property.getElementName(), rbr));
			}
		}
	}

	public void setElemenetId(String elemenetId) {
		this.elemenetId = elemenetId;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

}
