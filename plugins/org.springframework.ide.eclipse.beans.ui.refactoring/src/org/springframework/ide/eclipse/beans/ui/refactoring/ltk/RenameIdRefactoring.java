/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.ltk;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringChangeDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.refactoring.util.BeansRefactoringChangeUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Martin Lippert
 * @since 2.6.0
 */
@SuppressWarnings("restriction")
public class RenameIdRefactoring extends Refactoring {
	
	protected static final String ID_TYPE = "id-type";
	protected static final String FILE = "file";
	protected static final String NAME = "name";
	protected static final String OFFSET = "offset";
	protected static final String OLDNAME = "oldName";
	protected static final String REFERENCES = "references";

	private RenameIdType type;

	protected String beanId;
	protected IFile file;
	protected IDOMNode node;
	protected String oldBeanId;
	protected int offset;
	protected boolean updateReferences = true;
	
	public RenameIdRefactoring() {
	}
	
	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		try {
			pm.beginTask("Creating change...", 1);
			
			CompositeChange compositeChange = new CompositeChange("Rename Spring " + type.getType() + " id") {
				@Override
				public ChangeDescriptor getDescriptor() {
					String project = file.getProject().getName();
					String description = MessageFormat.format("Rename Spring " + type.getType() + " ''{0}''",
							new Object[] { oldBeanId });
					String comment = MessageFormat.format(
							"Rename Spring " + type.getType() + " from ''{0}'' to ''{1}''", new Object[] { oldBeanId,
									beanId });
					Map<String, String> arguments = new HashMap<String, String>();
					arguments.put(OLDNAME, oldBeanId);
					arguments.put(NAME, beanId);
					arguments.put(ID_TYPE, type.toString());
					arguments.put(FILE, file.getFullPath().toString());
					arguments.put(REFERENCES, Boolean.valueOf(updateReferences).toString());
					arguments.put(OFFSET, Integer.toString(offset));
					
					RenameIdRefactoringDescriptor idRefactoringDescriptor = new RenameIdRefactoringDescriptor(project, description, comment,
							arguments);
					return new RefactoringChangeDescriptor(idRefactoringDescriptor);
				}

			};

			Change change = BeansRefactoringChangeUtils.createRenameBeanIdChange(file, type, oldBeanId,
					beanId, updateReferences, pm);
			if (change != null) {
				compositeChange.add(change);
			}
			if (updateReferences) {
				addChangesForUpdatedReferences(type, compositeChange, pm);
			}
			return compositeChange;
		}
		finally {
			pm.done();
		}
	}

	private void addChangesForUpdatedReferences(RenameIdType descriptor, CompositeChange compositeChange, IProgressMonitor pm)
			throws CoreException {
		IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(file));
		if (config != null) {
			Set<IBeansConfig> visitedResources = new HashSet<IBeansConfig>();
			visitedResources.add(config);
			
			// rename id in imported configs
			for (IBeansImport import_ : config.getImports()) {
				for (IBeansConfig bc : import_.getImportedBeansConfigs()) {
					addChangesForUpdateReferences(bc, descriptor,
							compositeChange, visitedResources, pm);
				}
			}
			
			// rename id in configs that import this one
			IBeansConfig importingBeansConfig = BeansModelUtils.getImportingBeansConfig(config);
			if (importingBeansConfig != null) {
				addChangesForUpdateReferences(importingBeansConfig, descriptor, compositeChange,
						visitedResources, pm);
			}
			
			// rename id in bean config sets
			for (IBeansProject project : BeansCorePlugin.getModel().getProjects()) {
				for (IBeansConfigSet configSet : project.getConfigSets()) {
					if (configSet.getConfigs().contains(config)) {
						for (IBeansConfig bc : configSet.getConfigs()) {
							addChangesForUpdateReferences(bc,
									descriptor, compositeChange, visitedResources, pm);
						}
					}
				}
			}
		}
	}

	private void addChangesForUpdateReferences(IBeansConfig beansConfig,
			RenameIdType descriptor, CompositeChange compositeChange,
			Set<IBeansConfig> visitedResources, IProgressMonitor pm)
			throws CoreException {
		if (!visitedResources.contains(beansConfig) && !beansConfig.isElementArchived()) {
			IResource res = beansConfig.getElementResource();
			if (res.isAccessible() && res instanceof IFile) {
				visitedResources.add(beansConfig);
				Change refsChange = BeansRefactoringChangeUtils
						.createRenameBeanRefsChange((IFile) beansConfig.getElementResource(),
								descriptor, oldBeanId, beanId, pm);
				if (refsChange != null) {
					compositeChange.add(refsChange);
				}
			}
		}
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		try {
			monitor.beginTask("Checking preconditions...", 1);
			if (file == null) {
				status.merge(RefactoringStatus.createFatalErrorStatus("Config file not given"));
			}
			else if (!file.exists()) {
				status.merge(RefactoringStatus.createFatalErrorStatus(MessageFormat.format(
						"File ''{0}'' is not a " + "Spring IDE Beans Config.", new Object[] { file
								.getFullPath().toString() })));
			}
			if (node == null) {
				status.merge(RefactoringStatus.createFatalErrorStatus("Selection not given"));
			}
			else if (!BeansEditorUtils.hasAttribute(node, "id")) {
				status.merge(RefactoringStatus
						.createFatalErrorStatus("Selected XML element has no id"));
			}
		}
		finally {
			monitor.done();
		}
		return status;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		RefactoringStatus status = new RefactoringStatus();
		return status;
	}
	
	public RenameIdType getType() {
		return type;
	}
	
	public void setType(RenameIdType type) {
		this.type = type;
	}

	@Override
	public String getName() {
		return "Rename " + this.type;
	}
	
	public String getBeanId() {
		if (this.beanId == null) {
			return BeansEditorUtils.getAttribute(node, "id");
		}
		else {
			return this.beanId;
		}
	}

	public RefactoringStatus setBeanId(String beanId) {
		this.beanId = beanId;
		RefactoringStatus status = new RefactoringStatus();
		if (!StringUtils.hasText(beanId)) {
			status.merge(RefactoringStatus.createFatalErrorStatus(getType().getType() + " id cannot be empty"));
		}
		else if (this.node != null && this.node.getOwnerDocument().getElementById(beanId) != null) {
			status
					.merge(RefactoringStatus
							.createInfoStatus(getType().getType() + " id already used in current file"));
		}
		return status;
	}
	
	public void setFile(IFile file) {
		this.file = file;
	}

	public void setNode(IDOMNode node) {
		this.node = node;
		this.oldBeanId = BeansEditorUtils.getAttribute(node, "id");
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setUpdateReferences(boolean updateReferences) {
		this.updateReferences = updateReferences;
	}
	
	public RefactoringStatus initialize(Map<String, String> arguments) {
		RefactoringStatus status = new RefactoringStatus();

		String value = arguments.get(ID_TYPE);
		if (value != null) {
			setType(RenameIdType.valueOf(value));
		}

		value = arguments.get(REFERENCES);
		if (value != null) {
			setUpdateReferences(Boolean.valueOf(value).booleanValue());
		}

		value = arguments.get(FILE);
		if (value != null) {
			IContainer container = ResourcesPlugin.getWorkspace().getRoot();
			IResource resource = container.findMember(value);
			if (resource == null || !(resource instanceof IFile)) {
				status.merge(RefactoringStatus.createErrorStatus("Cannot get file"));
			}
			else {
				setFile((IFile) resource);
			}
		}

		value = arguments.get(NAME);
		if (value != null) {
			setBeanId(value);
		}

		value = arguments.get(OLDNAME);
		if (value != null) {
			this.oldBeanId = value;
		}

		value = arguments.get(OFFSET);
		if (value != null) {
			int offset = Integer.valueOf(value);
			IStructuredModel model = null;
			try {
				model = StructuredModelManager.getModelManager().getModelForRead(this.file);
				IndexedRegion inode = model.getIndexedRegion(offset);
				if (inode == null) {
					inode = model.getIndexedRegion(offset - 1);
				}
				if (inode instanceof IDOMNode) {
					this.node = (IDOMNode) inode;
				}
			}
			catch (IOException e) {
			}
			catch (CoreException e) {
			}
			finally {
				if (model != null) {
					model.releaseFromRead();
				}
			}

		}
		return status;
	}
	
}
