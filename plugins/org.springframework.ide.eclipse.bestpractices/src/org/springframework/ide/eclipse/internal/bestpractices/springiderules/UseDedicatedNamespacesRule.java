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
package org.springframework.ide.eclipse.internal.bestpractices.springiderules;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.bestpractices.BestPracticesPluginConstants;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * This rule checks for cases where legacy XML syntax is used when there is
 * dedicated namespace syntax available.
 * Legacy namespace usage is detected by looking for the fully qualified class
 * name in the XML
 * @author Wesley Coelho
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class UseDedicatedNamespacesRule implements IValidationRule<IBean, IBeansValidationContext> {

	public static final String RULE_ID = "legacyxmlusage";

	/**
	 * Map of fully qualified class names to the message indicating that there
	 * is a namespace that can be used instead of the bean definition specifying
	 * that class.
	 * 
	 */
	private static Map<String, String> legacyXmlUsageMap = new HashMap<String, String>();

	static {
		// JEE
		legacyXmlUsageMap.put("org.springframework.jndi.JndiObjectFactoryBean",
				"Consider using namespace syntax, e.g. <jee:jndi-lookup id=\"myId\" jndi-name=\"jdbc/MyDataSource\"/>");
		legacyXmlUsageMap
				.put(
						"org.springframework.ejb.access.LocalStatelessSessionProxyFactoryBean",
						"Consider using namespace syntax, e.g. <jee:local-slsb id=\"simpleSlsb\" jndi-name=\"ejb/RentalServiceBean\" business-interface=\"com.foo.service.RentalService\"/>");
		legacyXmlUsageMap
				.put(
						"org.springframework.ejb.access.SimpleRemoteStatelessSessionProxyFactoryBean",
						"<jee:remote-slsb id=\"complexRemoteEjb\" jndi-name=\"ejb/MyRemoteBean\" business-interface=\"com.foo.service.RentalService\" ...");

		// Util
		legacyXmlUsageMap.put("org.springframework.beans.factory.config.ListFactoryBean",
				"Consider using namespace syntax, e.g. <util:list id=\"myListId\"> <value>myValue1</value> ...");
		legacyXmlUsageMap.put("org.springframework.beans.factory.config.SetFactoryBean",
				"Consider using namespace syntax, e.g. <util:set id=\"mySetId\"> <value>myValue1</value> ...");
		legacyXmlUsageMap
				.put("org.springframework.beans.factory.config.MapFactoryBean",
						"Consider using namespace syntax, e.g. <util:map id=\"myMapId\"> <entry key=\"myKey\" value=\"myValue\"/> ...");
		legacyXmlUsageMap
				.put("org.springframework.beans.factory.config.FieldRetrievingFactoryBean",
						"Consider using namespace syntax, e.g. <util:constant static-field=\"com.mydomain.MyClass.MY_CONSTANT\"/>");

		// Transactions
		legacyXmlUsageMap
				.put("org.springframework.transaction.interceptor.TransactionInterceptor",
						"Consider using namespace syntax, e.g. <tx:advice id=\"txAdvice\" transaction-manager=\"txManager\" ...");
		legacyXmlUsageMap.put("org.springframework.transaction.annotation.AnnotationTransactionAttributeSource",
				"Consider using namespace syntax, e.g. <tx:annotation-driven transaction-manager=\"txManager\" ...");
		legacyXmlUsageMap.put("org.springframework.transaction.jta.JtaTransactionManager",
				"Consider using namespace syntax, e.g. <tx:jta-transaction-manager/>");
		legacyXmlUsageMap
				.put(
						"org.springframework.transaction.jta.OC4JJtaTransactionManager",
						"Consider using namespace syntax instead of referencing OC4JJtaTransactionManager directly, e.g. <tx:jta-transaction-manager/>");
		legacyXmlUsageMap
				.put(
						"org.springframework.transaction.jta.WebLogicJtaTransactionManager",
						"Consider using namespace syntax instead of referencing WebLogicJtaTransactionManager directly, e.g. <tx:jta-transaction-manager/>");
		legacyXmlUsageMap
				.put(
						"org.springframework.transaction.jta.WebSphereUowTransactionManager",
						"Consider using namespace syntax instead of referencing WebSphereUowTransactionManager directly, e.g. <tx:jta-transaction-manager/>");

		// Context
		legacyXmlUsageMap.put("org.springframework.beans.factory.config.PropertyPlaceholderConfigurer",
				"Consider using namespace syntax, e.g. <context:property-placeholder ...");

	}

	/**
	 * Returns <code>true</code> if this rule is able to validate the given
	 * {@link IModelElement} with the specified {@link IValidationContext}.
	 */
	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof IBean;
	}

	public void validate(IBean bean, IBeansValidationContext validationContext, IProgressMonitor progressMonitor) {
		String infoMessage = legacyXmlUsageMap.get(bean.getClassName());
		if (infoMessage != null) {
			try {
				if (bean.getElementResource() != null && bean.getElementResource().isAccessible()) {
					String path = bean.getElementResource().getLocation().toString();
					String beanDefinition = RuleUtil.getFileLines(path, bean.getElementStartLine(), bean
							.getElementEndLine());
					if (beanDefinition.indexOf(bean.getClassName()) > 0) {
						validationContext.info(bean, RULE_ID + ":" + bean.getClassName(), infoMessage);
					}
				}
			}
			catch (IOException e) {
				StatusHandler.log(new Status(Status.ERROR, BestPracticesPluginConstants.PLUGIN_ID,
						"Could not read bean XML", e));
			}
		}
	}
}
