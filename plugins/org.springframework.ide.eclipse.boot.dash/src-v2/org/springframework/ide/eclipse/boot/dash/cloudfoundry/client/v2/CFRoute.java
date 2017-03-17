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

import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import org.cloudfoundry.operations.routes.Route;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class CFRoute {

	public static final int NO_PORT = -1;

	final private String domain;
	final private String host;
	final private String path;
    final private int port;
    // The full constructed route value;
    final private String route;

	public CFRoute(Route route) {
		// V2 client Route doesn't seem to have port API
		this(route.getDomain(), route.getHost(), route.getPath(), NO_PORT);
	}

	public CFRoute(String domain, String host, String path, int port) {
		super();
		this.domain = domain;
		this.host = host;
		this.path = path;
		this.port = port;
		this.route = toRoute(host, domain);
	}

	public String getDomain() {
		return domain;
	}

	public String getHost() {
		return host;
	}

	public String getPath() {
		return path;
	}

	public int getPort() {
		return port;
	}


	public String getRoute() {
		return route;
	}


	protected String toRoute(String host, String domain) {
		if (host == null && domain == null) {
			return null;
		}
		StringWriter writer = new StringWriter();
		if (host != null) {
			writer.append(host);
			if (domain != null) {
				writer.append('.');
			}
		}
		if (domain != null) {
			writer.append(domain);
		}

		return writer.toString();
	}

	@Override
	public String toString() {
		return "CFRoute [domain=" + domain + ", host=" + host + ", path=" + path + ", port=" + port + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + port;
		result = prime * result + ((route == null) ? 0 : route.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CFRoute other = (CFRoute) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (port != other.port)
			return false;
		if (route == null) {
			if (other.route != null)
				return false;
		} else if (!route.equals(other.route))
			return false;
		return true;
	}

	/*
	 *
	 *
	 * Builder and utility methods
	 *
	 *
	 */

	public static CFRoute.Builder builder() {
		return new Builder();
	}

	public static CFRoute toRoute(String url, List<CFCloudDomain> domains) throws Exception {

		// String url = domain.getName();
		// url = url.replace("http://", "");
		URI newUri;
		try {
			newUri = URI.create(url);
		} catch (IllegalArgumentException e) {
			throw ExceptionUtil.exception(e);
		}

		String authority = newUri.getScheme() != null ? newUri.getAuthority() : newUri.getPath();
		String parsedDomainName = null;
		String parsedSubdomainName = null;
		String path = newUri.getPath();
		int port = newUri.getPort();

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
			throw ExceptionUtil.coreException("Unable to parse domain from: " + url
					+ ". The domain may not exist in the Cloud Foundry target. Please make sure that the URL uses a valid Cloud domain available in the Cloud Foundry target.");
		}
		return CFRoute.builder()
				.host(parsedSubdomainName)
				.domain(parsedDomainName)
				.path(path)
				.port(port)
				.build();
	}

	public static class Builder {
		private String domain;
		private String host;
		private String path = "";
		private int port = NO_PORT;

		public CFRoute build() {
			return new CFRoute(this.domain, this.host, this.path, this.port);
		}

		public Builder domain(String domain) {
			this.domain = domain;
			return this;
		}

		public Builder host(String host) {
			this.host = host;
			return this;
		}

		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}
	}

}
