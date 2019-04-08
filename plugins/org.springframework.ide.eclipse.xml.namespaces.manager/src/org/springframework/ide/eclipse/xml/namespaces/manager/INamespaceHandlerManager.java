/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.manager;

import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.ide.eclipse.xml.namespaces.INamespaceManager;

/**
 * Extension of {@link INamespaceManager} that adds functionality related
 * to {@link NamespaceHandlerResolver}.
 * <p>
 * This extension exists to avoid introducing a dependency on a type
 * from Spring Framework into "org.springframework.ide.eclipse.xml.namespaces"
 * plugin directly, while still allowing `org.springframework.ide.eclipse.beans.core`
 * legacy code to an implementation that provides an implementation for the
 * extended api.
 * 
 * @author Kris De Volder
 */
public interface INamespaceHandlerManager extends INamespaceManager {
	NamespaceHandlerResolver getNamespaceHandlerResolver();
}
