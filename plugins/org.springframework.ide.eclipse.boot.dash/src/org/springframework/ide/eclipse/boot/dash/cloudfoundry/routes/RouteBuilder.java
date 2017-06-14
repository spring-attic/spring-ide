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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFCloudDomainData;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFDomainStatus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

/**
 * This class implements the logics of translating from the multitude of attributes in a
 * `manifest.mf` like 'random-route', 'no-host', 'domain', 'domains', etc. into concrete
 * 'route bindings' that can be passed of to a lower-level cf client operation such as `mapRoute`
 * to bind a route to an app.
 * <p>
 * This process is somewhat complex because of the multitude of attributes, and their interactions,
 * as well as different interpretations of some attributes depending on the target domain being a
 * TCP vs HTTP domain.
 *
 * @author Kris De Volder
 */
public class RouteBuilder {

	private Map<String, CFCloudDomainData> domainsByName = new LinkedHashMap<>();

	public RouteBuilder(Collection<CFCloudDomainData> domains) {
		ImmutableMap.Builder<String, CFCloudDomainData> builder = ImmutableMap.builder();
		for (CFCloudDomainData d : domains) {
			builder.put(d.getName(), d);
		}
		domainsByName = builder.build();
	}

	public List<RouteBinding> buildRoutes(RouteAttributes manifest) {
		if (manifest.isNoRoute()) {
			return ImmutableList.of();
		}
		if (manifest.isRandomRoute()) {
			getDomains(manifest);
		}
		Builder<RouteBinding> builder = ImmutableList.builder();

		if (manifest.getRoutes()!=null) {
			for (String desiredUri : manifest.getRoutes()) {
				builder.add(buildRouteFromUri(desiredUri, manifest));
			}
		}
		return builder.build();
	}

	private void getDomains(RouteAttributes manifest) {
		Set<String> domains = new HashSet<>();
		List<String> ds = manifest.getDomains();
		if (ds!=null) {
			domains.addAll(ds);
		}
		String d = manifest.getDomain();
		if (d!=null) {
			domains.add(d);
		}
	}

	/**
	 * Create a RouteBinding from a 'target' uri. Such a routebinding is always specific
	 * and doesn't contain randomized components (i.e. no random host/port).
	 */
	private RouteBinding buildRouteFromUri(String _uri, RouteAttributes args) {
		ParsedUri uri = new ParsedUri(_uri);
		CFCloudDomainData bestDomain = domainsByName.values().stream()
			.filter(domain -> domainCanBeUsedFor(domain, uri))
			.max((d1, d2) -> Integer.compare(d1.getName().length(), d2.getName().length()))
			.orElse(null);
		if (bestDomain==null) {
			throw new IllegalStateException("No domain matching the given uri '"+_uri+"' could be found");
		}
		RouteBinding route = new RouteBinding();
		route.setDomain(bestDomain.getName());
		route.setHost(bestDomain.splitHost(uri.getHostAndDomain()));
		route.setPort(uri.getPort());
		return route;
	}

	public String getDefaultDomain() {
		return getDomains().stream()
				.filter(d -> d.getStatus()==CFDomainStatus.SHARED && d.getType()==CFDomainType.HTTP)
				.findFirst()
				.map(d -> d.getName())
				.orElse(null);
	}

	private Collection<CFCloudDomainData> getDomains() {
		return domainsByName.values();
	}

	/**
	 * Determines whether a given domain can be used to construct a route for a given
	 * target uri. This depends on a number of factors, such as the type of the domain
	 * (TCP vs HTTP), the type of uri (e.g. whether it has a port component)
	 */
	private boolean domainCanBeUsedFor(CFCloudDomainData domainData, ParsedUri uri) {
		String domain = domainData.getName();
		String hostAndDomain = uri.getHostAndDomain();
		String host;
		if (!hostAndDomain.endsWith(domain)) {
			return false;
		}
		if (domain.length()==hostAndDomain.length()) {
			//The uri matches domain precisely
			host = null;
		} else if (hostAndDomain.charAt(hostAndDomain.length()-domain.length()-1)=='.') {
			//THe uri matches as ${host}.${domain}
			host = hostAndDomain.substring(0, hostAndDomain.length()-domain.length()-1);
		} else {
			 //Couldn't match this domain to uri
			return false;
		}
		if (domainData.getType()==CFDomainType.TCP) {
			return host==null; //TCP routes don't allow setting a host, only a port
		} else if (domainData.getType()==CFDomainType.HTTP) {
			return uri.getPort()==null; //HTTP routes don't allow setting a port only a host
		} else {
			throw new IllegalStateException("Unknown domain type: "+domainData.getType());
		}
	}


}
