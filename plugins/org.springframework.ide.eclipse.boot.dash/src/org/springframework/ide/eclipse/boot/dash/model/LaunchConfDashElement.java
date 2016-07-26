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

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.views.sections.BootDashColumn;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.launch.util.BootLaunchUtils;
import org.springframework.ide.eclipse.boot.properties.editor.util.ArrayUtils;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

import com.google.common.collect.ImmutableSet;

/**
 * Concrete {@link BootDashElement} that wraps a launch config.
 *
 * @author Kris De Volder
 */
public class LaunchConfDashElement extends AbstractLaunchConfigurationsDashElement<ILaunchConfiguration> implements Deletable {

	private static final BootDashColumn[] COLUMNS = ArrayUtils.remove(LocalRunTarget.DEFAULT_COLUMNS,
			BootDashColumn.DEVTOOLS
	);

	private static final boolean DEBUG = false; //(""+Platform.getLocation()).contains("kdvolder");
	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	@Override
	public BootDashColumn[] getColumns() {
		return COLUMNS;
	}

	public LaunchConfDashElement(LocalBootDashModel bootDashModel, ILaunchConfiguration delegate) {
		super(bootDashModel, delegate);
	}

	@Override
	protected IPropertyStore createPropertyStore() {
		return PropertyStoreFactory.createFor(delegate);
	}

	@Override
	public ImmutableSet<ILaunchConfiguration> getLaunchConfigs() {
		return ImmutableSet.of(delegate);
	}

	@Override
	public IProject getProject() {
		return BootLaunchConfigurationDelegate.getProject(delegate);
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public ImmutableSet<ILaunch> getLaunches() {
		return ImmutableSet.copyOf(BootLaunchUtils.getLaunches(ImmutableSet.of(delegate)));
	}

	@Override
	public void dispose() {
		super.dispose();
		debug("Disposing: "+this);
	}

	@Override
	public BootProjectDashElement getParent() {
		IProject p = getProject();
		if (p!=null) {
			return getBootDashModel().getProjectElementFactory().createOrGet(p);
		}
		return null;
	}

	@Override
	public void delete(UserInteractions ui) {
		try {
			delegate.delete();
		} catch (Exception e) {
			BootDashActivator.log(e);
			ui.errorPopup("Could not delete: '"+getName()+"'", ExceptionUtil.getMessage(e));
		}
	}

}
