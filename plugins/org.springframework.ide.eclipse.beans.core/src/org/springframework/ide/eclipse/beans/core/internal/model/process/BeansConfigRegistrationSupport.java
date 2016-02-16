/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.process;

import java.util.Collection;

import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigRegistrationSupport;

/**
 * Utility that provides methods to register elements with the
 * {@link BeansConfig}.
 * @author Christian Dupuis
 * @since 2.0
 * @see ReaderEventListener
 */
public class BeansConfigRegistrationSupport implements
		IBeansConfigRegistrationSupport {

	private final Collection<IBean> beans;

	private final ReaderEventListener readerEventListener;

	public BeansConfigRegistrationSupport(final Collection<IBean> beans,
			final ReaderEventListener readerEventListener) {
		this.beans = beans;
		this.readerEventListener = readerEventListener;
	}

	public Collection<IBean> getBeans() {
		return beans;
	}

	public void registerAlias(AliasDefinition aliasDefinition) {
		readerEventListener.aliasRegistered(aliasDefinition);
	}

	public void registerComponent(ComponentDefinition componentDefinition) {
		readerEventListener.componentRegistered(componentDefinition);
	}
}
