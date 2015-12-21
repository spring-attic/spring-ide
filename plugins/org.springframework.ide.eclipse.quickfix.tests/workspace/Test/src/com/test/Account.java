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
 * @author Terry Denney
 */
public class Account {

	private int balance;
	
	public Account() {
	}
	
	public void initializeAccount() {
		balance = 0;
	}
	
	@Deprecated
	public void createAccount() {
		
	}
	
	public void disposeAccount() {
		AccountManager.unregisterAccount(this);
	}
	
	@Deprecated
	public void deleteAccount() {
		
	}
	
	public int getBalance() {
		return balance;
	}
	
	public void setBalance(int balance) {
		this.balance = balance;
	}
	
	public static class AccountReader {
		
		public void displayAccount() {
			System.out.println("Account with balance " + balance);
		}
		
	}
}
