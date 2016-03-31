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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.v2.domains.DomainResource;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationDetail.InstanceDetail;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.PushApplicationRequest.PushApplicationRequestBuilder;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.cloudfoundry.operations.stacks.Stack;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFInstanceState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFInstanceStats;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFOrganization;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFStack;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Various helper methods to 'wrap' objects returned by CF client into
 * our own types, so that we do not directly expose library types to our
 * code.
 *
 * @author Kris De Volder
 */
public class CFWrappingV2 {

	public static CFApplicationDetail wrap(ApplicationDetail details) {
		if (details!=null) {
			return new CFApplicationDetailData(details);
		}
		return null;
	}

	public static CFStack wrap(Stack stack) {
		if (stack!=null) {
			String name = stack.getName();
			return new CFStack() {
				@Override
				public String getName() {
					return name;
				}

				@Override
				public String toString() {
					return "CFStack("+name+")";
				}
			};
		}
		return null;
	}

	public static CFApplicationDetail wrap(CFApplication summary, ApplicationDetail details) {
		if (details==null) {
			//Detail lookup failed. App may not be running and we can't fetch instance data
			return new CFApplicationDetailData(summary);
		} else {
			return new CFApplicationDetailData(details);
		}
	}

	public static CFCloudDomain wrap(DomainResource domainRsrc) {
		if (domainRsrc!=null) {
			String name = domainRsrc.getEntity().getName();
			return new CFCloudDomain() {
				public String getName() {
					return name;
				}
				@Override
				public String toString() {
					return "CFCloudDomain("+name+")";
				}
			};
		}
		return null;
	}

	public static CFInstanceStats wrap(InstanceDetail instanceDetail) {
		return new CFInstanceStats() {
			@Override
			public CFInstanceState getState() {
				try {
					return CFInstanceState.valueOf(instanceDetail.getState());
				} catch (Exception e) {
					BootActivator.log(e);
					return CFInstanceState.UNKNOWN;
				}
			}
		};
	}

	public static CFApplication wrap(ApplicationSummary app) {
		return new CFApplication() {

			@Override
			public String getName() {
				return app.getName();
			}

			@Override
			public List<String> getUris() {
				return app.getUrls();
			}

			@Override
			public Integer getTimeout() {
				//XXX CF V2: getTimeout
				return null;
			}

			@Override
			public String getStack() {
				//XXX CF V2: getStack
				return null;
			}

			@Override
			public List<String> getServices() {
				//XXX CF V2: Application.getServices
				return ImmutableList.of();
			}

			@Override
			public Map<String, String> getEnvAsMap() {
				//XXX CF V2: Application.getEnv
				return ImmutableMap.of();
			}

			@Override
			public String getDetectedBuildpack() {
				//XXX CF V2: Application.getDetectedBuildpack
				return null;
			}

			@Override
			public String getCommand() {
				//XXX CF V2: Application.getCommand
				return null;
			}

			@Override
			public String getBuildpackUrl() {
				//XXX CF V2: Application.getBuildpackUrl
				return null;
			}

			@Override
			public CFAppState getState() {
				try {
					return CFAppState.valueOf(app.getRequestedState());
				} catch (Exception e) {
					BootActivator.log(e);
					return CFAppState.UNKNOWN;
				}
			}

			@Override
			public int getRunningInstances() {
				return app.getRunningInstances();
			}

			@Override
			public int getMemory() {
				return app.getMemoryLimit();
			}

			@Override
			public int getInstances() {
				return app.getInstances();
			}

			@Override
			public UUID getGuid() {
				return UUID.fromString(app.getId());
			}

			@Override
			public int getDiskQuota() {
				return app.getDiskQuota();
			}

			@Override
			public String toString() {
				return "CFApplication("+getName()+")";
			}
		};
	}

	public static CFService wrap(final ServiceInstance service) {
		return new CFService() {

			//XXX CF V2: cf service properties
			// Realign these properties and the corresponding properties page in the GUI as well
			// so that it displays similar infos as what is shown by 'cf service' UI.

			@Override
			public String getName() {
				return service.getName();
			}

			/**
			 * Deprecated because this information is not available in recent
			 * versions of CF. We should replace this info in our views with information
			 * similar to what CF CLI displays instead.
			 */
			@Override
			@Deprecated
			public String getVersion() {
				return null;
			}

			@Override
			public String getType() {
				//XXX CF V2 CFService.getType
				return null;
			}

			/**
			 * Deprecated because this information is not available in recent
			 * versions of CF. We should replace this info in our views with information
			 * similar to what CF CLI displays instead.
			 */
			@Override
			@Deprecated
			public String getProvider() {
				return null;
			}

			@Override
			public String getPlan() {
				return service.getPlan();
			}

			@Override
			public String getDashboardUrl() {
				//TODO: see https://github.com/cloudfoundry/cf-java-client/issues/426
				//Actually... the issue was resolved and then the commit fixing it reverted again
				return null;
			}
		};
	}

	public static CFAppState wrapAppState(String s) {
		try {
			return CFAppState.valueOf(s);
		} catch (Exception e) {
			BootActivator.log(e);
			return CFAppState.UNKNOWN;
		}
	}

	public static CFSpace wrap(OrganizationSummary org, SpaceSummary space) {
		return new CFSpace() {
			@Override
			public String getName() {
				return space.getName();
			}
			@Override
			public CFOrganization getOrganization() {
				return wrap(org);
			}
			@Override
			public UUID getGuid() {
				return UUID.fromString(space.getId());
			}

			@Override
			public String toString() {
				return "CFSpace("+org.getName()+" / "+getName()+")";
			}
		};
	}

	public static CFOrganization wrap(OrganizationSummary org) {
		return new CFOrganization() {
			@Override
			public String getName() {
				return org.getName();
			}

			@Override
			public UUID getGuid() {
				return UUID.fromString(org.getId());
			}
		};
	}

	public static CFBuildpack buildpack(String name) {
		return new CFBuildpack() {
			@Override
			public String getName() {
				return name;
			}
		};
	}

	public static PushApplicationRequestBuilder toPushRequest(CFPushArguments params) {
		return PushApplicationRequest.builder()
		.name(params.getAppName())
		.host(params.getHost())
		.domain(params.getDomain())
		.noRoute(params.isNoRoute())
		.noHostname(params.isNoHost())
		.memory(params.getMemory())
		.diskQuota(params.getDiskQuota())
		.timeout(params.getTimeout())
		.buildpack(params.getBuildpack())
		.command(params.getCommand())
		.stack(params.getStack())
		.instances(params.getInstances())
		.application(params.getApplicationData());
	}
}
