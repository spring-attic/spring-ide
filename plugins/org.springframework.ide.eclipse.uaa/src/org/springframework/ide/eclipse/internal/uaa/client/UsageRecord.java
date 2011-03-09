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

abstract class UsageRecord {
	
	private final Product product;
	
	public UsageRecord(Product product) {
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UsageRecord) {
			UsageRecord o = (UsageRecord) obj;
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