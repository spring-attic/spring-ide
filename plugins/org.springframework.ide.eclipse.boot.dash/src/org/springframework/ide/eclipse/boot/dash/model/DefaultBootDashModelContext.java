/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.boot.core.BootPreferences;
import org.springframework.ide.eclipse.boot.core.cli.BootInstallManager;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.ssh.SshTunnelFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.ssh.SshTunnelImpl;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.jmxtunnel.CloudFoundryRemoteBootAppsDataContributor;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.jmxtunnel.JmxSshTunnelManager;
import org.springframework.ide.eclipse.boot.dash.di.EclipseBeanLoader;
import org.springframework.ide.eclipse.boot.dash.di.SimpleDIContext;
import org.springframework.ide.eclipse.boot.dash.metadata.PropertyStoreFactory;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;
import org.springframework.ide.eclipse.boot.dash.remoteapps.RemoteBootAppsDataHolder;
import org.springframework.ide.eclipse.boot.dash.views.DefaultUserInteractions;
import org.springframework.ide.eclipse.boot.dash.views.DefaultUserInteractions.UIContext;
import org.springframework.ide.eclipse.boot.pstore.IPropertyStore;
import org.springframework.ide.eclipse.boot.pstore.IScopedPropertyStore;
import org.springframework.ide.eclipse.boot.pstore.PropertyStores;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * @author Kris De Volder
 */
public class DefaultBootDashModelContext extends BootDashModelContext {

	private IScopedPropertyStore<IProject> projectProperties = PropertyStores.createForProjects();

	private IScopedPropertyStore<RunTargetType> runTargetProperties = PropertyStoreFactory.createForRunTargets();

	private SecuredCredentialsStore securedStore = PropertyStoreFactory.createSecuredCredentialsStore();

	private IPropertyStore viewProperties = PropertyStores.backedBy(BootDashActivator.getDefault().getPreferenceStore());

	private IPropertyStore privateProperties = PropertyStores.createPrivateStore(BootDashActivator.getDefault().getStateLocation().append("private.properties"));

	private BootInstallManager bootInstalls = BootInstallManager.getInstance();


	private static SimpleDIContext createInjections() {
		SimpleDIContext injections = new SimpleDIContext();
		injections.defInstance(UIContext.class, () -> {
			try {
				IWorkbench wb = PlatformUI.getWorkbench();
				CompletableFuture<Shell> shell = new CompletableFuture<>();
				Display d = wb.getDisplay();
				d.syncExec(() -> {
					try {
						IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
						if (win!=null) {
							shell.complete(win.getShell());
						} else {
							shell.complete(d.getActiveShell());
						}
					} catch (Throwable e) {
						shell.completeExceptionally(e);
					}
				});
				return shell.get();
			} catch (Exception e) {
				throw ExceptionUtil.unchecked(e);
			}
		});
		injections.defInstance(UserInteractions.class, new DefaultUserInteractions(injections));
		injections.def(BootDashViewModel.class, BootDashViewModel::new);
		injections.def(RemoteBootAppsDataHolder.class, RemoteBootAppsDataHolder::new);
		injections.def(RemoteBootAppsDataHolder.Contributor.class, CloudFoundryRemoteBootAppsDataContributor::new);
		new EclipseBeanLoader(injections).loadFromExtensionPoint(BootDashActivator.INJECTIONS_EXTENSION_ID);
		return injections;
	}

	public DefaultBootDashModelContext() {
		super(createInjections());
	}

	@Override
	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	@Override
	public IPath getStateLocation() {
		return BootDashActivator.getDefault().getStateLocation();
	}

	@Override
	public ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	@Override
	public IScopedPropertyStore<IProject> getProjectProperties() {
		return projectProperties;
	}

	@Override
	public IScopedPropertyStore<RunTargetType> getRunTargetProperties() {
		return runTargetProperties;
	}

	@Override
	public SecuredCredentialsStore getSecuredCredentialsStore() {
		return securedStore;
	}

	@Override
	public void log(Exception e) {
		BootDashActivator.log(e);
	}

	@Override
	public LiveExpression<Pattern> getBootProjectExclusion() {
		return BootPreferences.getInstance().getProjectExclusionExp();
	}

	@Override
	public IPropertyStore getViewProperties() {
		return viewProperties;
	}

	@Override
	public IPropertyStore getPrivatePropertyStore() {
		return privateProperties;
	}

	@Override
	public BootInstallManager getBootInstallManager() {
		return bootInstalls;
	}

}
