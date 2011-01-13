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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.ide.eclipse.uaa.UaaPlugin;
import org.springframework.uaa.client.UaaService;
import org.springframework.uaa.client.VersionHelper;
import org.springframework.uaa.client.internal.UaaServiceImpl;
import org.springframework.uaa.client.protobuf.UaaClient.FeatureUse;
import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;
import org.springframework.uaa.client.protobuf.UaaClient.Product;
import org.springframework.uaa.client.protobuf.UaaClient.ProductUse;
import org.springframework.uaa.client.protobuf.UaaClient.UserAgent;

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

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private final List<ProductDescriptor> productDescriptors = new ArrayList<ProductDescriptor>();

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	private final CachingUaaServiceImpl service = new CachingUaaServiceImpl();

	/**
	 * {@inheritDoc}
	 */
	private boolean hasUserDecided() {
		try {
			r.lock();
			return !(service.getPrivacyLevel() == PrivacyLevel.UNDECIDED_TOU || service.getPrivacyLevel() == PrivacyLevel.DECLINE_TOU);
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		try {
			w.lock();
			service.clearIfPossible();
		}
		finally {
			w.unlock();
		}
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
	public String getUsageDataFromUserAgentHeader(String userAgent) {
		try {
			r.lock();
			return service.toString(service.fromHttpUserAgentHeaderValue(userAgent));
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserAgentHeader() {
		try {
			r.lock();
			return service.toHttpUserAgentHeaderValue();
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
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(UAA_PRODUCT_EXTENSION_POINT);
		if (point != null) {
			try {
				w.lock();
				for (IExtension extension : point.getExtensions()) {
					for (IConfigurationElement config : extension.getConfigurationElements()) {
						productDescriptors.add(new ExtensionProductDescriptor(config));
					}
				}
			}
			finally {
				w.unlock();
			}
		}
		productDescriptors.add(new ProductDescriptor());
	}

	/**
	 * Extension to Spring UAA's {@link UaaServiceImpl} that caches reported products and features until it can be
	 * flushed into UAA's internal storage.
	 */
	private class CachingUaaServiceImpl extends UaaServiceImpl {

		/** Internal cache of reported features */
		private List<ReportedFeature> features = new ArrayList<ReportedFeature>();

		/** Internal cache of reported products */
		private List<ReportedProduct> products = new ArrayList<ReportedProduct>();

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
				super.registerFeatureUsage(feature.getProduct(), feature.getFeatureName(), feature.getFeatureData());
			}
			features.clear();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void registerFeatureUsage(Product product, String featureName) {
			if (canRegister()) {
				flush();
				super.registerFeatureUsage(product, featureName);
			}
			else {
				cacheFeature(product, featureName, null);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void registerFeatureUsage(Product product, String featureName, byte[] featureData) {
			if (canRegister()) {
				flush();
				super.registerFeatureUsage(product, featureName, featureData);
			}
			else {
				cacheFeature(product, featureName, featureData);
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

		private void cacheFeature(Product product, String featureName, byte[] featureData) {
			features.add(new ReportedFeature(featureName, product, featureData));
		}

		private void cacheProduct(Product product, byte[] productData, String projectId) {
			products.add(new ReportedProduct(product, projectId, productData));
		}

		private boolean canRegister() {
			return hasUserDecided();
		}

		private class ReportedFeature {

			private final byte[] featureData;

			private final String featureName;

			private final Product product;

			public ReportedFeature(String featureName, Product product, byte[] featureData) {
				this.featureName = featureName;
				this.product = product;
				this.featureData = featureData;
			}

			public byte[] getFeatureData() {
				return featureData;
			}

			public String getFeatureName() {
				return featureName;
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

		private String sourceCodeIdentifier;

		public ExtensionProductDescriptor(IConfigurationElement element) {
			init(element);
		}

		private void init(IConfigurationElement element) {
			productId = element.getAttribute("id");
			sourceCodeIdentifier = element.getAttribute("source-control-identifier");

			rootPlugin = element.getNamespaceIdentifier();
			if (element.getAttribute("root-plugin") != null) {
				rootPlugin = element.getAttribute("root-plugin");
			}

			plugins = new HashMap<String, String>();
			for (IConfigurationElement featureElement : element.getChildren("feature")) {
				String feature = featureElement.getAttribute("id");
				for (IConfigurationElement pluginElement : featureElement.getChildren("plugin")) {
					plugins.put(pluginElement.getAttribute("id"), feature);
				}
			}

			// Try to create the product; we'll try again later if this one fails
			buildProduct(rootPlugin, productId, sourceCodeIdentifier);
		}

		protected boolean canRegister(String usedPlugin) {
			return plugins.containsKey(usedPlugin);
		}

		protected void registerProductIfRequired() {
			// If we initially failed to create the product it was probably because it wasn't installed when the
			// workbench started; but now it is so try again
			if (product == null) {
				buildProduct(rootPlugin, productId, sourceCodeIdentifier);
			}

			// Check if the product is already registered; if not register it before we capture feature usage
			if (!registered) {
				service.registerProductUsage(product);
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

		public final boolean registerFeatureUseIfMatch(String usedPlugin, Map<String, String> featureData) {
			if (canRegister(usedPlugin)) {

				registerProductIfRequired();
				registerFeature(usedPlugin, featureData);

				return true;
			}
			return false;
		}

		private void init() {
			buildProduct(Platform.PI_RUNTIME, "Eclipse", null);
		}

		protected void buildProduct(String symbolicName, String name, String sourceCodeIdentifier) {
			Bundle bundle = Platform.getBundle(symbolicName);
			if (bundle != null) {
				Version version = bundle.getVersion();

				Product.Builder b = Product.newBuilder();
				b.setName(name);
				b.setMajorVersion(version.getMajor());
				b.setMinorVersion(version.getMinor());
				b.setPatchVersion(version.getMicro());
				b.setReleaseQualifier(version.getQualifier());

				if (sourceCodeIdentifier != null && sourceCodeIdentifier.length() > 0) {
					b.setSourceControlIdentifier(sourceCodeIdentifier);
				}

				product = b.build();
			}
		}

		protected boolean canRegister(String usedPlugin) {
			// Due to privacy considerations only org.eclipse plugins will get recorded
			return usedPlugin.startsWith("org.eclipse");
		}

		protected void registerFeature(String usedPlugin, Map<String, String> featureData) {
			// Get existing feature data and merge with new data
			JSONObject json = mergeFeatureData(usedPlugin, featureData);

			// Add plug-in version number to the featureJson
			// Bundle bundle = Platform.getBundle(usedPlugin);
			// if (bundle != null) {
			// String version = (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
			// if (version != null) {
			// json.put("version", version);
			// }
			// }

			try {
				if (json.size() > 0) {
					service.registerFeatureUsage(product, usedPlugin, json.toJSONString().getBytes("UTF-8"));
				}
				else {
					service.registerFeatureUsage(product, usedPlugin);
				}
			}
			catch (UnsupportedEncodingException e) {
				// Cannot happen
			}
		}

		@SuppressWarnings("unchecked")
		private JSONObject mergeFeatureData(String usedPlugin, Map<String, String> featureData) {
			JSONObject existingFeatureData = new JSONObject();

			// Quick sanity check to prevent doing too much in case no new feature data has been presented
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

		private String getRegisteredFeatureData(String usedPlugin) {
			try {
				UserAgent userAgent = service.fromHttpUserAgentHeaderValue(getUserAgentHeader());
				if (userAgent != null) {
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
			catch (Exception e) {
				UaaPlugin
						.getDefault()
						.getLog()
						.log(new Status(IStatus.WARNING, UaaPlugin.PLUGIN_ID,
								"Error retrieving user agent header from UAA", e));
			}
			return null;
		}

		protected void registerProductIfRequired() {
			if (!registered) {
				Map<String, String> productData = new HashMap<String, String>();
				productData.put("platform",
						String.format("%s.%s.%s", Platform.getOS(), Platform.getWS(), Platform.getOSArch()));

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
					service.registerProductUsage(product, JSONObject.toJSONString(productData).getBytes("UTF-8"));
				}
				catch (UnsupportedEncodingException e) {
					// cannot happen
				}
				registered = true;
			}
		}
	}

}
