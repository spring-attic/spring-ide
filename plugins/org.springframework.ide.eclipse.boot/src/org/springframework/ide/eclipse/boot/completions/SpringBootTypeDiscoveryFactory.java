/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.completions;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeDiscovery;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeDiscoveryFactory;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

public class SpringBootTypeDiscoveryFactory implements ExternalTypeDiscoveryFactory {

	public SpringBootTypeDiscoveryFactory() {
	}

	/**
	 * TODO: Right now we only have a single instance that is used for any spring project. 
	 * However, it should really be an instance per SpringBoot version that is used by a project.
	 * <p>
	 * This doesn't make much sense yet because we don't have a way to create the suggestion data
	 * file based on a spring boot version.
	 */
	private SpringBootTypeDiscovery instance = null;
	
	@Override
	public synchronized ExternalTypeDiscovery discoveryFor(IJavaProject project) {
		if (isApplicable(project)) {
			try {
				if (instance==null) {
					instance = new SpringBootTypeDiscovery();
				}
				return instance;
			} catch (Exception e) {
				BootActivator.log(e);
			}
		}
		return null;
	}

	private boolean isApplicable(IJavaProject project) {
		//Maybe this should only apply to spring boot projects. But for now allow it to apply
		// to any spring project.
		return SpringCoreUtils.hasNature(project.getProject(), SpringCoreUtils.NATURE_ID);
	}

}
