/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

/**
 * Interface for an artifact that can be connected/disconnected
 *
 * @author Alex Boyko
 *
 */
public interface Connectable {

	public interface ConnectionStateListener {
		void changed(Connectable connectable);
	}

	void connect();

	void disconnect();

	boolean isConnected();

	void addConnectionStateListener(ConnectionStateListener l);

	void removeConnectionStateListener(ConnectionStateListener l);

}
