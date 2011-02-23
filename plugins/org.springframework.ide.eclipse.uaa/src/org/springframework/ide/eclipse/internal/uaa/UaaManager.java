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

import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.ide.eclipse.uaa.UaaPlugin;
import org.springframework.uaa.client.TransmissionService;
import org.springframework.uaa.client.UaaService;
import org.springframework.uaa.client.UrlHelper;
import org.springframework.uaa.client.VersionHelper;
import org.springframework.uaa.client.internal.JdkUrlTransmissionServiceImpl;
import org.springframework.uaa.client.internal.TransmissionAwareUaaServiceImpl;
import org.springframework.uaa.client.internal.UaaServiceImpl;
import org.springframework.uaa.client.protobuf.UaaClient.FeatureUse;
import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;
import org.springframework.uaa.client.protobuf.UaaClient.Product;
import org.springframework.uaa.client.protobuf.UaaClient.ProductUse;
import org.springframework.uaa.client.protobuf.UaaClient.UaaEnvelope;
import org.springframework.uaa.client.protobuf.UaaClient.UserAgent;
import org.springframework.uaa.client.util.XmlUtils;

/**
 * Helper class that coordinates with the Spring UAA service implementation.
 * <p>
 * This implementation primarily serves as wrapper around the {@link UaaService}.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class UaaManager implements IUaa {

	private static final String EMPTY_VERSION = "0.0.0.RELEASE";
	private static final String UAA_PRODUCT_EXTENSION_POINT = "org.springframework.ide.eclipse.uaa.product";

	private final ExecutorService executorService = Executors.newFixedThreadPool(5);

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	private List<ProductDescriptor> productDescriptors = new ArrayList<ProductDescriptor>();
	private CachingUaaServiceImpl service = new CachingUaaServiceImpl(new JdkUrlProxyAwareTransmissionService());

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
			return service.getReadablePayload();
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
			executorService.execute(new Runnable() {

				public void run() {
					try {
						w.lock();
						for (ProductDescriptor productDescriptor : productDescriptors) {
							if (productDescriptor.registerFeatureUseIfMatch(plugin, featureData)) {
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
			executorService.execute(new Runnable() {

				public void run() {
					try {
						w.lock();
						for (ProductDescriptor productDescriptor : productDescriptors) {
							if (productDescriptor.registerProjectUsage(feature, projectId, featureData)) {
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

	public void start() {
		
		
		init();
	}

	public void stop() {
		try {
			w.lock();
			service.flush();
		}
		finally {
			w.unlock();
		}
	}

	private void init() {
		// Since we run in an restricted environment we need to obtain the builder factory from the OSGi service
		// registry instead of trying to create a new one from the API
		XmlUtils.setDocumentBuilderFactory(SpringCoreUtils.getDocumentBuilderFactory());
		
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(UAA_PRODUCT_EXTENSION_POINT);
		if (point != null) {
			try {
				w.lock();
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement config : extension.getConfigurationElements()) {
						productDescriptors.add(new ExtensionProductDescriptor(config));
					}
				}
				productDescriptors.add(new ProductDescriptor());
			}
			finally {
				w.unlock();
			}
		}
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

	/**
	 * Extension to Spring UAA's {@link UaaServiceImpl} that caches reported
	 * products and features until it can be flushed into UAA's internal
	 * storage.
	 */
	private class CachingUaaServiceImpl extends TransmissionAwareUaaServiceImpl {

		/** Internal cache of reported features */
		private List<ReportedFeature> features = new ArrayList<ReportedFeature>();

		/** Internal cache of reported products */
		private List<ReportedProduct> products = new ArrayList<ReportedProduct>();

		public CachingUaaServiceImpl(TransmissionService transmssionService) {
			super(transmssionService);
		}

		/**
		 * Flushes the internal cache into the UAA backend store.
		 */
		public void flush() {
			if (products.size() == 0 && features.size() == 0) {
				return;
			}
			if (!canRegister()) {
				return;
			}

			for (ReportedProduct product : products) {
				super.registerProductUsage(product.getProduct(), product.getProductData(), product.getProjectId());
			}
			products.clear();

			for (ReportedFeature feature : features) {
				super.registerFeatureUsage(feature.getProduct(), feature.getFeatureUse(), feature.getFeatureData());
			}
			features.clear();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void registerFeatureUsage(Product product, FeatureUse feature) {
			if (canRegister()) {
				flush();
				super.registerFeatureUsage(product, feature);
			}
			else {
				cacheFeature(product, feature, null);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void registerFeatureUsage(Product product, FeatureUse feature, byte[] featureData) {
			if (canRegister()) {
				flush();
				super.registerFeatureUsage(product, feature, featureData);
			}
			else {
				cacheFeature(product, feature, featureData);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void registerProductUsage(Product product) {
			if (canRegister()) {
				flush();
				super.registerProductUsage(product);
			}
			else {
				cacheProduct(product, null, null);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void registerProductUsage(Product product, byte[] productData) {
			if (canRegister()) {
				flush();
				super.registerProductUsage(product, productData);
			}
			else {
				cacheProduct(product, productData, null);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void registerProductUsage(Product product, byte[] productData, String projectId) {
			if (canRegister()) {
				flush();
				super.registerProductUsage(product, productData, projectId);
			}
			else {
				cacheProduct(product, productData, projectId);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void registerProductUsage(Product product, String projectId) {
			if (canRegister()) {
				flush();
				super.registerProductUsage(product, projectId);
			}
			else {
				cacheProduct(product, null, projectId);
			}
		}

		private void cacheFeature(Product product, FeatureUse feature, byte[] featureData) {
			features.add(new ReportedFeature(feature, product, featureData));
		}

		private void cacheProduct(Product product, byte[] productData, String projectId) {
			products.add(new ReportedProduct(product, projectId, productData));
		}

		private boolean canRegister() {
			return UrlHelper.isUaaCommunicationsAllowed();
		}
		
		protected UaaEnvelope getPayload() {
			return super.createUaaEnvelop();
		}
		
		private class ReportedFeature {

			private final byte[] featureData;

			private final FeatureUse feature;
			
			private final Product product;

			public ReportedFeature(FeatureUse feature, Product product, byte[] featureData) {
				this.feature = feature;
				this.product = product;
				this.featureData = featureData;
			}

			public byte[] getFeatureData() {
				return featureData;
			}

			public FeatureUse getFeatureUse() {
				return feature;
			}

			public Product getProduct() {
				return product;
			}
		}

		private class ReportedProduct {
			private final Product product;

			private final byte[] productData;

			private final String projectId;

			public ReportedProduct(Product product, String projectId, byte[] productData) {
				this.product = product;
				this.projectId = projectId;
				this.productData = productData;
			}

			public Product getProduct() {
				return product;
			}

			public byte[] getProductData() {
				return productData;
			}

			public String getProjectId() {
				return projectId;
			}
		}
	}

	private class ExtensionProductDescriptor extends ProductDescriptor {

		private Map<String, String> plugins;

		private String productId;

		private String rootPlugin;

		private String sourceControlIdentifier;

		public ExtensionProductDescriptor(IConfigurationElement element) {
			init(element);
		}

		private void init(IConfigurationElement element) {
			productId = element.getAttribute("id");
			sourceControlIdentifier = element.getAttribute("source-control-identifier");

			rootPlugin = element.getNamespaceIdentifier();
			if (element.getAttribute("root-plugin") != null) {
				rootPlugin = element.getAttribute("root-plugin");
			}

			plugins = new HashMap<String, String>();
			for (IConfigurationElement featureElement : element.getChildren("feature")) {
				String feature = featureElement.getAttribute("id");
				if (feature != null) {
					for (IConfigurationElement pluginElement : featureElement.getChildren("plugin")) {
						String pluginId = pluginElement.getAttribute("id");
						if (pluginId != null) {
							// Verify that the plugin does not belong to another eclipse feature. This is 
							// required to associate plugins patched through feature patches to the correct
							// root feature (e.g. Groovy Eclipse patching JDT core)
							String owningFeature = getOwningEclipseFeature(pluginId);
							if (owningFeature == null || feature.equals(owningFeature)) {
								plugins.put(pluginElement.getAttribute("id"), feature);
							}
						}
					}
				}
			}

			// Try to create the product; we'll try again later if this one fails
			buildProduct(rootPlugin, productId, sourceControlIdentifier);
		}
		
		protected boolean canRegister(String usedPlugin) {
			return plugins.containsKey(usedPlugin);
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
	
	private class ProductDescriptor {

		protected Product product;

		protected boolean registered = false;

		public ProductDescriptor() {
			init();
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

		public final boolean registerFeatureUseIfMatch(String usedPlugin, Map<String, String> featureData) {
			if (canRegister(usedPlugin)) {

				registerProductIfRequired(null);
				registerFeature(usedPlugin, featureData);

				return true;
			}
			return false;
		}

		private String getRegisteredFeatureData(String usedPlugin) {
			try {
				UaaEnvelope uaaEnvelope = service.getPayload();
				if (uaaEnvelope != null && uaaEnvelope.getUserAgent() != null) {
					UserAgent userAgent = uaaEnvelope.getUserAgent();
					for (int i = 0; i < userAgent.getProductUseCount(); i++) {
						ProductUse p = userAgent.getProductUse(i);
						if (p.getProduct().getName().equals(product.getName())) {
							for (int j = 0; j < p.getFeatureUseCount(); j++) {
								FeatureUse f = p.getFeatureUse(j);
								if (f.getName().equals(usedPlugin) && !f.getFeatureData().isEmpty()) {
									return f.getFeatureData().toStringUtf8();
								}
							}
						}
					}
				}
			}
			catch (Exception e) {}
			return null;
		}

		private void init() {
			buildProduct(Platform.PI_RUNTIME, "Eclipse", null);
		}

		@SuppressWarnings("unchecked")
		private JSONObject mergeFeatureData(String usedPlugin, Map<String, String> featureData) {
			JSONObject existingFeatureData = new JSONObject();

			// Quick sanity check to prevent doing too much in case no new
			// feature data has been presented
			if (featureData == null || featureData.size() == 0) {
				return existingFeatureData;
			}

			// Load existing feature data from backend store
			String existingFeatureDataString = getRegisteredFeatureData(usedPlugin);
			if (existingFeatureDataString != null) {
				Object existingJson = JSONValue.parse(existingFeatureDataString);
				if (existingJson instanceof JSONObject) {
					existingFeatureData.putAll(((JSONObject) existingJson));
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

			return existingFeatureData;
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
			// Get existing feature data and merge with new data
			JSONObject json = mergeFeatureData(usedPlugin, featureData);
			
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
				// If additional feature data was supplied or is already registered pass it to UAA
				if (json.size() > 0) {
					service.registerFeatureUsage(product, feature, json.toJSONString().getBytes("UTF-8"));
				}
				else {
					service.registerFeatureUsage(product, feature);
				}
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

	/**
	 * Extension to {@link JdkUrlTransmissionServiceImpl} that hooks in the Eclipse {@link IProxyService}.
	 * @since 2.6.0
	 */
	private class JdkUrlProxyAwareTransmissionService extends JdkUrlTransmissionServiceImpl {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Proxy setupProxy(URL url) {
			IProxyData selectedProxy = getProxy(url);
			if (selectedProxy != null) {
				return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(selectedProxy.getHost(),
						selectedProxy.getPort()));
			}
			return super.setupProxy(url);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Authenticator setupProxyAuthentication(URL url, Proxy proxy) {
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
			IProxyService proxyService = UaaPlugin.getDefault().getProxyService();
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
		 * Select a proxy from the list of avaiable proxies. 
		 */
		private IProxyData selectProxy(String protocol, IProxyData[] proxies) {
			if (proxies == null || proxies.length == 0)
				return null;
			// If only one proxy is available, then use that
			if (proxies.length == 1) {
				return proxies[0];
			}
			// If more than one proxy is available, then if http/https protocol
			// then look for that one...if not found then use first
			if (protocol.equalsIgnoreCase("http")) { //$NON-NLS-1$
				for (int i = 0; i < proxies.length; i++) {
					if (proxies[i].getType().equals(IProxyData.HTTP_PROXY_TYPE))
						return proxies[i];
				}
			}
			else if (protocol.equalsIgnoreCase("https")) { //$NON-NLS-1$
				for (int i = 0; i < proxies.length; i++) {
					if (proxies[i].getType().equals(IProxyData.HTTPS_PROXY_TYPE))
						return proxies[i];
				}
			}
			// If we haven't found it yet, then return the first one.
			return proxies[0];
		}
	}

}
