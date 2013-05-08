/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.github;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * GitHub enforces rate limits on API users. An instance of this class 
 * contains info about the remaining quota.
 * 
 * @author Kris De Volder
 */
public class RateLimit {
	
	private class Rate {
		
		@SuppressWarnings("unused"
				//Although this appears 'unused' it is required and used 
				//reflectively by JacksonMapper to deserialise Json data.
		)
		public Rate() {}
		
		@JsonProperty("remaining")
		private int remaining;
		
		@JsonProperty("limit")
		private int limit;
		
		@Override
		public String toString() {
			return remaining + "/" + limit;
		}
		
	}
	
	/**
	 * @return Remaining quota as percentage of total quota. 
	 */
	public double getPercentRemaining() {
		return ((double)rate.remaining)/rate.limit;
	}

	/**
	 * @return Remaining number of requests before exceeding quota limit.
	 */
	public int getRemaining() {
		return rate.remaining;
	}
	
	@JsonProperty("rate")
	private Rate rate;
	
	@Override
	public String toString() {
		return ""+rate;
	}

}
