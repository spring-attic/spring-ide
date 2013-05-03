/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.eclipse.osgi.util.NLS;
import org.springframework.util.Assert;
import org.springsource.ide.eclipse.commons.core.ResourceProvider;

public class NameUrlPair {

	private String urlString;

	private String urlEncodedName;

	public NameUrlPair(String unencodedName, String urlString) throws URISyntaxException {
		setName(unencodedName);
		setUrlString(urlString);
	}

	public NameUrlPair(String encodedString) {
		super();

		String[] strings = encodedString.split(ResourceProvider.FIELD_SEPARATOR);
		if (strings == null || strings.length != 2) {
			urlEncodedName = "";
			urlString = "";
		}
		else {
			urlEncodedName = strings[0]; // should already be encoded here
			urlString = strings[1]; // should already be encoded here
		}
	}

	public String asCombinedString() {
		return urlEncodedName + ResourceProvider.FIELD_SEPARATOR + urlString + ResourceProvider.RECORD_SEPARATOR;
	}

	public String getUrlString() {
		return urlString;
	}

	public void setUrlString(String url) throws URISyntaxException {

		String despacedUrl = url.replace(" ", "%20");
		// encoding the whole URL means that you get URLs that look like
		// http%3C%2F%2Fmydomain%2Fblah%2Fblah%2Fblah.html
		// This is not what we want. The most likely error will be not
		// encoding spaces, so convert + and space to %20

		URI testUri = new URI(despacedUrl);
		// URL testUrl = new URL(url);

		if (testUri.getScheme() == null) {
			throw new URISyntaxException(url, NLS.bind("URL {0} has no protocol", url));
		}
		this.urlString = testUri.toASCIIString();

	}

	public String getName() {

		try {
			return URLDecoder.decode(urlEncodedName, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			// UTF-8 *is* supported, so we should never reach here.
			Assert.isTrue(false);
			return "";
		}
	}

	public void setName(String unencodedName) {
		try {
			this.urlEncodedName = URLEncoder.encode(unencodedName, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			this.urlEncodedName = "";
		}
	}

	public URL getUrl() {
		try {
			URL newUrl = new URL(urlString);
			return newUrl;
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	public void setUrl(URL url) {
		this.urlString = url.toExternalForm();
	}

	// To combine encoded strings into a multiple string, merely concatenate the
	// strings
	public static ArrayList<NameUrlPair> decodeMultipleNameUrlStrings(String nameUrlsString) {
		ArrayList<NameUrlPair> nameUrlPairs = new ArrayList<NameUrlPair>();

		if (nameUrlsString != null && nameUrlsString.length() != 0) {
			String[] nameUrlStrings = nameUrlsString.split(ResourceProvider.RECORD_SEPARATOR_PATTERN);
			for (String nameUrlString : nameUrlStrings) {
				String[] tuple = nameUrlString.split(ResourceProvider.FIELD_SEPARATOR_PATTERN);
				if (tuple.length == 2) {
					try {
						String myUnencodedName = URLDecoder.decode(tuple[0], "UTF-8");
						String myUnencodedUrl = URLDecoder.decode(tuple[1], "UTF-8");
						nameUrlPairs.add(new NameUrlPair(myUnencodedName, myUnencodedUrl));
					}
					catch (UnsupportedEncodingException e) {
						// There shouldn't be any encoding errors at this point.
						Assert.isTrue(false);
					}
					catch (URISyntaxException e) {
						// There shouldn't be any malformed URLs at this
						// point;
						Assert.isTrue(false);
					}
				}
				else {
					if (tuple.length == 1) {
						try {
							nameUrlPairs.add(new NameUrlPair("", tuple[0]));
						}
						catch (URISyntaxException e) {
							// There shouldn't be any malformed URLs at this
							// point;
							Assert.isTrue(false);
						}
					}
					else {
						System.err
								.println("INTERNAL ERROR: Now this is strange: there are either too many strings or not enough.");
					}
				}
			}

		}
		return nameUrlPairs;
	}

	@Override
	public String toString() {
		return asCombinedString().trim();
	}

}