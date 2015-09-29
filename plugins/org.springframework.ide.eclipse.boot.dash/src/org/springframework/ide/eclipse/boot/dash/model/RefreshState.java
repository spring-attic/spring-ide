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

import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

/**
 * Represents the different states a 'refreshable' element may be in.
 *
 * @author Alex Boyko
 * @author Kris De Volder
 */
public abstract class RefreshState {
	public static final RefreshState READY = new SimpleState("READY");
	public static final RefreshState LOADING = new SimpleState("LOADING");
	public static RefreshState error(String msg) {
		return new ErrorState(msg);
	}
	public static RefreshState error(Exception e) {
		return error(ExceptionUtil.getMessage(e));
	}

	public abstract boolean isError();

	////////////////////////// implementation //////////////////////////////////////

	private static class SimpleState extends RefreshState {

		private String id;

		public SimpleState(String id) {
			this.id = id;
		}

		@Override
		public boolean isError() {
			return false;
		}

		@Override
		public String toString() {
			return id;
		}
	}

	private static class ErrorState extends RefreshState {

		private final String msg;

		public ErrorState(String msg) {
			this.msg = msg;
		}

		@Override
		public String toString() {
			if (msg!=null) {
				return "ERROR: "+msg;
			} else {
				return "ERROR";
			}
		}

		@Override
		public boolean isError() {
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((msg == null) ? 0 : msg.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ErrorState other = (ErrorState) obj;
			if (msg == null) {
				if (other.msg != null)
					return false;
			} else if (!msg.equals(other.msg))
				return false;
			return true;
		}

	}

}