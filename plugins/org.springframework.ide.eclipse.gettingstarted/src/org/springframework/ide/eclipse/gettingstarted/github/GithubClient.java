/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.github;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.github.auth.Credentials;
import org.springframework.ide.eclipse.gettingstarted.github.auth.NullCredentials;
import org.springframework.web.client.RestTemplate;

import org.springframework.core.NestedRuntimeException;

/**
 * A GithubClient instance needs to configured with some credentials and then it is able to
 * talk to github using its rest api to obtain information about github repos, users, 
 * organisations etc.
 * 
 * @author Kris De Volder
 */
public class GithubClient {
	
	private Credentials credentials;
	private RestTemplate client;

	/**
	 * Create a GithubClient with default credentials. The default credentials
	 * are a basic authentication username plus password read from a "user.properties"
	 * fetched from the classloader.
	 */
	public GithubClient() {
		this(createDefaultCredentials());
	}
	
	public GithubClient(Credentials c) {
		this.credentials = c;
		this.client= createRestTemplate();
	}

	private static Credentials createDefaultCredentials() {
		InputStream stream = GithubClient.class.getResourceAsStream("user.properties");
		if (stream!=null) {
			try {
				Properties props = new Properties();
				props.load(stream);
				String username = props.getProperty("name");
				String passwd = props.getProperty("passwd");
				return new BasicAuthCredentials(username, passwd);
			} catch (Throwable e) {
				GettingStartedActivator.log(e);
			} finally {
				try {
					stream.close();
				} catch (IOException e) {
					//throw new Error(e);
				}
			}
		}
		return new NullCredentials();
	}
	
	private String addHost(String path) {
		if (!path.startsWith("/")) {
			path = "/"+path;
		}
		return "https://api.github.com"+path;
	}

	/**
	 * Fetch info on the official Spring 'guides' repos hosted on github.
	 */
	public Repo[] getGuidesRepos() {
		//The guides are all the projects listed under 'springframework-meta' organization.
		return getOrgRepos("springframework-meta");
	}

	/**
	 * Fetch info about repos under a given organization.
	 */
	public Repo[] getOrgRepos(String orgName) {
		return get("/orgs/{orgName}/repos", Repo[].class, orgName);
	}

	/**
	 * Helper method to fetch json data from some url (or url template) 
	 * and parse the data into an object of a given type.
	 */
	private <T> T get(String url, Class<T> type, Object... vars) {
		return client.getForObject(addHost(url), type, vars);
	}

	private RestTemplate createRestTemplate() {
		RestTemplate rest = new RestTemplate();
		
		//Add authentication
		rest = credentials.apply(rest);
		
		///Add json parsing capability using Jackson mapper.
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new MappingJacksonHttpMessageConverter());
		rest.setMessageConverters(messageConverters); 

		return rest;
	}

	/**
	 * Add hoc testing code. 
	 */
	public static void main(String[] args) throws Exception {
		GithubClient github = new GithubClient();
		Repo[] resp = github.getGuidesRepos();
		
		for (Repo repo : resp) {
			System.out.println(repo.getName());
			System.out.println("   "+repo.getDescription());
			System.out.println("   "+repo.getUrl());
		}
		
		System.out.println(github.getRateLimit());
	}

	/**
	 * Retrieve info on remaining API rate limit quota for this client.
	 */
	public RateLimit getRateLimit() {
		//TODO: this implementation makes a request to get the rate limit remaining.
		// but github attaches this info to every response in the response headers.
		// So we could cache it from the last request made through this client instead
		// of making a request to fetch it.
		return get("/rate_limit", RateLimit.class);
	}

	
}
