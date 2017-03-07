/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.net.URI;
import java.util.List;

import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.WrappingBootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.ActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RestActuatorClient;
import org.springsource.ide.eclipse.commons.livexp.core.AsyncLiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

import com.google.common.collect.ImmutableList;

public abstract class CloudDashElement<T> extends WrappingBootDashElement<T> {

	public CloudDashElement(BootDashModel bootDashModel, T delegate) {
		super(bootDashModel, delegate);
	}

	private LiveExpression<ImmutableList<RequestMapping>> liveRequestMappings;

	protected ActuatorClient getActuatorClient(URI target) {
		return new RestActuatorClient(target, getTypeLookup(), getRestTemplate());
	}

	@Override
	public List<RequestMapping> getLiveRequestMappings() {
		synchronized (this) {
			if (liveRequestMappings==null) {
				final LiveExpression<URI> actuatorUrl = getActuatorUrl();
				liveRequestMappings = new AsyncLiveExpression<ImmutableList<RequestMapping>>(null, "Fetch request mappings for '"+getName()+"'") {
					protected ImmutableList<RequestMapping> compute() {
						URI target = actuatorUrl.getValue();
						if (target!=null) {
							ActuatorClient client = getActuatorClient(target);
							List<RequestMapping> list = client.getRequestMappings();
							if (list!=null) {
								return ImmutableList.copyOf(client.getRequestMappings());
							}
						}
						return null;
					}

				};
				liveRequestMappings.dependsOn(actuatorUrl);
				addElementState(liveRequestMappings);
				addDisposableChild(liveRequestMappings);
			}
		}
		return liveRequestMappings.getValue();
	}

	protected LiveExpression<URI> getActuatorUrl() {
		return LiveExpression.constant(null);
	}

}
