/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.github;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.ide.eclipse.wizard.gettingstarted.github.auth.BasicAuthCredentials;
import org.springframework.ide.eclipse.wizard.gettingstarted.github.auth.Credentials;
import org.springframework.ide.eclipse.wizard.gettingstarted.github.auth.NullCredentials;
import org.springframework.ide.eclipse.wizard.gettingstarted.util.Spring3MappingJacksonHttpMessageConverter;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

/**
 * A GithubClient instance needs to configured with some credentials and then it is able to
 * talk to github using its rest api to obtain information about github repos, users,
 * organisations etc.
 *
 * @author Kris De Volder
 */
public class GithubClient {

	private static final Pattern GITHUB_HOST = Pattern.compile("(.*\\.|)github\\.com");
		//pattern should match 'github.com' and api.github.com'

	private static final int CONNECT_TIMEOUT = 10000;

	private static final boolean DEBUG = false;
	private static final boolean LOG_GITHUB_RATE_LIMIT = false;

	private final Credentials credentials;
	private final RestTemplate client;

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
		//Try system properties
		String username = System.getProperty("github.user.name");
		String password = System.getProperty("github.user.password");
		if (username!=null && password!=null) {
			return new BasicAuthCredentials(GITHUB_HOST, username, password);
		}
		//No credentials found. Try proceeding without credentials.
		return new NullCredentials();
	}

	private String addHost(String path) {
		if (path.startsWith("http")) {
			return path;
		}
		if (!path.startsWith("/")) {
			path = "/"+path;
		}
		return "https://api.github.com"+path;
	}

	/**
	 * Fetch info about repos under a given organization.
	 */
	public Repo[] getOrgRepos(String orgName) {
		return get("/orgs/{orgName}/repos", Repo[].class, orgName);

//		return get("/orgs/{orgName}/repos?per_page=100", Repo[].class, orgName);
	}

	/**
	 * Fetch info about repos under a given user name
	 */
	public Repo[] getUserRepos(String userName) {
		return get("/users/{userName}/repos", Repo[].class, userName);
	}


	/**
	 * Get repos for the authenticated user. This seems to be the only way to list private repos
	 * associated with a user. This only works over an authenticated github connection.
	 */
	public Repo[] getMyRepos() {
		return get("/user/repos", Repo[].class);
	}

	/**
	 * Fetch info about a repo identified by an owner and a name
	 */
	public Repo getRepo(String owner, String repo) {
		return get("/repos/{owner}/{repo}", Repo.class, owner, repo);
	}

	/**
	 * Helper method to fetch json data from some url (or url template)
	 * and parse the data into an object of a given type.
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String url, Class<T> type, Object... vars) {
		url = addHost(url);
		if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			//Assume this means we have to support response pagination as described in:
			//http://developer.github.com/v3/#pagination
			ArrayList<Object> results = new ArrayList<Object>();
			do {
				ResponseEntity<T> entity = client.getForEntity(url, type, vars);
				Object[] pageResults = (Object[])entity.getBody(); //cast is safe because T is an array type.
				for (Object r : pageResults) {
					results.add(r);
				}
				url = getNextPageUrl(entity);
			} while (url!=null);
			return (T) results.toArray((Object[])Array.newInstance(componentType, results.size()));
		} else {
			try {
				return client.getForObject(url, type, vars);
			} catch (HttpServerErrorException e) {
				throw new Error("Error reading: "+url);
			}
		}
	}


	/**
	 * Get the url of the next page in a paginated result.
	 * May return null if there is no next page.
	 * <p>
	 * See http://developer.github.com/v3/#pagination
	 */
	private static <T> String getNextPageUrl(ResponseEntity<T> entity) {
		List<String> linkHeader = entity.getHeaders().get("Link");
		if (linkHeader!=null) {
			//Example of header String:
			//<https://api.github.com/organizations/4161866/repos?page=2>; rel="next", <https://api.github.com/organizations/4161866/repos?page=2>; rel="last"
			Pattern nextPat = Pattern.compile("<([^<]*)>;\\s*rel=\"next\"");
			for (String string : linkHeader) {
				System.out.println(string);
				Matcher m = nextPat.matcher(string);
				if (m.find()) {
					return m.group(1);
				}
			}
		}
		return null; //no pagination info found
	}

	protected static String getNormalisedProtocol(String protocol) {
		return protocol.toUpperCase();
	}

	private RestTemplate createRestTemplate() {
		RestTemplate rest = new RestTemplate();
//		IProxyService proxyService = GettingStartedActivator.getDefault().getProxyService();
//		if (proxyService!=null && proxyService.isProxiesEnabled()) {
//			final IProxyData[] existingProxies = proxyService.getProxyData();
//			if (existingProxies != null && existingProxies.length>0) {
//				//TODO: Do some magic to configure proxies on the http request based on its url.
//
//				//some interesting code in here:
//				//org.cloudfoundry.ide.eclipse.internal.server.core.CloudFoundryClientFactory.getProxy(URL)
//			}
//		}

		//Add authentication
		rest = credentials.apply(rest);

		//Add json parsing capability using Jackson mapper.
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
		messageConverters.add(new Spring3MappingJacksonHttpMessageConverter());

		rest.setMessageConverters(messageConverters);

		//Add rate limit logging
		if (LOG_GITHUB_RATE_LIMIT) {
	        rest.getInterceptors().add(new ClientHttpRequestInterceptor() {

				//@Override
				public ClientHttpResponse intercept(HttpRequest request,
						byte[] body, ClientHttpRequestExecution execution)
						throws IOException {
					ClientHttpResponse res = execution.execute(request, body);
					System.out.println("==== Github: "+request.getURI()+ "  =========");
					for (Entry<String, List<String>> header : res.getHeaders().entrySet()) {
						if (header.getKey().contains("RateLimit")) {
							System.out.print(header.getKey()+":");
							for (String value : header.getValue()) {
								System.out.print(" "+value);
							}
							System.out.println();
						}
					}
					System.out.println("======================= ");
					return res;
				}
			});

		}

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
     * Download content from a url and save to an outputstream. Use same credentials as
     * other operations in this client. May need to use this to download stuff like
     * zip file from github if the repo it comes from is private.
     */
	public void fetch(URL url, OutputStream writeTo) throws IOException {
		URLConnection conn = null;
		InputStream input = null;
		try {
			conn = url.openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT);
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

	/**
	 * For some quick add-hoc testing
	 */
	public static void main(String[] args) {
		GithubClient gh = new GithubClient();
		for (int i = 0; i < 5; i++) {
			Repo[] repos = gh.getOrgRepos("spring-guides");
			System.out.println(repos.length);
		}
	}

}
