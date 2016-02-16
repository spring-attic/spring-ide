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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Leo Dos Santos
 * @author Terry Denney
 */
public class AccountManager {

	private static Set<Account> accounts = new HashSet<Account>();
	
	public static Account createDefaultAccount() {
		Account account = new Account();
		accounts.add(account);
		return account;
	}
	
	public static void unregisterAccount(Account account) {
		accounts.remove(account);
	}
	
	@Deprecated
	public static Account createTempAccount() {
		return null;
	}
}
