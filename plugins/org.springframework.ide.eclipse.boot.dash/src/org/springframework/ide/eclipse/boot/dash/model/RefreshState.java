/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.base.Objects;

/**
 * Represents the different states a 'refreshable' element may be in.
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public class RefreshState {
	public static final RefreshState READY = new RefreshState("READY");
	public static final RefreshState LOADING = new RefreshState("LOADING");
	private static final RefreshState ERROR = new RefreshState("ERROR");
	private static final RefreshState WARNING = new RefreshState("ERROR");

	public static RefreshState error(String msg) {
		return new RefreshState(ERROR.id, msg);
	}

	public static RefreshState error(Throwable e) {
		return error(ExceptionUtil.getMessage(e));
	}

	public static RefreshState warning(String message) {
		return new RefreshState(WARNING.id, message);
	}

	public static RefreshState loading(String message) {
		return new RefreshState(LOADING.id, message);
	}

	private String id;
	private String message;

	private RefreshState(String id) {
		this.id = id;
	}

	private RefreshState(String id, String message) {
		this(id);
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return id + (message == null || message.isEmpty() ? "" : message);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, message);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == getClass()) {
			RefreshState other = (RefreshState) obj;
			return Objects.equal(id, other.id) && Objects.equal(message, other.message);
		}
		return false;
	}

	public boolean isError() {
		return id==ERROR.id;
	}

}