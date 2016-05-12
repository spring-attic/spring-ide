/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

/**
 * Password dialog model. Provides ability to specify password and whether it
 * needs to be stored.
 *
 * @author Alex Boyko
 *
 */
public class PasswordDialogModel {

	final private String fUser;
	final private String fTargetId;
	final private LiveVariable<String> fPasswordVar;
	final private LiveVariable<Boolean> fStoreVar;
	private int fButtonPressed = -1;

	public PasswordDialogModel(String user, String targetId, String password, boolean secureStore) {
		super();
		fUser = user;
		fTargetId = targetId;
		fPasswordVar = new LiveVariable<>(password);
		fStoreVar = new LiveVariable<>(secureStore);
	}

	public PasswordDialogModel(String user, String targetId, boolean secureStore) {
		this(user, targetId, null, secureStore);
	}

	public String getUser() {
		return fUser;
	}

	public String getTargetId() {
		return fTargetId;
	}

	public LiveVariable<String> getPasswordVar() {
		return fPasswordVar;
	}

	public LiveVariable<Boolean> getStoreVar() {
		return fStoreVar;
	}

	public void buttonPressed(int button) {
		fButtonPressed = button;
	}

	public boolean isCancelled() {
		return fButtonPressed == IDialogConstants.CANCEL_ID;
	}

	public boolean isOk() {
		return fButtonPressed == IDialogConstants.OK_ID;
	}

}
