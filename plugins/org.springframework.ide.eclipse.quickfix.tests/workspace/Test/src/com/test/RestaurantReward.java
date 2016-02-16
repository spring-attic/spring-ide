/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package com.test;

/**
 * @author Leo Dos Santos
 */
public class RestaurantReward {

	private String date;
	
	private int award;
	
	private boolean recurring;

	public RestaurantReward(String date, int award, boolean recurring) {
		this.date = date;
		this.award = award;
		this.recurring = recurring;
	}
	
}
