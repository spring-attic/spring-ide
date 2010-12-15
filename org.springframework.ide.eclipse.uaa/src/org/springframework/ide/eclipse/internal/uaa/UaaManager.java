/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
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
import java.net.URL;
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
import org.eclipse.core.runtime.Platform;
import org.json.simple.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springframework.uaa.client.UaaService;
import org.springframework.uaa.client.UrlHelper;
import org.springframework.uaa.client.VersionHelper;
import org.springframework.uaa.client.internal.UaaServiceImpl;
import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;
import org.springframework.uaa.client.protobuf.UaaClient.Product;

/**
 * Helper class that coordinates with the Spring UAA service implementation.
 * <p>
 * This implementation primarily serves as wrapper around the {@link UaaService}.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class UaaManager implements IUaa {

	private static final String UAA_PRODUCT_EXTENSION_POINT = "org.springframework.ide.eclipse.uaa.product";

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	private List<ProductDescriptor> productDescriptors = new ArrayList<ProductDescriptor>();

	private final CachingUaaServiceImpl service = new CachingUaaServiceImpl();

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	/**
	 * {@inheritDoc}
	 */
	public boolean canOpenUrl(URL url) {
		if (UrlHelper.isVMwareDomain(url)) {
			return canOpenVMwareUrls();
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canOpenVMwareUrls() {
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
	public void registerFeatureUse(final String plugin, final String json) {
		if (plugin != null) {
			executorService.execute(new Runnable() {

				public void run() {
					try {
						w.lock();
						for (ProductDescriptor productDescriptor : productDescriptors) {
							if (productDescriptor.registerFeatureUseIfMatch(plugin, json)) {
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
			version = "0.0.0.RELEASE";
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
	 * Extension to Spring UAA's {@link UaaServiceImpl} that caches reported products and features
	 * until it can be flushed into UAA's internal storage.  
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
			return canOpenVMwareUrls();
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

		protected Product product;

		public ExtensionProductDescriptor(IConfigurationElement element) {
			init(element);
		}

		public boolean registerFeatureUseIfMatch(String usedPlugin, String featureJson) {
			if (plugins.containsKey(usedPlugin)) {
				if (!this.registered) {
					service.registerProductUsage(product);
					this.registered = true;
				}
				if (featureJson != null) {
					try {
						service.registerFeatureUsage(product, plugins.get(usedPlugin), featureJson.getBytes("UTF-8"));
					}
					catch (UnsupportedEncodingException e) {
						// cannot happen
					}
				}
				else {
					service.registerFeatureUsage(product, plugins.get(usedPlugin));
				}
				return true;
			}
			return false;
		}

		private void init(IConfigurationElement element) {
			this.plugins = new HashMap<String, String>();
			String productId = element.getAttribute("id");
			String sourceCodeIdentifier = element.getAttribute("source-control-identifier");

			String rootPlugin = element.getNamespaceIdentifier();
			if (element.getAttribute("root-plugin") != null) {
				rootPlugin = element.getAttribute("root-plugin");
			}

			for (IConfigurationElement featureElement : element.getChildren("feature")) {
				String feature = featureElement.getAttribute("id");
				for (IConfigurationElement pluginElement : featureElement.getChildren("plugin")) {
					plugins.put(pluginElement.getAttribute("id"), feature);
				}
			}

			Bundle bundle = Platform.getBundle(rootPlugin);
			Version version = bundle.getVersion();

			Product.Builder b = Product.newBuilder();
			b.setName(productId);
			b.setMajorVersion(version.getMajor());
			b.setMinorVersion(version.getMinor());
			b.setPatchVersion(version.getMicro());
			b.setReleaseQualifier(version.getQualifier());

			if (sourceCodeIdentifier != null && sourceCodeIdentifier.length() > 0) {
				b.setSourceControlIdentifier(sourceCodeIdentifier);
			}

			this.product = b.build();
		}
	}

	private class ProductDescriptor {

		protected Product product;

		protected boolean registered = false;

		public ProductDescriptor() {
			init();
		}

		@SuppressWarnings("unchecked")
		public boolean registerFeatureUseIfMatch(String usedPlugin, String featureJson) {
			// Due to privacy considerations only org.eclipse plugins will get recorded
			if (usedPlugin.startsWith("org.eclipse")) {
				if (!this.registered) {
					JSONObject json = new JSONObject();
					json.put("platform",
							String.format("%s.%s.%s", Platform.getOS(), Platform.getWS(), Platform.getOSArch()));

					if (System.getProperty("eclipse.buildId") != null) {
						json.put("build.id", System.getProperty("eclipse.buildId"));
					}
					if (System.getProperty("eclipse.product") != null) {
						json.put("product", System.getProperty("eclipse.product"));
					}
					if (System.getProperty("eclipse.application") != null) {
						json.put("application", System.getProperty("eclipse.application"));
					}

					try {
						service.registerProductUsage(product, json.toJSONString().getBytes("UTF-8"));
					}
					catch (UnsupportedEncodingException e) {
						// cannot happen
					}
					this.registered = true;
				}

				if (featureJson != null) {
					try {
						service.registerFeatureUsage(product, usedPlugin, featureJson.getBytes("UTF-8"));
					}
					catch (UnsupportedEncodingException e) {
						// cannot happen
					}
				}
				else {
					service.registerFeatureUsage(product, usedPlugin);
				}

				return true;
			}
			return false;
		}

		private void init() {
			Bundle bundle = Platform.getBundle(Platform.PI_RUNTIME);
			Version version = bundle.getVersion();

			Product.Builder b = Product.newBuilder();
			b.setName("Eclipse");
			b.setMajorVersion(version.getMajor());
			b.setMinorVersion(version.getMinor());
			b.setPatchVersion(version.getMicro());
			b.setReleaseQualifier(version.getQualifier());

			this.product = b.build();
		}
	}

}
