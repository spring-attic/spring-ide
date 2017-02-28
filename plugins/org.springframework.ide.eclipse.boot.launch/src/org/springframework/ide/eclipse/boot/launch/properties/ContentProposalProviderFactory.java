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
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * An implementation of ContentProposalProviderFactory can be contributed by another
 * plugin via {@link ProposalExtensionPoint}.
 * <p>
 * The factory will be used to create a {@link IContentProposalProvider} to compute
 * content assist proposals for the Spring Boot Launch Configuration editor (i.e. to suggest
 * spring-boot property name completions for the property table in the main tab).
 *
 * @author Kris De Volder
 */
public interface ContentProposalProviderFactory {
	IContentProposalProvider create(LiveExpression<IProject> project);
}
