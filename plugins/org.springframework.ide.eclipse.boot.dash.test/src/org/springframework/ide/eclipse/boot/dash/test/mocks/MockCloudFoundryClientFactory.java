///*******************************************************************************
// * Copyright (c) 2016 Pivotal, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     Pivotal, Inc. - initial API and implementation
// *******************************************************************************/
//package org.springframework.ide.eclipse.boot.dash.test.mocks;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import org.cloudfoundry.client.lib.ApplicationLogListener;
//import org.cloudfoundry.client.lib.ClientHttpResponseCallback;
//import org.cloudfoundry.client.lib.CloudCredentials;
//import org.cloudfoundry.client.lib.CloudFoundryOperations;
//import org.cloudfoundry.client.lib.RestLogCallback;
//import org.cloudfoundry.client.lib.StartingInfo;
//import org.cloudfoundry.client.lib.StreamingLogToken;
//import org.cloudfoundry.client.lib.UploadStatusCallback;
//import org.cloudfoundry.client.lib.archive.ApplicationArchive;
//import org.cloudfoundry.client.lib.domain.ApplicationLog;
//import org.cloudfoundry.client.lib.domain.ApplicationStats;
//import org.cloudfoundry.client.lib.domain.CloudApplication;
//import org.cloudfoundry.client.lib.domain.CloudApplication.DebugMode;
//import org.cloudfoundry.client.lib.domain.CloudDomain;
//import org.cloudfoundry.client.lib.domain.CloudEvent;
//import org.cloudfoundry.client.lib.domain.CloudInfo;
//import org.cloudfoundry.client.lib.domain.CloudOrganization;
//import org.cloudfoundry.client.lib.domain.CloudQuota;
//import org.cloudfoundry.client.lib.domain.CloudRoute;
//import org.cloudfoundry.client.lib.domain.CloudSecurityGroup;
//import org.cloudfoundry.client.lib.domain.CloudService;
//import org.cloudfoundry.client.lib.domain.CloudServiceBroker;
//import org.cloudfoundry.client.lib.domain.CloudServiceInstance;
//import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
//import org.cloudfoundry.client.lib.domain.CloudSpace;
//import org.cloudfoundry.client.lib.domain.CloudStack;
//import org.cloudfoundry.client.lib.domain.CloudUser;
//import org.cloudfoundry.client.lib.domain.CrashesInfo;
//import org.cloudfoundry.client.lib.domain.InstancesInfo;
//import org.cloudfoundry.client.lib.domain.Staging;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
//import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
//import org.springframework.security.oauth2.common.OAuth2AccessToken;
//import org.springframework.web.client.ResponseErrorHandler;
//
//import com.google.common.collect.ImmutableList;
//
//import junit.framework.AssertionFailedError;
//
//public class MockCloudFoundryClientFactory extends CloudFoundryClientFactory {
//
//	@Override
//	public CloudFoundryOperations getClient(CloudCredentials credentials, URL apiUrl, String orgName, String spaceName,
//			boolean isSelfsigned) throws Exception {
//		return client;
//	}
//
//	private CloudOrganization org = new CloudSpace(meta, name, organization);
//
//	private List<CloudSpace> spaces = new ArrayList<>();
//
//	public String createSpace(String name) {
//		spaces.add(new CloudSpa)
//	}
//
//	/**
//	 * The mock client this factory returns. Exposed as a public final to allow
//	 * tests to 'program' the mock or verify interactions with mockito.
//	 */
//	public final CloudFoundryOperations client = new CloudFoundryOperations() {
//
//
//		@Override
//		public void addDomain(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void addRoute(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void associateAuditorWithSpace(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void associateAuditorWithSpace(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void associateAuditorWithSpace(String arg0, String arg1, String arg2) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void associateDeveloperWithSpace(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void associateDeveloperWithSpace(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void associateDeveloperWithSpace(String arg0, String arg1, String arg2) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void associateManagerWithSpace(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void associateManagerWithSpace(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void associateManagerWithSpace(String arg0, String arg1, String arg2) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void bindRunningSecurityGroup(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void bindSecurityGroup(String arg0, String arg1, String arg2) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void bindService(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void bindStagingSecurityGroup(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void createApplication(String arg0, Staging arg1, Integer arg2, List<String> arg3, List<String> arg4) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void createApplication(String arg0, Staging arg1, Integer arg2, Integer arg3, List<String> arg4,
//				List<String> arg5) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void createQuota(CloudQuota arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void createSecurityGroup(CloudSecurityGroup arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void createSecurityGroup(String arg0, InputStream arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void createService(CloudService arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void createServiceBroker(CloudServiceBroker arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void createSpace(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void createUserProvidedService(CloudService arg0, Map<String, Object> arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void createUserProvidedService(CloudService arg0, Map<String, Object> arg1, String arg2) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void debugApplication(String arg0, DebugMode arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void deleteAllApplications() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void deleteAllServices() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void deleteApplication(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void deleteDomain(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudRoute> deleteOrphanedRoutes() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void deleteQuota(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void deleteRoute(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void deleteSecurityGroup(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void deleteService(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void deleteServiceBroker(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void deleteSpace(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CloudApplication getApplication(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CloudApplication getApplication(UUID arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public Map<String, Object> getApplicationEnvironment(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public Map<String, Object> getApplicationEnvironment(UUID arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudEvent> getApplicationEvents(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public InstancesInfo getApplicationInstances(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public InstancesInfo getApplicationInstances(CloudApplication arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public Map<CloudApplication, ApplicationStats> getApplicationStats(List<CloudApplication> arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public ApplicationStats getApplicationStats(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public ApplicationStats getApplicationStats(CloudApplication arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudApplication> getApplications() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudApplication> getApplicationsWithBasicInfo() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public URL getCloudControllerUrl() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CloudInfo getCloudInfo() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public Map<String, String> getCrashLogs(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CrashesInfo getCrashes(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CloudDomain getDefaultDomain() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudDomain> getDomains() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudDomain> getDomainsForOrg() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudEvent> getEvents() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public String getFile(String arg0, int arg1, String arg2) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public String getFile(String arg0, int arg1, String arg2, int arg3) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public String getFile(String arg0, int arg1, String arg2, int arg3, int arg4) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public String getFileTail(String arg0, int arg1, String arg2, int arg3) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public Map<String, String> getLogs(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CloudOrganization getOrgByName(String arg0, boolean arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public Map<String, CloudUser> getOrganizationUsers(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudOrganization> getOrganizations() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudDomain> getPrivateDomains() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CloudQuota getQuotaByName(String arg0, boolean arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudQuota> getQuotas() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<ApplicationLog> getRecentLogs(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudRoute> getRoutes(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudSecurityGroup> getRunningSecurityGroups() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CloudSecurityGroup getSecurityGroup(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudSecurityGroup> getSecurityGroups() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CloudService getService(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CloudServiceBroker getServiceBroker(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudServiceBroker> getServiceBrokers() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public CloudServiceInstance getServiceInstance(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudServiceOffering> getServiceOfferings() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public List<CloudService> getServices() {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public List<CloudDomain> getSharedDomains() {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public CloudSpace getSpace(String arg0) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public List<UUID> getSpaceAuditors(String arg0) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public List<UUID> getSpaceAuditors(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public List<UUID> getSpaceDevelopers(String arg0) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public List<UUID> getSpaceDevelopers(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public List<UUID> getSpaceManagers(String arg0) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public List<UUID> getSpaceManagers(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public List<CloudSpace> getSpaces() {
//			return ImmutableList.copyOf(spaces);
//		}
//
//		@Override
//		public List<CloudSpace> getSpacesBoundToSecurityGroup(String arg0) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public CloudStack getStack(String arg0) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public List<CloudStack> getStacks() {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public String getStagingLogs(StartingInfo arg0, int arg1) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public List<CloudSecurityGroup> getStagingSecurityGroups() {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public OAuth2AccessToken login() {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public void logout() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void openFile(String arg0, int arg1, String arg2, ClientHttpResponseCallback arg3) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void register(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public void registerRestLogListener(RestLogCallback arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void removeDomain(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void rename(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public StartingInfo restartApplication(String arg0) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public void setQuotaToOrg(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void setResponseErrorHandler(ResponseErrorHandler arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public StartingInfo startApplication(String arg0) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public void stopApplication(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public StreamingLogToken streamLogs(String arg0, ApplicationLogListener arg1) {
//			throw new Error("Stub not implemented");
//		}
//
//		@Override
//		public void unRegisterRestLogListener(RestLogCallback arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void unbindRunningSecurityGroup(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void unbindSecurityGroup(String arg0, String arg1, String arg2) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void unbindService(String arg0, String arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void unbindStagingSecurityGroup(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void unregister() {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateApplicationDiskQuota(String arg0, int arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateApplicationEnv(String arg0, Map<String, String> arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateApplicationEnv(String arg0, List<String> arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateApplicationInstances(String arg0, int arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateApplicationMemory(String arg0, int arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateApplicationServices(String arg0, List<String> arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateApplicationStaging(String arg0, Staging arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateApplicationUris(String arg0, List<String> arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updatePassword(String arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updatePassword(CloudCredentials arg0, String arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateQuota(CloudQuota arg0, String arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateSecurityGroup(CloudSecurityGroup arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateSecurityGroup(String arg0, InputStream arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateServiceBroker(CloudServiceBroker arg0) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void updateServicePlanVisibilityForBroker(String arg0, boolean arg1) {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void uploadApplication(String arg0, String arg1) throws IOException {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void uploadApplication(String arg0, File arg1) throws IOException {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void uploadApplication(String arg0, ApplicationArchive arg1) throws IOException {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void uploadApplication(String arg0, File arg1, UploadStatusCallback arg2) throws IOException {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void uploadApplication(String arg0, String arg1, InputStream arg2) throws IOException {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void uploadApplication(String arg0, ApplicationArchive arg1, UploadStatusCallback arg2)
//				throws IOException {
//			throw new Error("Stub not implemented");
//
//		}
//
//		@Override
//		public void uploadApplication(String arg0, String arg1, InputStream arg2, UploadStatusCallback arg3)
//				throws IOException {
//			throw new Error("Stub not implemented");
//
//		}
//
//	};
//}
