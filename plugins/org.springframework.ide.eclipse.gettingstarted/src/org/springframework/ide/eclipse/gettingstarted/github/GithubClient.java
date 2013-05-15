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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.Spring3MappingJacksonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.ide.eclipse.gettingstarted.GettingStartedActivator;
import org.springframework.ide.eclipse.gettingstarted.github.auth.BasicAuthCredentials;
import org.springframework.ide.eclipse.gettingstarted.github.auth.Credentials;
import org.springframework.ide.eclipse.gettingstarted.github.auth.NullCredentials;
import org.springframework.ide.eclipse.gettingstarted.util.IOUtil;
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
	
	private static final boolean DEBUG = false;
	
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

	public static Credentials createDefaultCredentials() {
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
		} else {
			//Try system properties
			String username = System.getProperty("github.user.name");
			String password = System.getProperty("github.user.password");
			if (username!=null && password!=null) {
				return new BasicAuthCredentials(username, password);
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
	public List<Repo> getGuidesRepos() {
		//The guides are all the projects listed under 'springframework-meta' organization.
		Repo[] _repos = getOrgRepos("springframework-meta");
		ArrayList<Repo> repos = new ArrayList<Repo>(_repos.length);
		for (Repo repo : _repos) {
			if (repo.getName().startsWith("gs-")) {
				repos.add(repo);
			}
		}
		return repos;
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
		
		//Add json parsing capability using Jackson mapper.
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new Spring3MappingJacksonHttpMessageConverter());

// The code below doesn't work because by the time we get to use the inputstream RestTemplate will
// have closed it already. A message converter must read the input it is interested in before
// returning... Too bad :-(
		
//		//Add capability to get raw data as an InputStream
//		messageConverters.add(new HttpMessageConverter<InputStream>() {
//
//			@Override
//			public boolean canRead(Class<?> klass, MediaType mt) {
//				return InputStream.class.isAssignableFrom(klass);
//			}
//
//			@Override
//			public boolean canWrite(Class<?> arg0, MediaType mt) {
//				//This message converter is only for getting data not sending.
//				return false;
//			}
//
//			@Override
//			public List<MediaType> getSupportedMediaTypes() {
//				return Arrays.asList(MediaType.ALL);
//			}
//
//			@Override
//			public InputStream read(Class<? extends InputStream> arg0,
//					HttpInputMessage response) throws IOException,
//					HttpMessageNotReadableException {
//				return response.getBody();
//			}
//
//			@Override
//			public void write(InputStream arg0, MediaType arg1,
//					HttpOutputMessage arg2) throws IOException,
//					HttpMessageNotWritableException {
//				throw new HttpMessageNotWritableException("This message converter is only for reading");
//			}
//			
//		});
		rest.setMessageConverters(messageConverters); 

		return rest;
	}

//	/**
//	 * Add hoc testing code. 
//	 */
//	public static void main(String[] args) throws Exception {
//		GithubClient github = new GithubClient();
//		Repo[] resp = github.getGuidesRepos();
//		
//		for (Repo repo : resp) {
//			System.out.println(repo.getName());
//			System.out.println("   "+repo.getDescription());
//			System.out.println("   "+repo.getUrl());
//		}
//		
//		System.out.println(github.getRateLimit());
//	}

	/**
	 * Retrieve info on remaining API rate limit quota for this client.
	 */
	public RateLimit getRateLimit() {
		//TODO: this implementation makes a request to get the rate limit remaining.
		// but github attaches this info to every response in the response headers.
		// So we could cache it from the last request made through this client instead
		// of making another request to fetch it.
		return get("/rate_limit", RateLimit.class);
	}

	
    /**
     * Download content from a url and save to an outputstream. Use same credentials as
     * other operations in this client. May need to use this to download stuff like
     * zip file from github if the repo it comes from is private.
     */
	public void fetch(URL url, OutputStream writeTo) throws IOException {
		URLConnection conn = null;
		InputStream input = null;
		try {
			conn = url.openConnection();
			credentials.apply(conn);
			conn.connect();
			if (DEBUG) {
				System.out.println(">>> "+url);
				Map<String, List<String>> headers = conn.getHeaderFields();
				for (Entry<String, List<String>> header : headers.entrySet()) {
					System.out.println(header.getKey()+":");
					for (String value : header.getValue()) {
						System.out.println("   "+value);
					}
				}
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			}
			
			input = conn.getInputStream();
			IOUtil.pipe(input, writeTo);
		} finally {
			if (input!=null) {
				try {
					input.close();
				} catch (Throwable e) {
					//ignore.
				}
			}
		}
	}

	
}
