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

public class CFRouteBuilder {
	private String domain;
	private String host;
	private String path;
	private int port = CFRoute.NO_PORT;
	private String fullRoute;

	public CFRoute build() {
		return new CFRoute(this.domain, this.host, this.path, this.port, this.fullRoute);
	}

	public CFRouteBuilder domain(String domain) {
		this.domain = domain;
		// may seem like the more ideal place is to build the full route when
		// building the route, rather than repeating
		// the process each time a domain, host, path or port value is set
		// but the "from" option should be allowed to overwrite the full route
		// as well since it already
		// has the full value. Therefore re-construct the full value if the
		// route is being built piece by piece, but not in from
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

	public CFRouteBuilder from(Route route) {
		// Route doesn't seem to have API to get a port
		this.port = CFRoute.NO_PORT;
		this.domain = route.getDomain();
		this.host = route.getHost();
		this.path = route.getPath();
		this.fullRoute = buildRouteVal(this.host, this.domain, this.path, this.port);
		return this;
	}

	public CFRouteBuilder from(String desiredUrl, Collection<String> domains)  {
		// Based on CLI cf/actors/routes.go and testing CLI directly with
		// different "routes" values:
		// 1. Paths is not allowed in TCP route (valid TCP route:
		// "tcp.spring.io:8888")
		// 2. Ports are not allowed in HTTP route (valid HTTP route:
		// "myapps.cfapps.io/pathToApp/home")
		// 3. Schemes (e.g. "http://") are not allowed in routes values.
		// Anything that has a ":" is assumed to be TCP route followed by a port
		// 4. Route can just be domain, or host and domain
		//
		// Therefore, routes values cannot be treated as URIs or URLs, but a
		// combination of domain, host, path and port
		// NOTE: The validation above doesn't need to take place here. The
		// client or CF will validate correct combinations of routes.
		// However, We may want to implement similar
		// validation to the CF manifest editor though.

		String matchedDomain = null;
		String matchedHost = null;
		String hostAndDomain = desiredUrl;
		String path = null;
		int port = CFRoute.NO_PORT;

		// Split into hostDomain segment, port and path
		String[] pathSegments = hostAndDomain.split("/");
		if (pathSegments != null && pathSegments.length > 0) {
			if (pathSegments.length > 1) {
				path = String.join("/", Arrays.copyOfRange(pathSegments, 1, pathSegments.length));
				// Path has to start with "/" or CF throws exception
				if (!path.startsWith("/")) {
					path("/" + path);
				}
			}
			hostAndDomain = pathSegments[0];
		}

		String[] portSegments = hostAndDomain.split(":");
		if (portSegments != null && portSegments.length > 0) {

			if (portSegments.length == 2) {
				port = Integer.parseInt(portSegments[1]);
				port(port);
			}

			hostAndDomain = portSegments[0];
		}

		matchedDomain = findDomain(hostAndDomain, domains);

		if (matchedDomain != null) {
			domain(matchedDomain);
			matchedHost = hostAndDomain.substring(0, hostAndDomain.length() - matchedDomain.length());
			while (matchedHost.endsWith(".")) {
				matchedHost = matchedHost.substring(0, matchedHost.length() - 1);
			}

			if (StringUtils.hasText(matchedHost)) {
				host(matchedHost);
			}
		} else  {
			// Do a basic split on '.', where first segment is the host, and the rest domain
			String[] remainingSegments = hostAndDomain.split("\\.");
			if (remainingSegments != null && remainingSegments.length > 0) {
				if (remainingSegments.length > 1) {
					String domain = String.join(".", Arrays.copyOfRange(remainingSegments, 1, remainingSegments.length));
					domain(domain);
				}
				host(remainingSegments[0]);
			} else {
				// set the whole remaining value as a host
				host(hostAndDomain);
			}
		}

		// Be sure to set the full route:
		this.fullRoute = desiredUrl;
		return this;
	}

	public static String findDomain(String hostDomain, Collection<String> domains) {
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

	/**
	 * A basic building of a full route value. It performs no validation, just builds based on
	 * whether the parameter are set
	 *
	 * @param host
	 * @param domain
	 * @param path
	 * @param port
	 * @return Route value build with the given components. Empty if all
	 *         components are empty or null.
	 */
	public static String buildRouteVal(String host, String domain, String path, int port) {

		StringWriter writer = new StringWriter();
		if (StringUtils.hasText(host)) {
			writer.append(host);
		}

		if (StringUtils.hasText(domain)) {
			if (StringUtils.hasText(host)) {
				writer.append('.');
			}
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

	public CFRouteBuilder from(String desiredUrl, List<CFCloudDomain> cloudDomains)  {
		List<String> domains = cloudDomains
								.stream()
								.map(CFCloudDomain::getName)
								.collect(Collectors.toList());
		return from(desiredUrl, domains);
	}
}