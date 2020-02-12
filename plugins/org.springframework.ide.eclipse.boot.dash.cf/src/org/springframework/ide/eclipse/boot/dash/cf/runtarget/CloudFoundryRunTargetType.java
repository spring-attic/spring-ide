/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.runtarget;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.DefaultCloudFoundryClientFactoryV2;
import org.springframework.ide.eclipse.boot.dash.cf.debug.SshTunnelFactory;
import org.springframework.ide.eclipse.boot.dash.cf.devtools.DevtoolsUtil;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.CloudFoundryTargetWizardModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.RunTargetWizard;
import org.springframework.ide.eclipse.boot.dash.cf.jmxtunnel.JmxSshTunnelManager;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.DefaultWizardModelUserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.WizardModelUserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RemoteRunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetTypeFactory;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.util.UiUtil;
import org.springframework.ide.eclipse.boot.util.ProcessTracker;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

import com.google.gson.Gson;

/**
 * @author Kris De Volder
 */
public class CloudFoundryRunTargetType extends AbstractRunTargetType<CloudFoundryTargetProperties> implements RemoteRunTargetType<CloudFoundryTargetProperties>, Disposable {

	public static RunTargetTypeFactory factory = context -> {
		return new CloudFoundryRunTargetType(context, DefaultCloudFoundryClientFactoryV2.INSTANCE);
	};

	private static final ImageDescriptor SMALL_ICON = BootDashActivator.getImageDescriptor("icons/cloud_obj.png");

	private CloudFoundryClientFactory clientFactory;
	private final BootDashModelContext context;
	private WizardModelUserInteractions interactions;
	private AtomicBoolean processTrackerInitialized = new AtomicBoolean(false);
	private ProcessTracker devtoolsProcessTracker;


	private CloudFoundryRunTargetType(BootDashModelContext context, CloudFoundryClientFactory clientFactory) {
		super(context, "Cloud Foundry");
		this.context = context;
		context.injections.assertDefinitionFor(SshTunnelFactory.class);
		context.injections.assertDefinitionFor(JmxSshTunnelManager.class);

		this.clientFactory = clientFactory;
		//TODO: Should be injected and merged with other user interactions, but required too much
		// refactoring to implement in limited time
		this.interactions = new DefaultWizardModelUserInteractions();
	}

	@Override
	public void openTargetCreationUi(LiveSetVariable<RunTarget> targets) {
		CloudFoundryTargetWizardModel model = new CloudFoundryTargetWizardModel(this, clientFactory,
				targets.getValues(), context, interactions);
		RunTargetWizard wizard = new RunTargetWizard(model);
		Shell shell = UiUtil.getShell();
		if (shell != null) {
			WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() == Dialog.OK) {
				RunTarget target = wizard.getRunTarget();
				if (target != null) {
					targets.add(target);
				}
			}
		}
	}

	@Override
	public boolean canInstantiate() {
		return true;
	}

	@Override
	public RunTarget createRunTarget(CloudFoundryTargetProperties props) {
		ensureProcessTracker();
		return props instanceof CloudFoundryTargetProperties
				? new CloudFoundryRunTarget((CloudFoundryTargetProperties) props, this, clientFactory)
				: new CloudFoundryRunTarget(new CloudFoundryTargetProperties(props, this, context), this, clientFactory);
	}


	private void ensureProcessTracker() {
		if (processTrackerInitialized.compareAndSet(false, true)) {
			context.injections.whenCreated(BootDashViewModel.class, (model) -> {
				devtoolsProcessTracker = DevtoolsUtil.createProcessTracker(model);
			});
		}
	}

	@Override
	public ImageDescriptor getIcon() {
		return SMALL_ICON;
	}

	@Override
	public String getDefaultNameTemplate() {
		return "%o : %s - [%a]";
	}

	@Override
	public String getTemplateHelpText() {
		return
				"Enter a template pattern. The following variable substitution are defined:\n" +
				"   '%u': username\n" +
				"   '%o': organization\n" +
				"   '%s': space\n" +
				"   '%a': API URL\n" +
				"\n" +
				"To escape a variable name simply repeat the '%' sign. E.g. '%%u'";
	}

	public static CloudFoundryRunTargetType withClient(BootDashModelContext context, CloudFoundryClientFactory clientFactory) {
		return new CloudFoundryRunTargetType(context, clientFactory);
	}

	@Override
	public void dispose() {
		if (devtoolsProcessTracker != null) {
			devtoolsProcessTracker.dispose();
			devtoolsProcessTracker = null;
		}
	}

	@Override
	public CloudFoundryTargetProperties parseParams(String serializedTargetParams) {
		Gson gson = new Gson();
		@SuppressWarnings("unchecked")
		Map<String,String> map = gson.fromJson(serializedTargetParams, Map.class);
		return new CloudFoundryTargetProperties(new TargetProperties(map, this), this, context);
	}

	@Override
	public String serialize(CloudFoundryTargetProperties props) {
		return props.toJson();
	}
}
