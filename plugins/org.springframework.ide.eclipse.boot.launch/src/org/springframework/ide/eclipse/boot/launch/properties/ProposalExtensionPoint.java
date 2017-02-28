/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public class ProposalExtensionPoint {

	private static final String EXTENSION_ID = "org.springframework.ide.eclipse.boot.launch.property.proposals";

	private static final IContentProposal[] NO_PROPOSALS= {};

	private static ContentProposalProviderFactory factory = null;

	public static IContentProposalProvider create(LiveExpression<IProject> project) {
		return getFactory().create(project);
	}

	private synchronized static ContentProposalProviderFactory getFactory() {
		if (factory==null) {
			factory = load();
		}
		return factory;
	}

	private static ContentProposalProviderFactory load() {
		try {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			for (IConfigurationElement e : registry.getConfigurationElementsFor(EXTENSION_ID)) {
				if (factory==null) {
					factory = (ContentProposalProviderFactory)e.createExecutableExtension("class");
				} else {
					Log.error("More than one ContentProposalProviderFactory found. Ignoring all but one of them");
				}
			}
		} catch (Exception e) {
			Log.log(e);
		} finally {
			if (factory==null) {
				Log.error("No ContentProposalProviderFactory found. Using a dummy factory. Content assist proposals in Boot Launch Conf editor won't be available!");
				factory = new ContentProposalProviderFactory() {
					@Override
					public IContentProposalProvider create(LiveExpression<IProject> project) {
						return (contents, pos) -> NO_PROPOSALS;
					}
				};
			}
		}
		return factory;
	}


}
