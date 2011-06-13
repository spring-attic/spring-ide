/*******************************************************************************
 * Copyright (c) 2010, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.prefs.Preferences;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.internal.uaa.client.QueueingUaaServiceExtension;
import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.ide.eclipse.uaa.UaaPlugin;
import org.springframework.ide.eclipse.uaa.UaaUtils;
import org.springframework.uaa.client.TransmissionAwareUaaService;
import org.springframework.uaa.client.TransmissionEventListener;
import org.springframework.uaa.client.UaaService;
import org.springframework.uaa.client.VersionHelper;
import org.springframework.uaa.client.internal.BasicProxyService;
import org.springframework.uaa.client.internal.JdkUrlTransmissionServiceImpl;
import org.springframework.uaa.client.protobuf.UaaClient.FeatureUse;
import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;
import org.springframework.uaa.client.protobuf.UaaClient.Product;
import org.springframework.uaa.client.util.Base64;
import org.springframework.uaa.client.util.PgpUtils;
import org.springframework.uaa.client.util.PreferencesUtils;
import org.springframework.uaa.client.util.StreamUtils;
import org.springframework.uaa.client.util.StringUtils;
import org.springframework.uaa.client.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Helper class that coordinates with the Spring UAA service implementation.
 * <p>
 * This implementation primarily serves as wrapper around the {@link UaaService}.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class UaaManager implements IUaa {

	private static final String DETECTED_PRODUCTS_KEY = "detected_eclipse_products";
	private static final String DETECTED_PRODUCTS_SIGNATURE_KEY = "detected_eclipse_products_signature";
	private static final byte[] EMPTY = {};
	private static final String EMPTY_STRING = "";
	private static final String EMPTY_VERSION = "0.0.0.RELEASE";
	private static final long MIN_REPORTING_INTERVAL = 1000L * 60L * 60L * 12L; // report a unique usage record only all 12h
	private static final Preferences P = PreferencesUtils.getPreferencesFor(UaaManager.class);
	
	private static final URL SIGNATURE_URL;
	private static final URL UAA_URL;
	
	static {
		// Produce the urls safely
		try {
			UAA_URL = new URL("http://uaa.springsource.org/uaa-eclipse.xml");
			SIGNATURE_URL = new URL("http://uaa.springsource.org/uaa-eclipse.xml.asc");
		}
		catch (MalformedURLException neverHappens) {
			throw new IllegalStateException(neverHappens);
		}
	}
	
	private static final String THREAD_NAME_TEMPLATE = "Reporting Thread-%s (%s/%s.%s.%s)";
	private final AtomicInteger threadCount = new AtomicInteger(0);
	private final ExecutorService executorService = Executors.newFixedThreadPool(1, new ThreadFactory() {
		
		public Thread newThread(Runnable runnable) {
			Product uaaProduct = VersionHelper.getUaa();
			Thread reportingThread = new Thread(runnable, String.format(THREAD_NAME_TEMPLATE, threadCount.incrementAndGet(), 
					uaaProduct.getName(), uaaProduct.getMajorVersion(), uaaProduct.getMinorVersion(), uaaProduct.getPatchVersion()));
			reportingThread.setDaemon(true);
			return reportingThread;
		}
	});

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private List<ProductDescriptor> productDescriptors = new CopyOnWriteArrayList<UaaManager.ProductDescriptor>();
	private List<RegistrationAttempt> registrationAttempts = new CopyOnWriteArrayList<RegistrationAttempt>();
	private QueueingUaaServiceExtension service = new QueueingUaaServiceExtension(new JdkUrlTransmissionServiceImpl(new EclipseProxyService()));
	
	public UaaService getUaaService() {
		return service;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getPrivacyLevel() {
		try {
			r.lock();
			return this.service.getPrivacyLevel().getNumber();
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReadablePayload() {
		try {
			r.lock();
			return StringUtils.toString(service.getPayload(), true, 2);
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerFeatureUse(String plugin) {
		registerFeatureUse(plugin, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerFeatureUse(final String plugin, final Map<String, String> featureData) {
		if (plugin != null) {
			
			// Before we trigger eventually expensive background reporting, check if this
			// feature hasn't recently been reported; if so just skip
			final RegistrationAttempt attempt = new FeatureUseRegistrationAttempt(plugin, featureData);
			if (shouldSkipRegistrationAttempt(attempt)) {
				return;
			}

			executorService.execute(new Runnable() {

				public void run() {
					try {
						w.lock();
												
						for (ProductDescriptor productDescriptor : productDescriptors) {
							if (productDescriptor.registerFeatureUseIfMatch(plugin, featureData)) {
								registrationAttempts.add(attempt);
								return;
							}
						}
					}
					catch (IllegalArgumentException e) {
						// Ignore as it may sporadically come up from the preferences API
					}
					finally {
						w.unlock();
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerProductUse(String productId, String version) {
		registerProductUse(productId, version, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerProductUse(final String productId, String version, final String projectId) {
		if (version == null) {
			version = EMPTY_VERSION;
		}

		final String versionString = version;

		if (productId != null) {

			// Before we trigger eventually expensive background reporting, check if this
			// product hasn't recently been reported; if so just skip
			final RegistrationAttempt attempt = new ProductRegistrationAttempt(productId, versionString, projectId);
			if (shouldSkipRegistrationAttempt(attempt)) {
				return;
			}
			
			executorService.execute(new Runnable() {

				public void run() {
					try {
						w.lock();
						
						Product product = null;
						try {
							Version version = Version.parseVersion(versionString);
							Product.Builder productBuilder = Product.newBuilder();
							productBuilder.setName(productId);
							productBuilder.setMajorVersion(version.getMajor());
							productBuilder.setMinorVersion(version.getMinor());
							productBuilder.setPatchVersion(version.getMicro());
							productBuilder.setReleaseQualifier(version.getQualifier());
							// product.setSourceControlIdentifier();

							product = productBuilder.build();
						}
						catch (IllegalArgumentException e) {
							// As a fallback we use the Spring UAA way of producing products
							product = VersionHelper.getProduct(productId, versionString);
						}

						if (projectId == null) {
							service.registerProductUsage(product);
						}
						else {
							service.registerProductUsage(product, projectId);
						}
						registrationAttempts.add(attempt);
					}
					finally {
						w.unlock();
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerProjectUsageForProduct(final String feature, final String projectId,
			final Map<String, String> featureData) {
		if (feature != null) {

			// Before we trigger eventually expensive background reporting, check if this
			// project hasn't recently been reported; if so just skip
			final RegistrationAttempt attempt = new ProjectUsageRegistrationAttempt(feature, projectId, featureData);
			if (shouldSkipRegistrationAttempt(attempt)) {
				return;
			}
			
			executorService.execute(new Runnable() {

				public void run() {
					try {
						w.lock();
						
						
						for (ProductDescriptor productDescriptor : productDescriptors) {
							if (productDescriptor.registerProjectUsage(feature, projectId, featureData)) {
								registrationAttempts.add(attempt);
								return;
							}
						}
					}
					catch (IllegalArgumentException e) {
						// Ignore as it may sporadically come up from the preferences API
					}
					finally {
						w.unlock();
					}
				}
			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPrivacyLevel(int level) {
		try {
			w.lock();
			service.setPrivacyLevel(PrivacyLevel.valueOf(level));
		}
		finally {
			w.unlock();
		}
	}
	
	/**
	 * Starts up this {@link UaaManager} instance.
	 */
	public void start() {
		// Since we run in an restricted environment we need to obtain the builder factory from the OSGi service
		// registry instead of trying to create a new one from the API
		XmlUtils.setDocumentBuilderFactory(UaaUtils.getDocumentBuilderFactory());
		
		try { initProductDescriptions(getDefaultDetectedProducts(), getDefaultDetectedProductsSignaturer()); }
		catch (IOException e) {}
		
		// Add Transmission listener so that we can download the list of detected products periodically.
		service.addTransmissionEventListener(new TransmissionEventListener() {

			public void afterTransmission(TransmissionType type, boolean success) {
				if (type == TransmissionType.DOWNLOAD && success) {
					InputStream configuration = null;
					InputStream configurationSignature = null;
					try {
						configuration = service.getTransmissionService().download(UAA_URL);
						configurationSignature = service.getTransmissionService().download(SIGNATURE_URL);
						initProductDescriptions(configuration, configurationSignature);
					}
					catch (IOException e) {}
					finally {
						
						// Safely close the streams
						if (configuration != null) {
							try { configuration.close(); }
							catch (IOException e) {}
						}
						if (configurationSignature != null) {
							try { configurationSignature.close(); }
							catch (IOException e) {}
						}
					}
				}
			}

			public void beforeTransmission(TransmissionType type) {
			}});
		
		// After starting up and reporting the initial state we should send the data
		Job transmissionJob = new Job("Initializing Spring UAA") {
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (service instanceof TransmissionAwareUaaService) {
					((TransmissionAwareUaaService) service).requestTransmission();
				}
				return Status.OK_STATUS;
			}
		};
		transmissionJob.setSystem(true);
		// Schedule this for 10 minutes into the running instance
		transmissionJob.schedule(10L * 60L * 1000L);
	}
	
	/**
	 * Stops this {@link UaaManager} instance. Shuts down all internal resources like thread pools etc.
	 */
	public void stop() {
		try {
			w.lock();
			executorService.shutdown();
			service.stop();
		}
		finally {
			w.unlock();
		}
	}
	
	/**
	 * Returns the default products that is being shipped with Spring IDE. 
	 */
	private InputStream getDefaultDetectedProducts() throws IOException {
		return UaaManager.class.getResourceAsStream("/org/springframework/ide/eclipse/internal/uaa/uaa-eclipse.xml");
	}
	
	/**
	 * Returns the signature of default products file that is being shipped with Spring IDE. 
	 */
	private InputStream getDefaultDetectedProductsSignaturer() throws IOException {
		return UaaManager.class.getResourceAsStream("/org/springframework/ide/eclipse/internal/uaa/uaa-eclipse.xml.asc");
	}
	
	/**
	 * Returns the id of the feature that owns the given <code>plugin</code>.  
	 */
	private String getOwningEclipseFeature(String plugin) {
		IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
		for (IBundleGroupProvider provider : providers) {
			for (IBundleGroup group : provider.getBundleGroups()) {
				if (group.getIdentifier().startsWith("org.eclipse")) {
					for (Bundle bundle : group.getBundles()) {
						if (plugin.equals(bundle.getSymbolicName())) {
							return group.getIdentifier();
						}
					}
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private byte[] mergeData(byte[] existingData, byte[] data) {
		// Quick sanity check to prevent doing too much in case no new data has been presented
		if (data == null || data.length == 0) {
			return existingData;
		}
		
		// Load existing feature data
		JSONObject existingFeatureData = new JSONObject();
		if (existingData != null && existingData.length > 0) {
			Object existingJson = JSONValue.parse(new String(existingData));
			if (existingJson instanceof JSONObject) {
				existingFeatureData.putAll(((JSONObject) existingJson));
			}
		}
		
		// Load new data into JSON object
		Map<String, String> featureData = new JSONObject();
		if (data != null && data.length > 0) {
			Object json = JSONValue.parse(new String(data));
			if (json instanceof JSONObject) {
				featureData.putAll((JSONObject) json);
			}
		}

		// Merge feature data: merge those values whose keys already exist
		featureData = new HashMap<String, String>(featureData);
		for (Map.Entry<String, Object> existingEntry : new HashMap<String, Object>(existingFeatureData).entrySet()) {
			if (featureData.containsKey(existingEntry.getKey())) {
				String newValue = featureData.get(existingEntry.getKey());
				Object existingValue = existingEntry.getValue();
				if (!newValue.equals(existingValue)) {
					if (existingValue instanceof List) {
						List<String> existingValues = (List<String>) existingValue;
						if (!existingValues.contains(newValue)) {
							existingValues.add(newValue);
						}
					}
					else {
						List<String> value = new ArrayList<String>();
						value.add((String) existingValue);
						value.add(featureData.get(existingEntry.getKey()));
						existingFeatureData.put(existingEntry.getKey(), value);
					}
				}
				featureData.remove(existingEntry.getKey());
			}
		}

		// Merge the remaining new values
		existingFeatureData.putAll(featureData);

		try { return existingFeatureData.toJSONString().getBytes("UTF-8"); }
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
	 * Initializes the {@link ProductDescriptor}s based on the content of <code>inputStream</code>.
	 * A local copy stored in Preferences API is checked and only overriden if the given <code>inputStream</code>
	 * represents a newer version. 
	 */
	private boolean initProductDescriptions(InputStream inputStream, InputStream signatureStream) {
		try {
			
			if (inputStream == null || signatureStream == null) return false;
			
			// Convert the incoming input stream into a byte[] to start with (simplifies subsequent storage etc)
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamUtils.copy(inputStream, baos);
			byte[] incoming = baos.toByteArray();
			
			baos = new ByteArrayOutputStream();
			StreamUtils.copy(signatureStream, baos);
			byte[] incomingSignature = baos.toByteArray();
			
			if (incoming.length == 0 || incomingSignature.length == 0) return false;
			
			// Retrieve the existing document from the Preference API (if any) so we can consider version upgrade issues
			byte[] existing = Base64.decode(P.get(DETECTED_PRODUCTS_KEY, EMPTY_STRING));
			byte[] existingSignature = Base64.decode(P.get(DETECTED_PRODUCTS_SIGNATURE_KEY, EMPTY_STRING));
			
			// Check signatures of both to see if need to discard a stream
			if (!PgpUtils.validateConfigurationSignature(new ByteArrayInputStream(incoming), new ByteArrayInputStream(incomingSignature))) {
				incoming = EMPTY;
				incomingSignature = EMPTY;
			}
			if (!PgpUtils.validateConfigurationSignature(new ByteArrayInputStream(existing), new ByteArrayInputStream(existingSignature))) {
				existing = EMPTY;
				existingSignature = EMPTY;
			} 
			
			if (existing != EMPTY && existing.length > 0 && existingSignature != EMPTY && existingSignature.length > 0) {
				// We need to consider version differences
				int existingVersion = getVersion(existing);
				int incomingVersion = getVersion(incoming);
				if (existingVersion >= incomingVersion) {
					// We're not going to do any replacement as the new version is no better than our current version
					// Instead we'll treat our existing version as the newest version and continue running but only if the signature is valid.
					incoming = existing;
					incomingSignature = existingSignature;
				}
			}
			
			if (incoming == EMPTY && incoming.length == 0) {
				return false;
			}
			
			// Parse the incoming bytes into an XML document
			Document d = XmlUtils.parse(new ByteArrayInputStream(incoming));
			Element docElement = d.getDocumentElement();
	
			// Build products list
			List<ProductDescriptor> newProductDescriptors = new ArrayList<UaaManager.ProductDescriptor>();
			NodeList nodeList = docElement.getElementsByTagName("product");
			for (int i = 0; i < nodeList.getLength(); i++) {
				newProductDescriptors.add(new ExtensionProductDescriptor((Element) nodeList.item(i)));
			}
			newProductDescriptors.add(new ProductDescriptor());
			
			// Override the global list
			productDescriptors.clear();
			productDescriptors.addAll(newProductDescriptors);
			
			// Replace the existing cached version if required
			if (incoming != existing) {
				// We need to do a replacement, as we didn't simply parse the existing one
				String incomingBase64 = Base64.encodeBytes(incoming, Base64.GZIP);
				String incomingSignatureBase64 = Base64.encodeBytes(incomingSignature, Base64.GZIP);
				P.put(DETECTED_PRODUCTS_KEY, incomingBase64);
				P.put(DETECTED_PRODUCTS_SIGNATURE_KEY, incomingSignatureBase64);
				P.flush();
				return true;
			}
			
			return false;
		}
		catch (Throwable e) {}
		return false;
	}

	/**
	 * Checks if a given {@link RegistrationAttempt} should be allowed.
	 * Returns <code>true</code> if no similar {@link RegistrationAttempt} has already been queued. 
	 */
	private boolean shouldSkipRegistrationAttempt(RegistrationAttempt attempt) {
		int ix = registrationAttempts.indexOf(attempt);
		return ix >= 0 && !registrationAttempts.get(ix).shouldRegisterAgain();
	}

	/**
	 * Looks up the version attribute from the incoming XML document's <product version='someInteger'>.
	 * @param document to parse (required)
	 * @return the version number (or an exception of something went wrong, eg invalid document etc)
	 */
	private static int getVersion(byte[] document) {
		try { 
			Document d = XmlUtils.parse(new ByteArrayInputStream(document));
			Element docElement = d.getDocumentElement();
			String ver = docElement.getAttribute("version");
			return new Integer(ver);
		}
		catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Extension to {@link BasicProxyService} that hooks in the Eclipse {@link IProxyService}.
	 * @since 2.6.0
	 */
	private class EclipseProxyService extends BasicProxyService {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Proxy setupProxy(URL url) {
			IProxyData proxy = getProxy(url);
			if (proxy != null && proxy.getHost() != null && proxy.getPort() >= 0 && proxy.getPort() <= 65535) {
				try {
					return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
				}
				catch (IllegalArgumentException e) {}
			}
			return super.setupProxy(url);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Authenticator setupProxyAuthentication(URL url, Proxy proxy) {
			final IProxyData selectedProxy = getProxy(url);
			if (selectedProxy != null && (selectedProxy.getUserId() != null || selectedProxy.getPassword() != null)) {
				return new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(selectedProxy.getUserId(),
								(selectedProxy.getPassword() != null ? selectedProxy.getPassword().toCharArray() : null));
					}
				};
			}
			return super.setupProxyAuthentication(url, proxy);
		}

		/**
		 * Resolves a proxy from the {@link IProxyService} for the given <code>url</code>. 
		 */
		private IProxyData getProxy(URL url) {
			IProxyService proxyService = (UaaPlugin.getDefault() != null ? UaaPlugin.getDefault().getProxyService() : null);
			if (url != null && service != null & proxyService != null && proxyService.isProxiesEnabled()) {
				try {
					URI uri = url.toURI();
					IProxyData[] proxies = proxyService.select(uri);
					return selectProxy(uri.getScheme(), proxies);
				}
				catch (URISyntaxException e) {
					// ignore this
				}
			}
			return null;
		}
		
		/**
		 * Select a proxy from the list of available proxies. 
		 */
		private IProxyData selectProxy(String protocol, IProxyData[] proxies) {
			if (proxies == null || proxies.length == 0)
				return null;
			// If only one proxy is available, then use that
			if (proxies.length == 1) {
				return proxies[0];
			}
			// If more than one proxy is available, then if http/https protocol then look for that one...
			// if not found then use first
			if (protocol.equalsIgnoreCase("http")) {
				for (int i = 0; i < proxies.length; i++) {
					if (proxies[i].getType().equals(IProxyData.HTTP_PROXY_TYPE))
						return proxies[i];
				}
			}
			else if (protocol.equalsIgnoreCase("https")) {
				for (int i = 0; i < proxies.length; i++) {
					if (proxies[i].getType().equals(IProxyData.HTTPS_PROXY_TYPE))
						return proxies[i];
				}
			}
			// If we haven't found it yet, then return the first one.
			return proxies[0];
		}
	}
	
	private class ExtensionProductDescriptor extends ProductDescriptor {

		private Map<String, String> pluginsToFeatureMapping;

		private String productId;

		private String rootPlugin;

		private String sourceControlIdentifier;

		public ExtensionProductDescriptor(Element element) {
			init(element);
		}

		private void init(Element element) {
			productId = element.getAttribute("id");
			if (element.getAttribute("source-control-identifier") != null && element.getAttribute("source-control-identifier").length() > 0) {
				sourceControlIdentifier = element.getAttribute("source-control-identifier");
			}
			if (element.getAttribute("root-plugin") != null && element.getAttribute("root-plugin").length() > 0) {
				rootPlugin = element.getAttribute("root-plugin");
			}

			pluginsToFeatureMapping = new HashMap<String, String>();
			NodeList features = element.getElementsByTagName("feature");
			for (int i = 0; i < features.getLength(); i++) {
				Element featureElement = (Element) features.item(i);
				String feature = featureElement.getAttribute("id");
				if (feature != null) {
					NodeList plugins = featureElement.getElementsByTagName("plugin");
					for (int j = 0; j < plugins.getLength(); j++) {
						Element pluginElement = (Element) plugins.item(j);
						String pluginId = pluginElement.getAttribute("id");
						if (pluginId != null) {
							// Verify that the plugin does not belong to another eclipse feature. This is 
							// required to associate plugins patched through feature patches to the correct
							// root feature (e.g. Groovy Eclipse patching JDT core)
							String owningFeature = getOwningEclipseFeature(pluginId);
							if (owningFeature == null || feature.equals(owningFeature)) {
								pluginsToFeatureMapping.put(pluginElement.getAttribute("id"), feature);
							}
						}
					}
				}
			}

			// Try to create the product; we'll try again later if this one fails
			buildProduct(rootPlugin, productId, sourceControlIdentifier);
		}
		
		protected boolean canRegister(String usedPlugin) {
			return pluginsToFeatureMapping.containsKey(usedPlugin);
		}

		protected void registerProductIfRequired(String project) {
			// If we initially failed to create the product it was probably because it wasn't installed
			// when the workbench started; but now it is so try again
			if (product == null) {
				buildProduct(rootPlugin, productId, sourceControlIdentifier);
			}

			// Check if the product is already registered; if not register it before we capture feature usage
			if (!registered) {
				if (project != null) {
					service.registerProductUsage(product, project);
				}
				else {
					service.registerProductUsage(product);
				}
				registered = true;
			}
		}

	}

	private static class FeatureUseRegistrationAttempt extends RegistrationAttempt {
		
		private final Map<String, String> featureData;
		private final String plugin;
		
		public FeatureUseRegistrationAttempt(String plugin, Map<String, String> featureData) {
			this.plugin = plugin;
			this.featureData = featureData;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof FeatureUseRegistrationAttempt)) {
				return false;
			}
			FeatureUseRegistrationAttempt other = (FeatureUseRegistrationAttempt) obj;
			if (featureData == null) {
				if (other.featureData != null) {
					return false;
				}
			}
			else if (!featureData.equals(other.featureData)) {
				return false;
			}
			if (plugin == null) {
				if (other.plugin != null) {
					return false;
				}
			}
			else if (!plugin.equals(other.plugin)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((featureData == null) ? 0 : featureData.hashCode());
			result = prime * result + ((plugin == null) ? 0 : plugin.hashCode());
			return result;
		}

	}

	private class ProductDescriptor {

		protected Product product;

		protected boolean registered = false;

		public ProductDescriptor() {
			init();
		}

		public final boolean registerFeatureUseIfMatch(String usedPlugin, Map<String, String> featureData) {
			if (canRegister(usedPlugin)) {

				registerProductIfRequired(null);
				registerFeature(usedPlugin, featureData);

				return true;
			}
			return false;
		}

		public final boolean registerProjectUsage(String usedPlugin, String project, Map<String, String> featureData) {
			if (canRegister(usedPlugin) && project != null) {

				registerProductIfRequired(project);
				registerFeature(usedPlugin, featureData);
				service.registerProductUsage(product, project);

				return true;
			}
			return false;
		}

		private void init() {
			buildProduct(Platform.PI_RUNTIME, "Eclipse", null);
		}

		protected void buildProduct(String symbolicName, String name, String sourceControlIdentifier) {
			Bundle bundle = Platform.getBundle(symbolicName);
			if (bundle != null) {
				Version version = bundle.getVersion();

				Product.Builder b = Product.newBuilder();
				b.setName(name);
				b.setMajorVersion(version.getMajor());
				b.setMinorVersion(version.getMinor());
				b.setPatchVersion(version.getMicro());
				b.setReleaseQualifier(version.getQualifier());

				if (sourceControlIdentifier != null && sourceControlIdentifier.length() > 0) {
					b.setSourceControlIdentifier(sourceControlIdentifier);
				}
				else {
					String sourceControlId = (String) bundle.getHeaders().get("Source-Control-Identifier");
					if (sourceControlId != null && sourceControlId.length() > 0) {
						b.setSourceControlIdentifier(sourceControlId);
					}
					sourceControlId = (String) bundle.getHeaders().get("Git-Commit-Hash");
					if (sourceControlId != null && sourceControlId.length() > 0) {
						b.setSourceControlIdentifier(sourceControlId);
					}
				}

				product = b.build();
			}
		}

		protected boolean canRegister(String usedPlugin) {
			// Due to privacy considerations only org.eclipse plugins and features will get recorded
			return usedPlugin.startsWith("org.eclipse");
		}

		protected void registerFeature(String usedPlugin, Map<String, String> featureData) {
			// Initialize new map in case null was supplied
			if (featureData == null) {
				featureData = Collections.emptyMap();
			}
			
			// Get the feature version from the plugin
			String featureVersion = null;
			Bundle bundle = Platform.getBundle(usedPlugin);
			if (bundle != null) {
				String version = (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
				if (version != null) {
					featureVersion = version.toString();
				}
			}
			
			// Obtain the FeatureUse record
			FeatureUse feature = VersionHelper.getFeatureUse(usedPlugin, featureVersion);
			
			try {
				// Get existing feature data to merge
				byte[] existingData = service.getFeatureUseData(product, feature);
				byte[] newData = JSONObject.toJSONString(featureData).getBytes("UTF-8"); 

				// If additional feature data was supplied or is already registered pass it to UAA
				service.registerFeatureUsage(product, feature, mergeData(existingData, newData));
			}
			catch (UnsupportedEncodingException e) { 
				// Cannot happen 
			}
		}

		protected void registerProductIfRequired(String project) {
			if (!registered) {
				
				// Populate a map of product data with details of the hosting Eclipse runtime
				Map<String, String> productData = new HashMap<String, String>();
				productData.put("platform", String.format("%s.%s.%s", Platform.getOS(), Platform.getWS(), Platform.getOSArch()));

				if (System.getProperty("eclipse.buildId") != null) {
					productData.put("buildId", System.getProperty("eclipse.buildId"));
				}
				if (System.getProperty("eclipse.product") != null) {
					productData.put("product", System.getProperty("eclipse.product"));
				}
				if (System.getProperty("eclipse.application") != null) {
					productData.put("application", System.getProperty("eclipse.application"));
				}

				try {
					if (project != null) {
						service.registerProductUsage(product, JSONObject.toJSONString(productData).getBytes("UTF-8"), project);
					}
					else {
						service.registerProductUsage(product, JSONObject.toJSONString(productData).getBytes("UTF-8"));
					}
				}
				catch (UnsupportedEncodingException e) { 
					// cannot happen 
				}
				registered = true;
			}
		}
	}
	
	private static class ProductRegistrationAttempt extends RegistrationAttempt {
		
		private final String productId; 
		private final String projectId;
		private final String version;
	
		public ProductRegistrationAttempt(String productId, String version, String projectId) {
			this.productId = productId;
			this.version = version;
			this.projectId = projectId;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ProductRegistrationAttempt)) {
				return false;
			}
			ProductRegistrationAttempt other = (ProductRegistrationAttempt) obj;
			if (productId == null) {
				if (other.productId != null) {
					return false;
				}
			}
			else if (!productId.equals(other.productId)) {
				return false;
			}
			if (projectId == null) {
				if (other.projectId != null) {
					return false;
				}
			}
			else if (!projectId.equals(other.projectId)) {
				return false;
			}
			if (version == null) {
				if (other.version != null) {
					return false;
				}
			}
			else if (!version.equals(other.version)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((productId == null) ? 0 : productId.hashCode());
			result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
			result = prime * result + ((version == null) ? 0 : version.hashCode());
			return result;
		}
	}
	
	private static class ProjectUsageRegistrationAttempt extends RegistrationAttempt {
		
		private final String feature;
		private final Map<String, String> featureData;
		private final String projectId;

		public ProjectUsageRegistrationAttempt(String feature, String projectId, Map<String, String> featureData) {
			this.feature = feature;
			this.projectId = projectId;
			this.featureData = featureData;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof ProjectUsageRegistrationAttempt)) {
				return false;
			}
			ProjectUsageRegistrationAttempt other = (ProjectUsageRegistrationAttempt) obj;
			if (feature == null) {
				if (other.feature != null) {
					return false;
				}
			}
			else if (!feature.equals(other.feature)) {
				return false;
			}
			if (featureData == null) {
				if (other.featureData != null) {
					return false;
				}
			}
			else if (!featureData.equals(other.featureData)) {
				return false;
			}
			if (projectId == null) {
				if (other.projectId != null) {
					return false;
				}
			}
			else if (!projectId.equals(other.projectId)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((feature == null) ? 0 : feature.hashCode());
			result = prime * result + ((featureData == null) ? 0 : featureData.hashCode());
			result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
			return result;
		}
	}
	
	private static abstract class RegistrationAttempt {
		
		private final long registrationAttemptTime = new Date().getTime();

		public boolean shouldRegisterAgain() {
			return System.currentTimeMillis() > registrationAttemptTime + MIN_REPORTING_INTERVAL;
		}
	}
	
}
