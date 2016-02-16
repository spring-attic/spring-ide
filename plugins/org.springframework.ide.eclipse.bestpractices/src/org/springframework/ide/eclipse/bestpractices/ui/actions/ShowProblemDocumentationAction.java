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
package org.springframework.ide.eclipse.bestpractices.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.internal.bestpractices.springiderules.AvoidDriverManagerDataSource;
import org.springframework.ide.eclipse.internal.bestpractices.springiderules.ImportElementsAtTopRule;
import org.springframework.ide.eclipse.internal.bestpractices.springiderules.ParentBeanSpecifiesAbstractClassRule;
import org.springframework.ide.eclipse.internal.bestpractices.springiderules.RefElementRule;
import org.springframework.ide.eclipse.internal.bestpractices.springiderules.TooManyBeansInFileRule;
import org.springframework.ide.eclipse.internal.bestpractices.springiderules.UnnecessaryValueElementRule;
import org.springframework.ide.eclipse.internal.bestpractices.springiderules.UseBeanInheritance;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.UiUtil;


/**
 * @author Wesley Coelho
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class ShowProblemDocumentationAction implements IObjectActionDelegate {

	private static Map<String, String> errorIdToUrlMap = new HashMap<String, String>();

	private static final String ERROR_ID_KEY = "errorId";

	private static final String DEFAULT_URL = "http://www.springframework.org/documentation";

	static {
		errorIdToUrlMap.put("NO_CONSTRUCTOR",
				"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-constructor-injection");
		errorIdToUrlMap.put("CLASS_NOT_FOUND",
				"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-factory-class");
		errorIdToUrlMap.put("UNDEFINED_REFERENCED_BEAN",
				"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-dependencies");
		errorIdToUrlMap
				.put("UNDEFINED_INIT_METHOD",
						"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-factory-lifecycle-initializingbean");
		errorIdToUrlMap
				.put("UNDEFINED_DESTROY_METHOD",
						"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-factory-lifecycle-disposablebean");
		errorIdToUrlMap
				.put("REQUIRED_PROPERTY_MISSING",
						"http://static.springframework.org/spring/docs/2.5.x/reference/metadata.html#metadata-annotations-required");
		errorIdToUrlMap.put("NO_SETTER",
				"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-setter-injection");

		errorIdToUrlMap
				.put("legacyxmlusage:org.springframework.jndi.JndiObjectFactoryBean",
						"http://static.springframework.org/spring/docs/2.5.x/reference/xsd-config.html#xsd-config-body-schemas-jee");

		errorIdToUrlMap
				.put("legacyxmlusage:org.springframework.ejb.access.LocalStatelessSessionProxyFactoryBean",
						"http://static.springframework.org/spring/docs/2.5.x/reference/xsd-config.html#xsd-config-body-schemas-jee-local-slsb");

		errorIdToUrlMap
				.put("legacyxmlusage:org.springframework.ejb.access.SimpleRemoteStatelessSessionProxyFactoryBean",
						"http://static.springframework.org/spring/docs/2.5.x/reference/xsd-config.html#xsd-config-body-schemas-jee-remote-slsb");

		errorIdToUrlMap
				.put("legacyxmlusage:org.springframework.beans.factory.config.ListFactoryBean",
						"http://static.springframework.org/spring/docs/2.5.x/reference/xsd-config.html#xsd-config-body-schemas-util-list");

		errorIdToUrlMap
				.put("legacyxmlusage:org.springframework.beans.factory.config.SetFactoryBean",
						"http://static.springframework.org/spring/docs/2.5.x/reference/xsd-config.html#xsd-config-body-schemas-util-set");

		errorIdToUrlMap
				.put("legacyxmlusage:org.springframework.beans.factory.config.MapFactoryBean",
						"http://static.springframework.org/spring/docs/2.5.x/reference/xsd-config.html#xsd-config-body-schemas-util-map");

		errorIdToUrlMap
				.put("legacyxmlusage:org.springframework.beans.factory.config.FieldRetrievingFactoryBean",
						"http://static.springframework.org/spring/docs/2.5.x/reference/xsd-config.html#xsd-config-body-schemas-util-frfb");

		errorIdToUrlMap.put("legacyxmlusage:org.springframework.transaction.interceptor.TransactionInterceptor",
				"http://static.springframework.org/spring/docs/2.5.x/reference/transaction.html#tx-decl-explained");

		errorIdToUrlMap.put(
				"legacyxmlusage:org.springframework.transaction.annotation.AnnotationTransactionAttributeSource",
				"http://static.springframework.org/spring/docs/2.5.x/reference/transaction.html");

		errorIdToUrlMap
				.put("legacyxmlusage:org.springframework.transaction.jta.JtaTransactionManager",
						"http://static.springframework.org/spring/docs/2.5.x/reference/transaction.html#transaction-strategies");

		errorIdToUrlMap
				.put(
						"legacyxmlusage:org.springframework.transaction.jta.OC4JJtaTransactionManager",
						"http://static.springframework.org/spring/docs/2.5.x/reference/transaction.html#transaction-application-server-integration-oc4j");

		errorIdToUrlMap
				.put(
						"legacyxmlusage:org.springframework.transaction.jta.WebLogicJtaTransactionManager",
						"http://static.springframework.org/spring/docs/2.5.x/reference/transaction.html#transaction-application-server-integration-weblogic");

		errorIdToUrlMap
				.put(
						"legacyxmlusage:org.springframework.transaction.jta.WebSphereUowTransactionManager",
						"http://static.springframework.org/spring/docs/2.5.x/reference/transaction.html#transaction-application-server-integration-websphere");

		errorIdToUrlMap
				.put("legacyxmlusage:org.springframework.beans.factory.config.PropertyPlaceholderConfigurer",
						"http://static.springframework.org/spring/docs/2.5.x/reference/xsd-config.html#xsd-config-body-schemas-context-pphc");

		errorIdToUrlMap
				.put(
						AvoidDriverManagerDataSource.ERROR_ID,
						"http://static.springframework.org/spring/docs/2.5.x/api/org/springframework/jdbc/datasource/DriverManagerDataSource.html");
		errorIdToUrlMap.put(ImportElementsAtTopRule.ERROR_ID,
				"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-factory-xml-import");
		errorIdToUrlMap
				.put(ParentBeanSpecifiesAbstractClassRule.ERROR_ID,
						"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-child-bean-definitions");
		errorIdToUrlMap.put(RefElementRule.ERROR_ID,
				"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-ref-element");
		errorIdToUrlMap.put(TooManyBeansInFileRule.ERROR_ID,
				"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-factory-xml-import");
		errorIdToUrlMap.put(UnnecessaryValueElementRule.ERROR_ID,
				"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-value-element");
		errorIdToUrlMap
				.put(UseBeanInheritance.ERROR_ID,
						"http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-child-bean-definitions");

	}

	private String targetUrl = DEFAULT_URL;

	private IMarker getMarker(ISelection selection) {
		if (selection instanceof IMarker) {
			return (IMarker) selection;
		}
		else if (selection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) selection;
			Object firstElement = structuredSelection.getFirstElement();
			if (firstElement != null && firstElement instanceof IMarker) {
				return (IMarker) firstElement;
			}
		}

		return null;
	}

	public void run(IAction action) {
		UiUtil.openUrl(targetUrl);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		targetUrl = DEFAULT_URL;
		IMarker marker = getMarker(selection);
		if (marker != null) {
			try {
				String errorId = (String) marker.getAttribute(ERROR_ID_KEY);
				String url = errorIdToUrlMap.get(errorId);
				if (url != null) {
					targetUrl = url;
				}
			}
			catch (CoreException e) {
				StatusHandler.log(e.getStatus());
			}
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// Nothing to do
	}
}
