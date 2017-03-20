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
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

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
		setRoute(this.host, this.domain, this.path, this.port);
		return this;
	}

	public CFRouteBuilder host(String host) {
		this.host = host;
		setRoute(this.host, this.domain, this.path, this.port);
		return this;
	}

	public CFRouteBuilder path(String path) {
		this.path = path;
		setRoute(this.host, this.domain, this.path, this.port);
		return this;
	}

	public CFRouteBuilder port(int port) {
		this.port = port;
		setRoute(this.host, this.domain, this.path, this.port);
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

		String matchedDomain = null;
		String matchedHost = null;
		String path = findPath(desiredUrl);
		int port = findPort(desiredUrl);
		String hostDomain = desiredUrl;

		if (port != CFRoute.NO_PORT && StringUtils.hasText(path)) {
			throw ExceptionUtil.coreException(
					"Route: " + desiredUrl + " cannot have both port and path. Port:" + port + " Path:" + path);
		}

		if (StringUtils.hasText(path) && hostDomain.indexOf(path) > 0) {
			path(path);
			hostDomain = hostDomain.substring(0, hostDomain.indexOf(path) - 1);
		} else if (port != CFRoute.NO_PORT && hostDomain.indexOf(Integer.toString(port)) > 0) {
			port(port);
			hostDomain = hostDomain.substring(0, hostDomain.indexOf(Integer.toString(port)) - 1);
		}

		matchedDomain = findDomain(hostDomain, domains);

		if (!StringUtils.hasText(matchedDomain)) {
			throw ExceptionUtil.coreException("Unable to parse domain from: " + fullRoute
					+ ". The domain may not exist in the Cloud Foundry target. Please make sure that the URL uses a valid Cloud domain available in the Cloud Foundry target.");
		}
		domain(matchedDomain);

		matchedHost = hostDomain.substring(0, hostDomain.length()-matchedDomain.length());
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
		if (hostDomain.indexOf(".") + 1 >= 0) {
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
		// value is if the route is a tcp route
		String[] segments = desiredUrl.split(":");
		return segments != null && segments.length == 2 ? Integer.parseInt(segments[1]) : CFRoute.NO_PORT;
	}

	/**
	 * This is the "old" version of the parser that may have incorrectly assumed that "routes" are URIs.
	 * Retaining for reference for now. However, {@link #from(String, List)} should be used instead. This should be
	 * deleted once {@link #from(String, List)} is verified as correct
	 * @param fullRoute
	 * @param domains
	 * @return
	 * @throws Exception
	 */
	@Deprecated
	public CFRouteBuilder asHostDomainfrom(String fullRoute, List<CFCloudDomain> domains) throws Exception {
		// String url = domain.getName();
		// url = url.replace("http://", "");
		URI newUri;
		try {
			newUri = URI.create(fullRoute);
		} catch (IllegalArgumentException e) {
			throw ExceptionUtil.exception(e);
		}

		String authority = newUri.getScheme() != null ? newUri.getAuthority() : newUri.getPath();
		String parsedDomainName = null;
		String parsedSubdomainName = null;

		if (authority != null) {
			for (CFCloudDomain domain : domains) {
				// Be sure to check for last segment rather than last String
				// value
				// otherwise: Example: "validdomain" is a valid domain:
				// sub.domainvaliddomain will be parsed
				// successfully as a valid application URL, even though
				// "domainvaliddomain" is not a valid domain. Instead, this
				// should be the correct
				// URL: sub.domain.validdomain. A URL with just "validdomain"
				// should also
				// parse the domain part correctly (but no subdomain)
				String domainName = domain.getName();
				String domainSegment = '.' + domainName;
				if (authority.equals(domainName)) {
					parsedDomainName = domainName;
					break;
				} else if (authority.endsWith(domainSegment)) {
					parsedDomainName = domainName;
					// Any portion of the authority before the separating '.' is
					// the
					// subdomain. To avoid including the separating '.' between
					// subdomain and domain itself as being part of the
					// subdomain, only parse the subdomain if there
					// is an actual '.' before the domain value in the authority
					if (domainSegment.length() < authority.length()) {
						parsedSubdomainName = authority.substring(0, authority.lastIndexOf(domainSegment));
					}
					break;
				}
			}
		}

		if (parsedDomainName == null || parsedDomainName.trim().length() == 0) {
			throw ExceptionUtil.coreException("Unable to parse domain from: " + fullRoute
					+ ". The domain may not exist in the Cloud Foundry target. Please make sure that the URL uses a valid Cloud domain available in the Cloud Foundry target.");
		}
		host(parsedSubdomainName);
		domain(parsedDomainName);
		return this;
	}

	protected void setRoute(String host, String domain, String path, int port) {
		if (host == null && domain == null) {
			return;
		}
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

		// Either path or port, but not both
		if (StringUtils.hasText(path)) {
			writer.append('/');
			writer.append(path);
		} else if (port != CFRoute.NO_PORT) {
			writer.append(':');
			writer.append(Integer.toString(port));
		}

		this.fullRoute = writer.toString();
	}

	public CFRouteBuilder from(String desiredUrl, List<CFCloudDomain> cloudDomains) throws Exception {
		List<String> domains = cloudDomains
								.stream()
								.map(CFCloudDomain::getName)
								.collect(Collectors.toList());
		return from(desiredUrl, domains);
	}


}