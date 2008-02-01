/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;

/**
 * Unit test for {@link Introspector}.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class IntrospectorTest extends BeansCoreTestCase {
	
	/**
	 * Several tests dealing with intertype declared methods on the bean class 
	 */
	public void testDeclaredMethods() throws CoreException, IOException {
		IProject project = createPredefinedProject("aspectj"); 		
		IType foo = JdtUtils.getJavaType(project, "org.springframework.Foo");
		Set<IMethod> methods = Introspector.findAllWritableProperties(foo);
		assertTrue(!methods.isEmpty() && methods.toArray().length > 1);
		assertTrue(Introspector.hasWritableProperty(foo, "bar"));
		assertNotNull(Introspector.getWritableProperty(foo, "bar"));
		assertNotNull(Introspector.getReadableProperty(foo, "bar"));
		assertTrue(Introspector.hasWritableProperty(foo, "foochen"));
		methods = Introspector.findAllNoParameterMethods(foo, "getBar");
		assertTrue(!methods.isEmpty() && methods.toArray().length == 1);  
	}

	public void testDoesExtend() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation"); 		
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		assertTrue(Introspector.doesExtend(foo, "org.springframework.Base"));
		assertTrue(!Introspector.doesExtend(foo, "org.springframework.beans.factory.BeanFactory"));
	}

	public void testDoesImplement() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation"); 		
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		assertTrue(Introspector.doesImplement(foo, "org.springframework.FooInterface"));
		assertTrue(!Introspector.doesImplement(foo, "org.springframework.beans.factory.BeanFactory"));
		IType base = JdtUtils.getJavaType(project, "org.springframework.Base");
		assertTrue(Introspector.doesImplement(base, "org.springframework.FooInterface"));
	}

	public void testfindAllConstructor() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation"); 		
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		Set<IMethod> cons = Introspector.findAllConstructors(foo);
		assertTrue(!cons.isEmpty());
		assertTrue(cons.toArray().length == 3);
	}
	
	public void testFindAllMethodsWithFilter() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation"); 		
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		Set<IMethod> methods = Introspector.findAllMethods(foo, new IMethodFilter() {

			public boolean matches(IMethod method, String prefix) {
				return true;
			}});
		assertTrue(!methods.isEmpty());
		assertTrue(methods.toArray().length == 8);

		methods = Introspector.findAllMethods(foo, "set", new IMethodFilter() {
			
			public boolean matches(IMethod method, String prefix) {
				return (method.getElementName().startsWith(prefix));
			}});
		assertTrue(!methods.isEmpty());
		assertTrue(methods.toArray().length == 3);
	} 

	public void testFindAllMethods() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation"); 		
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		// only methods; no constructors
		Set<IMethod> methods = Introspector.findAllMethods(foo, "", -1, Public.DONT_CARE, Static.DONT_CARE);
		assertTrue(methods.toArray().length == 8);
		// only public setters (static does not matter)
		methods = Introspector.findAllMethods(foo, "set", 1, Public.YES, Static.DONT_CARE);
		assertTrue(methods.toArray().length == 3);
		// only public setters (non-static)
		methods = Introspector.findAllMethods(foo, "set", 1, Public.YES, Static.NO);
		assertTrue(methods.toArray().length == 2);
		// only protected methods setters (static does not matter)
		methods = Introspector.findAllMethods(foo, "", -1, Public.NO, Static.DONT_CARE);
		assertTrue(methods.toArray().length == 2);
		// only protected methods setters (static)
		methods = Introspector.findAllMethods(foo, "", -1, Public.NO, Static.YES);
		assertTrue(methods.toArray().length == 1);
	} 
	
	public void testGetAllImplementedInterfaces() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation"); 		
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		Set<IType> interfaces = Introspector.getAllImplenentedInterfaces(foo);
		assertTrue(interfaces.toArray().length == 2);
		IType base = JdtUtils.getJavaType(project, "org.springframework.Base");
		interfaces = Introspector.getAllImplenentedInterfaces(base);
		assertTrue(interfaces.toArray().length == 1);
	}

	public void testGetAllMethods() throws CoreException, IOException {
		IProject project = createPredefinedProject("validation"); 		
		IType foo = JdtUtils.getJavaType(project, "org.springframework.SubClass");
		Set<IMethod> methods = Introspector.getAllMethods(foo);
		assertTrue(methods.toArray().length == 8);
		IType base = JdtUtils.getJavaType(project, "org.springframework.Base");
		methods = Introspector.getAllMethods(base);
		assertTrue(methods.toArray().length == 4);
	}
	
	
	
}
