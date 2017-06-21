/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes;

import java.util.Collection;
import java.util.List;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlGraphDeploymentProperties;

import com.google.common.collect.ImmutableList;

/**
 * Data object containing attributes from a manifest.yml related to route bindings.
 * (i.e. any attribute in the manifest.yml file that potentially affects the routes
 * that will be bound to an app on push, is included here.
 * <p>
 * This is intented to be a 'dumb' data object. It should have no smarts around interpreting the
 * meanings of attributes. Its only purpose is to be a container for
 * exactly the values as found in the manifest.mf file.
 *
 * @author Kris De Volder
 */
public class RouteAttributes {

	public static RouteAttributes fromUris(Collection<String> uris) {
		RouteAttributes it = new RouteAttributes();
		if (uris.isEmpty()) {
			it.noRoute = true;
		} else {
			it.routes = ImmutableList.copyOf(uris);
		}
		return it;
	}

	private String appName;
	private List<String> routes;
	private String host;
	private List<String> hosts;
	private String domain;
	private List<String> domains;
	private boolean noHost;
	private boolean noRoute;
	private boolean randomRoute;

	public RouteAttributes(String appName) {
		this.appName = appName;
		this.routes = null;
		this.domain = null;
		this.domains = null;
		this.host = null;
		this.hosts = null;
		this.noRoute = false;
		this.randomRoute = false;
		this.noHost = false;
	}

	public RouteAttributes(YamlGraphDeploymentProperties manifest) {
		this.appName = manifest.getAppName();
		this.routes = manifest.getRawRoutes();
		this.domain = manifest.getRawDomain();
		this.domains = manifest.getRawDomains();
		this.host = manifest.getRawHost();
		this.hosts = manifest.getRawHosts();
		this.noRoute = manifest.getRawNoRoute();
		this.randomRoute = manifest.getRawRandomRoute();
		this.noHost = manifest.getRawNoHost();
	}

	public RouteAttributes(CFApplication app) {
		this.appName = app.getName();
		this.routes = app.getUris();
		this.domain = null;
		this.domains = null;
		this.host = null;
		this.hosts = null;
		this.noRoute = false;
		this.randomRoute = false;
		this.noHost = false;
	}

	public RouteAttributes() {
		// TODO Auto-generated constructor stub
	}

	public List<String> getRoutes() {
		return routes;
	}
	public String getHost() {
		return host;
	}
	public List<String> getHosts() {
		return hosts;
	}
	public String getDomain() {
		return domain;
	}
	public List<String> getDomains() {
		return domains;
	}
	public boolean isNoHost() {
		return noHost;
	}
	public boolean isNoRoute() {
		return noRoute;
	}
	public boolean isRandomRoute() {
		return randomRoute;
	}
	public String getAppName() {
		return appName;
	}
}
