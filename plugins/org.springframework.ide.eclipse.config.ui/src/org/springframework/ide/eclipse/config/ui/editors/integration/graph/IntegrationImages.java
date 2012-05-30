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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.config.graph.BadgedImageDescriptor;
import org.springframework.ide.eclipse.config.graph.ScaledImageDescriptor;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;


/**
 * @author Leo Dos Santos
 */
public class IntegrationImages {

	private static final URL baseURL = ConfigUiPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	private static ImageRegistry imageRegistry;

	private static final double SCALE = 0.5;

	private static final String INT = "integration"; //$NON-NLS-1$

	private static final String INT16 = "integration16"; //$NON-NLS-1$

	// Element Icons

	public static final ImageDescriptor AGGREGATOR = create(INT, "aggregator.png"); //$NON-NLS-1$

	public static final ImageDescriptor AGGREGATOR_SMALL = scale(AGGREGATOR, SCALE);

	public static final ImageDescriptor BRIDGE = create(INT, "bridge.png"); //$NON-NLS-1$

	public static final ImageDescriptor BRIDGE_SMALL = scale(BRIDGE, SCALE);

	public static final ImageDescriptor CHAIN = create(INT, "chain.png"); //$NON-NLS-1$

	public static final ImageDescriptor CHAIN_SMALL = scale(CHAIN, SCALE);

	public static final ImageDescriptor CHANNEL = create(INT, "channel.png"); //$NON-NLS-1$

	public static final ImageDescriptor CHANNEL_GRAY = ImageDescriptor.createWithFlags(CHANNEL, SWT.IMAGE_GRAY);

	public static final ImageDescriptor CHANNEL_SMALL = scale(CHANNEL, SCALE);

	public static final ImageDescriptor CLAIM_CHECK = create(INT, "claim-check.png"); //$NON-NLS-1$

	public static final ImageDescriptor CLAIM_CHECK_SMALL = scale(CLAIM_CHECK, SCALE);

	public static final ImageDescriptor CONTENT_FILTER = create(INT, "content-filter.png"); //$NON-NLS-1$

	public static final ImageDescriptor CONTENT_FILTER_SMALL = scale(CONTENT_FILTER, SCALE);

	public static final ImageDescriptor CONTROL_BUS = create(INT, "control-bus.png"); //$NON-NLS-1$

	public static final ImageDescriptor CONTROL_BUS_SMALL = scale(CONTROL_BUS, SCALE);

	public static final ImageDescriptor DELAYER = create(INT, "delayer.png"); //$NON-NLS-1$

	public static final ImageDescriptor DELAYER_SMALL = scale(DELAYER, SCALE);

	public static final ImageDescriptor ENRICHER = create(INT, "enricher.png"); //$NON-NLS-1$

	public static final ImageDescriptor ENRICHER_SMALL = scale(ENRICHER, SCALE);

	public static final ImageDescriptor EXCEPTION_ROUTER = create(INT, "exception-router.png"); //$NON-NLS-1$

	public static final ImageDescriptor EXCEPTION_ROUTER_SMALL = scale(EXCEPTION_ROUTER, SCALE);

	public static final ImageDescriptor FILTER = create(INT, "filter.png"); //$NON-NLS-1$

	public static final ImageDescriptor FILTER_SMALL = scale(FILTER, SCALE);

	public static final ImageDescriptor INBOUND_ADAPTER = create(INT, "inbound-adapter.png"); //$NON-NLS-1$

	public static final ImageDescriptor INBOUND_ADAPTER_SMALL = scale(INBOUND_ADAPTER, SCALE);

	public static final ImageDescriptor INBOUND_GATEWAY = create(INT, "inbound-gateway.png"); //$NON-NLS-1$

	public static final ImageDescriptor INBOUND_GATEWAY_SMALL = scale(INBOUND_GATEWAY, SCALE);

	public static final ImageDescriptor OUTBOUND_ADAPTER = create(INT, "outbound-adapter.png"); //$NON-NLS-1$

	public static final ImageDescriptor OUTBOUND_ADAPTER_SMALL = scale(OUTBOUND_ADAPTER, SCALE);

	public static final ImageDescriptor OUTBOUND_GATEWAY = create(INT, "outbound-gateway.png"); //$NON-NLS-1$

	public static final ImageDescriptor OUTBOUND_GATEWAY_SMALL = scale(OUTBOUND_GATEWAY, SCALE);

	public static final ImageDescriptor PLACEHOLDER = create(INT, "placeholder.png"); //$NON-NLS-1$

	public static final ImageDescriptor PLACEHOLDER_SMALL = scale(PLACEHOLDER, SCALE);

	public static final ImageDescriptor PUBSUB_CHANNEL = create(INT, "pubsub-channel.png"); //$NON-NLS-1$

	public static final ImageDescriptor PUBSUB_CHANNEL_SMALL = scale(PUBSUB_CHANNEL, SCALE);

	public static final ImageDescriptor RECIPIENT_LIST = create(INT, "recipient-list.png"); //$NON-NLS-1$

	public static final ImageDescriptor RECIPIENT_LIST_SMALL = scale(RECIPIENT_LIST, SCALE);

	public static final ImageDescriptor RESEQUENCER = create(INT, "resequencer.png"); //$NON-NLS-1$

	public static final ImageDescriptor RESEQUENCER_SMALL = scale(RESEQUENCER, SCALE);

	public static final ImageDescriptor ROUTER = create(INT, "router.png"); //$NON-NLS-1$

	public static final ImageDescriptor ROUTER_SMALL = scale(ROUTER, SCALE);

	public static final ImageDescriptor SERVICE_ACTIVATOR = create(INT, "service-activator.png"); //$NON-NLS-1$

	public static final ImageDescriptor SERVICE_ACTIVATOR_SMALL = scale(SERVICE_ACTIVATOR, SCALE);

	public static final ImageDescriptor SPLITTER = create(INT, "splitter.png"); //$NON-NLS-1$

	public static final ImageDescriptor SPLITTER_SMALL = scale(SPLITTER, SCALE);

	public static final ImageDescriptor TRANSFORMER = create(INT, "transformer.png"); //$NON-NLS-1$

	public static final ImageDescriptor TRANSFORMER_SMALL = scale(TRANSFORMER, SCALE);

	// Namespace Badges

	public static final ImageDescriptor BADGE_SI = create(INT16, "spring-integration.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_AMQP = create(INT16, "spring-integration-amqp.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_EVENT = create(INT16, "spring-integration-event.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_FEED = create(INT16, "spring-integration-feed.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_FILE = create(INT16, "spring-integration-file.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_FTP = create(INT16, "spring-integration-ftp.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_GEMFIRE = create(INT16, "spring-integration-gemfire.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_GROOVY = create(INT16, "spring-integration-groovy.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_HTTP = create(INT16, "spring-integration-http.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_HTTPINVOKER = create(INT16, "spring-integration-httpinvoker.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_IP = create(INT16, "spring-integration-ip.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_JDBC = create(INT16, "spring-integration-jdbc.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_JMS = create(INT16, "spring-integration-jms.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_JMX = create(INT16, "spring-integration-jmx.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_MAIL = create(INT16, "spring-integration-mail.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_REDIS = create(INT16, "spring-integration-redis.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_RMI = create(INT16, "spring-integration-rmi.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_SCRIPTING = create(INT16, "spring-integration-scripting.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_SECURITY = create(INT16, "spring-integration-security.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_SFTP = create(INT16, "spring-integration-sftp.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_STREAM = create(INT16, "spring-integration-stream.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_TWITTER = create(INT16, "spring-integration-twitter.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_WS = create(INT16, "spring-integration-ws.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_XML = create(INT16, "spring-integration-xml.gif"); //$NON-NLS-1$

	public static final ImageDescriptor BADGE_SI_XMPP = create(INT16, "spring-integration-xmpp.gif"); //$NON-NLS-1$

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		}
		catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
		}
		return imageRegistry;
	}

	public static Image getImageWithBadge(ImageDescriptor descriptor, ImageDescriptor badge) {
		String key = "" + descriptor.hashCode(); //$NON-NLS-1$
		key += badge.hashCode();
		ImageDescriptor cache = getImageRegistry().getDescriptor(key);
		if (cache == null) {
			cache = new BadgedImageDescriptor(descriptor, badge, false, false);
			getImageRegistry().put(key, cache);
		}
		return CommonImages.getImage(cache);
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (baseURL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(baseURL, buffer.toString());
	}

	private static ImageDescriptor scale(ImageDescriptor descriptor, double scale) {
		String key = "" + descriptor.hashCode(); //$NON-NLS-1$
		key += new Double(scale).hashCode();
		ImageDescriptor cache = getImageRegistry().getDescriptor(key);
		if (cache == null) {
			cache = new ScaledImageDescriptor(descriptor, scale);
			getImageRegistry().put(key, cache);
		}
		return cache;
	}

}
