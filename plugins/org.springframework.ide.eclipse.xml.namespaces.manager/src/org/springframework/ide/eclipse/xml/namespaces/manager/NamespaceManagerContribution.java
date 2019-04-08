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

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IProject;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionListener;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionResolver;

public class NamespaceManagerContribution implements INamespaceHandlerManager {

	public NamespaceManagerContribution() {
	}

	@Override
	public void unregisterNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		SpringXmlNamespacesManagerPlugin.unregisterNamespaceDefinitionListener(listener);
	}

	@Override
	public void registerNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		SpringXmlNamespacesManagerPlugin.registerNamespaceDefinitionListener(listener);
	}

	@Override
	public void notifyNamespaceDefinitionListeners(IProject project) {
		SpringXmlNamespacesManagerPlugin.notifyNamespaceDefinitionListeners(project);
	}

	@Override
	public INamespaceDefinitionResolver getNamespaceDefinitionResolver() {
		return SpringXmlNamespacesManagerPlugin.getNamespaceDefinitionResolver();
	}

	@Override
	public CompletableFuture<?> nameSpaceHandlersReady() {
		return SpringXmlNamespacesManagerPlugin.nameSpaceHandlersReady;
	}

	@Override
	public NamespaceHandlerResolver getNamespaceHandlerResolver() {
		return SpringXmlNamespacesManagerPlugin.getNamespaceHandlerResolver();
	}
}
