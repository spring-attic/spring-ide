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
package org.springframework.ide.eclipse.beans.ui.graph.model;

import java.util.List;
import java.util.Map;

import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;

/**
 * Extension interface that can be implemented by clients that want to contribute additional
 * {@link Bean}s and {@link Reference}s to the Bean dependency graph of Spring IDE.
 * <p>
 * This can be used to add dependencies that are not reflected in Spring IDE's structured model and
 * only exist at runtime. So for example dependencies resulting from using auto-wiring.
 * <p>
 * Implementations of this interface do not need to be thread-safe.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IGraphContentExtender {

	/**
	 * Add additional {@link Bean}s and {@link Reference}s to the given {@link Map} and {@link List}
	 * .
	 * <p>
	 * Make sure that the target and source {@link Bean} exists in the <code>beans</code> map for
	 * every newly added {@link Reference}.
	 * @param beans {@link Map} of beans already added from Spring IDE's structured model. It is OK
	 * to add and remove {@link Bean} instances. The key of the map is the element name of the bean
	 * instance ( {@link IBean#getElementName()}).
	 * @param beansReferences list of {@link Reference}s
	 * @param root the element on which the graph is opened. Can be {@link IBean},
	 * {@link IBeansConfig}, {@link IBeansConfigSet}.
	 * @param context the context of the graph. Can be {@link IBean}, {@link IBeansConfig},
	 * {@link IBeansConfigSet}.
	 */
	void addAdditionalBeans(Map<String, Bean> beans, List<Reference> beansReferences,
			IBeansModelElement root, IBeansModelElement context);

}
