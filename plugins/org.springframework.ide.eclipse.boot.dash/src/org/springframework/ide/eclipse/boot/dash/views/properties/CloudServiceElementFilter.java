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
package org.springframework.ide.eclipse.boot.dash.views.properties;

import org.eclipse.jface.viewers.IFilter;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudServiceInstanceDashElement;

/**
 * Filter for Cloud Service elements
 *
 * @author Alex Boyko
 *
 */
public class CloudServiceElementFilter implements IFilter {

	@Override
	public boolean select(Object toTest) {
		return toTest instanceof CloudServiceInstanceDashElement;
	}

}
