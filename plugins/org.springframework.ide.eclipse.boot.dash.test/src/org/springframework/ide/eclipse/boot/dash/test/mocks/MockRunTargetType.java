/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import org.eclipse.jface.resource.ImageDescriptor;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveSetVariable;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;

public class MockRunTargetType extends AbstractRunTargetType {

	private boolean requiresCredentials;

	public MockRunTargetType(String name) {
		super(name);
	}

	@Override
	public boolean canInstantiate() {
		return true;
	}

	@Override
	public void openTargetCreationUi(LiveSetVariable<RunTarget> targets) {
	}

	@Override
	public RunTarget createRunTarget(TargetProperties properties) {
		return new MockRunTarget(this, properties, requiresCredentials);
	}

	@Override
	public ImageDescriptor getIcon() {
		return null;
	}

	public void setRequiresCredentials(boolean requires) {
		this.requiresCredentials = requires;
	}

}
