/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.springframework.ide.eclipse.boot.util.StringUtil;

/**
 * @author Kris De Volder
 */
public class TagFilterBoxModel extends FilterBoxModel<BootDashElement> {

	@Override
	protected Filter<BootDashElement> createFilterForInput(String text) {
		if (StringUtil.hasText(text)) {
			return new TagSearchFilter(text);
		} else {
			return Filters.acceptAll();
		}
	}

}
