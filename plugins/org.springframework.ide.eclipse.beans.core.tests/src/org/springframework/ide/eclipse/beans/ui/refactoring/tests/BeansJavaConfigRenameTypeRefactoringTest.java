/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Andrew Eisenberg
 * @since 3.4.0
 */
public class BeansJavaConfigRenameTypeRefactoringTest {
	
	private IProject project;
	private BeansModel model;
	private BeansProject beansProject;
	private IJavaProject javaProject;
    private BeansModel origModel;
	
    protected final Refactoring createRefactoring(RefactoringDescriptor descriptor) throws CoreException {
        RefactoringStatus status= new RefactoringStatus();
        Refactoring refactoring= descriptor.createRefactoring(status);
        assertNotNull("refactoring should not be null", refactoring);
        assertTrue("status should be ok, but was: " + status, status.isOK());
        return refactoring;
    }
    protected IUndoManager getUndoManager() {
        IUndoManager undoManager= RefactoringCore.getUndoManager();
        undoManager.flush();
        return undoManager;
    }
    protected void executePerformOperation(final PerformChangeOperation perform, IWorkspace workspace) throws CoreException {
        workspace.run(perform, new NullProgressMonitor());
    }

    protected RefactoringStatus performRefactoring(Refactoring ref, boolean providesUndo, boolean performOnFail) throws Exception {
        // force updating of indexes
        StsTestUtil.buildProject(javaProject);
        IUndoManager undoManager= getUndoManager();
        final CreateChangeOperation create= new CreateChangeOperation(
            new CheckConditionsOperation(ref, CheckConditionsOperation.ALL_CONDITIONS),
            RefactoringStatus.FATAL);
        final PerformChangeOperation perform= new PerformChangeOperation(create);
        perform.setUndoManager(undoManager, ref.getName());
        IWorkspace workspace= ResourcesPlugin.getWorkspace();
        executePerformOperation(perform, workspace);
        RefactoringStatus status= create.getConditionCheckingStatus();
        Assert.assertTrue("Change wasn't executed", perform.changeExecuted() || ! perform.changeExecutionFailed());
        Change undo= perform.getUndoChange();
        if (providesUndo) {
            assertNotNull("Undo doesn't exist", undo);
            assertTrue("Undo manager is empty", undoManager.anythingToUndo());
        } else {
            assertNull("Undo manager contains undo but shouldn't", undo);
        }
        return status;
    }

	
    private void performRefactoring(IType configType, IType renameType, String newName, String newFullName) throws Exception {
        String origFullNameDot = configType.getFullyQualifiedName('.');
        String origFullNameDollar = configType.getFullyQualifiedName('$');

        assertNotNull(getConfigForClassName(origFullNameDollar));
        assertNotNull(beansProject.getConfig(getConfigForClassName(origFullNameDollar)));
        assertNull(getConfigForClassName(newFullName));

        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory
                .createRenameJavaElementDescriptor(IJavaRefactorings.RENAME_TYPE);
        descriptor.setUpdateReferences(true);
        descriptor.setJavaElement(renameType);
        descriptor.setNewName(newName);
        
        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring, false, true);
        
        assertTrue("Refactoring produced an error: " + result, result.isOK());
        IType newConfigType = javaProject.findType(newFullName, new NullProgressMonitor());
        assertNotNull("Couldn't find renamed type", newConfigType);
        assertTrue("New type should exist", newConfigType.exists());
        assertFalse("Original type should not exist", configType.exists());
        assertNull(getConfigForClassName(origFullNameDot));
        assertNotNull(getConfigForClassName(newFullName));
        assertNotNull(beansProject.getConfig(getConfigForClassName(newFullName)));
        
    }

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", "org.springframework.ide.eclipse.beans.core.tests");
		javaProject = JdtUtils.getJavaProject(project);
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
		model.addProject(beansProject);
		origModel = (BeansModel) BeansCorePlugin.getModel();
		BeansCorePlugin.setModel(model);
	}
	
	@After
	public void deleteProject() throws Exception {
	    BeansCorePlugin.setModel(origModel);
		project.delete(true, null);
	}
	
	private BeansConfigId getConfigForClassName(String cName) throws JavaModelException {
	    return BeansConfigId.create(javaProject.findType(cName, DefaultWorkingCopyOwner.PRIMARY, null), javaProject.getProject());	
	}
	
	@Test
	public void testBasicTypeRename() throws Exception {
		IType configClass = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		beansProject.addConfig(getConfigForClassName("org.test.spring.SimpleConfigurationClass"), IBeansConfig.Type.MANUAL);
		performRefactoring(configClass, configClass, "NewClassName", "org.test.spring.NewClassName");
	}

	@Test
	public void testTypeRenameInnerConfigurationClass() throws Exception {
		IType configClass = javaProject.findType("org.test.spring.OuterConfigurationClass.InnerConfigurationClass");
		beansProject.addConfig(getConfigForClassName("org.test.spring.OuterConfigurationClass$InnerConfigurationClass"), IBeansConfig.Type.MANUAL);
        performRefactoring(configClass, configClass, "NewClassName", "org.test.spring.OuterConfigurationClass.NewClassName");
	}

	@Test
	public void testTypeRenameOuterConfigurationClass() throws Exception {
		IType outerClass = javaProject.findType("org.test.spring.OuterConfigurationClass");
        IType configClass = javaProject.findType("org.test.spring.OuterConfigurationClass.InnerConfigurationClass");
		beansProject.addConfig(getConfigForClassName("org.test.spring.OuterConfigurationClass$InnerConfigurationClass"), IBeansConfig.Type.MANUAL);
        performRefactoring(configClass, outerClass, "NewClassName", "org.test.spring.NewClassName.InnerConfigurationClass");
	}
//
//	@Test
//	public void testTypeRenameDoubleOuterOuterConfigurationClass() throws Exception {
//		IType outerClass = javaProject.findType("org.test.spring.DoubleOuterConfigurationClass");
//		beansProject.addConfig(getConfigForClassName("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass"), IBeansConfig.Type.MANUAL);
//		
//		BeansJavaConfigRenameTypeRefactoringParticipant participant = new BeansJavaConfigRenameTypeRefactoringParticipant();
//		RenameTypeArguments arguments = new RenameTypeArguments("NewClassName", true, false, null);
//		RenameResourceProcessor processor = new RenameResourceProcessor(outerClass.getResource());
//		participant.initialize(processor, outerClass, arguments);
//		BeansJavaConfigTypeChange change = (BeansJavaConfigTypeChange) participant.createChange(new NullProgressMonitor());
//		change.setBeansModel(model);
//		change.perform(new NullProgressMonitor());
//		
//		assertNull(beansProject.getConfig(getConfigForClassName("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass")));
//		IBeansConfig newConfig = beansProject.getConfig(getConfigForClassName("org.test.spring.NewClassName$OuterConfigurationClass$InnerConfigurationClass"));
//		assertNotNull(newConfig);
//	}
//
//	@Test
//	public void testTypeRenameDoubleOuterConfigurationClass() throws Exception {
//		IType outerClass = javaProject.findType("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass");
//		beansProject.addConfig(getConfigForClassName("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass"), IBeansConfig.Type.MANUAL);
//		
//		BeansJavaConfigRenameTypeRefactoringParticipant participant = new BeansJavaConfigRenameTypeRefactoringParticipant();
//		RenameTypeArguments arguments = new RenameTypeArguments("NewClassName", true, false, null);
//		RenameResourceProcessor processor = new RenameResourceProcessor(outerClass.getResource());
//		participant.initialize(processor, outerClass, arguments);
//		BeansJavaConfigTypeChange change = (BeansJavaConfigTypeChange) participant.createChange(new NullProgressMonitor());
//		change.setBeansModel(model);
//		change.perform(new NullProgressMonitor());
//		
//		assertNull(beansProject.getConfig(getConfigForClassName("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass")));
//		IBeansConfig newConfig = beansProject.getConfig(getConfigForClassName("org.test.spring.DoubleOuterConfigurationClass$NewClassName$InnerConfigurationClass"));
//		assertNotNull(newConfig);
//	}
//
//	@Test
//	public void testTypeRenameDoubleOuterInnerConfigurationClass() throws Exception {
//		IType outerClass = javaProject.findType("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass");
//		beansProject.addConfig(getConfigForClassName("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass"), IBeansConfig.Type.MANUAL);
//		
//		BeansJavaConfigRenameTypeRefactoringParticipant participant = new BeansJavaConfigRenameTypeRefactoringParticipant();
//		RenameTypeArguments arguments = new RenameTypeArguments("NewClassName", true, false, null);
//		RenameResourceProcessor processor = new RenameResourceProcessor(outerClass.getResource());
//		participant.initialize(processor, outerClass, arguments);
//		BeansJavaConfigTypeChange change = (BeansJavaConfigTypeChange) participant.createChange(new NullProgressMonitor());
//		change.setBeansModel(model);
//		change.perform(new NullProgressMonitor());
//		
//		assertNull(beansProject.getConfig(getConfigForClassName("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass")));
//		IBeansConfig newConfig = beansProject.getConfig(getConfigForClassName("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$NewClassName"));
//		assertNotNull(newConfig);
//	}
//
}
