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
import java.util.UUID;

import org.cloudfoundry.client.lib.domain.CloudOrganization;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudSpace;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

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

	public static List<CFSpace> wrapSpaces(List<CloudSpace> spaces) {
		Builder<CFSpace> builder = ImmutableList.builder();
		for (CloudSpace s : spaces) {
			builder.add(wrap(s));
		}
		return builder.build();
	}

	public static List<CFService> wrapServices(List<CloudService> services) {
		Builder<CFService> builder = ImmutableList.builder();
		for (CloudService s : services) {
			builder.add(wrap(s));
		}
		return builder.build();
	}

	public static CFService wrap(final CloudService s) {
		return new CFService() {
			@Override
			public String getName() {
				return s.getName();
			}
		};
	}

	private static CFSpace wrap(final CloudSpace s) {
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


}
