/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.viewers.IFilter;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;

/**
 * Filter for local app elements for the properties section
 *
 * @author Alex Boyko
 *
 */
public class LocalElementFilter implements IFilter {

	@Override
	public boolean select(Object toTest) {
		return toTest instanceof BootDashElement && RunTargetTypes.LOCAL.equals(((BootDashElement)toTest).getTarget().getType());
	}

}
