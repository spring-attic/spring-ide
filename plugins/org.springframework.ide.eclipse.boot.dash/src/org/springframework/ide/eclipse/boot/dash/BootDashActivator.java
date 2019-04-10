/*******************************************************************************
 * Copyright (c) 2015, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.BootDashBuildpackHintProvider;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.BuildpackHintGenerator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CfTargetsInfo;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CfTargetsInfo.Target;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CfTargetsInfo.TargetDiagnosticMessages;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTargetType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultClientRequestsV2;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.DefaultBootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypes;
import org.springframework.ide.eclipse.boot.dash.prefs.RemoteAppsPrefs;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.cloudfoundry.manifest.editor.ManifestEditorActivator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableSet;

/**
 * The activator class controls the plug-in life cycle
 */
public class BootDashActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.boot.dash"; //$NON-NLS-1$

	public static final String DT_ICON_ID = "devttools";
	public static final String MANIFEST_ICON = "manifest";
	public static final String CLOUD_ICON = "cloud";
	public static final String REFRESH_ICON = "refresh";
	public static final String SERVICE_ICON = "service";
	public static final String SERVICE_INACTIVE_ICON = "service-inactive";
	public static final String BOOT_ICON = "boot";
	public static final String CHECK_ICON = "check";
	public static final String CHECK_GREYSCALE_ICON = "check-greyscale";

	// NOTE: using ':' to separate the "shorter" part of the message from the longer. The longer part may be shown in the UI by expanding the hover info
	private static final String TARGET_SOURCE = "Boot Dashboard";
	private static final String NO_ORG_SPACE = "Boot Dashboard - No org/space selected: Verify Cloud Foundry target connection in Boot Dashboard or login via 'cf' CLI";
	// Make this a "generic" message, instead of using "Boot Dash" prefix as it shows general instructions when there are not targets
	private static final String NO_TARGETS = "No Cloud Foundry targets found: Create a target in Boot Dashboard or login via 'cf' CLI";
	private static final String CONNECTION_ERROR = "Boot Dashboard - Error connecting to Cloud Foundry target: Verify network connection or that existing target has valid credentials.";


	// The shared instance
	private static BootDashActivator plugin;

	private BootDashViewModel model;

	private final ValueListener<ClientRequests> clientsChangedListener = (exp, client) -> {
		if (client instanceof DefaultClientRequestsV2) {
			addRefreshTokenListener((DefaultClientRequestsV2) client);
		}
	};

	/**
	 * The constructor
	 */
	public BootDashActivator() {
	}

	private IProxyService proxyService;

	public synchronized IProxyService getProxyService() {
		if (proxyService==null) {
			BundleContext bc = getBundle().getBundleContext();
			if (bc!=null) {
				ServiceReference<IProxyService> sr = bc.getServiceReference(IProxyService.class);
				proxyService = bc.getService(sr);
			}
		}
		return proxyService;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// Initialize config for manifest YAML LS
		updateCloudTargetsInManifestEditor(getModel().getRunTargets().getValues());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static BootDashActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Deprecated. Use static methods in {@link Log} instead.
	 */
	@Deprecated public static void log(Throwable e) {
		Log.log(e);
	}

	/**
	 * Deprecated. Use {@link Log}.warn() instead.
	 */
	@Deprecated public static void logWarning(String message) {
		Log.warn(message);
	}

	public BootDashViewModel getModel() {
		if (model==null) {
			DefaultBootDashModelContext context = new DefaultBootDashModelContext();
			model = new BootDashViewModel(context,
					RunTargetTypes.LOCAL,
					new CloudFoundryRunTargetType(context, DefaultCloudFoundryClientFactoryV2.INSTANCE)
					// RunTargetTypes.LATTICE
			);
			ManifestEditorActivator.getDefault().setBuildpackProvider(new BootDashBuildpackHintProvider(model, new BuildpackHintGenerator()));

			model.getRunTargets().addListener(new ValueListener<ImmutableSet<RunTarget>>() {

				@Override
				public void gotValue(LiveExpression<ImmutableSet<RunTarget>> exp, ImmutableSet<RunTarget> value) {
					// On target changes, add the client listener in each target so that when the client changes, a notification is sent
					addClientChangeListeners(value);
				}
			});

//			DebugSelectionListener debugSelectionListener = new DebugSelectionListener(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService());
//			model.addDisposableChild(debugSelectionListener);

			model.getJmxSshTunnelManager().getUrls().addListener((exp, v) -> {
//				System.out.println(">>>>> jmx urls ===");
//				for (String url : exp.getValue()) {
//					System.out.println(url);
//				}
//				System.out.println("<<<<< jmx urls ===");
				sendRemoteBootAppUrls();
			});
			RemoteAppsPrefs.addListener(this::sendRemoteBootAppUrls);
		}
		return model;
	}

	private void sendRemoteBootAppUrls() {
		ImmutableSet.Builder<List<String>> allRemoteApps = ImmutableSet.builder();
		if (model!=null) {
			allRemoteApps.addAll(model.getJmxSshTunnelManager().getUrls().getValue());
		}
		allRemoteApps.addAll(new RemoteAppsPrefs().getRemoteAppData());

		try {
			Bundle lsBundle = Platform.getBundle("org.springframework.tooling.boot.ls");
			if (lsBundle != null && lsBundle.getState() != Bundle.INSTALLED) {
				Class<?> lsClass = lsBundle.loadClass("org.springframework.tooling.boot.ls.BootLanguageServerPlugin");
				Method lsMethod = lsClass.getMethod("getRemoteBootApps");
				@SuppressWarnings("unchecked")
				LiveSetVariable<List<String>> remoteBootAppsVar = (LiveSetVariable<List<String>>) lsMethod.invoke(null);
				remoteBootAppsVar.replaceAll(allRemoteApps.build());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateCloudTargetsInManifestEditor(ImmutableSet<RunTarget> value) {
		Set<RunTarget> toUpdate = value == null ? ImmutableSet.of() : value;

		CfTargetsInfo targetsInfo = asTargetsInfo(toUpdate);
		ManifestEditorActivator.getDefault().setCfTargetsInfo(targetsInfo);
	}

	/**
	 * Add a listener to be notified when the refresh token becomes available OR
	 * changes
	 */
	private void addRefreshTokenListener(DefaultClientRequestsV2 client) {
		if (client != null && client.getRefreshTokens() != null && this.model != null
				&& this.model.getRunTargets() != null) {
			client.getRefreshTokens().doOnNext((token) ->
			        // Although the refresh token change is for ONE client (i.e. one target)
			        // compute cloud target information for ALL currently connected targets
			        // as the manifest editor is updated with the full up-to-date list of connected
			        // boot dash targets
					updateCloudTargetsInManifestEditor(this.model.getRunTargets().getValue())
				).subscribe();
			client.getRefreshTokens().doOnComplete(() ->
				updateCloudTargetsInManifestEditor(this.model.getRunTargets().getValue())
			).subscribe();
		}
	}

	private void addClientChangeListeners(ImmutableSet<RunTarget> targets) {
		if (targets != null) {
			for (RunTarget runTarget : targets) {
				if (runTarget instanceof CloudFoundryRunTarget) {
					((CloudFoundryRunTarget) runTarget).addConnectionStateListener(clientsChangedListener);
				}
			}
		}
	}

	private CfTargetsInfo asTargetsInfo(Collection<RunTarget> targets) {
		List<CfTargetsInfo.Target> collectedTargets = new ArrayList<>();
		for (RunTarget runTarget : targets) {
			if (runTarget instanceof CloudFoundryRunTarget) {

				CloudFoundryRunTarget cloudFoundryRunTarget = (CloudFoundryRunTarget) runTarget;
				if (cloudFoundryRunTarget.isConnected()) {
					String token = cloudFoundryRunTarget.getClient().getRefreshToken();
					if (token != null) {
						CloudFoundryTargetProperties properties = cloudFoundryRunTarget.getTargetProperties();
						String target = properties.getUrl();
						String org = properties.getOrganizationName();
						String space = properties.getSpaceName();
						boolean sslDisabled = properties.skipSslValidation();

						CfTargetsInfo.Target integrationTarget = new Target();

						integrationTarget.setApi(target);
						integrationTarget.setOrg(org);
						integrationTarget.setSpace(space);
						integrationTarget.setSslDisabled(sslDisabled);
						integrationTarget.setRefreshToken(token);
						collectedTargets.add(integrationTarget);
					}
				}
			}
		}

		CfTargetsInfo targetsInfo = new CfTargetsInfo();
		targetsInfo.setCfTargets(collectedTargets);
		targetsInfo.setDiagnosticMessages(getDiagnosticMessages());
		return targetsInfo ;
	}

	private TargetDiagnosticMessages getDiagnosticMessages() {
		TargetDiagnosticMessages messages = new TargetDiagnosticMessages();
		messages.setConnectionError(CONNECTION_ERROR);
		messages.setNoOrgSpace(NO_ORG_SPACE);
		messages.setNoTargetsFound(NO_TARGETS);
		messages.setTargetSource(TARGET_SOURCE);
		return messages;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(DT_ICON_ID, getImageDescriptor("/icons/DT.png"));
		reg.put(CLOUD_ICON, getImageDescriptor("/icons/cloud_obj.png"));
		reg.put(MANIFEST_ICON, getImageDescriptor("icons/selectmanifest.gif"));
		reg.put(REFRESH_ICON, getImageDescriptor("/icons/refresh.png"));
		reg.put(SERVICE_ICON, getImageDescriptor("icons/service.png"));
		reg.put(SERVICE_INACTIVE_ICON, getImageDescriptor("icons/service-inactive.png"));
		reg.put(BOOT_ICON, getImageDescriptor("icons/boot-icon.png"));
		reg.put(CHECK_ICON, getImageDescriptor("icons/check.png"));
		reg.put(CHECK_GREYSCALE_ICON, getImageDescriptor("icons/check_greyedout.png"));
	}

	public static IEclipsePreferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(PLUGIN_ID);
	}

}
