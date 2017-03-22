/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.cloudfoundry.operations.routes.Route;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class CFRouteBuilder {
	private String domain;
	private String host;
	private String path;
	private int port = CFRoute.NO_PORT;
	private String fullRoute;
	private boolean randomPort;

	public CFRoute build() {
		return new CFRoute(this.domain, this.host, this.path, this.port, this.fullRoute, this.randomPort);
	}

	public CFRouteBuilder domain(String domain) {
		this.domain = domain;
		// may seem like the more ideal place is to build the full route when building the route, rather than repeating
		// the process each time a domain, host, path or port value is set
		// but the "from" option should be allowed to overwrite the full route as well since it already
		// has the full value. Therefore re-construct the full value if the route is being built piece by piece, but not in from
		this.fullRoute = buildRouteVal(this.host, this.domain, this.path, this.port);
		return this;
	}

	public CFRouteBuilder host(String host) {
		this.host = host;
		this.fullRoute = buildRouteVal(this.host, this.domain, this.path, this.port);
		return this;
	}

	public CFRouteBuilder path(String path) {
		this.path = path;
		this.fullRoute = buildRouteVal(this.host, this.domain, this.path, this.port);
		return this;
	}

	public CFRouteBuilder port(int port) {
		this.port = port;
		this.fullRoute = buildRouteVal(this.host, this.domain, this.path, this.port);
		return this;
	}

	public CFRouteBuilder randomPort(boolean randomPort) {
		this.randomPort = randomPort;
		return this;
	}

	public CFRouteBuilder from(Route route) {
		// Route doesn't seem to have API to get a port
		int port = CFRoute.NO_PORT;
		domain(route.getDomain());
		host(route.getHost());
		path(route.getPath());
		port(port);
		return this;
	}


	public CFRouteBuilder from(String desiredUrl, Collection<String> domains) throws Exception {
		// Based on CLI cf/actors/routes.go and testing CLI directly with different "routes" values:
		// 1. Paths is not allowed in TCP route (valid TCP route: "tcp.spring.io:8888")
		// 2. Ports are not allowed in HTTP route (valid HTTP route: "myapps.cfapps.io/pathToApp/home")
		// 3. Schemes (e.g. "http://") are not allowed in routes values. Anything that has a ":" is assumed to be TCP route followed by a port
		// 4. Route can just be domain, or host and domain
		//
		// Therefore, routes values cannot be treated as URIs or URLs, but a combination of domain, host, path and port
		// NOTE: The validation above doesn't need to take place here. The client or CF will validate correct combinations of routes.

		String matchedDomain = null;
		String matchedHost = null;
		String path = findPath(desiredUrl);
		int port = findPort(desiredUrl);
		String hostAndDomain = desiredUrl;

		// Remove the port and path parts of the route to get the hostAndDomain combination
		if (StringUtils.hasText(path) && hostAndDomain.indexOf(path) > 0) {
			path(path);
			// one index less than the path will skip the starting '/'
			hostAndDomain = hostAndDomain.substring(0, hostAndDomain.indexOf(path) - 1);
		}
		if (port != CFRoute.NO_PORT && hostAndDomain.indexOf(Integer.toString(port)) > 0) {
			port(port);
			// one index less than the port will skip the starting ':'
			hostAndDomain = hostAndDomain.substring(0, hostAndDomain.indexOf(Integer.toString(port)) - 1);
		}

		matchedDomain = findDomain(hostAndDomain, domains);

		if (!StringUtils.hasText(matchedDomain)) {
			throw ExceptionUtil.coreException("Unable to parse domain from: " + fullRoute
					+ ". The domain may not exist in the Cloud Foundry target. Please make sure that the URL uses a valid Cloud domain available in the Cloud Foundry target.");
		}
		domain(matchedDomain);

		matchedHost = hostAndDomain.substring(0, hostAndDomain.length()-matchedDomain.length());
		// Remove ending '.'
		while (matchedHost.endsWith(".")) {
			matchedHost = matchedHost.substring(0, matchedHost.length()-1);
		}

		if (StringUtils.hasText(matchedHost)) {
			host(matchedHost);
		}

		// Be sure to set the full route:
		this.fullRoute = desiredUrl;
		return this;
	}

	protected String findDomain(String hostDomain, Collection<String> domains) {
		if (hostDomain == null) {
			return null;
		}
		// find exact match
		for (String name : domains) {
			if (hostDomain.equals(name)) {
				return hostDomain;
			}
		}
		// Otherwise split on the first "." and try again
		if (hostDomain.indexOf(".")  >= 0 && hostDomain.indexOf(".") + 1 < hostDomain.length()) {
			String remaining = hostDomain.substring(hostDomain.indexOf(".") + 1, hostDomain.length());
			return findDomain(remaining, domains);
		} else {
	        return null;
		}
	}

	protected String findPath(String desiredUrl) {
		String[] segments = desiredUrl.split("/");
		return segments!=null && segments.length > 1 ? String.join("/", Arrays.copyOfRange(segments, 1, segments.length)) : null;
	}

	protected int findPort(String desiredUrl) {
		// No need to consider schemes like "http://" because route values in
		// manifest.yml do not start with scheme.
		// Based on routes.go code, the only occurrence of a ":" in a routes
		// value is if the route is a tcp route. There should be no other supported occurrence of ":"
		String[] segments = desiredUrl.split(":");
		return segments != null && segments.length == 2 ? Integer.parseInt(segments[1]) : CFRoute.NO_PORT;
	}


	/**
	 * Builds a full route value. Some parts of boot dash need the full route value for compare/merge.
	 *
	 * @param host
	 * @param domain
	 * @param path
	 * @param port
	 */
	protected String buildRouteVal(String host, String domain, String path, int port) {

		StringWriter writer = new StringWriter();
		if (StringUtils.hasText(host)) {
			writer.append(host);
			if (StringUtils.hasText(domain)) {
				writer.append('.');
			}
		}
		if (StringUtils.hasText(domain)) {
			writer.append(domain);
		}
		if (port != CFRoute.NO_PORT) {
			writer.append(':');
			writer.append(Integer.toString(port));
		}
		if (StringUtils.hasText(path)) {
			if (!path.startsWith("/")) {
				writer.append('/');
			}
			writer.append(path);
		}


		return writer.toString();
	}

	public CFRouteBuilder from(String desiredUrl, List<CFCloudDomain> cloudDomains) throws Exception {
		List<String> domains = cloudDomains
								.stream()
								.map(CFCloudDomain::getName)
								.collect(Collectors.toList());
		return from(desiredUrl, domains);
	}
}