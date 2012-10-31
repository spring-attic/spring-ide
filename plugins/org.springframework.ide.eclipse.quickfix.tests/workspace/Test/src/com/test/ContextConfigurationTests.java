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
package com.test;

import org.springframework.test.context.ContextConfiguration;

/**
 * @author Kaitlin Duck Sherwood
 */

public class ContextConfigurationTests {
	
	@ContextConfiguration(locations=)
	class Test1 {
		
	}
	
	@ContextConfiguration(locations="" 
	class Test2 {
		
	}
	
	@ContextConfiguration(locations=" 
	class Test3 {
		
	}
	
	@ContextConfiguration(locations={} 
	class Test4 {
		
	}
	
	@ContextConfiguration(locations={"" 
	class Test5 {
		
	}
	
	@ContextConfiguration(locations={""} 
	class Test6 {
		
	}
	
	@ContextConfiguration(locations={"one.xml" 
	class Test7 {
		
	}
	
	@ContextConfiguration(locations={"one.xml"} 
	class Test8 {
		
	}
	
	@ContextConfiguration(locations={"one.xml", 
	class Test9 {
		
	}
	
	@ContextConfiguration(locations={"one.xml",} 
	class Test10 {
		
	}
	
	@ContextConfiguration(locations={"one.xml", "" 
	class Test11 {
		
	}
	
	@ContextConfiguration(locations={"one.xml", ""} 
	class Test12 {
		
	}
	
	@ContextConfiguration(locations={"one.xml", "two.xml" 
	class Test13 {
		
	}
	
	@ContextConfiguration(locations={"one.xml", "two.xml"} 
	class Test13b {
		
	}
	
	@ContextConfiguration(locations={"one.xml", "two.xml",} 
	class Test13c {
		
	}
	
	@ContextConfiguration(locations={"one.xml", "two.xml"," 
	class Test14 {
		
	}
	
	@ContextConfiguration(locations={"one.xml", "two.xml","" 
	class Test14b {
		
	}
	
	@ContextConfiguration(locations={"one.xml", "two.xml",""} 
	class Test15 {
		
	}
	
	@ContextConfiguration(locations={"one.xml", "two.xml","three.xml" 
	class Test16 {
		
	}
	
	@ContextConfiguration(locations={"one.xml", "two.xml","three.xml"} 
	class Test17 {
		
	}
	
	@ContextConfiguration(locations={asldfkjs; alsdkfjaslfj} 
	class Test18 {
		
	}
	
	@ContextConfiguration(locations={)} 
	class TestA {
		
	}
	
	@ContextConfiguration(locations={alskdfjaslfdj} 
	class Test19 {
		
	}
	
	@ContextConfiguration(locations={location={location={ 
	class TestC {
		
	}
	
	@ContextConfiguration(locations={"doesNotExist
			class TestB {
				
			}
			
	@ContextConfiguration(locations={" 
	class Test3b {
						
	}
	
	@ContextConfiguration(locations={
	class Test3c {
						
	}

	@ContextConfiguration(locations= {
	class Test3d {
						
	}

	@ContextConfiguration(locations=  {
	class Test3e {
						
	}

	@ContextConfiguration(locations=
	class Test3f {
						
	}

	@ContextConfiguration(locations
	class Test3f {
						
	}
	
	@ContextConfiguration(x=47, y=52, locations={ locations={
			class Test29 {			
			}
	}
	
	@ContextConfiguration(x=47, locations={"foo.xml"}, y=52
			class Test30 {			
			}
	}
	
	@ContextConfiguration(x={47,"aString"}, locations={"foo.xml"}, y=52
			class Test31 {		
		
	}
			}
	}
	
	@ContextConfiguration(locations={"foo.xml"  ,   "b"      }
			class Test32 {		
		
	}
	
	@ContextConfiguration(locations={"foo.xml"  ,   "b   "      }
	class Test33 {		

	}

	@ContextConfiguration(locations={ "doesNotExist
	class TestE {
	}

	@ContextConfiguration(locations={ "doesNotExist"
	class TestF {
	}
	@ContextConfiguration(locations={ "doesNotExist}
	class TestG {
	}

	@ContextConfiguration(locations={ "doesNotExist"}
	class TestH {
	}

	@ContextConfiguration(locations={ "doesNotExist" , "foo.xml"
	class TestI {
	}
	
	@ContextConfiguration(locations={ "doesNotExist" , "foo.xml"}
	class TestJ {
	}
	
	@ContextConfiguration(locations = { "add" , "foo.xml"})
	class TestK {
	}
	
	@ContextConfiguration(locations = { "import" })
	class TestL {
	}
	
	@ContextConfiguration(locations = { "att" , "foo.xml"}
	class TestM {
	}
	
	// testMultidirectoryFilenameCompletion
	@ContextConfiguration(locations = { "subdir"}
	class TestN {
	}
	
	// testValueCompletion
	@ContextConfiguration(value = { "subdir"}
	class TestO {
	}
	
	// testTypingStartOfClasspath
	@ContextConfiguration(value = { "src"}
	class TestP {
	}
	
	// testNonClasspathTypeFullPath
	@ContextConfiguration(value = { "non-classpath/arb"}
	class TestQ {
	}
	
	// testNonClasspathTypeFullPathAmbiguous
	@ContextConfiguration(value = { "non-classpath"}
	class TestR {
	}
	
	// testNonClasspathTypeBasename
	@ContextConfiguration(value = { "arbitrary"}
	class TestR {
	}
	
	// testFileColon
	@ContextConfiguration(value = { "file:"}
	class TestS {
	}
	
	// testClasspathColon
	@ContextConfiguration(value = { "classpath:"}
	class TestT {
	}
	
}
