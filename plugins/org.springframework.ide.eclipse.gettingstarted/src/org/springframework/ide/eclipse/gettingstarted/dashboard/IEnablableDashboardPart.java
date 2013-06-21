/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.gettingstarted.dashboard;

/**
 * 'Parts' (i.e. pages or things that can be added to pages) that need some
 * logic to decide if they are 'enabled' in the current STS installation
 * should implement this interface.
 * <p>
 * Anything that doesn't implement this interface will be enabled unconditionally.
 * 
 * @author Kris De Volder
 */
public interface IEnablableDashboardPart {
	
	public boolean shouldAdd();

}
