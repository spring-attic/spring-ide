/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.XMLMemento;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReferenceModel;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelMarkerUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelPeristence {

	private static final Map<String, IElementFactory> ELEMENT_FACTORIES;

	static {
		ELEMENT_FACTORIES = new HashMap<String, IElementFactory>();
		ELEMENT_FACTORIES.put(AnnotationAspectDefinitionElementFactory.FACTORY_ID,
				new AnnotationAspectDefinitionElementFactory());
		ELEMENT_FACTORIES.put(AnnotationIntroductionDefinitionElementFactory.FACTORY_ID,
				new AnnotationIntroductionDefinitionElementFactory());
		ELEMENT_FACTORIES.put(BeanAspectDefinitionElementFactory.FACTORY_ID,
				new BeanAspectDefinitionElementFactory());
		ELEMENT_FACTORIES.put(BeanIntroductionDefinitionElementFactory.FACTORY_ID,
				new BeanIntroductionDefinitionElementFactory());
		ELEMENT_FACTORIES.put(JavaAdvisorDefinitionElementFactory.FACTORY_ID,
				new JavaAdvisorDefinitionElementFactory());
		ELEMENT_FACTORIES.put(AopReferenceElementFactory.FACTORY_ID,
				new AopReferenceElementFactory());
	}

	private static final String AOP_PROJECT_ELEMENT = "aop-project";

	private static final String AOP_REFERENCE_ELEMENT = "aop-reference";

	private static final String AOP_REFERENCE_MODEL_ELEMENT = "aop-reference-model";

	private static final String ASPECT_DEFINITION_ELEMENT = "aspect-definition";

	private static final String FACTORY_ID = "factory-id";

	private static final String NAME_ATTRIBUTE = "name";

	private IPath defaultFile = null;

	public AopReferenceModelPeristence() {
		this.defaultFile = Activator.getDefault().getStateLocation().append(".state");
	}

	private IMemento appendNewChild(IMemento memento, String type) {
		memento.createChild(type);
		IMemento[] ms = memento.getChildren(type);
		if (ms != null && ms.length > 0) {
			return ms[ms.length - 1];
		}
		return memento;
	}

	private void createAopProjects(IAopReferenceModel model, XMLMemento memento) {
		IMemento[] projects = memento.getChildren(AOP_PROJECT_ELEMENT);
		if (projects != null && projects.length > 0) {
			for (IMemento project : projects) {
				String projectName = project.getString(NAME_ATTRIBUTE);
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				IResource member = root.findMember(projectName);
				if (member instanceof IProject) {
					IJavaProject javaProject = JavaCore.create((IProject) member);
					IAopProject aopProject = model.getProject(javaProject);

					createAspectDefinitions(projects, project, aopProject);

					// recreate the marker
					Set<IAopReference> references = aopProject.getAllReferences();
					for (IAopReference reference : references) {
						AopReferenceModelMarkerUtils.createMarker(reference, reference
								.getResource());
					}
				}
			}
		}
	}

	private void createAopReferences(IAopProject aopProject, IMemento aspectDefinitionM,
			IAspectDefinition aspectDefinition) {
		String factoryId;
		IMemento[] aopReferences = aspectDefinitionM.getChildren(AOP_REFERENCE_ELEMENT);
		if (aopReferences != null && aopReferences.length > 0) {
			for (IMemento aopReferenceM : aopReferences) {
				factoryId = aopReferenceM.getString(FACTORY_ID);
				IAopReference aopReference = (IAopReference) ELEMENT_FACTORIES.get(factoryId)
						.createElement(aopReferenceM);
				// The aopReference can be null if the resource has been deleted or is an external
				if (aopReference != null) {
					aopReference.setDefinition(aspectDefinition);
					aopProject.addAopReference(aopReference);
				}
			}
		}
	}

	private void createAspectDefinitions(IMemento[] projects, IMemento project,
			IAopProject aopProject) {
		IMemento[] aspectDefinitions = project.getChildren(ASPECT_DEFINITION_ELEMENT);
		if (projects != null && projects.length > 0) {
			for (IMemento aspectDefinitionM : aspectDefinitions) {
				String factoryId = aspectDefinitionM.getString(FACTORY_ID);
				IAspectDefinition aspectDefinition = (IAspectDefinition) ELEMENT_FACTORIES.get(
						factoryId).createElement(aspectDefinitionM);
				createAopReferences(aopProject, aspectDefinitionM, aspectDefinition);
			}
		}
	}

	protected boolean isPersisted() {
		return defaultFile.toFile().exists();
	}

	protected synchronized void loadReferenceModel() {
		if (!shouldModelByPersisted() || !isPersisted()) {
			return;
		}

		IAopReferenceModel model = Activator.getModel();
		Reader reader = null;
		try {
			reader = new FileReader(defaultFile.toFile());
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			createAopProjects(model, memento);
		}
		catch (Exception e) {
			Activator.log("Cannot load .state model file", e);
			// re-init aop reference model
			Activator.getModel().clearProjects();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {
				}
			}
		}
	}

	private boolean shouldModelByPersisted() {
		IScopeContext context = new InstanceScope();
		IEclipsePreferences node = context.getNode(Activator.PLUGIN_ID);
		return node.getBoolean(Activator.PERSIST_AOP_MODEL_PREFERENCE, true);
	}

	protected synchronized void saveReferenceModel() {
		if (!shouldModelByPersisted()) {
			if (isPersisted()) {
				defaultFile.toFile().delete();
			}
			return;
		}

		XMLMemento memento = XMLMemento.createWriteRoot(AOP_REFERENCE_MODEL_ELEMENT);
		Collection<IAopProject> projects = Activator.getModel().getProjects();
		for (IAopProject project : projects) {
			IMemento projectM = appendNewChild(memento, AOP_PROJECT_ELEMENT);
			projectM.putString(NAME_ATTRIBUTE, project.getProject().getElementName());

			Set<IAopReference> refs = project.getAllReferences();
			Map<IAspectDefinition, List<IAopReference>> maps = new HashMap<IAspectDefinition, List<IAopReference>>();
			for (IAopReference ref : refs) {
				if (maps.containsKey(ref.getDefinition())) {
					maps.get(ref.getDefinition()).add(ref);
				}
				else {
					List<IAopReference> r = new ArrayList<IAopReference>();
					r.add(ref);
					maps.put(ref.getDefinition(), r);
				}
			}

			for (Map.Entry<IAspectDefinition, List<IAopReference>> entry : maps.entrySet()) {
				IMemento definitionM = appendNewChild(projectM, ASPECT_DEFINITION_ELEMENT);
				if (entry.getKey() instanceof IAdaptable) {
					IPersistableElement pers = (IPersistableElement) ((IAdaptable) entry.getKey())
							.getAdapter(IPersistableElement.class);
					if (pers != null) {
						pers.saveState(definitionM);
						definitionM.putString(FACTORY_ID, pers.getFactoryId());
					}
					for (IAopReference ref : entry.getValue()) {
						if (ref instanceof IAdaptable) {
							IPersistableElement pers2 = (IPersistableElement) ((IAdaptable) ref)
									.getAdapter(IPersistableElement.class);
							if (pers2 != null) {
								IMemento refM = appendNewChild(definitionM, AOP_REFERENCE_ELEMENT);
								pers2.saveState(refM);
								refM.putString(FACTORY_ID, pers2.getFactoryId());
							}
						}
					}
				}
			}
		}

		// save memento to default map file
		Writer writer = null;
		try {
			writer = new FileWriter(defaultFile.toFile());
			memento.save(writer);
		}
		catch (IOException e) {
		}
		finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				}
				catch (IOException e) {
				}
			}
		}
	}
}
