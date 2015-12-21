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
public class AutowiredAccountContribution {

	private Account account;
	
	@org.springframework.beans.factory.annotation.Autowired
	public AutowiredAccountContribution(Account account) {
		this.account = account;
	}
	
	public void setAccount(Account account) {
		this.account = account;
	}
	
}
