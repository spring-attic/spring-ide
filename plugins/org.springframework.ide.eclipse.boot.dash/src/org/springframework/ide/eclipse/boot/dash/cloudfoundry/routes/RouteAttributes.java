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

import java.util.List;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlGraphDeploymentProperties;

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

	private String appName;
	private List<String> routes;
	private String host;
	private List<String> hosts;
	private String domain;
	private List<String> domains;
	private boolean noHost;
	private boolean noRoute;
	private boolean randomRoute;

	public RouteAttributes(YamlGraphDeploymentProperties manifest) {
		this.appName = manifest.getAppName();
		this.routes = manifest.getRoutes();
		this.host = manifest.getRawHost();
		this.hosts = manifest.getRawHosts();
	}

	public List<String> getRoutes() {
		return routes;
	}
	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public List<String> getHosts() {
		return hosts;
	}
	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public List<String> getDomains() {
		return domains;
	}
	public void setDomains(List<String> domains) {
		this.domains = domains;
	}
	public boolean isNoHost() {
		return noHost;
	}
	public void setNoHost(boolean noHost) {
		this.noHost = noHost;
	}
	public boolean isNoRoute() {
		return noRoute;
	}
	public void setNoRoute(boolean noRoute) {
		this.noRoute = noRoute;
	}
	public boolean isRandomRoute() {
		return randomRoute;
	}
	public void setRandomRoute(boolean randomRoute) {
		this.randomRoute = randomRoute;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}

}
