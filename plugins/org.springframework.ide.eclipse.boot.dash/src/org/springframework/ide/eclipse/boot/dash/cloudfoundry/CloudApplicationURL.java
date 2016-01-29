/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.io.StringWriter;
import java.net.URI;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.runtime.CoreException;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

/**
 * Application URL that can only be defined by subdomain and domain segments.
 * Unparsed Application URLs are not meant to be supported by this
 * representation.
 *
 */
public class CloudApplicationURL {

	private String subdomain;

	private String domain;

	private String url;

	public CloudApplicationURL(String subdomain, String domain) {
		this.subdomain = subdomain;
		this.domain = domain;
		url = getSuggestedApplicationURL(subdomain, domain);
	}

	protected String getSuggestedApplicationURL(String subdomain, String domain) {
		if (subdomain == null && domain == null) {
			return null;
		}
		StringWriter writer = new StringWriter();
		if (subdomain != null) {
			writer.append(subdomain);
			if (domain != null) {
				writer.append('.');
			}
		}
		if (domain != null) {
			writer.append(domain);
		}

		return writer.toString();
	}

	/**
	 * Subdomain or host is generally the first segments of a URL appended to a known
	 * domain: "subdomain.domain". Example: springmvcapp.cfapps.io ->
	 * springmvcapp is the subdomain if the domain is "cfapps.io"
	 *
	 * @return the first segments not part of a known domain. It may be empty.
	 */
	public String getSubdomain() {
		return subdomain;
	}

	/**
	 * Trailing segments of a URL.
	 *
	 * @return trailing segments of a URL.
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 *
	 * @return full URL with both subdomain and domain appended together.
	 */
	public String getUrl() {
		return url;
	}

	public static CloudApplicationURL getCloudApplicationURL(String url, List<CloudDomain> domains) throws Exception {

		// String url = domain.getName();
		// url = url.replace("http://", "");
		URI newUri;
		try {
			newUri = URI.create(url);
		} catch (IllegalArgumentException e) {
			throw new CoreException(ExceptionUtil.status(e));
		}

		String authority = newUri.getScheme() != null ? newUri.getAuthority() : newUri.getPath();
		String parsedDomainName = null;
		String parsedSubdomainName = null;
		if (authority != null) {
			for (CloudDomain domain : domains) {
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
		return new CloudApplicationURL(parsedSubdomainName, parsedDomainName);
	}

	/*
	 * GENERATED
	 */

	@Override
	public String toString() {
		return "CloudApplicationURL [subdomain=" + subdomain + ", domain=" + domain + ", url=" + url + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain == null) ? 0 : domain.hashCode());
		result = prime * result + ((subdomain == null) ? 0 : subdomain.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		CloudApplicationURL other = (CloudApplicationURL) obj;
		if (domain == null) {
			if (other.domain != null)
				return false;
		} else if (!domain.equals(other.domain))
			return false;
		if (subdomain == null) {
			if (other.subdomain != null)
				return false;
		} else if (!subdomain.equals(other.subdomain))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
