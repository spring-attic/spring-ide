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
package org.springframework.ide.eclipse.roo.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.junit.Test;
import org.springframework.ide.eclipse.roo.ui.internal.StyledTextAppender;


/**
 * @author Martin Lippert
 * @author Leo Dos Santos
 */
public class StyledTextAppenderTest extends TestCase {

	@Test
	public void testCreatedFile() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Created SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx");
		assertTrue(matcher.matches());
		assertEquals("Created ", matcher.group(1));
		assertEquals("SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx", matcher.group(2));
		assertNull(matcher.group(5));
	}

	@Test
	public void testUpdatedFile() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Updated SRC_MAIN_JAVA/r1/web/ApplicationConversionServiceFactoryBean_Roo_ConversionService.aj");
		assertTrue(matcher.matches());
		assertEquals("Updated ", matcher.group(1));
		assertEquals("SRC_MAIN_JAVA/r1/web/ApplicationConversionServiceFactoryBean_Roo_ConversionService.aj", matcher.group(2));
		assertNull(matcher.group(5));
	}
	
	@Test
	public void testCreatedModule() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();
		
		Matcher matcher = pattern.matcher("Created module1|SRC_MAIN_JAVA/com/foo/MyEntity_Roo_JavaBean.aj");
		assertTrue(matcher.matches());
		assertEquals("Created ", matcher.group(1));
		assertEquals("module1|SRC_MAIN_JAVA/com/foo/MyEntity_Roo_JavaBean.aj", matcher.group(2));
		assertNull(matcher.group(5));
	}
	
	@Test
	public void testUpdatedModule() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();
		
		Matcher matcher = pattern.matcher("Updated module1|SRC_MAIN_RESOURCES/META-INF/persistence.xml");
		assertTrue(matcher.matches());
		assertEquals("Updated ", matcher.group(1));
		assertEquals("module1|SRC_MAIN_RESOURCES/META-INF/persistence.xml", matcher.group(2));
		assertNull(matcher.group(5));
	}
	
	@Test
	public void testSpringConfigRoot() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();
		
		Matcher matcher = pattern.matcher("Created module1|SPRING_CONFIG_ROOT/applicationContext.xml");
		assertTrue(matcher.matches());
		assertEquals("Created ", matcher.group(1));
		assertEquals("module1|SPRING_CONFIG_ROOT/applicationContext.xml", matcher.group(2));
		assertNull(matcher.group(5));
	}

	@Test
	public void testDeletedFile() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Managed SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx");
		assertTrue(matcher.matches());
		assertEquals("Managed ", matcher.group(1));
		assertEquals("SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx", matcher.group(2));
		assertNull(matcher.group(5));
	}

	@Test
	public void testUnknownAction() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Unknown SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx");
		assertTrue(matcher.matches());
		assertEquals("Unknown ", matcher.group(1));
		assertEquals("SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx", matcher.group(2));
		assertNull(matcher.group(5));
	}

	@Test
	public void testSomethingAfterTheFileName() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Unknown SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx this is the end");
		assertTrue(matcher.matches());
		assertEquals("Unknown ", matcher.group(1));
		assertEquals("SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx", matcher.group(2));
		assertEquals(" this is the end", matcher.group(5));
	}

	@Test
	public void testColonDirectlyAfterFileName() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Unknown SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx : this is the end");
		assertTrue(matcher.matches());
		assertEquals("Unknown ", matcher.group(1));
		assertEquals("SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx", matcher.group(2));
		assertEquals(" : this is the end", matcher.group(5));
	}

	@Test
	public void testSpaceAndColonDirectlyAfterFileName() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Unknown SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx : this is the end");
		assertTrue(matcher.matches());
		assertEquals("Unknown ", matcher.group(1));
		assertEquals("SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx", matcher.group(2));
		assertEquals(" : this is the end", matcher.group(5));
	}

	@Test
	public void testRootFileName() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Updated ROOT/pom.xml");
		assertTrue(matcher.matches());
		assertEquals("Updated ", matcher.group(1));
		assertEquals("ROOT/pom.xml", matcher.group(2));
		assertNull(matcher.group(5));
	}
	
	@Test
	public void testRootFileNameWithAddOn() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Updated ROOT/pom.xml [added dependencies org.hsqldb:hsqldb:1.8.0.10, org.hibernate:hibernate-core:3.6.4.Final, org.hibernate:hibernate-entitymanager:3.6.4.Final, org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.0.Final, org.hibernate:hibernate-validator:4.1.0.Final, javax.validation:validation-api:1.0.0.GA, cglib:cglib-nodep:2.2, javax.transaction:jta:1.1, org.springframework:spring-jdbc:${spring.version}, org.springframework:spring-orm:${spring.version}, commons-pool:commons-pool:1.5.4, commons-dbcp:commons-dbcp:1.3; added repository https://repository.jboss.org/nexus/content/repositories/releases]");
		assertTrue(matcher.matches());
		assertEquals("Updated ", matcher.group(1));
		assertEquals("ROOT/pom.xml", matcher.group(2));
		assertEquals(" [added dependencies org.hsqldb:hsqldb:1.8.0.10, org.hibernate:hibernate-core:3.6.4.Final, org.hibernate:hibernate-entitymanager:3.6.4.Final, org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.0.Final, org.hibernate:hibernate-validator:4.1.0.Final, javax.validation:validation-api:1.0.0.GA, cglib:cglib-nodep:2.2, javax.transaction:jta:1.1, org.springframework:spring-jdbc:${spring.version}, org.springframework:spring-orm:${spring.version}, commons-pool:commons-pool:1.5.4, commons-dbcp:commons-dbcp:1.3; added repository https://repository.jboss.org/nexus/content/repositories/releases]", matcher.group(5));
	}
	
	@Test
	public void testSRCFileNameWithAddOn() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Updated SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx [added dependencies org.hsqldb:hsqldb:1.8.0.10, org.hibernate:hibernate-core:3.6.4.Final, org.hibernate:hibernate-entitymanager:3.6.4.Final, org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.0.Final, org.hibernate:hibernate-validator:4.1.0.Final, javax.validation:validation-api:1.0.0.GA, cglib:cglib-nodep:2.2, javax.transaction:jta:1.1, org.springframework:spring-jdbc:${spring.version}, org.springframework:spring-orm:${spring.version}, commons-pool:commons-pool:1.5.4, commons-dbcp:commons-dbcp:1.3; added repository https://repository.jboss.org/nexus/content/repositories/releases]");
		assertTrue(matcher.matches());
		assertEquals("Updated ", matcher.group(1));
		assertEquals("SRC_MAIN_WEBAPP/WEB-INF/views/votes/list.jspx", matcher.group(2));
		assertEquals(" [added dependencies org.hsqldb:hsqldb:1.8.0.10, org.hibernate:hibernate-core:3.6.4.Final, org.hibernate:hibernate-entitymanager:3.6.4.Final, org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.0.Final, org.hibernate:hibernate-validator:4.1.0.Final, javax.validation:validation-api:1.0.0.GA, cglib:cglib-nodep:2.2, javax.transaction:jta:1.1, org.springframework:spring-jdbc:${spring.version}, org.springframework:spring-orm:${spring.version}, commons-pool:commons-pool:1.5.4, commons-dbcp:commons-dbcp:1.3; added repository https://repository.jboss.org/nexus/content/repositories/releases]", matcher.group(5));
	}

	@Test
	public void testNonMatchingFile() {
		Pattern pattern = new StyledTextAppender(null).getHyperlinkPattern();

		Matcher matcher = pattern.matcher("Created SRC_MAIN_WEBAPP/WEB-INF/views/public");
		assertFalse(matcher.matches());
	}

}
