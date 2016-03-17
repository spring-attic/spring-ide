///*******************************************************************************
// * Copyright (c) 2015, 2016 Pivotal, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     Pivotal, Inc. - initial API and implementation
// *******************************************************************************/
//package org.springframework.ide.eclipse.boot.dash.cloudfoundry;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.cloudfoundry.client.lib.domain.CloudApplication;
//import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
//
///**
// * Caches {@link CloudApplication} and associated information, like the project
// * mapping and running app instances and stats, by application name. Fetching an
// * updated cloud application and its instance and stat information can be a
// * long-running process, therefore for responsiveness, a cache is maintained.
// * <p/>
// * It also allows the {@link BootDashElement} for Cloud applications to be a
// * stateless handle, making the element life-cycle management simpler and more
// * likely to be consistent in terms of providing information about the actual
// * application regardless how many times the element is created or deleted.
// * <p/>
// * API is also available to update the cache
// */
//public class CloudAppCache {
//
//	//TODO: when we use v2 client this info is probably integerated with the rest of the appCache.
//	// Then this should be removed.
//	private Map<String, String> healthChecks = new HashMap<>();
//
//
//	public CloudAppCache() {
//	}
//
//
//	public synchronized String getHealthCheck(CloudAppDashElement e) {
//		return healthChecks.get(e.getName());
//	}
//
//	public synchronized void setHealthCheck(CloudAppDashElement e, String healthCheck) {
//		healthChecks.put(e.getName(), healthCheck);
//	}
//
//
//}
