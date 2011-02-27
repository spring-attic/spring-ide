/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.uaa.client.TransmissionService;
import org.springframework.uaa.client.internal.TransmissionAwareUaaServiceImpl;
import org.springframework.uaa.client.internal.UaaServiceImpl;
import org.springframework.uaa.client.protobuf.UaaClient.FeatureUse;
import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;
import org.springframework.uaa.client.protobuf.UaaClient.Product;
import org.springframework.uaa.client.protobuf.UaaClient.UaaEnvelope;
import org.springframework.util.ObjectUtils;

/**
 * Extension to Spring UAA's {@link UaaServiceImpl} that caches reported products and features
 * until it can be flushed into UAA's internal storage.
 * 
 * @author Christian Dupuis
 * @since 2.5.2.SR3
 */
public class CachingUaaServiceExtension extends TransmissionAwareUaaServiceImpl {

	/** Internal cache of reported products and features; make this an ordered list as we want 
	 * to maintain the correct ordering when replaying it later */
	private List<RegistrationRecord> registrations = new ArrayList<RegistrationRecord>();

	public CachingUaaServiceExtension(TransmissionService transmssionService) {
		super(transmssionService);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPrivacyLevel(PrivacyLevel privacyLevel) {
		super.setPrivacyLevel(privacyLevel);
		
		// Since we change the privacy level we may be able to flush the internal storage
		flushIfPossible();
	}

	/**
	 * Flushes the internal cache into the UAA backend store.
	 */
	public void flushIfPossible() {
		if (registrations.size() == 0) {
			// Nothing to flush
			return;
		}
		
		if (!isUaaTermsOfUseAccepted()) {
			// We are not allowed to save any usage data
			return;
		}

		// If we got this far we can now store reported product and feature usages into the backend store
		for (RegistrationRecord record : registrations) {
			if (record instanceof ProductRegistrationRecord) {
				super.registerProductUsage(record.getProduct(), ((ProductRegistrationRecord) record).getProductData(), 
						((ProductRegistrationRecord) record).getProjectId());
			}
			else if (record instanceof FeatureUseRegistrationRecord) {
				super.registerFeatureUsage(record.getProduct(), ((FeatureUseRegistrationRecord) record).getFeatureUse(), 
						((FeatureUseRegistrationRecord) record).getFeatureData());
			}
		}
		registrations.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerFeatureUsage(Product product, FeatureUse feature) {
		registerFeatureUsage(product, feature, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerFeatureUsage(Product product, FeatureUse feature, byte[] featureData) {
		if (isUaaTermsOfUseAccepted()) {
			flushIfPossible();
			super.registerFeatureUsage(product, feature, featureData);
		}
		else {
			cacheFeatureUseRegistrationRecord(product, feature, featureData);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerProductUsage(Product product) {
		registerProductUsage(product, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerProductUsage(Product product, byte[] productData) {
		registerProductUsage(product, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerProductUsage(Product product, String projectId) {
		registerProductUsage(product, null, projectId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerProductUsage(Product product, byte[] productData, String projectId) {
		if (isUaaTermsOfUseAccepted()) {
			flushIfPossible();
			super.registerProductUsage(product, productData, projectId);
		}
		else {
			cacheProductRegistrationRecord(product, productData, projectId);
		}
	}

	private void cacheFeatureUseRegistrationRecord(Product product, FeatureUse feature, byte[] featureData) {
		cacheRegistractionRecord(new FeatureUseRegistrationRecord(feature, product, featureData));
	}

	private void cacheProductRegistrationRecord(Product product, byte[] productData, String projectId) {
		cacheRegistractionRecord(new ProductRegistrationRecord(product, projectId, productData));
	}
	
	private void cacheRegistractionRecord(RegistrationRecord record) {
		// Don't cache it again if we already have that record
		if (!registrations.contains(record)) {
			registrations.add(record);
		}
	}

	public UaaEnvelope getPayload() {
		return super.createUaaEnvelope();
	}
	
	private abstract class RegistrationRecord {
		
		private final Product product;
		
		public RegistrationRecord(Product product) {
			this.product = product;
		}

		public Product getProduct() {
			return product;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RegistrationRecord) {
				RegistrationRecord o = (RegistrationRecord) obj;
				return ObjectUtils.nullSafeEquals(product.toByteArray(), o.getProduct().toByteArray());
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ObjectUtils.nullSafeHashCode(product.toByteArray());
			return result;
		}
	}
	
	private class FeatureUseRegistrationRecord extends RegistrationRecord {

		private final FeatureUse feature;
		private final byte[] featureData;

		public FeatureUseRegistrationRecord(FeatureUse feature, Product product, byte[] featureData) {
			super(product);
			this.feature = feature;
			this.featureData = featureData;
		}

		public byte[] getFeatureData() {
			return featureData;
		}

		public FeatureUse getFeatureUse() {
			return feature;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				if (obj instanceof FeatureUseRegistrationRecord) {
					FeatureUseRegistrationRecord o = (FeatureUseRegistrationRecord) obj;
					if (ObjectUtils.nullSafeEquals(feature.toByteArray(), o.getFeatureUse().toByteArray())) {
						if (ObjectUtils.nullSafeEquals(featureData, o.getFeatureData())) {
							return true;
						}
					}
				}
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ObjectUtils.nullSafeHashCode(feature.toByteArray());
			result = prime * result + ObjectUtils.nullSafeHashCode(featureData);
			return result;
		}

	}

	private class ProductRegistrationRecord extends RegistrationRecord {

		private final byte[] productData;
		private final String projectId;

		public ProductRegistrationRecord(Product product, String projectId, byte[] productData) {
			super(product);
			this.projectId = projectId;
			this.productData = productData;
		}

		public byte[] getProductData() {
			return productData;
		}

		public String getProjectId() {
			return projectId;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (super.equals(obj)) {
				if (obj instanceof ProductRegistrationRecord) {
					ProductRegistrationRecord o = (ProductRegistrationRecord) obj;
					if (ObjectUtils.nullSafeEquals(productData, o.getProductData())) {
						if ((projectId == null && o.getProjectId() == null) || projectId != null && projectId.equals(o.getProjectId())) {
							return true;
						}
					}
				}
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ObjectUtils.nullSafeHashCode(productData);
			result = prime * result + ObjectUtils.nullSafeHashCode(projectId);
			return result;
		}
	}
}