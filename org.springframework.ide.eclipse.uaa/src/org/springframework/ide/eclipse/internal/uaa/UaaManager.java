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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
import org.springframework.uaa.client.UaaService;
import org.springframework.uaa.client.internal.UaaServiceImpl;
import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;
import org.springframework.uaa.client.protobuf.UaaClient.Product;

/**
 * Helper class that coordinates with the Spring UAA service implementation.
 * <p>
 * This implementation primarily serves as wrapper around the {@link UaaService}.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class UaaManager {

	private List<ProductDescriptor> productDescriptors = new ArrayList<ProductDescriptor>();

	private final UaaService service = new UaaServiceImpl();

	public UaaManager() {
		init();
	}

	public int getPrivacyLevel() {
		return this.service.getPrivacyLevel().getNumber();
	}

	public String getUserAgentContents(String userAgent) {
		return service.toString(service.fromHttpUserAgentHeaderValue(userAgent));
	}

	public String getUserAgentHeader() {
		return service.toHttpUserAgentHeaderValue();
	}

	public void registerFeatureUse(String plugin) {
		if (plugin != null) {
			for (ProductDescriptor productDescriptor : productDescriptors) {
				if (productDescriptor.registerFeatureUseIfMatch(plugin, service)) {
					return;
				}
			}
		}
	}

	public void registerProductUse(String productId, String versionString) {
		if (productId != null && versionString != null) {
			Version version = Version.parseVersion(versionString);
			Product.Builder product = Product.newBuilder();
			product.setName(productId);
			product.setMajorVersion(version.getMajor());
			product.setMinorVersion(version.getMinor());
			product.setPatchVersion(version.getMicro());
			product.setReleaseQualifier(version.getQualifier());
			// product.setSourceControlIdentifier();
			
			service.registerProductUsage(product.build());
		}
	}

	public void setPrivacyLevel(int level) {
		service.setPrivacyLevel(PrivacyLevel.valueOf(level));
	}

	private void init() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				"org.springframework.ide.eclipse.uaa.product");
		if (point != null) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					productDescriptors.add(new ExtensionProductDescriptor(config));
				}
			}
		}
		productDescriptors.add(new ProductDescriptor());
	}

	private static class ExtensionProductDescriptor extends ProductDescriptor {

		private String feature;

		private Map<String, String> plugins;

		protected Product product;

		public ExtensionProductDescriptor(IConfigurationElement element) {
			init(element);
		}

		public synchronized boolean registerFeatureUseIfMatch(String usedPlugin, UaaService service) {
			if (plugins.containsKey(usedPlugin)) {
				if (!this.registered) {
					service.registerProductUsage(product);
					this.registered = true;
				}
				service.registerFeatureUsage(product, plugins.get(usedPlugin));
				return true;
			}
			return false;
		}

		private void init(IConfigurationElement element) {
			this.plugins = new HashMap<String, String>();
			this.feature = element.getAttribute("id");
			String rootPlugin = element.getNamespaceIdentifier();

			for (IConfigurationElement featureElement : element.getChildren("feature")) {
				String feature = featureElement.getAttribute("id");
				for (IConfigurationElement pluginElement : featureElement.getChildren("plugin")) {
					plugins.put(pluginElement.getAttribute("id"), feature);
				}
			}

			Bundle bundle = Platform.getBundle(rootPlugin);
			Version version = bundle.getVersion();

			Product.Builder b = Product.newBuilder();
			b.setName(feature);
			b.setMajorVersion(version.getMajor());
			b.setMinorVersion(version.getMinor());
			b.setPatchVersion(version.getMicro());
			b.setReleaseQualifier(version.getQualifier());
			// b.setSourceControlIdentifier();

			this.product = b.build();
		}
	}

	private static class ProductDescriptor {

		protected Product product;

		protected boolean registered = false;

		public ProductDescriptor() {
			init();
		}

		public synchronized boolean registerFeatureUseIfMatch(String usedPlugin, UaaService service) {
			// Due to privacy considerations only org.eclipse plugins will get recorded
			if (usedPlugin.startsWith("org.eclipse")) {
				if (!this.registered) {
					service.registerProductUsage(product);
					service.registerFeatureUsage(
							product,
							String.format("platform: %s.%s.%s", Platform.getOS(), Platform.getWS(),
									Platform.getOSArch()));
					if (System.getProperty("eclipse.buildId") != null) {
						service.registerFeatureUsage(product,
								String.format("build.id: %s", System.getProperty("eclipse.buildId")));
					}
					if (System.getProperty("eclipse.product") != null) {
						service.registerFeatureUsage(product,
								String.format("eclipse.product: %s", System.getProperty("eclipse.product")));
					}
					if (System.getProperty("eclipse.application") != null) {
						service.registerFeatureUsage(product,
								String.format("eclipse.application: %s", System.getProperty("eclipse.application")));
					}
					this.registered = true;
				}
				service.registerFeatureUsage(product, usedPlugin);
				return true;
			}
			return false;
		}

		private void init() {
			Bundle bundle = Platform.getBundle(Platform.PI_RUNTIME);
			Version version = bundle.getVersion();

			Product.Builder b = Product.newBuilder();
			b.setName("org.eclipse");
			b.setMajorVersion(version.getMajor());
			b.setMinorVersion(version.getMinor());
			b.setPatchVersion(version.getMicro());
			b.setReleaseQualifier(version.getQualifier());

			this.product = b.build();
		}

	}

}
