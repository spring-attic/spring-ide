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
package org.springframework.ide.eclipse.cft.applications;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cft.server.core.internal.ApplicationUrlLookupService;
import org.eclipse.cft.server.core.internal.CloudFoundryServer;
import org.eclipse.cft.server.core.internal.CloudServerUtil;
import org.eclipse.cft.server.core.internal.StringUtils;
import org.eclipse.cft.server.core.internal.client.CloudFoundryApplicationModule;
import org.eclipse.cft.server.ui.DefaultApplicationWizardDelegate;
import org.eclipse.cft.server.ui.internal.wizards.ApplicationWizardDelegate;
import org.eclipse.cft.server.ui.internal.wizards.ApplicationWizardDescriptor;
import org.eclipse.cft.server.ui.internal.wizards.CloudFoundryApplicationEnvVarWizardPage;
import org.eclipse.cft.server.ui.internal.wizards.CloudFoundryApplicationServicesWizardPage;
import org.eclipse.cft.server.ui.internal.wizards.CloudFoundryApplicationWizardPage;
import org.eclipse.cft.server.ui.internal.wizards.CloudFoundryDeploymentWizardPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;

public class SpringBootAppWizardDelegate extends DefaultApplicationWizardDelegate {

	protected void createPages(ApplicationWizardDescriptor descriptor, IServer server, IModule module,
			List<IWizardPage> defaultPages) throws CoreException {
		CloudFoundryServer cloudServer = CloudServerUtil.getCloudServer(server);
		CloudFoundryApplicationModule applicationModule = cloudServer.getExistingCloudModule(module);
		ApplicationUrlLookupService urllookup = ApplicationUrlLookupService.getCurrentLookup(cloudServer);

		SpringBootDeploymentPage deploymentPage = new SpringBootDeploymentPage(cloudServer,
				applicationModule, descriptor, urllookup, this);

		CloudFoundryApplicationWizardPage applicationNamePage = new CloudFoundryApplicationWizardPage(cloudServer,
				applicationModule, descriptor);

		defaultPages.add(applicationNamePage);

		defaultPages.add(deploymentPage);

		CloudFoundryApplicationServicesWizardPage servicesPage = new CloudFoundryApplicationServicesWizardPage(
				cloudServer, applicationModule, descriptor);

		defaultPages.add(servicesPage);

		defaultPages.add(new CloudFoundryApplicationEnvVarWizardPage(cloudServer, descriptor.getDeploymentInfo()));
	}

	public class SpringBootDeploymentPage extends CloudFoundryDeploymentWizardPage {

		public SpringBootDeploymentPage(CloudFoundryServer server, CloudFoundryApplicationModule module,
				ApplicationWizardDescriptor descriptor, ApplicationUrlLookupService urlLookup,
				ApplicationWizardDelegate delegate) {
			super(server, module, descriptor, urlLookup, delegate);
		}

		@Override
		protected void setUrlInDescriptor(String url) {

			if (StringUtils.isEmpty(url)) {
				// Set an empty list if URL is empty as it can cause problems
				// when
				// deploying a standalone application
				List<String> urls = new ArrayList<String>();

				descriptor.getDeploymentInfo().setUris(urls);
				return;
			}
			super.setUrlInDescriptor(url);
		}

	}
}
