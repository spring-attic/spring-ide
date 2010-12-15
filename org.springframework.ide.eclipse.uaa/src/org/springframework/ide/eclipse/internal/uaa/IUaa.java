/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa;

import java.net.URL;

/**
 * @author Christian Dupuis
 * @since 2.5.2
 */
public interface IUaa {

	boolean canOpenUrl(URL url);

	boolean canOpenVMwareUrls();
	
	void clear();
	
	int getPrivacyLevel();

	String getUserAgentContents(String userAgentHeader);
	
	String getUserAgentHeader();
	
	void setPrivacyLevel(int level);
	
}
