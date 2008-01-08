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
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
import org.eclipse.wst.xml.core.internal.contentmodel.modelquery.ModelQuery;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.modelquery.ModelQueryUtil;
import org.eclipse.wst.xsd.contentmodel.internal.XSDImpl.XSDElementDeclarationAdapter;
import org.eclipse.xsd.impl.XSDElementDeclarationImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link NamespaceHandler} that creates {@link BeanDefinition}s based on
 * Spring's tool namespace.
 * <p>
 * The following annotation is currently supported:
 * 
 * <pre>
 * &lt;xsd:element name=&quot;foobar&quot;&gt;
 *     &lt;xsd:annotation&gt;
 *         &lt;xsd:appinfo&gt;
 *             &lt;tool:annotation&gt;
 *                 &lt;tool:exports type=&quot;com.foo.Bar&quot;/&gt;
 *             &lt;/tool:annotation&gt;
 *         &lt;/xsd:appinfo&gt;
 *     &lt;/xsd:annotation&gt;
 *     ...
 *  &lt;/xsd:element&gt;
 * </pre>
 * 
 * Using the above XSD definition in the following form
 * 
 * <pre>
 * &lt;foobar id=&quot;foobeanchen&quot; /&gt;
 * </pre>
 * 
 * would create a BeanDefinition with bean class <code>com.foo.Bar</code> and
 * id <code>foobeanchen</code>.
 * 
 * @author Christian Dupuis
 * @since 2.0.3
 */
@SuppressWarnings("restriction")
public class ToolAnnotationBasedNamespaceHandler implements NamespaceHandler {

	private static final String DEFAULT_ID_XPATH = "@id";

	private static final String TYPE_ATTRIBUTE = "type";

	private static final String IDENTIFIER_ATTRIBUTE = "identifier";

	private static final String EXPORTS_ELEMENT = "exports";

	private static final String ANNOTATION_ELEMENT = "annotation";

	private static final String TOOL_NAMESPACE_URI = 
		"http://www.springframework.org/schema/tool";

	private final IBeansConfig beansConfig;

	
	/**
	 * Constructor taking a {@link IBeansConfig}.
	 */
	public ToolAnnotationBasedNamespaceHandler(IBeansConfig file) {
		this.beansConfig = file;
	}

	public BeanDefinitionHolder decorate(Node source,
			BeanDefinitionHolder definition, ParserContext parserContext) {
		// Don't do anything; there are no decoration annotations defined.
		return definition;
	}

	public void init() {
		// Don't do anything.
	}

	/**
	 * Entry point for the parsing a given element.
	 * <p>
	 * This implementation simply delegates to
	 * {@link #parseElement(Node, ParserContext)} and registers the returned
	 * {@link ComponentDefinition} with the given {@link ParserContext}.
	 * <p>
	 * Note: this implementation always returns <code>null</code>.
	 */
	public BeanDefinition parse(Element element, ParserContext parserContext) {

		// Get a component definition for the given element.
		ComponentDefinition componentDefinition = parseElement(element,
				parserContext);

		if (componentDefinition instanceof BeanComponentDefinition) {
			parserContext
					.registerBeanComponent((BeanComponentDefinition) componentDefinition);
		}
		else if (componentDefinition != null) {
			parserContext.registerComponent(componentDefinition);
		}
		else {
			// emit a warning that the NamespaceHandler cannot be found
			parserContext.getReaderContext().warning(
					"Unable to locate Spring NamespaceHandler for XML schema namespace ["
							+ element.getNamespaceURI() + "]",
					parserContext.extractSource(element.getParentNode()));
		}

		return null;
	}

	/**
	 * Parses the given element and looks for a tool:annotation -> tool:export
	 * definition on the corresponding XSD definition.
	 * <p>
	 * First tries to get a {@link ComponentDefinition} from the element itself.
	 * After that the child nodes of the given elements are checked for tool
	 * meta data annotations.
	 * <p>
	 * If there are annotations on child elements a
	 * {@link CompositeComponentDefinition} will be created carrying the nested
	 * {@link ComponentDefinition}s.
	 * @param element the element to parse
	 * @param parserContext the current parser context
	 * @return a {@link ComponentDefinition} created from the given element
	 */
	private ComponentDefinition parseElement(Node element,
			ParserContext parserContext) {

		// Get - if any - the component for the given element.
		ComponentDefinition rootComponent = parseSingleElement(element,
				parserContext);

		List<ComponentDefinition> nestedComponents = new ArrayList<ComponentDefinition>();
		NodeList nestedElements = element.getChildNodes();
		for (int i = 0; i < nestedElements.getLength(); i++) {
			Node nestedElement = nestedElements.item(i);
			if (nestedElement.getNodeType() == Node.ELEMENT_NODE) {
				ComponentDefinition nestedComponent = parseElement(
						nestedElement, parserContext);
				if (nestedComponent != null) {
					nestedComponents.add(nestedComponent);
				}
			}
		}

		if (nestedComponents.size() > 0) {
			CompositeComponentDefinition compositeComponentDefinition = new CompositeComponentDefinition(
					element.getNodeName(), parserContext.extractSource(element));
			for (ComponentDefinition componentDefinition : nestedComponents) {
				compositeComponentDefinition
						.addNestedComponent(componentDefinition);
			}

			// TODO CD implement custom CompositeComponentDefinition to carry
			// along the rootComponent if any.
			return compositeComponentDefinition;
		}

		return rootComponent;
	}

	/**
	 * Parses a single element and looks for tool:annotations on the
	 * corresponding XSD definition.
	 * <p>
	 * The tool:annotation can either be on the type definition itself or on a
	 * referenced type definition. If a annotation is found the type itself it
	 * will overrule any other on the referenced type.
	 * @param element the element to parse
	 * @param parserContext the current parser context
	 * @return a {@link ComponentDefinition} created from the given element
	 */
	private ComponentDefinition parseSingleElement(Node element,
			ParserContext parserContext) {

		IStructuredModel model = null;
		try {
			// Load the StructedModel for the given file.
			model = getStructuredModel();
			if (model != null) {

				String localName = element.getLocalName();
				String namespaceUri = element.getNamespaceURI();

				// Get a list of nodes for given localName and namespace from
				// the loaded StructuredModel
				Document document = ((DOMModelImpl) model).getDocument();
				NodeList nodes = document.getElementsByTagNameNS(namespaceUri,
						localName);

				if (nodes.getLength() > 0) {

					// Only interested in one node as we need to get the
					// attached meta data and not the concrete element data
					Node node = nodes.item(0);

					// Look for meta data. The query returns null in case no XSD
					// can be found.
					ModelQuery modelQuery = ModelQueryUtil
							.getModelQuery(document);
					if (modelQuery != null) {

						// Get the element declaration from the model query.
						CMElementDeclaration result = modelQuery
								.getCMElementDeclaration((Element) node);

						// If the XSD is not found this check will fail.
						if (result instanceof XSDElementDeclarationAdapter) {
							XSDElementDeclarationImpl elementDeclaration = (XSDElementDeclarationImpl) ((XSDElementDeclarationAdapter) result)
									.getKey();

							ComponentDefinition componentDefinition = null;

							// 1. Get component definition for directly attached
							// annotations.
							if (elementDeclaration.getAnnotation() != null) {
								componentDefinition = processAnnotations(
										element, elementDeclaration
												.getAnnotation()
												.getApplicationInformation(),
										parserContext);
							}

							// 2. If no directly attached annotation could be
							// found, try the referenced type definition if any.
							if (componentDefinition == null
									&& elementDeclaration.getTypeDefinition() != null
									&& elementDeclaration.getTypeDefinition()
											.getAnnotation() != null) {
								componentDefinition = processAnnotations(
										element, elementDeclaration
												.getTypeDefinition()
												.getAnnotation()
												.getApplicationInformation(),
										parserContext);

							}

							return componentDefinition;
						}
					}
				}
			}
		}
		catch (IOException e) {
			// Don't do anything.
		}
		catch (CoreException e) {
			// Don't do anything.
		}
		finally {
			// Make sure to release the StructuredModel again.
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return null;
	}

	/**
	 * Parses a list of xsd:appinfo elements and looks for tool:annotations
	 * @param element the root element
	 * @param appInfos the appinfo elements found on the XSD definition for the
	 * element's type
	 * @param parserContext the current parser context
	 * @return a {@link ComponentDefinition} created from the given element
	 */
	private ComponentDefinition processAnnotations(Node element,
			List<Element> appInfos, ParserContext parserContext) {

		// Iterate the xsd:appinfo elements.
		for (Element elem : appInfos) {
			NodeList annotations = elem.getChildNodes();
			// Iterate children for tool:annotation elements.
			for (int j = 0; j < annotations.getLength(); j++) {
				Node toolAnnotationElement = annotations.item(j);
				if (toolAnnotationElement.getNodeType() == Node.ELEMENT_NODE
						&& ANNOTATION_ELEMENT.equals(toolAnnotationElement
								.getLocalName())
						&& TOOL_NAMESPACE_URI.equals(toolAnnotationElement
								.getNamespaceURI())) {
					NodeList specialToolAnnotationElements = toolAnnotationElement
							.getChildNodes();
					// Iterate children for tool:exports elements.
					for (int k = 0; k < specialToolAnnotationElements
							.getLength(); k++) {
						Node specialToolAnnotation = specialToolAnnotationElements
								.item(k);
						if (specialToolAnnotation.getNodeType() == Node.ELEMENT_NODE
								&& EXPORTS_ELEMENT.equals(specialToolAnnotation
										.getLocalName())) {

							return createBeanComponentDefinition(element,
									parserContext, specialToolAnnotation);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Creates a {@link ComponentDefinition} based on the attribute values of
	 * the tool annotation.
	 * @param element the element to create {@link ComponentDefinition} for
	 * @param parserContext the current parser context
	 * @param specialToolAnnotation the tool:exports annotation
	 * @return the finally created {@link ComponentDefinition}
	 */
	protected ComponentDefinition createBeanComponentDefinition(Node element,
			ParserContext parserContext, Node specialToolAnnotation) {
		NamedNodeMap attributes = specialToolAnnotation.getAttributes();
		String id = (attributes.getNamedItem(IDENTIFIER_ATTRIBUTE) != null ? attributes
				.getNamedItem(IDENTIFIER_ATTRIBUTE).getTextContent()
				: DEFAULT_ID_XPATH);
		String type = (attributes.getNamedItem(TYPE_ATTRIBUTE) != null ? attributes
				.getNamedItem(TYPE_ATTRIBUTE).getTextContent()
				: null);

		// Create the final BeanDefinition.
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClassName(type);
		beanDefinition.setSource(parserContext.extractSource(element));

		// Make sure that this BeanDefinition is treated as INFRASTRUCTURE
		// so that it will not be validated and can be filtered.
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

		return new BeanComponentDefinition(beanDefinition, getIdentifier(
				element, id, beanDefinition));
	}

	/**
	 * Gets the identifier for a {@link ComponentDefinition}.
	 * <p>
	 * First the given XPath expression will be evaluated against the given
	 * element. If this evaluation does not return any useful value, the
	 * implementation will fall back to generate an identifier based on
	 * {@link UniqueBeanNameGenerator#generateBeanName}.
	 * @param element the element to evaluate the given identifier expression
	 * against
	 * @param identifierExpression XPath expression to identify the identifier
	 * value
	 * @param beanDefinition a created BeanDefinition required for the
	 * {@link UniqueBeanNameGenerator}
	 * @return a string identifier for the given element
	 */
	protected String getIdentifier(Node element, String identifierExpression,
			BeanDefinition beanDefinition) {
		String identifier = null;
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath path = factory.newXPath();
			identifier = path.evaluate(identifierExpression, element);
		}
		catch (XPathExpressionException e) {
			// Safely ignore that here.
		}

		if (!StringUtils.hasText(identifier)) {
			identifier = UniqueBeanNameGenerator.generateBeanName(
					beanDefinition, beansConfig);
		}
		return identifier;
	}

	/**
	 * Returns the {@link IStructuredModel} for the {@link IBeansConfig}-backing
	 * IFile.
	 */
	private IStructuredModel getStructuredModel() throws IOException,
			CoreException {
		IStructuredModel model;
		model = StructuredModelManager.getModelManager()
				.getExistingModelForRead(
						(IFile) beansConfig.getElementResource());
		if (model == null) {
			model = StructuredModelManager.getModelManager().getModelForRead(
					(IFile) beansConfig.getElementResource());
		}
		return model;
	}

}
