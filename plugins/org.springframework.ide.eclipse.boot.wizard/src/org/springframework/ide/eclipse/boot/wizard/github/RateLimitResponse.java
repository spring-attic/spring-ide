/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.github;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class RateLimitResponse {

	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class RateLimit {
		private int limit;
		private int remaining;
		private int reset;
		public int getLimit() {
			return limit;
		}
		public void setLimit(int limit) {
			this.limit = limit;
		}
		public int getRemaining() {
			return remaining;
		}
		public void setRemaining(int remaining) {
			this.remaining = remaining;
		}
		public int getReset() {
			return reset;
		}
		public void setReset(int reset) {
			this.reset = reset;
		}
	}

	private RateLimit rate;

	public RateLimit getRate() {
		return rate;
	}

	public void setRate(RateLimit rate) {
		this.rate = rate;
	}

}
