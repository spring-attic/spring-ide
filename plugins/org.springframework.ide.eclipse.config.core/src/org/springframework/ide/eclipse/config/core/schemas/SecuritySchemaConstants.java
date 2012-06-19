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
package org.springframework.ide.eclipse.config.core.schemas;

/**
 * Security schema derived from
 * <code>http://www.springframework.org/schema/security/spring-security-2.0.4.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.0.0
 * @version Spring Security 2.0.4
 */
public class SecuritySchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/security"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_ANONYMOUS = "anonymous"; //$NON-NLS-1$

	public static String ELEM_ANY_USER_SERVICE = "any-user-service"; //$NON-NLS-1$

	public static String ELEM_AUTHENTICATION_MANAGER = "authentication-manager"; //$NON-NLS-1$

	public static String ELEM_AUTHENTICATION_PROVIDER = "authentication-provider"; //$NON-NLS-1$

	public static String ELEM_CONCURRENT_SESSION_CONTROL = "concurrent-session-control"; //$NON-NLS-1$

	public static String ELEM_CUSTOM_AFTER_INVOCATION_PROVIDER = "custom-after-invocation-provider"; //$NON-NLS-1$

	public static String ELEM_CUSTOM_AUTHENTICATION_PROVIDER = "custom-authentication-provider"; //$NON-NLS-1$

	public static String ELEM_CUSTOM_FILTER = "custom-filter"; //$NON-NLS-1$

	public static String ELEM_FILTER_CHAIN = "filter-chain"; //$NON-NLS-1$

	public static String ELEM_FILTER_CHAIN_MAP = "filter-chain-map"; //$NON-NLS-1$

	public static String ELEM_FILTER_INVOCATION_DEFINITION_SOURCE = "filter-invocation-definition-source"; //$NON-NLS-1$

	public static String ELEM_FORM_LOGIN = "form-login"; //$NON-NLS-1$

	public static String ELEM_GLOBAL_METHOD_SECURITY = "global-method-security"; //$NON-NLS-1$

	public static String ELEM_HTTP = "http"; //$NON-NLS-1$

	public static String ELEM_HTTP_BASIC = "http-basic"; //$NON-NLS-1$

	public static String ELEM_INTERCEPT_METHODS = "intercept-methods"; //$NON-NLS-1$

	public static String ELEM_INTERCEPT_URL = "intercept-url"; //$NON-NLS-1$

	public static String ELEM_JDBC_USER_SERVICE = "jdbc-user-service"; //$NON-NLS-1$

	public static String ELEM_LDAP_AUTHENICATION_PROVIDER = "ldap-authentication-provider"; //$NON-NLS-1$

	public static String ELEM_LDAP_SERVER = "ldap-server"; //$NON-NLS-1$

	public static String ELEM_LDAP_USER_SERVICE = "ldap-user-service"; //$NON-NLS-1$

	public static String ELEM_LOGOUT = "logout"; //$NON-NLS-1$

	public static String ELEM_OPENID_LOGIN = "openid-login"; //$NON-NLS-1$

	public static String ELEM_PASSWORD_COMPARE = "password-compare"; //$NON-NLS-1$

	public static String ELEM_PASSWORD_ENCODER = "password-encoder"; //$NON-NLS-1$

	public static String ELEM_PROTECT = "protect"; //$NON-NLS-1$

	public static String ELEM_PROTECT_POINTCUT = "protect-pointcut"; //$NON-NLS-1$

	public static String ELEM_PORT_MAPPING = "port-mapping"; //$NON-NLS-1$

	public static String ELEM_PORT_MAPPINGS = "port-mappings"; //$NON-NLS-1$

	public static String ELEM_REMEMBER_ME = "remember-me"; //$NON-NLS-1$

	public static String ELEM_SALT_SOURCE = "salt-source"; //$NON-NLS-1$

	public static String ELEM_USER = "user"; //$NON-NLS-1$

	public static String ELEM_USER_SERVICE = "user-service"; //$NON-NLS-1$

	public static String ELEM_X509 = "x509"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ACCESS = "access"; //$NON-NLS-1$

	public static String ATTR_ACCESS_DECISION_MANAGER_REF = "access-decision-manager-ref"; //$NON-NLS-1$

	public static String ATTR_ACCESS_DENIED_PAGE = "access-denied-page"; //$NON-NLS-1$

	public static String ATTR_AFTER = "after"; //$NON-NLS-1$

	public static String ATTR_ALIAS = "alias"; //$NON-NLS-1$

	public static String ATTR_ALWAYS_USE_DEFAULT_TARGET = "always-use-default-target"; //$NON-NLS-1$

	public static String ATTR_AUTHENTICATION_FAILURE_URL = "authentication-failure-url"; //$NON-NLS-1$

	public static String ATTR_AUTHORITIES = "authorities"; //$NON-NLS-1$

	public static String ATTR_AUTHORITIES_BY_USERNAME_QUERY = "authorities-by-username-query"; //$NON-NLS-1$

	public static String ATTR_AUTO_CONFIG = "auto-config"; //$NON-NLS-1$

	public static String ATTR_BASE64 = "base64"; //$NON-NLS-1$

	public static String ATTR_BEFORE = "before"; //$NON-NLS-1$

	public static String ATTR_CACHE_REF = "cache-ref"; //$NON-NLS-1$

	public static String ATTR_CREATE_SESSION = "create-session"; //$NON-NLS-1$

	public static String ATTR_DATA_SOURCE_REF = "data-source-ref"; //$NON-NLS-1$

	public static String ATTR_DEFAULT_TARGET_URL = "default-target-url"; //$NON-NLS-1$

	public static String ATTR_DISABLED = "disabled"; //$NON-NLS-1$

	public static String ATTR_ENTRY_POINT_REF = "entry-point-ref"; //$NON-NLS-1$

	public static String ATTR_EXCEPTION_IF_MAXIMUM_EXCEEDED = "exception-if-maximum-exceeded"; //$NON-NLS-1$

	public static String ATTR_EXPIRED_URL = "expired-url"; //$NON-NLS-1$

	public static String ATTR_EXPRESSION = "expression"; //$NON-NLS-1$

	public static String ATTR_FILTERS = "filters"; //$NON-NLS-1$

	public static String ATTR_GRANTED_AUTHORITY = "granted-authority"; //$NON-NLS-1$

	public static String ATTR_GROUP_AUTHORIES_BY_USERNAME_QUERY = "group-authorities-by-username-query"; //$NON-NLS-1$

	public static String ATTR_GROUP_ROLE_ATTRIBUTE = "group-role-attribute"; //$NON-NLS-1$

	public static String ATTR_GROUP_SEARCH_BASE = "group-search-base"; //$NON-NLS-1$

	public static String ATTR_GROUP_SEARCH_FILTER = "group-search-filter"; //$NON-NLS-1$

	public static String ATTR_HASH = "hash"; //$NON-NLS-1$

	public static String ATTR_HTTP = "http"; //$NON-NLS-1$

	public static String ATTR_HTTPS = "https"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_INVALIDATE_SESSION = "invalidate-session"; //$NON-NLS-1$

	public static String ATTR_JSR250_ANNOTATIONS = "jsr250-annotations"; //$NON-NLS-1$

	public static String ATTR_KEY = "key"; //$NON-NLS-1$

	public static String ATTR_LDIF = "ldif"; //$NON-NLS-1$

	public static String ATTR_LOCKED = "locked"; //$NON-NLS-1$

	public static String ATTR_LOGIN_PAGE = "login-page"; //$NON-NLS-1$

	public static String ATTR_LOGIN_PROCESSING_URL = "login-processing-url"; //$NON-NLS-1$

	public static String ATTR_LOGOUT_SUCCESS_URL = "logout-success-url"; //$NON-NLS-1$

	public static String ATTR_LOGOUT_URL = "logout-url"; //$NON-NLS-1$

	public static String ATTR_LOWERCASE_COMPARISONS = "lowercase-comparisons"; //$NON-NLS-1$

	public static String ATTR_MANAGER_DN = "manager-dn"; //$NON-NLS-1$

	public static String ATTR_MANAGER_PASSWORD = "manager-password"; //$NON-NLS-1$

	public static String ATTR_MAX_SESSIONS = "max-sessions"; //$NON-NLS-1$

	public static String ATTR_METHOD = "method"; //$NON-NLS-1$

	public static String ATTR_NAME = "name"; //$NON-NLS-1$

	public static String ATTR_ONCE_PER_REQUEST = "once-per-request"; //$NON-NLS-1$

	public static String ATTR_PASSWORD = "password"; //$NON-NLS-1$

	public static String ATTR_PASSWORD_ATTRIBUTE = "password-attribute"; //$NON-NLS-1$

	public static String ATTR_PATH_TYPE = "path-type"; //$NON-NLS-1$

	public static String ATTR_PATTERN = "pattern"; //$NON-NLS-1$

	public static String ATTR_PORT = "port"; //$NON-NLS-1$

	public static String ATTR_POSITION = "position"; //$NON-NLS-1$

	public static String ATTR_PROPERTIES = "properties"; //$NON-NLS-1$

	public static String ATTR_REALM = "realm"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_REQUIRES_CHANNEL = "requires-channel"; //$NON-NLS-1$

	public static String ATTR_ROLE_PREFIX = "role-prefix"; //$NON-NLS-1$

	public static String ATTR_ROOT = "root"; //$NON-NLS-1$

	public static String ATTR_SECURED_ANNOTATIONS = "secured-annotations"; //$NON-NLS-1$

	public static String ATTR_SERVER_REF = "server-ref"; //$NON-NLS-1$

	public static String ATTR_SERVICES_REF = "services-ref"; //$NON-NLS-1$

	public static String ATTR_SERVLET_API_PROVISION = "servlet-api-provision"; //$NON-NLS-1$

	public static String ATTR_SESSION_CONTROLLER_REF = "session-controller-ref"; //$NON-NLS-1$

	public static String ATTR_SESSION_FIXATION_PROTECTION = "session-fixation-protection"; //$NON-NLS-1$

	public static String ATTR_SESSION_REGISTRY_ALIAS = "session-registry-alias"; //$NON-NLS-1$

	public static String ATTR_SESSION_REGISTRY_REF = "session-registry-ref"; //$NON-NLS-1$

	public static String ATTR_SUBJECT_PRINCIPAL_REF = "subject-principal-ref"; //$NON-NLS-1$

	public static String ATTR_SYSTEM_WIDE = "system-wide"; //$NON-NLS-1$

	public static String ATTR_TOKEN_REPOSITORY_REF = "token-repository-ref"; //$NON-NLS-1$

	public static String ATTR_TOKEN_VALIDITY_SECONDS = "token-validity-seconds"; //$NON-NLS-1$

	public static String ATTR_URL = "url"; //$NON-NLS-1$

	public static String ATTR_USER_DETAILS_CLASS = "user-details-class"; //$NON-NLS-1$

	public static String ATTR_USER_DN_PATTERN = "user-dn-pattern"; //$NON-NLS-1$

	public static String ATTR_USER_PROPERTY = "user-property"; //$NON-NLS-1$

	public static String ATTR_USER_SEARCH_BASE = "user-search-base"; //$NON-NLS-1$

	public static String ATTR_USER_SEARCH_FILTER = "user-search-filter"; //$NON-NLS-1$

	public static String ATTR_USER_SERVICE_REF = "user-service-ref"; //$NON-NLS-1$

	public static String ATTR_USERS_BY_USERNAME_QUERY = "users-by-username-query"; //$NON-NLS-1$

	public static String ATTR_USERNAME = "username"; //$NON-NLS-1$

}
