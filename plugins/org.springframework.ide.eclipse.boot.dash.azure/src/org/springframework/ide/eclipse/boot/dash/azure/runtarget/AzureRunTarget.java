/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.azure.runtarget;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.azure.BootDashAzurePlugin;
import org.springframework.ide.eclipse.boot.dash.azure.client.STSAzureClient;
import org.springframework.ide.eclipse.boot.dash.azure.client.SpringServiceClient;
import org.springframework.ide.eclipse.boot.dash.model.AbstractRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTarget;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableSet;

public class AzureRunTarget extends AbstractRunTarget<AzureTargetParams> implements RemoteRunTarget<SpringServiceClient, AzureTargetParams> {

	private final AzureTargetParams params;
	protected final LiveVariable<SpringServiceClient> client = new LiveVariable<>();

	/**
	 * Creates a target in 'connected' state.
	 */
	public AzureRunTarget(AzureRunTargetType type, STSAzureClient client) {
		this(type, client.getTargetParams());
		this.client.setValue(client.getSpringServiceClient());
	}

	/**
	 * Create a target in a not connected state, but with all the info needed to
	 * estabslish a connection (at a later time).
	 */
	public AzureRunTarget(AzureRunTargetType type, AzureTargetParams properties) {
		super(type, properties.getClusterId(), properties.getClusterName());
		this.params = properties;
	}

	@Override
	public ILaunchConfiguration createLaunchConfig(IJavaProject jp, IType mainType) throws Exception {
		throw new UnsupportedOperationException();
	}

	@Override
	public AzureBootDashModel createSectionModel(BootDashModelContext context, BootDashViewModel parent) {
		return new AzureBootDashModel(this, parent);
	}

	@Override
	public boolean canRemove() {
		return true;
	}

	@Override
	public boolean canDeployAppsTo() {
		return true;
	}

	@Override
	public boolean canDeployAppsFrom() {
		return false;
	}

	@Override
	public AzureTargetParams getParams() {
		return params;
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isConnected() {
		SpringServiceClient connection = client.getValue();
		return connection!=null;
	}

	@Override
	public void addConnectionStateListener(ValueListener<SpringServiceClient> l) {
		client.addListener(l);
	}

	@Override
	public void removeConnectionStateListener(ValueListener<SpringServiceClient> l) {
		client.removeListener(l);
	}

	public ImmutableSet<AzureAppElement> fetchApps() {
//		SpringServiceClient client = this.client.getValue();
//		if (client!=null) {
//			client.getSpringManager().apps().listAsync(resourceGroupName, serviceName);
//		}
		return ImmutableSet.of();
	}

	@Override
	public String getDisplayName() {
		return getResourceGroupName() + " : "+getClusterName() + " ["+getSubscriptionName()+"]";
	}

	private String getSubscriptionName() {
		return params.getSubscriptionName();
	}

	private String getClusterName() {
		return params.getClusterName();
	}

	private String getResourceGroupName() {
		String clusterId = params.getClusterId();
		// Example clusterId="/subscriptions/9036e83e-2238-42a4-9b2a-ecd80d4cc38d/resourceGroups/resource-test-dc/providers/Microsoft.AppPlatform/Spring/piggymetrics"
		String[] parts = StringUtils.splitPreserveAllTokens(clusterId, '/');
		return parts[4];
	}

	@Override
	public ImageDescriptor getIcon() {
		if (isConnected()) {
			return getType().getIcon();
		} else {
			return BootDashAzurePlugin.getImageDescriptor("icons/azure-inactive.png");
		}
	}
}
