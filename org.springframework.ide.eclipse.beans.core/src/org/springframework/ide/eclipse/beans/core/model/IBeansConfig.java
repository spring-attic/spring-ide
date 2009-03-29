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
package org.springframework.ide.eclipse.beans.core.model;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This interface provides information for a Spring beans configuration.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IBeansConfig extends IBeansModelElement, IResourceModelElement, IBeanClassAware {

	enum Type {
		MANUAL, AUTO_DETECTED
	}

	String EXTERNAL_FILE_NAME_PREFIX = "external:/";

	String DEFAULT_LAZY_INIT = "false";

	String DEFAULT_AUTO_WIRE = "no";

	String DEFAULT_DEPENDENCY_CHECK = "none";

	String DEFAULT_INIT_METHOD = "";

	String DEFAULT_DESTROY_METHOD = "";

	String DEFAULT_MERGE = "false";

	String getDefaultLazyInit();

	String getDefaultAutowire();

	String getDefaultDependencyCheck();

	String getDefaultInitMethod();

	String getDefaultDestroyMethod();

	String getDefaultMerge();

	Set<IBeansImport> getImports();

	Set<IBeanAlias> getAliases();

	IBeanAlias getAlias(String name);

	Set<IBeansComponent> getComponents();

	Set<IBean> getBeans();

	IBean getBean(String name);

	boolean hasBean(String name);

	/**
	 * Returns <code>true</code> if the underlying resource has been changed
	 * @since 2.0.3
	 */
	boolean resourceChanged();

	/**
	 * Type of this configuration file. Could either be manual or automatic configured
	 * @since 2.0.5
	 */
	Type getType();

	/**
	 * Returns <code>true</code> if the beans structure model is affected by the change to java
	 * model.
	 * <p>
	 * NOTE: this method does not check if the beans structure model needs re-validation; only of the
	 * model elements changed. E.g. because of using the context:compontent-scan functionality. 
	 * @since 2.2.1
	 */
	boolean configAffectedByJavaChange(IResource resource);
}
