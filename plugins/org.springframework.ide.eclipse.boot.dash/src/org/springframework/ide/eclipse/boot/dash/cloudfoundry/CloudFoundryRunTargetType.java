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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.DefaultWizardModelUserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.WizardModelUserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.AbstractRunTargetType;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.TargetProperties;
import org.springframework.ide.eclipse.boot.dash.views.RunTargetWizard;
import org.springsource.ide.eclipse.commons.livexp.core.LiveSetVariable;

/**
 * @author Kris De Volder
 */
public class CloudFoundryRunTargetType extends AbstractRunTargetType {

	private static final ImageDescriptor SMALL_ICON = BootDashActivator.getImageDescriptor("icons/cloud_obj.png");

	private CloudFoundryClientFactory clientFactory;

	private final BootDashModelContext context;

	private WizardModelUserInteractions interactions;

	public CloudFoundryRunTargetType(BootDashModelContext context, CloudFoundryClientFactory clientFactory) {
		super(context, "Cloud Foundry");
		this.context = context;
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
		Shell shell = CloudFoundryUiUtil.getShell();
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
	public RunTarget createRunTarget(TargetProperties props) {
		return props instanceof CloudFoundryTargetProperties
				? new CloudFoundryRunTarget((CloudFoundryTargetProperties) props, this, clientFactory)
				: new CloudFoundryRunTarget(new CloudFoundryTargetProperties(props, this), this, clientFactory);
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
}
