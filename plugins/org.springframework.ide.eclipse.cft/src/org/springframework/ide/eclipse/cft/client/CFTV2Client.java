/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cft.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.doppler.LogMessage;
import org.cloudfoundry.doppler.MessageType;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.LogsRequest;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.ProxyConfiguration;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.eclipse.cft.server.core.internal.CloudErrorUtil;
import org.eclipse.cft.server.core.internal.CloudFoundryServer;
import org.eclipse.cft.server.core.internal.CloudServerUtil;
import org.eclipse.cft.server.core.internal.client.CFClient;
import org.eclipse.cft.server.core.internal.client.CFCloudCredentials;
import org.eclipse.cft.server.core.internal.log.AppLogUtil;
import org.eclipse.cft.server.core.internal.log.CFApplicationLogListener;
import org.eclipse.cft.server.core.internal.log.CFStreamingLogToken;
import org.eclipse.cft.server.core.internal.log.CloudLog;
import org.eclipse.cft.server.core.internal.log.LogContentType;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.cft.CFTConsole;
import org.springframework.ide.eclipse.cft.Log;

import reactor.core.Cancellation;
import reactor.core.publisher.Flux;

public class CFTV2Client implements CFClient {

	public static final String HTTP_KEEP_ALIVE_SYSTEM_PROPERTY = "http.keepAlive";
	private CFCloudCredentials credentials;
	private CloudFoundryServer cloudServer;
	private String orgName;
	private String spaceName;
	private CloudFoundryClient v2Client = null;
	private CloudFoundryOperations v2Operations = null;

	public CFTV2Client(CloudFoundryServer cloudServer, CFCloudCredentials credentials, String orgName,
			String spaceName) {

		Assert.isNotNull(cloudServer);
		Assert.isNotNull(credentials);
		Assert.isNotNull(orgName);
		Assert.isNotNull(spaceName);

		this.credentials = credentials;
		this.cloudServer = cloudServer;
		this.orgName = orgName;
		this.spaceName = spaceName;
	}

	@Override
	public String login() throws CoreException {
		// clear exist client
		this.v2Client = null;
		this.v2Operations = null;
		getV2Operations();
		return null;
	}

	@Override
	public CFStreamingLogToken streamLogs(String appName, CFApplicationLogListener listener) throws CoreException {
		return internalStreamLogs(appName, listener, false);
	}

	@Override
	public List<CloudLog> getRecentLogs(String appName) throws CoreException {
		CFTConsole.getDefault().printToConsole(appName, cloudServer, "Fetching recent logs for application: " + appName);

		CloudFoundryOperations operations = getV2Operations();
		return operations.applications().logs(LogsRequest.builder().name(appName).recent(true).build())
				.map((logMessage) -> {
					MessageType messageType = logMessage.getMessageType();
					LogContentType contentType = messageType == MessageType.ERR
							? LogContentType.APPLICATION_LOG_STS_ERROR
							: LogContentType.APPLICATION_LOG_STD_OUT;
					String formatted = format(logMessage.getMessage());
					if (formatted != null) {
						return new CloudLog(formatted, contentType);
					}
					return null;
				}).toStream().filter((cloudLog) -> cloudLog != null).collect(Collectors.toList());
	}

	private String format(String message) {
		return message != null ? message + '\n' : null;
	}

	private CFStreamingLogToken internalStreamLogs(String appName, CFApplicationLogListener listener,
			boolean recentLogs) throws CoreException {
		CFTConsole.getDefault().printToConsole(appName, cloudServer, "Streaming logs for application: 	" + appName);
		CloudFoundryOperations operations = getV2Operations();
		V2LogListener v2Listener = asV2LogListener(listener);
		Flux<LogMessage> stream = operations.applications()
				.logs(LogsRequest.builder().name(appName).recent(recentLogs).build());
		final Cancellation cancellation = stream.subscribe(v2Listener::onMessage, v2Listener::onError);
		return new CFStreamingLogToken() {

			@Override
			public void cancel() {
				cancellation.dispose();
			}
		};
	}

	protected String getHost() {
		URI uri = URI.create(cloudServer.getUrl());
		return uri.getHost();
	}

	protected boolean skipSsl() {
		// For v2, using self-signed == skip SSL. Manually trusting self signed
		// certificate
		// is interpreted as skipping actual certificate check.
		return cloudServer.isSelfSigned();
	}

	protected ProxyConfiguration getProxyConfiguration() {
		try {
			IProxyData proxyData = CloudServerUtil.getProxy(new URL(cloudServer.getUrl()));
			if (proxyData != null) {
				String proxyHost = proxyData.getHost();
				int proxyPort = proxyData.getPort();
				String user = proxyData.getUserId();
				String password = proxyData.getPassword();
				return ProxyConfiguration.builder().host(proxyHost)
						.port(proxyPort == -1 ? Optional.empty() : Optional.of(proxyPort))
						.username(Optional.ofNullable(user)).password(Optional.ofNullable(password)).build();
			}
		} catch (MalformedURLException e) {
			Log.log(e);
		}
		return null;
	}

	protected CloudFoundryOperations getV2Operations() throws CoreException {

		if (this.v2Operations == null) {
			try {

				boolean keepAlive = keepAlive();
				DefaultConnectionContext connection = DefaultConnectionContext.builder()
						.proxyConfiguration(Optional.ofNullable(getProxyConfiguration())).apiHost(getHost())
						// SSL handshake timeout may need to be increased, for
						// log streaming
						.sslHandshakeTimeout(Duration.ofSeconds(cloudServer.getSslHandshakeTimeout()))
						.keepAlive(keepAlive).skipSslValidation(skipSsl()).build();

				PasswordGrantTokenProvider tokenProvider = PasswordGrantTokenProvider.builder()
						.username(credentials.getUser()).password(credentials.getPassword()).build();

				ReactorUaaClient uaaClient = ReactorUaaClient.builder().connectionContext(connection)
						.tokenProvider(tokenProvider).build();

				ReactorDopplerClient dopplerClient = ReactorDopplerClient.builder().connectionContext(connection)
						.tokenProvider(tokenProvider).build();

				this.v2Client = ReactorCloudFoundryClient.builder().connectionContext(connection)
						.tokenProvider(tokenProvider).build();

				this.v2Operations = DefaultCloudFoundryOperations.builder().cloudFoundryClient(v2Client)
						.dopplerClient(dopplerClient).uaaClient(uaaClient).organization(orgName).space(spaceName)
						.build();

			} catch (Throwable e) {
				throw CloudErrorUtil.toCoreException(e);
			}
		}
		return this.v2Operations;
	}

	private boolean keepAlive() {
		return getBooleanSystemProp(HTTP_KEEP_ALIVE_SYSTEM_PROPERTY).isPresent();
	}

	private Optional<Boolean> getBooleanSystemProp(String name) {
		String str = System.getProperty(name);
		if (str != null) {
			return Optional.of(Boolean.valueOf(str));
		}
		return Optional.empty();
	}

	protected V2LogListener asV2LogListener(final CFApplicationLogListener listener) {
		return new V2LogListener() {

			@Override
			public void onMessage(LogMessage log) {

				CloudLog cloudLog = new CloudLog(log.getApplicationId(), AppLogUtil.format(log.getMessage()),
						new Date(log.getTimestamp()), LogContentType.APPLICATION_LOG_STD_OUT, log.getSourceInstance(),
						log.getSourceType());
				listener.onMessage(cloudLog);
			}

			@Override
			public void onError(Throwable exception) {
				listener.onError(exception);
			}

			@Override
			public void onComplete() {
				listener.onComplete();
			}
		};
	}

	public interface V2LogListener {

		void onMessage(LogMessage log);

		void onComplete();

		void onError(Throwable exception);

	}
}
