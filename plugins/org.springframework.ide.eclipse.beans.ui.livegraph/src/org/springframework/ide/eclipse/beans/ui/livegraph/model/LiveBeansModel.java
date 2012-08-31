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
package org.springframework.ide.eclipse.beans.ui.livegraph.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A mock pre-generated beans model
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansModel {

	private final List<LiveBean> beans;

	public LiveBeansModel() {
		beans = new ArrayList<LiveBean>();

		LiveBean twitterInbound = new LiveBean("twitterInbound");
		LiveBean searchAdapter = new LiveBean("searchAdapter");
		LiveBean twitterTemplate = new LiveBean("twitterTemplate");
		LiveBean anonCEFB = new LiveBean("<anonymous> ConsumerEndpointFactoryBean");
		LiveBean tfBean0 = new LiveBean("org.springframework.integration.config.TransformerFactoryBean#0");
		LiveBean tfBean1 = new LiveBean("org.springframework.integration.config.TransformerFactoryBean#1");
		LiveBean twitterOut = new LiveBean("int:channel twitterOut");
		LiveBean twitterOutAdapter = new LiveBean("twitterOut.adapter");

		twitterInbound.addChild(twitterTemplate);
		searchAdapter.addChild(twitterTemplate);
		anonCEFB.addChild(tfBean1);
		tfBean0.addChild(twitterOut);
		tfBean1.addChild(twitterOut);

		beans.add(twitterInbound);
		beans.add(searchAdapter);
		beans.add(twitterTemplate);
		beans.add(anonCEFB);
		beans.add(tfBean0);
		beans.add(tfBean1);
		beans.add(twitterOut);
		beans.add(twitterOutAdapter);
	}

	public List<LiveBean> getBeans() {
		return beans;
	}

}
