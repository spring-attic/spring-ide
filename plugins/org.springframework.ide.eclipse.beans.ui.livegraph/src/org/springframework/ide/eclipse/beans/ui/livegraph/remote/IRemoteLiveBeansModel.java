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
package org.springframework.ide.eclipse.beans.ui.livegraph.remote;

/**
 * A mock interface for a hypothetical MBean exposed by the Spring Framework.
 * 
 * @author Leo Dos Santos
 */
public interface IRemoteLiveBeansModel {

	String getName();

	void setName(String name);

}
