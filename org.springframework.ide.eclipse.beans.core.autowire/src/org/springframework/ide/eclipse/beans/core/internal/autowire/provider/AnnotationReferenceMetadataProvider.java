/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.autowire.provider;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.core.autowire.AnnotatedBeanReference;
import org.springframework.ide.eclipse.beans.core.autowire.internal.model.AnnotatedProperty;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.metadata.BeanMetadataProviderAdapter;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadataProvider;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;

/**
 * {@link IBeanMetadataProvider} analyzes and determines connections between Spring beans based off
 * of their annotations.
 * @author Jared Rodriguez
 * @author Christian Dupuis
 * @since 2.0.5
 */
/**
 * TODO: * Do processing only if annotation based autowiring is enabled in {@link IBeansConfig}
 * and/or {@link IBeansConfigSet}. * Process XML autowiring configurations as well
 */
public class AnnotationReferenceMetadataProvider extends BeanMetadataProviderAdapter implements
		IBeanMetadataProvider {

	private Map<IType, List<AnnotatedBeanReference>> beanReferencesCache = new HashMap<IType, List<AnnotatedBeanReference>>();

	private ASTParser parser = null;

	public Set<IBeanProperty> provideBeanProperties(IBean bean, IBeansConfig beansConfig,
			IProgressMonitor progressMonitor) {

		Set<IBeanProperty> properties = new LinkedHashSet<IBeanProperty>();

		IType type = JdtUtils.getJavaType(bean.getElementResource().getProject(), bean
				.getClassName());

		List<AnnotatedBeanReference> references = parseType(type, progressMonitor);
		for (AnnotatedBeanReference reference : references) {
			IBeanProperty beanProperty = getAnnotationProperty(reference, bean);
			if (beanProperty != null) {
				properties.add(beanProperty);
			}
		}

		return properties;
	}

	/**
	 * Returns an {@link AnnotatedProperty} for the passed in parameters. If no valid property can
	 * be constructed due to a lack of information, then null is returned. This method guarantees
	 * that a valid AnnotatedProperty is constructed.
	 * @param reference the reference to be processed
	 * @param bean the bean for which this reference is to be created
	 * @param iproject the project in which the annotation and bean were found
	 * @return a valid property or null if one could not be created
	 */
	private IBeanProperty getAnnotationProperty(AnnotatedBeanReference reference, IBean bean) {
		AnnotatedProperty beanProperty = null;
		String referenceName = reference.getReferenceName();

		// if we do not know the bean's name, but we know its class
		// then find any bean of the same class
		if (referenceName == null && reference.getClassName() != null) {
			IBeansProject bProject = BeansModelUtils.getProject(bean);
			referenceName = getBeanNameForClass(reference.getClassName(), bProject);
			if (referenceName != null)
				reference.setReferenceName(referenceName);
			else {
				Set<String> allClasses = bProject.getBeanClasses();
				for (String tClass : allClasses) {
					IType type = JdtUtils.getJavaType(bProject.getProject(), tClass);
					if (org.springframework.ide.eclipse.core.java.Introspector.hasSuperType(type,
							reference.getClassName())) {
						referenceName = getBeanNameForClass(tClass, bProject);
						if (referenceName != null) {
							reference.setReferenceName(referenceName);
							break;
						}
					}
				}
			}
		}

		// if we have a name for the referenced bean, then make a reference
		if (referenceName != null) {
			RuntimeBeanReference iBeanRef = new RuntimeBeanReference(referenceName);
			PropertyValue propertyValue = new PropertyValue(reference.getPropertyName(), iBeanRef);
			propertyValue.setSource(reference.getLocation());
			beanProperty = new AnnotatedProperty(bean, propertyValue);
		}

		return beanProperty;
	}

	/**
	 * Returns the name of the first bean found in the project whose class matches that passed.
	 * @param className the name of the class to find
	 * @param project the project to search
	 * @return the name of the first bean found or null if none match
	 */
	private String getBeanNameForClass(String className, IBeansProject project) {
		Set<IBean> beans = project.getBeans(className);
		if (beans.size() > 0)
			return beans.iterator().next().getElementName();
		return null;
	}

	/**
	 * From a collection of modifiers on an ASTNode, this routine determines if one is for Qualifier
	 * and returns the value if so.
	 * @param modifiers collection of modifiers on an AST node
	 * @return the qualifier value
	 */
	@SuppressWarnings("unchecked")
	private String getQualifier(List modifiers) {
		// qualifier is a single member annotation
		Iterator i = modifiers.iterator();
		while (i.hasNext()) {
			// qualifier is a single member annotation
			Object o = i.next();
			if (o instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation sma = (SingleMemberAnnotation) o;
				String name = sma.getTypeName().getFullyQualifiedName();
				if (name.equals("org.springframework.beans.factory.annotation.Qualifier")
						|| name.equals("Qualifier"))
					return unQuoteValue(sma.getValue().toString());
			}
		}

		return null;
	}

	/**
	 * Gets a Type as a String.
	 * @param type
	 * @return
	 */
	private String getTypeAsString(Type type) {
		if (type instanceof SimpleType)
			return ((SimpleType) type).getName().getFullyQualifiedName();
		else if (type instanceof QualifiedType)
			return ((QualifiedType) type).getName().getFullyQualifiedName();
		return null;
	}

	/**
	 * Strips the start and stop quotes from a String. All value objects in AST are quoted.
	 * @param value the value from which to strip encapsulating quotes
	 * @return the value without encapsulating quotes
	 */
	private String unQuoteValue(String value) {
		StringBuilder sbvalue = new StringBuilder(value);
		if (sbvalue.charAt(0) == '"')
			sbvalue.deleteCharAt(0); // zap the starting quote
		if (sbvalue.charAt(sbvalue.length() - 1) == '"')
			sbvalue.deleteCharAt(sbvalue.length() - 1); // zap the end quote
		return sbvalue.toString();
	}

	/**
	 * Extracts the name of the variable from the {@link FieldDeclaration}. This is done by
	 * examining the fragments within the field. There should only ever be a single fragment for a
	 * field even though a list is returned. the first fragment with a name is used for determining
	 * the reference name.
	 * @param ref the reference to be filled
	 * @param field the field declaration to process
	 */
	@SuppressWarnings("unchecked")
	protected void fillBeanReferenceParentDetail(AnnotatedBeanReference ref, FieldDeclaration field) {
		List<VariableDeclarationFragment> fragments = (List<VariableDeclarationFragment>) field
				.fragments();
		for (VariableDeclarationFragment fragment : fragments) {
			if (fragment.getName() != null) {
				ref.setPropertyName(fragment.getName().getFullyQualifiedName());
				break;
			}
		}

		ref.setClassName(getTypeAsString(field.getType()));
		ref.setReferenceName(getQualifier(field.modifiers()));
	}

	/**
	 * Extracts the method name from the passed method. If it is a getter or setter method, then the
	 * name is parsed appropriately so as to return the field name.
	 * @param ref the reference to be filled
	 * @param method the method declaration to process
	 */
	protected void fillBeanReferenceParentDetail(AnnotatedBeanReference ref,
			MethodDeclaration method) {
		StringBuilder methodName = new StringBuilder(method.getName().getFullyQualifiedName());
		// strip off the get or set and then lowercase the first char
		if (methodName.indexOf("get") == 0 || methodName.indexOf("set") == 0) {
			methodName.delete(0, 3);
			methodName = new StringBuilder(Introspector.decapitalize(methodName.toString()));
		}

		ref.setPropertyName(methodName.toString());
		if (method.parameters().size() == 1) {
			// if there is a single parameter to the method, assume it is a set
			SingleVariableDeclaration var = (SingleVariableDeclaration) method.parameters()
					.iterator().next();
			ref.setClassName(getTypeAsString(var.getType()));
		}
		else if (method.parameters().size() == 0) {
			// if no parameters, assume it is a get method
			ref.setClassName(getTypeAsString(method.getReturnType2()));
		}

		ref.setReferenceName(getQualifier(method.modifiers()));
	}

	/**
	 * Analyzes a root ASTNode to find all annotations used within the file.
	 * @param root the root ASTNode to analyze
	 * @return a list of all annotation nodes
	 */
	protected AstAnnotationVisitor getAnnotationNodes(final ASTNode root) {
		AstAnnotationVisitor visitor = new AstAnnotationVisitor();
		root.accept(visitor);
		return visitor;
	}

	/**
	 * Gets the AST parser that will be continually reused across builds.
	 * @return the ASTParser
	 */
	protected ASTParser getASTParser() {
		if (parser == null) {
			parser = ASTParser.newParser(AST.JLS3);
		}
		return parser;
	}

	/**
	 * Populates a {@link AnnotatedBeanReference} describing the resource annotation passed. This
	 * method extracts information from the parent node to be able to better describe the reference
	 * being generated. Currently, parents of type {@link MethodDeclaration) and
	 * {@link FieldDeclaration} are supported.
	 * @param annotation the annotation for which a reference is generated
	 * @return the new {@link AnnotatedBeanReference} or null if there was an unknown parent node
	 * type
	 */
	@SuppressWarnings("unchecked")
	protected AnnotatedBeanReference getBeanReference(ASTNode annotation, String fullClassName,
			IJavaProject jProject) {
		AnnotatedBeanReference ref = new AnnotatedBeanReference();

		// get information on the name the annotation is binding to from the
		// parent element. For Autowired annotations, grab as much as we
		// can from the parent, like the reference name and type
		if (annotation.getParent() instanceof MethodDeclaration)
			fillBeanReferenceParentDetail(ref, (MethodDeclaration) annotation.getParent());
		else if (annotation.getParent() instanceof FieldDeclaration) {
			fillBeanReferenceParentDetail(ref, (FieldDeclaration) annotation.getParent());
		}
		else
			return null;

		// get the value of the name member of the @Resource annotation
		// and set it as the reference name
		if (annotation instanceof NormalAnnotation) {
			NormalAnnotation na = (NormalAnnotation) annotation;
			List<MemberValuePair> values = (List<MemberValuePair>) na.values();
			for (MemberValuePair pair : values) {
				String name = pair.getName().getFullyQualifiedName();
				if (name.equalsIgnoreCase("name"))
					ref.setReferenceName(unQuoteValue(pair.getValue().toString()));
			}
		}
		else {
			IType targetType = JdtUtils.getJavaType(jProject.getProject(), fullClassName);
			if (targetType != null) {
				ref.setClassName(JdtUtils.resolveClassName(ref.getClassName(), targetType));
			}
		}

		return ref;
	}

	protected List<AnnotatedBeanReference> parseType(IType type, IProgressMonitor monitor) {

		if (beanReferencesCache.containsKey(type)) {
			return beanReferencesCache.get(type);
		}

		if (type == null || type.isBinary() || monitor.isCanceled()) {
			return Collections.emptyList();
		}

		// set the source data
		getASTParser().setSource(type.getCompilationUnit());

		// perform the AST parse and return the resource nodes
		AstAnnotationVisitor annotationVisitor = getAnnotationNodes(getASTParser().createAST(
				monitor));
		String className = type.getFullyQualifiedName();

		// if there are no annotations we need, then remove any storage for
		// this file and move on
		if (annotationVisitor.getAnnotations().size() <= 0 || monitor.isCanceled()) {
			return Collections.emptyList();
		}

		List<AnnotatedBeanReference> references = new ArrayList<AnnotatedBeanReference>();

		// loop through all @Resource tags to find relationships
		for (ASTNode resourceAnnotation : annotationVisitor.getAnnotations()) {
			AnnotatedBeanReference ref = getBeanReference(resourceAnnotation, className.toString(),
					type.getJavaProject());
			if (ref != null) {
				try {
					IJavaElement je = type.getCompilationUnit().getElementAt(
							resourceAnnotation.getParent().getStartPosition());
					ref.setLocation(new JavaModelSourceLocation(je));
					references.add(ref);
				}
				catch (JavaModelException e) {
				}
			}
		}
		// Add to cache
		beanReferencesCache.put(type, references);
		return references;
	}

}
