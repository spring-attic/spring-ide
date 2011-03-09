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
import org.springframework.uaa.client.internal.UaaServiceImpl;
import org.springframework.uaa.client.protobuf.UaaClient.FeatureUse;
import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;
import org.springframework.uaa.client.protobuf.UaaClient.Product;
import org.springframework.uaa.client.protobuf.UaaClient.UaaEnvelope;

/**
 * Extension to Spring UAA's {@link UaaServiceImpl} that caches reported products and features
 * until it can be flushed into UAA's internal storage.
 * 
 * @author Christian Dupuis
 * @since 2.5.2.SR3
 */
public class CachingUaaServiceExtension extends QueueingUaaServiceExtension {

	/** Internal cache of reported products and features; make this an ordered list as we want 
	 * to maintain the correct ordering when replaying it later */
	private List<UsageRecord> usageRecords = new ArrayList<UsageRecord>();

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
	private void flushIfPossible() {
		if (usageRecords.size() == 0) {
			// Nothing to flush
			return;
		}
		
		if (!isUaaTermsOfUseAccepted()) {
			// We are not allowed to save any usage data
			return;
		}

		// If we got this far we can now store reported product and feature usages into the backend store
		for (UsageRecord record : usageRecords) {
			if (record instanceof ProductUsageRecord) {
				super.registerProductUsage(record.getProduct(), ((ProductUsageRecord) record).getProductData(), 
						((ProductUsageRecord) record).getProjectId());
			}
			else if (record instanceof FeatureUseUsageRecord) {
				super.registerFeatureUsage(record.getProduct(), ((FeatureUseUsageRecord) record).getFeatureUse(), 
						((FeatureUseUsageRecord) record).getFeatureData());
			}
		}
		usageRecords.clear();
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
			cacheFeatureUseUsageRecord(product, feature, featureData);
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
			cacheProductUsageRecord(product, productData, projectId);
		}
	}

	private void cacheFeatureUseUsageRecord(Product product, FeatureUse feature, byte[] featureData) {
		cacheUsageRecord(new FeatureUseUsageRecord(feature, product, featureData));
	}

	private void cacheProductUsageRecord(Product product, byte[] productData, String projectId) {
		cacheUsageRecord(new ProductUsageRecord(product, projectId, productData));
	}
	
	private void cacheUsageRecord(UsageRecord record) {
		// Don't cache it again if we already have that record
		if (!usageRecords.contains(record)) {
			usageRecords.add(record);
		}
	}

	public UaaEnvelope getPayload() {
		return super.createUaaEnvelope();
	}
	
	public void stop() {
		flushIfPossible();
		super.stop();
	}
}