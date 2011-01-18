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
package org.springframework.ide.eclipse.mylyn.ui;

import org.eclipse.mylyn.context.ui.InterestFilter;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * Extension of Mylyn's {@link InterestFilter} that understands the Spring Explorer
 * root node {@link ISpringProject}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class SpringExplorerInterestFilter extends InterestFilter {

	@Override
	protected boolean isRootElement(Object object) {
		return object instanceof ISpringProject;
	}
}
