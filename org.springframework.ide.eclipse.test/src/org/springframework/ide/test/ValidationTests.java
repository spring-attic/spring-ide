/*
* Copyright 2004 the original author or authors.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/ 
package org.springframework.ide.test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IPackageFragment;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtil;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectNature;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * this class tests the config file validator. The test
 * method is to create a config file with certain expected
 * errors, and check the validation creates the expected
 * error markers
 * @author Loren Rosen
 *
 */
public class ValidationTests extends AbstractSpringIdeTest {
	
	public ValidationTests(String name){
		super(name);
	}
		
	protected String xmlTestFileName = "sample.xml";
	
	protected static String SimpleBeanText =
		"public class SimpleBean {" +
	    "private String stuff;" +
		"public String getStuff() {return stuff;}" +
		"public void setStuff(String value) {this.stuff=value;}}";
	
	/**
	 * create the Java file(s) used by the tests
	 * @throws Exception
	 */
	private void createBeanClass() throws Exception {
		IPackageFragment pack = project.createPackage("pack1");
	    IType type = project.createType(pack, "SimpleBean.java",
	      SimpleBeanText);
	}

	private void renameBeanClassProperty() throws Exception {
	    IType type = BeansModelUtil.getJavaType(project.getProject(),
	    		"pack1.SimpleBean");
	    IMethod[] methods = type.getMethods();
	    for (int i = 0; i < methods.length; i++) {
			IMethod method = methods[i];
			if ("setStuff".equals(method.getElementName())) {
				method.rename("setStuf", false, null);
			}
		}
	}

	public void testValidationErrors() throws Exception {
		createBeanClass();
        IFile xmlFile = createXmlFile("sample.xml");
		
		IMarker[] markers = getFailureMarkers();
		assertEquals("Wrong number of validation errors (problem markers)", 4, markers.length);
		
		// test that we get an error for a bean class
		// that isn't in the class path
		IMarker marker = markers[0];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(6, marker.getAttribute(IMarker.LINE_NUMBER,0));
		assertEquals(
				"Class 'org.springframework.beans.factory.config.PropertyPlaceholderConfigurer' not found", 
				marker.getAttribute(IMarker.MESSAGE,""));
		 
		// test that we get an error for a bean class
		// that isn't in the class path -- this time
		// the class has a simple name (not qualifed with
		// some other package)
		marker = markers[1];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(10, marker.getAttribute(IMarker.LINE_NUMBER,0));
		assertEquals(
				"Class 'absent' not found",
				marker.getAttribute(IMarker.MESSAGE,""));
		
		// test that we get an error for a bean class
		// which tried to set an non-existent property
		marker = markers[2];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(16, marker.getAttribute(IMarker.LINE_NUMBER,0));
		assertEquals(
				"No setter for property 'absentProperty' found in class 'pack1.SimpleBean'",
				marker.getAttribute(IMarker.MESSAGE,""));

		// test that we get an error for a child bean
		// whose parent doesn't exist
		marker = markers[3];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(20, marker.getAttribute(IMarker.LINE_NUMBER,0));
		assertEquals(
				"Undefined parent root bean",
				marker.getAttribute(IMarker.MESSAGE,""));		

		renameBeanClassProperty();
		project.waitForAutoBuild();

		markers = getFailureMarkers();
		assertEquals("Wrong number of validation errors (problem markers)", 5, markers.length);
	}
	
	 public void testParseErrors() throws Exception {

        IFile xmlFile = createXmlFile("parseError.xml");
        
        IMarker[] markers = getFailureMarkers(xmlFile);
        
        assertEquals(1, markers.length);
        
        // test that we get an error for an XML
        // parsing problem
        IMarker marker = markers[0];
        assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
        assertEquals(6, marker.getAttribute(IMarker.LINE_NUMBER,0));
        assertEquals(
                        "Attribute \"undefinedAttribute\" must be declared for element type \"bean\".", 
                        marker.getAttribute(IMarker.MESSAGE,""));
}



	
    private  IFile createXmlFile(String name) throws CoreException, Exception {
		IFolder xmlFolder = project.createXmlFolder();		
		IFile xmlFile = createEmptyFile(xmlFolder, name);
		
		assertTrue(xmlFile.getLocation().toFile().exists());
		
		IProject eclipseProject = project.getProject();
		BeansCoreUtils.addProjectNature(eclipseProject, BeansProjectNature.NATURE_ID);				
		project.waitForAutoBuild();

		IBeansModel model = BeansCorePlugin.getModel();
		IBeansProject beansProject = model.getProject(eclipseProject);
		assertNotNull("No sample project in model", beansProject);

		List configs = new ArrayList();
		String config = xmlFile.getProjectRelativePath().toString();
		configs.add(config);
		((BeansProject) beansProject).setConfigs(configs);
		project.waitForAutoBuild();

		IBeansConfig beansConfig = beansProject.getConfig(xmlFile);
		assertNotNull("No sample config in model", beansConfig);
		
		// there's an apparent bug whereby the validation
		// of the xml file isn't done when the file is added
		// to the list of config files, but only after the
		// file is modified. So we don't bother to put anything
		// into the file until now.
		updateTestFile(xmlFolder, name);		
		project.waitForAutoBuild();
		return xmlFile;
    }
    
	/**
	 * create an empty file with the indicated name in the indicated
	 * folder
	 * @param destFolder the folder in which to create the file
	 * @param name the name of the file to be created
	 * @return an eclipse handle on the newly created file
	 * @throws CoreException
	 */
	protected IFile createEmptyFile(IFolder destFolder, String name) throws CoreException {
		IFile destFile = destFolder.getFile(name);
		byte[] buf = new byte[] {};
		InputStream stream = new ByteArrayInputStream(buf);
		destFile.create(stream, false, null);
		return destFile;
	}
	
	/**
	 * copy the test data file from the plugin source area into the 
	 * workspace being tested, in the indicated folder. 
	 * return a handle on the newly created file
	 */
	protected IFile copyTestFile (IFolder destFolder, String name) throws Exception {
		IFile destFile = destFolder.getFile(name);
		String sourceName = getSourceDataPath() + java.io.File.separator + name;
		InputStream stream = new FileInputStream(sourceName);
		destFile.create(stream, false, null);
		return destFile;
	}
	
	/**
	 * update the test data file, which is in the indicated
	 * folder, using the file of the same name
	 * from the plugin source area. 
	 * return a handle on the newly created file
	 */
	protected IFile updateTestFile (IFolder destFolder, String name) throws Exception {
		IFile destFile = destFolder.getFile(name);
		String sourceName = getSourceDataPath() + java.io.File.separator + name;
		InputStream stream = new FileInputStream(sourceName);
		destFile.setContents(stream, false, false, null);
		return destFile;
	}
		
	/**
	 * @return the path to the root of the source for this plugin
	 */
	protected String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.springframework.ide.eclipse.test").getEntry("/");
			return new File(  Platform.asLocalURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @return the path to the data to be tested (text files which
	 * will be copied into the test area)
	 */
	public String getSourceDataPath() {
		return getPluginDirectoryPath() +  java.io.File.separator + "data";
	}
	
	/**
	 * get the failure markers for the workspace
	 * @return an array of all the failure markers
	 * @throws CoreException
	 */
	private IMarker[] getFailureMarkers() throws CoreException {
		  IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		  return root.findMarkers(
		  		null,
				false,
				IResource.DEPTH_INFINITE);
		}
	
	 private IMarker[] getFailureMarkers(IResource resource) throws CoreException {
        return resource.findMarkers(
        			null,
				false,
				IResource.DEPTH_INFINITE);
      }


}