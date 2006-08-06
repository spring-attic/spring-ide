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
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * this class tests the config file validator. The test method is to create a
 * config file with certain expected errors, and check the validation creates
 * the expected error markers
 * 
 * @author Loren Rosen
 * 
 */
public class ValidationTests extends AbstractSpringIdeTest {

	public ValidationTests(String name) {
		super(name);
	}

	protected String xmlTestFileName = "sample.xml";

	protected static String SimpleBeanText = "public class SimpleBean {"
			+ "private String stuff;"
			+ "public String getStuff() {return stuff;}"
			+ "public void setStuff(String value) {this.stuff=value;}}";

	protected static String ConstructedBeanText = "public class ConstructedBean {"
			+ "private String stuff;"
			+ "public ConstructedBean(String stuff, String more, String evenMore) { this.stuff = stuff;}"
			+ "public String getStuff() {return stuff;}"
			+ "public void setStuff(String value) {this.stuff=value;}}";

	/**
	 * create the Java files used by the tests
	 * 
	 * @throws Exception
	 */
	private void createBeanClasses() throws Exception {
		IPackageFragment pack = project.createPackage("pack1");
		project.createType(pack, "SimpleBean.java", SimpleBeanText);
		project.createType(pack, "ConstructedBean.java", ConstructedBeanText);
		project.waitForAutoBuild();

	}

	private void renameBeanClassProperty() throws Exception {
		IType type = BeansModelUtils.getJavaType(project.getProject(),
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
		createBeanClasses();
		BeansProject beansProject = createBeansProject();
		IFile xmlFile = createXmlFile("sample.xml", beansProject);

		IMarker[] markers = getFailureMarkers();
		assertEquals("Wrong number of validation errors (problem markers)", 7,
				markers.length);

		// test that we get an error for a bean class
		// that isn't in the class path
		IMarker marker = markers[0];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(6, marker.getAttribute(IMarker.LINE_NUMBER, 0));
		assertEquals(
				"Class 'org.springframework.beans.factory.config.PropertyPlaceholderConfigurer' not found",
				marker.getAttribute(IMarker.MESSAGE, ""));

		// test that we get an error for a bean class
		// that isn't in the class path -- this time
		// the class has a simple name (not qualifed with
		// some other package)
		marker = markers[1];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(10, marker.getAttribute(IMarker.LINE_NUMBER, 0));
		assertEquals("Class 'absent' not found", marker.getAttribute(
				IMarker.MESSAGE, ""));

		// test that we get an error for a bean class
		// which tried to set an non-existent property
		marker = markers[2];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(16, marker.getAttribute(IMarker.LINE_NUMBER, 0));
		assertEquals(
				"No setter for property 'absentProperty' found in class 'pack1.SimpleBean'",
				marker.getAttribute(IMarker.MESSAGE, ""));

		// test that we get an error for a child bean
		// whose parent doesn't exist
		marker = markers[3];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(20, marker.getAttribute(IMarker.LINE_NUMBER, 0));
		assertEquals("Undefined parent root bean", marker.getAttribute(
				IMarker.MESSAGE, ""));

		// test that we get an error for a bean
		// that has no parent or class
		marker = markers[4];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(23, marker.getAttribute(IMarker.LINE_NUMBER, 0));
		assertEquals("Bean definition has neither 'class' nor 'parent'", marker
				.getAttribute(IMarker.MESSAGE, ""));

		// test that we get an error for a bean
		// that calls the bean constructor with the wrong
		// number of arguments.
		marker = markers[5];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(25, marker.getAttribute(IMarker.LINE_NUMBER, 0));
		assertEquals(
				"No constructor with 1 argument defined in class 'pack1.ConstructedBean'",
				marker.getAttribute(IMarker.MESSAGE, ""));

		marker = markers[6];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(30, marker.getAttribute(IMarker.LINE_NUMBER, 0));
		assertEquals(
				"No constructor with 2 arguments defined in class 'pack1.ConstructedBean'",
				marker.getAttribute(IMarker.MESSAGE, ""));

		renameBeanClassProperty();
		project.waitForAutoBuild();

		markers = getFailureMarkers();
		assertEquals("Wrong number of validation errors (problem markers)", 8,
				markers.length);
	}

	public void testParseErrors() throws Exception {
		BeansProject beansProject = createBeansProject();
		IFile xmlFile = createXmlFile("parseError.xml", beansProject);

		IMarker[] markers = getFailureMarkers(xmlFile);

		assertEquals(1, markers.length);

		// test that we get an error for an XML
		// parsing problem
		IMarker marker = markers[0];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(6, marker.getAttribute(IMarker.LINE_NUMBER, 0));
		assertEquals(
				"Attribute \"undefinedAttribute\" must be declared for element type \"bean\".",
				marker.getAttribute(IMarker.MESSAGE, ""));
	}

	public void testValidateConfigSetErrors() throws Exception {
		createBeanClasses();
		BeansProject beansProject = createBeansProject();
		IFolder xmlFolder = project.createXmlFolder();
		// TODO: try to create the xml folder only once per test
		IFile xmlFileA = createXmlFile("a.xml", beansProject);
		IFile xmlFileB = createXmlFile("b.xml", beansProject);

		ArrayList configNames = new ArrayList();
		configNames.add("xml/a.xml");
		configNames.add("xml/b.xml");
		BeansConfigSet b = new BeansConfigSet(beansProject, "configSet",
				configNames);
		b.setAllowBeanDefinitionOverriding(false);
		// TODO: really should run this test twice, with this flag set and unset

		Collection c = beansProject.getConfigSets();
		ArrayList l = new ArrayList(c);
		l.add(b);
		beansProject.setConfigSets(l);
		beansProject.saveDescription();
		updateTestFile(xmlFolder, "b.xml");
		project.waitForAutoBuild();

		IMarker[] markers = getFailureMarkers(xmlFileB);

		assertEquals(1, markers.length);

		// test that we get an error where one
		// bean overrides another in a config set
		IMarker marker = markers[0];
		assertEquals(marker.getType(), IBeansProjectMarker.PROBLEM_MARKER);
		assertEquals(6, marker.getAttribute(IMarker.LINE_NUMBER, 0));
		assertEquals("Overrides another bean in config set 'configSet'", marker
				.getAttribute(IMarker.MESSAGE, ""));

	}

	private BeansProject createBeansProject() throws CoreException {
		IProject eclipseProject = project.getProject();
		SpringCoreUtils.addProjectNature(eclipseProject, SpringCore.NATURE_ID,
				new NullProgressMonitor());
		project.waitForAutoBuild();

		IBeansModel model = BeansCorePlugin.getModel();
		BeansProject beansProject = (BeansProject) model
				.getProject(eclipseProject);
		assertNotNull("No sample project in model", beansProject);
		return beansProject;
	}

	private IFile createXmlFile(String name, BeansProject beansProject)
			throws CoreException, Exception {
		// System.out.println("enter createXmlFile");
		IFolder xmlFolder = project.createXmlFolder();
		IFile xmlFile = createEmptyFile(xmlFolder, name);

		assertTrue(xmlFile.getLocation().toFile().exists());

		// BeansProject beansProject = createBeansProject();
		beansProject.addConfig(xmlFile, false);
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
	 * create an empty file with the indicated name in the indicated folder
	 * 
	 * @param destFolder
	 *            the folder in which to create the file
	 * @param name
	 *            the name of the file to be created
	 * @return an eclipse handle on the newly created file
	 * @throws CoreException
	 */
	protected IFile createEmptyFile(IFolder destFolder, String name)
			throws CoreException {
		IFile destFile = destFolder.getFile(name);
		byte[] buf = new byte[] {};
		InputStream stream = new ByteArrayInputStream(buf);
		destFile.create(stream, false, null);
		return destFile;
	}

	/**
	 * copy the test data file from the plugin source area into the workspace
	 * being tested, in the indicated folder. return a handle on the newly
	 * created file
	 */
	protected IFile copyTestFile(IFolder destFolder, String name)
			throws Exception {
		IFile destFile = destFolder.getFile(name);
		String sourceName = getSourceDataPath() + java.io.File.separator + name;
		InputStream stream = new FileInputStream(sourceName);
		destFile.create(stream, false, null);
		return destFile;
	}

	/**
	 * update the test data file, which is in the indicated folder, using the
	 * file of the same name from the plugin source area. return a handle on the
	 * newly created file
	 */
	protected IFile updateTestFile(IFolder destFolder, String name)
			throws Exception {
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
			URL platformURL = Platform.getBundle(
					"org.springframework.ide.eclipse.test").getEntry("/");
			return new File(Platform.asLocalURL(platformURL).getFile())
					.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return the path to the data to be tested (text files which will be
	 *         copied into the test area)
	 */
	public String getSourceDataPath() {
		return getPluginDirectoryPath() + java.io.File.separator + "data";
	}

	/**
	 * Returns the failure markers for the workspace.
	 */
	private IMarker[] getFailureMarkers() throws CoreException {
		return getFailureMarkers(ResourcesPlugin.getWorkspace().getRoot());
	}

	/**
	 * Returns the failure markers for given resource.
	 */
	private IMarker[] getFailureMarkers(IResource resource)
			throws CoreException {
		return resource.findMarkers(null, false, IResource.DEPTH_INFINITE);
	}
}
