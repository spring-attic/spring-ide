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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudApplication.AppState;
import org.cloudfoundry.client.lib.domain.CloudOrganization;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceInstance;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.cloudfoundry.client.lib.domain.CloudStack;
import org.cloudfoundry.client.lib.domain.Staging;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

/**
 * Static helper methods to wrap data from the cf clients into our own types (thereby avoiding a direct dependency
 * on types from the client library).
 *
 * @author Kris De Volder
 */
public class CFWrapping {

	//TODO: 'wrapifying' everything is a work in progress. Only things we have needed to mock so
	// far have been 'wrapified'.

	public static CFOrganization wrap(final CloudOrganization organization) {
		if (organization!=null) {
			return new CFOrganization() {
				@Override
				public String getName() {
					return organization.getName();
				}

				@Override
				public UUID getGuid() {
					return organization.getMeta().getGuid();
				}
			};
		}
		return null;
	}

	public static List<CFSpace> wrapSpaces(List<CloudSpace> spaces) {
		if (spaces!=null) {
			Builder<CFSpace> builder = ImmutableList.builder();
			for (CloudSpace s : spaces) {
				builder.add(wrap(s));
			}
			return builder.build();
		}
		return null;
	}

	public static List<CFStack> wrapStacks(List<CloudStack> stacks) {
		if (stacks!=null) {
			Builder<CFStack> builder = ImmutableList.builder();
			for (CloudStack s : stacks) {
				builder.add(wrap(s));
			}
			return builder.build();
		}
		return null;
	}

	public static List<CFApplication> wrapApps(List<CloudApplication> apps) {
		if (apps!=null) {
			Builder<CFApplication> builder = ImmutableList.builder();
			for (CloudApplication a : apps) {
				builder.add(wrap(a));
			}
			return builder.build();
		}
		return null;
	}

	public static CFApplication wrap(final CloudApplication a) {
		if (a!=null) {
			return new CFApplicationWrapper(a);
		}
		return null;
	}
	private static class CFApplicationWrapper implements CFApplication {
		private final CloudApplication a;

		public CFApplicationWrapper(CloudApplication a) {
			this.a = a;
		}

		@Override
		public String getName() {
			return a.getName();
		}

		private CloudApplication unwrap() {
			return a;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CFApplicationWrapper) {
				return ((CFApplicationWrapper)obj).unwrap().equals(unwrap());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return a.hashCode();
		}

		@Override
		public int getInstances() {
			return a.getInstances();
		}

		@Override
		public int getRunningInstances() {
			return a.getRunningInstances();
		}

		@Override
		public Map<String, String> getEnvAsMap() {
			return a.getEnvAsMap();
		}

		@Override
		public int getMemory() {
			return a.getMemory();
		}

		@Override
		public UUID getGuid() {
			return a.getMeta().getGuid();
		}

		@Override
		public List<String> getServices() {
			return a.getServices();
		}

		@Override
		public String getBuildpackUrl() {
			Staging s = a.getStaging();
			if (s!=null) {
				return s.getBuildpackUrl();
			}
			return null;
		}

		@Override
		public List<String> getUris() {
			return a.getUris();
		}

		@Override
		public String getDetectedBuildpack() {
			Staging s = a.getStaging();
			if (s!=null) {
				return s.getDetectedBuildpack();
			}
			return null;
		}

		@Override
		public Integer getTimeout() {
			Staging s = a.getStaging();
			if (s!=null) {
				return s.getHealthCheckTimeout();
			}
			return null;
		}

		@Override
		public String getCommand() {
			Staging s = a.getStaging();
			if (s!=null) {
				return s.getCommand();
			}
			return null;
		}

		@Override
		public String getStack() {
			Staging s = a.getStaging();
			if (s!=null) {
				return s.getStack();
			}
			return null;
		}

		@Override
		public AppState getState() {
			return a.getState();
		}

		@Override
		public int getDiskQuota() {
			return a.getDiskQuota();
		}
	}

	public static CloudApplication unwrap(final CFApplication a) {
		if (a!=null) {
			return ((CFApplicationWrapper)a).unwrap();
		}
		return null;
	}

	public static List<CloudApplication> unwrapApps(final List<CFApplication> apps) {
		if (apps!=null) {
			Builder<CloudApplication> builder = ImmutableList.builder();
			for (CFApplication a : apps) {
				builder.add(unwrap(a));
			}
			return builder.build();
		}
		return null;
	}

	public static CFService wrap(final CloudService s, final CloudServiceInstance instance) {
		if (s!=null) {
			return new CFService() {
				@Override
				public String getName() {
					return s.getName();
				}

				@Override
				public String getPlan() {
					return s.getPlan();
				}

				@Override
				public String getProvider() {
					return s.getProvider();
				}

				@Override
				public String getVersion() {
					return s.getVersion();
				}

				@Override
				public String getDashboardUrl() {
					return instance.getDashboardUrl();
				}

				@Override
				public String getType() {
					return instance.getType();
				}

			};
		}
		return null;
	}

	public static CFStack wrap(final CloudStack s) {
		if (s!=null) {
			return new CFStack() {
				@Override
				public String getName() {
					return s.getName();
				}
			};
		}
		return null;
	}

	private static CFSpace wrap(final CloudSpace s) {
		if (s!=null) {
			return new CFSpace() {
				private final CFOrganization org = wrap(s.getOrganization());

				@Override
				public String toString() {
					return getName() + "@" + getOrganization().getName();
				}


				@Override
				public String getName() {
					return s.getName();
				}

				@Override
				public CFOrganization getOrganization() {
					return org;
				}


				@Override
				public UUID getGuid() {
					return s.getMeta().getGuid();
				}
			};
		}
		return null;
	}

	public static Map<CFApplication, ApplicationStats> wrap(Map<CloudApplication, ApplicationStats> stats) {
		if (stats!=null) {
			ImmutableMap.Builder<CFApplication, ApplicationStats> builder = ImmutableMap.builder();
			for (Entry<CloudApplication, ApplicationStats> e : stats.entrySet()) {
				builder.put(wrap(e.getKey()), e.getValue());
			}
			return builder.build();
		}
		return null;
	}

}
