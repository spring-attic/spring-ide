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
package org.springframework.ide.eclipse.boot.dash.azure;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTargetType;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

import com.google.gson.Gson;

public class AzureRunTargetType extends AbstractRunTargetType<AzureTargetParams> implements RemoteRunTargetType<AzureTargetParams> {

	public AzureRunTargetType(SimpleDIContext injections) {
		super(injections, "Azure Spring Cloud");
	}

	@Override
	public boolean canInstantiate() {
		return true;
	}

	@Override
	public void openTargetCreationUi(LiveSetVariable<RunTarget> targets) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public RunTarget<AzureTargetParams> createRunTarget(AzureTargetParams properties) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public ImageDescriptor getIcon() {
		return BootDashAzurePlugin.getImageDescriptor("/icons/azure.png");
	}

	@Override
	public AzureTargetParams parseParams(String serializedTargetParams) {
		//TODO: the code below seems pretty generic. Can we provide a default implementation for it?
		Gson gson = new Gson();
		return gson.fromJson(serializedTargetParams, AzureTargetParams.class);
	}

	@Override
	public String serialize(AzureTargetParams targetParams) {
		//TODO: the code below seems pretty generic. Can we provide a default implementation for it?
		Gson gson = new Gson();
		return gson.toJson(targetParams);
	}
}
