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

import org.springframework.uaa.client.protobuf.UaaClient.Product;
import org.springframework.util.ObjectUtils;

class ProductUsageRecord extends UsageRecord {

	private final byte[] productData;
	private final String projectId;

	public ProductUsageRecord(Product product, String projectId, byte[] productData) {
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
			if (obj instanceof ProductUsageRecord) {
				ProductUsageRecord o = (ProductUsageRecord) obj;
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