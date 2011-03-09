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

import org.springframework.uaa.client.protobuf.UaaClient.FeatureUse;
import org.springframework.uaa.client.protobuf.UaaClient.Product;
import org.springframework.util.ObjectUtils;

class FeatureUseUsageRecord extends UsageRecord {

	private final FeatureUse feature;
	private final byte[] featureData;

	public FeatureUseUsageRecord(FeatureUse feature, Product product, byte[] featureData) {
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
			if (obj instanceof FeatureUseUsageRecord) {
				FeatureUseUsageRecord o = (FeatureUseUsageRecord) obj;
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