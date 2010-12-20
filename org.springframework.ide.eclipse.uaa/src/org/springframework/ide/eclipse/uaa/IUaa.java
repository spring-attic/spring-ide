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
package org.springframework.ide.eclipse.uaa;

import java.net.URL;
import java.util.Map;

import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;

/**
 * Main entry point into the Spring UAA infrastructure.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public interface IUaa {

	/** Declined terms of use; no data will be recorded and send */
	int DECLINE_TOU = PrivacyLevel.DECLINE_TOU.getNumber();

	/** Capture and send full usage data */
	int FULL_DATA = PrivacyLevel.ENABLE_UAA.getNumber();

	/** Capture and send limited usage data */
	int LIMITED_DATA = PrivacyLevel.LIMITED_DATA.getNumber();

	/** Capture and send no usage data */
	int NO_DATA = PrivacyLevel.DISABLE_UAA.getNumber();

	/** User has not decided on terms of use; no data will be recorded and send */
	int UNDECIDED_TOU = PrivacyLevel.UNDECIDED_TOU.getNumber();

	/** Default privacy level */
	int DEFAULT_PRIVACY_LEVEL = FULL_DATA;

	/**
	 * Registers usage of the given feature identified by <code>plugin</code>.
	 */
	void registerFeatureUse(String plugin);

	/**
	 * Registers usage of the given feature identified by <code>plugin</code> and <code>json</code>.
	 */
	void registerFeatureUse(String plugin, Map<String, String> featureData);

	/**
	 * Registers usage of a given project identified by <code>productId</code> and <code>version</code>.
	 */
	void registerProductUse(String productId, String version);

	/**
	 * Registers usage of a given project identified by <code>productId</code>, <code>version</code> and
	 * <code>projectId</code>.
	 */
	void registerProductUse(String productId, String version, String projectId);

	/**
	 * Checks if the given {@link URL} is accessible. Takes into account privacy settings, TOU acceptance and requested
	 * host.
	 */
	boolean canOpenUrl(URL url);

	/**
	 * Checks if the VMware domains are accessible. Takes into account privacy settings, TOU acceptance and requested
	 * host.
	 */
	boolean canOpenVMwareUrls();

	/**
	 * Clears out any stored usage data.
	 */
	void clear();

	/**
	 * Returns the UAA privacy level.
	 * @see #FULL_DATA
	 * @see #LIMITED_DATA
	 * @see #NO_DATA
	 * @see #DECLINE_TOU
	 * @see #UNDECIDED_TOU
	 */
	int getPrivacyLevel();

	/**
	 * Returns the usage data from the given encoded user-agent header.
	 */
	String getUsageDataFromUserAgentHeader(String userAgentHeader);

	/**
	 * Returns the encoded user-agent header.
	 */
	String getUserAgentHeader();

	/**
	 * Sets the privacy level.
	 * @see #FULL_DATA
	 * @see #LIMITED_DATA
	 * @see #NO_DATA
	 * @see #DECLINE_TOU
	 * @see #UNDECIDED_TOU
	 */
	void setPrivacyLevel(int level);

}
