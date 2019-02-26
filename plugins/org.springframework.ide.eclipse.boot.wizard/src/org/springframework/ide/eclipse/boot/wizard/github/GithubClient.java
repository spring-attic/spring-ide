/*******************************************************************************
 *  Copyright (c) 2013, 2019 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.github;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;

import org.springframework.ide.eclipse.boot.wizard.BootWizardActivator;
import org.springframework.ide.eclipse.boot.wizard.github.auth.BasicAuthCredentials;
import org.springframework.ide.eclipse.boot.wizard.github.auth.Credentials;
import org.springframework.ide.eclipse.boot.wizard.github.auth.NullCredentials;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * A GithubClient instance needs to configured with some credentials and then it is able to
 * talk to github using its rest api to obtain information about github repos, users,
 * organisations etc.
 *
 * @author Kris De Volder
 */
public class GithubClient {

	private static final Pattern GITHUB_HOST = Pattern.compile("(.*\\.|)github\\.com");
//		pattern should match 'github.com' and api.github.com'

	private static final int CONNECT_TIMEOUT = 10000;

	private static final boolean DEBUG = false;
//	private static final boolean LOG_GITHUB_RATE_LIMIT = false;

	private final Credentials credentials;
	private final Client client;

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
		this.client= createRestClient();
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
		return get("/orgs/{orgName}/repos", Repo[].class, ImmutableMap.of("orgName", orgName));

//		return get("/orgs/{orgName}/repos?per_page=100", Repo[].class, orgName);
	}

	/**
	 * Fetch the remaining rate limit.
	 */
	public RateLimitResponse getRateLimit() throws IOException {
		return get("/rate_limit", RateLimitResponse.class);
	}


	/**
	 * Fetch info about repos under a given user name
	 */
	public Repo[] getUserRepos(String userName) {
		return get("/users/{userName}/repos", Repo[].class, ImmutableMap.of("userName", userName));
	}

	/**
	 * Get repos for the authenticated user. This seems to be the only way to list private repos
	 * associated with a user. This only works over an authenticated github connection.
	 */
	public Repo[] getMyRepos() {
		try {
			return get("/user/repos", Repo[].class);
		} catch (Throwable e) {
			BootWizardActivator.log(e);
		}
		return new Repo[0];
	}

	/**
	 * Fetch info about a repo identified by an owner and a name
	 */
	public Repo getRepo(String owner, String repo) {
		return get("/repos/{owner}/{repo}", Repo.class, ImmutableMap.of(
				"owner", owner,
				"repo", repo
		));
	}

	public <T> T get(String url, Class<T> type) {
		return get(url, type, ImmutableMap.of());
	}

	/**
	 * Helper method to fetch json data from some url (or url template)
	 * and parse the data into an object of a given type.
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String url, Class<T> type, Map<String, Object> vars) {
		url = addHost(url);
		if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			//Assume this means we have to support response pagination as described in:
			//http://developer.github.com/v3/#pagination
			ArrayList<Object> results = new ArrayList<>();
			WebTarget webtarget = client.target(url).resolveTemplates(vars);
			do {
				Response response = webtarget.request(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN).get();
				Object[] pageResults = (Object[])response.readEntity(type);
				for (Object r : pageResults) {
					results.add(r);
				}
				url = getNextPageUrl(response);
				if (url!=null) {
					webtarget = client.target(url);
				} else {
					webtarget = null;
				}
			} while (webtarget!=null);
			return (T) results.toArray((Object[])Array.newInstance(componentType, results.size()));
		} else {
			return  client.target(url).resolveTemplates(vars).request(MediaType.APPLICATION_JSON).get(type);
		}
	}


	/**
	 * Get the url of the next page in a paginated result.
	 * May return null if there is no next page.
	 * <p>
	 * See http://developer.github.com/v3/#pagination
	 */
	private static <T> String getNextPageUrl(Response response) {
		List<Object> linkHeader = response.getHeaders().get("Link");
		if (linkHeader!=null) {
			//Example of header String:
			//<https://api.github.com/organizations/4161866/repos?page=2>; rel="next", <https://api.github.com/organizations/4161866/repos?page=2>; rel="last"
			Pattern nextPat = Pattern.compile("<([^<]*)>;\\s*rel=\"next\"");
			for (Object _header : linkHeader) {
				String string = (String)_header;
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

	public static class SimpleJacksonBodyReader<T> implements MessageBodyReader<T> {

		//Note: There's a bit of 'wheel reinventing' going on here. Jersey framework has a 'JacksonFeature' to support
		//  marshalling and unmarshaling of json objects using Jackson. Doing this is ourself is not great. But....
		//
		//  But it turns out that the whole of Jersey framework with all the json dependencies from various bundles
		//  in Orbit repo is quite a complex puzzle to setup and get running without any errors. Implementing a
		//  simplified MessageReader which is just good enough to handle our limited use cases seemed a lot easier
		//  (and drags in a lot fewer dependencies).

		ObjectMapper mapper = new ObjectMapper();

		static final Set<String> CAN_PARSE = ImmutableSet.of("text/plain", "application/json");

		@Override
		public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
			//We don't need complex logics here because the assumption is that we only ever ask for and expect json
			//so we will always try to parse it with jackson mapper.
			String mType = mediaType.getType()+"/"+mediaType.getSubtype();
			return CAN_PARSE.contains(mType);
		}

		@Override
		public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
				MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
				throws IOException, WebApplicationException {
			return mapper.readerFor(type).readValue(new InputStreamReader(entityStream, mediaType.getParameters().get(MediaType.CHARSET_PARAMETER)));
		}
	}

	private Client createRestClient() {
		ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

		try {
			//
			// ClientBuilder uses a simple Class.forName to instantiate the configured client builder
			// (which is the one from Jersey glassfish), which results in the thread context class loader
			// to be used to find that class instead of this bundles classloader.
			// That could result in a linkage error if the jersey glassfish client builder gets found in a
			// different bundle that we expect here (and which might not be wired to javax.ws.rs)
			//
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

			Client client = ClientBuilder.newClient();

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
			client = credentials.apply(client);

			//Add json parsing capability using Jackson mapper.
			client.register(SimpleJacksonBodyReader.class);

			//Add rate limit logging
			//		if (LOG_GITHUB_RATE_LIMIT) {
			//	        rest.getInterceptors().add(new ClientHttpRequestInterceptor() {
			//
			//				//@Override
			//				@Override
			//				public ClientHttpResponse intercept(HttpRequest request,
			//						byte[] body, ClientHttpRequestExecution execution)
			//						throws IOException {
			//					ClientHttpResponse res = execution.execute(request, body);
			//					System.out.println("==== Github: "+request.getURI()+ "  =========");
			//					for (Entry<String, List<String>> header : res.getHeaders().entrySet()) {
			//						if (header.getKey().contains("RateLimit")) {
			//							System.out.print(header.getKey()+":");
			//							for (String value : header.getValue()) {
			//								System.out.print(" "+value);
			//							}
			//							System.out.println();
			//						}
			//					}
			//					System.out.println("======================= ");
			//					return res;
			//				}
			//			});
			//
			//		}

			return client;
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextLoader);
		}
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
