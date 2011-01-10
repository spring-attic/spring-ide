/*******************************************************************************
 * Copyright (c) 2009, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractXmlValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IXmlValidationContext;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.util.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML-based {@link IValidationRule} that uses Spring's Tool annotation to validate attribute values.
 * <p>
 * Also validates special XML namespace elements that don't support tool annotations. Currently the following attributes
 * and elements are supported:
 * <ul>
 * <li>util:constant static-field</li>
 * <li>osgi:interfaces value</li>
 * </ul>
 * @author Christian Dupuis
 * @author Terry Hon
 * @since 2.2.7
 */
public class NamespaceElementsRule extends AbstractXmlValidationRule {

	private final List<IAttributeValidator> ATTRIBUTE_VALIDATORS;

	private final List<IElementValidator> ELEMENT_VALIDATORS;

	{
		ATTRIBUTE_VALIDATORS = new ArrayList<IAttributeValidator>();
		ATTRIBUTE_VALIDATORS.add(new UtilStaticFieldAttributeValidator());
		ATTRIBUTE_VALIDATORS.add(new BlueprintInterfaceAttributeValidator());
		ATTRIBUTE_VALIDATORS.add(new BlueprintDependesOnAttributeValidator());

		ELEMENT_VALIDATORS = new ArrayList<IElementValidator>();
		ELEMENT_VALIDATORS.add(new OsgiInterfacesElementValidator());
	}

	/**
	 * Internal list of full-qualified class names that should be ignored by this validation rule.
	 */
	private List<String> ignorableClasses = null;

	/**
	 * Internal list of bean names that should be ignored by this validation rule.
	 */
	private List<String> ignorableBeans = new ArrayList<String>();

	public void setIgnorableClasses(String classNames) {
		if (StringUtils.hasText(classNames)) {
			this.ignorableClasses = Arrays.asList(StringUtils.delimitedListToStringArray(classNames, ",", "\r\n\f "));
		}
	}

	public void setIgnorableBeans(String beanNames) {
		if (StringUtils.hasText(beanNames)) {
			this.ignorableBeans = Arrays.asList(StringUtils.delimitedListToStringArray(beanNames, ",", "\r\n\f "));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean supports(Node n) {
		// only validate non standard nodes; nodes from the bean namespace are validated with all the subsequent
		// validation rules
		return !NamespaceUtils.DEFAULT_NAMESPACE_URI.equals(n.getNamespaceURI())
				&& n.getNodeType() == Node.ELEMENT_NODE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validate(Node n, IXmlValidationContext context) {
		validate(n, null, context);
	}

	public void validate(Node n, String attributeNameToCheck, IXmlValidationContext context) {

		if (attributeNameToCheck == null) {
			// Validate with one of the pre-configured validators
			for (IElementValidator validator : ELEMENT_VALIDATORS) {
				if (validator.supports(n)) {
					validator.validate(n, context);
				}
			}
		}

		// No Iterate over all the attributes to trigger validation of values
		NamedNodeMap attributes = n.getAttributes();
		if (attributes != null && attributes.getLength() > 0) {
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				String attributeName = attribute.getLocalName();

				// Only check attribute that is marked to check
				if (attributeNameToCheck != null && !attributeNameToCheck.equals(attributeName)) {
					continue;
				}

				// Attributes can be annotated with Tool annotation -> validate based on annotation
				for (ToolAnnotationData annotationData : context.getToolAnnotation(n, attributeName)) {

					// Check bean references
					if ("ref".equals(annotationData.getKind())) {
						validateBeanReference(n, attribute, context);
					}

					// Check class name
					if (Class.class.getName().equals(annotationData.getExpectedType())) {
						validateClassName(n, attribute, context, annotationData);
					}

					// Check method name
					if (annotationData.getExpectedMethodType() != null && attribute.getNodeValue() != null) {
						validateMethodName(evaluateXPathExpression(annotationData.getExpectedMethodType(), n), n,
								attribute, context);
					}
					else if (annotationData.getExpectedMethodRef() != null && attribute.getNodeValue() != null) {
						try {
							AbstractBeanDefinition referencedBeanDefinition = (AbstractBeanDefinition) context
									.getCompleteRegistry().getBeanDefinition(
											evaluateXPathExpression(annotationData.getExpectedMethodRef(), n));
							String className = referencedBeanDefinition.getBeanClassName();
							validateMethodName(className, n, attribute, context);
						}
						catch (NoSuchBeanDefinitionException e) {
							// Ignore this as it has already been reported
						}
					}
				}

				// Validate with one of the pre-configured validators
				for (IAttributeValidator validator : ATTRIBUTE_VALIDATORS) {
					if (validator.supports(n, attribute)) {
						validator.validate(n, attribute, context);
					}
				}
			}
		}
	}

	/**
	 * Validate given method reference.
	 */
	private void validateMethodName(String className, Node n, Node attribute, IXmlValidationContext context) {
		IType type = JdtUtils.getJavaType(context.getRootElementProject(), className);
		try {
			if (type != null) {
				String methodName = attribute.getNodeValue();
				if (Introspector.findMethod(type, methodName, -1, Public.DONT_CARE, Static.DONT_CARE) == null) {
					context.error(n, "METHOD_NOT_FOUND", "Method '" + methodName + "' not found in class '" + className
							+ "'", new ValidationProblemAttribute("METHOD", methodName),
							new ValidationProblemAttribute("CLASS", className));
				}
			}
		}
		catch (Exception e) {
			SpringCore.log(e);
		}
	}

	/**
	 * Validate given class reference.
	 */
	private void validateClassName(Node n, Node attribute, IXmlValidationContext context,
			ToolAnnotationData annotationData) {
		String className = attribute.getNodeValue();
		if (className != null && !SpringCoreUtils.hasPlaceHolder(className) && !ignorableClasses.contains(className)) {
			IType type = JdtUtils.getJavaType(context.getRootElementProject(), className);

			// Verify class is found
			if (type == null || (type.getDeclaringType() != null && className.indexOf('$') == -1)) {
				context.error(n, "CLASS_NOT_FOUND", "Class '" + className + "' not found",
						new ValidationProblemAttribute("CLASS", className));
				return;
			}

			try {
				// Check if type is part for give type hierarchy
				if (annotationData.getAssignableTo() != null) {
					if (!JdtUtils.doesImplement(context.getRootElementResource(), type, annotationData
							.getAssignableTo())) {
						context.error(n, "CLASS_IS_NOT_IN_HIERACHY", "'" + className + "' is not a sub type of '"
								+ annotationData.getAssignableTo() + "'");
					}
				}

				// Check if type is either a concrete class or interface as requested by the tool
				// annotation
				if ("class-only".equals(annotationData.getAssignableToRestriction()) && type.isInterface()) {
					context.error(n, "CLASS_IS_INTERFACE", "'" + className
							+ "' specifies an interface where a class is required");
				}
				else if ("interface-only".equals(annotationData.getAssignableToRestriction()) && !type.isInterface()) {
					context.error(n, "CLASS_IS_CLASS", "'" + className
							+ "' specifies a class where an interface is required");
				}
			}
			catch (JavaModelException e) {
			}
		}
	}

	/**
	 * Validate given bean reference.
	 */
	private void validateBeanReference(Node n, Node attribute, IXmlValidationContext context) {
		String beanName = attribute.getNodeValue();
		if (beanName != null && !SpringCoreUtils.hasPlaceHolder(beanName) && !ignorableBeans.contains(beanName)) {
			try {
				context.getCompleteRegistry().getBeanDefinition(beanName);
			}
			catch (NoSuchBeanDefinitionException e) {
				context.warning(n, "UNDEFINED_REFERENCED_BEAN", "Referenced bean '" + beanName + "' not found",
						new ValidationProblemAttribute("BEAN", beanName));
			}
			catch (BeanDefinitionStoreException e) {
				// Need to make sure that the parent of a parent does not use placeholders
				Throwable exp = e;
				boolean placeHolderFound = false;
				while (exp != null && exp.getCause() != null) {
					String msg = exp.getCause().getMessage();
					if (msg.contains(SpringCoreUtils.PLACEHOLDER_PREFIX)
							&& msg.contains(SpringCoreUtils.PLACEHOLDER_SUFFIX)) {
						placeHolderFound = true;
						break;
					}
					exp = exp.getCause();
				}
				if (!placeHolderFound) {
					context.warning(n, "UNDEFINED_REFERENCED_BEAN", "Refrenced bean '" + beanName + "' not found",
							new ValidationProblemAttribute("BEAN", beanName));
				}
			}
		}
	}

	/**
	 * Evaluates XPath expressions against the given node.
	 */
	private String evaluateXPathExpression(String xpath, Node node) {
		XPathFactory factory = XPathFactory.newInstance();
		XPath path = factory.newXPath();
		try {
			return path.evaluate(xpath, node);
		}
		catch (XPathExpressionException e) {
			return null;
		}
	}

	/**
	 * Implementations of this interface can validate attributes and their values.
	 */
	interface IAttributeValidator {

		boolean supports(Node n, Node attribute);

		void validate(Node n, Node attribute, IXmlValidationContext context);

	}

	/**
	 * Implementations of this interface can validate elements.
	 */
	interface IElementValidator {

		boolean supports(Node n);

		void validate(Node n, IXmlValidationContext context);

	}

	/**
	 * Simply validation for:
	 * 
	 * <pre>
	 * 	&lt;util:constant static-field=&quot;org.springframework.core.Ordered.HIGHEST_PRECEDENCE&quot;/&gt;
	 * </pre>
	 */
	private class UtilStaticFieldAttributeValidator implements IAttributeValidator {

		public boolean supports(Node n, Node attribute) {
			return "http://www.springframework.org/schema/util".equals(n.getNamespaceURI())
					&& "constant".equals(n.getLocalName()) && "static-field".equals(attribute.getNodeName());
		}

		public void validate(Node n, Node attribute, IXmlValidationContext context) {
			try {
				String fieldName = attribute.getNodeValue();
				if (fieldName != null) {
					int ix = fieldName.lastIndexOf('.');
					if (ix > 0) {
						String className = fieldName.substring(0, ix);
						fieldName = fieldName.substring(ix + 1);
						IType type = JdtUtils.getJavaType(context.getRootElementProject(), className);
						if (type != null) {
							IField field = type.getField(fieldName);
							if (!field.exists()) {
								context.error(n, "FIELD_NOT_FOUND", "Field '" + fieldName + "' not found on class '"
										+ className + "'", new ValidationProblemAttribute("CLASS", className),
										new ValidationProblemAttribute("FIELD", fieldName));
							}
							else if (!type.isEnum() && !Flags.isStatic(field.getFlags())) {
								context.error(n, "FIELD_NOT_STATIC", "Field '" + fieldName + "' on class '" + className
										+ "' is not static", new ValidationProblemAttribute("CLASS", className),
										new ValidationProblemAttribute("FIELD", fieldName));
							}
						}
						else {
							context.error(n, "CLASS_NOT_FOUND", "Class '" + className + "' not found",
									new ValidationProblemAttribute("CLASS", className));
						}
					}
				}
			}
			catch (JavaModelException e) {
				// Ignore here as we report class not found and such
			}
		}
	}

	/**
	 * Simply validation for:
	 * 
	 * <pre>
	 * 	&lt;osgi:reference id=&quot;test1&quot; &gt; 
	 * 		&lt;osgi:interfaces&gt;
	 * 			&lt;value&gt;java.util.List&lt;/value&gt;	
	 * 		&lt;/osgi:interfaces&gt;
	 * 	&lt;/osgi:reference&gt;
	 * </pre>
	 */
	private class OsgiInterfacesElementValidator implements IElementValidator {

		public boolean supports(Node n) {
			return ("http://www.springframework.org/schema/osgi".equals(n.getNamespaceURI()) || "http://www.osgi.org/xmlns/blueprint/v1.0.0"
					.equals(n.getNamespaceURI()))
					&& "interfaces".equals(n.getLocalName());
		}

		public void validate(Node n, IXmlValidationContext context) {
			NodeList children = n.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE && "value".equals(child.getLocalName())
						&& child.getFirstChild() != null && child.getFirstChild().getNodeType() == Node.TEXT_NODE) {
					String className = child.getFirstChild().getNodeValue();
					IType type = JdtUtils.getJavaType(context.getRootElementProject(), className);

					// Verify class is found
					if (type == null || (type.getDeclaringType() != null && className.indexOf('$') == -1)) {
						context.error(child, "CLASS_NOT_FOUND", "Class '" + className + "' not found",
								new ValidationProblemAttribute("Class", className));
						continue;
					}

					try {
						if (!type.isInterface()) {
							context.error(child, "CLASS_IS_CLASS", "'" + className
									+ "' specifies a class where an interface is required");
						}
					}
					catch (JavaModelException e) {
					}
				}
			}
		}
	}

	/**
	 * Simply validation for:
	 * 
	 * <pre>
	 * 	&lt;bp:reference interface=&quot;java.util.List&quot;
	 * </pre>
	 */
	private class BlueprintInterfaceAttributeValidator implements IAttributeValidator {

		private final ToolAnnotationData annotationData;

		public BlueprintInterfaceAttributeValidator() {
			this.annotationData = new ToolAnnotationData();
			this.annotationData.setAssignableToRestriction("interface-only");
		}

		public boolean supports(Node n, Node attribute) {
			return "http://www.osgi.org/xmlns/blueprint/v1.0.0".equals(n.getNamespaceURI())
					&& "interface".equals(attribute.getLocalName());
		}

		public void validate(Node n, Node attribute, IXmlValidationContext context) {
			validateClassName(n, attribute, context, annotationData);
		}
	}

	/**
	 * Simply validation for:
	 * 
	 * <pre>
	 * 	&lt;bp:reference depends-on=&quot;ref&quot;
	 * </pre>
	 */
	private class BlueprintDependesOnAttributeValidator implements IAttributeValidator {

		public boolean supports(Node n, Node attribute) {
			return "http://www.osgi.org/xmlns/blueprint/v1.0.0".equals(n.getNamespaceURI())
					&& "depends-on".equals(attribute.getLocalName());
		}

		public void validate(Node n, Node attribute, IXmlValidationContext context) {
			validateBeanReference(n, attribute, context);
		}
	}

}
