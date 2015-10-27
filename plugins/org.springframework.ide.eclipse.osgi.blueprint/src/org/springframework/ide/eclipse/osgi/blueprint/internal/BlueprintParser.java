/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.springframework.ide.eclipse.osgi.blueprint.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gemini.blueprint.blueprint.config.internal.BlueprintDefaultsDefinition;
import org.eclipse.gemini.blueprint.blueprint.config.internal.ParsingUtils;
import org.eclipse.gemini.blueprint.blueprint.config.internal.support.InstanceEqualityRuntimeBeanReference;
import org.eclipse.gemini.blueprint.blueprint.reflect.internal.support.OrderedManagedProperties;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.parsing.BeanEntry;
import org.springframework.beans.factory.parsing.ConstructorArgumentEntry;
import org.springframework.beans.factory.parsing.ParseState;
import org.springframework.beans.factory.parsing.PropertyEntry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.ManagedArray;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Stateful class that handles the parsing details of a &lt;component&gt; elements. Borrows heavily from
 * {@link BeanDefinitionParserDelegate}.
 * 
 * <b>Note</b>: Due to its stateful nature, this class is not thread safe.
 * 
 * <b>Note</b>: Since the namespace is important when parsing elements and since mixed elements, from both rfc124 and
 * Spring can coexist in the same file, reusing the {@link BeanDefinitionParserDelegate delegate} isn't entirely
 * possible since the two state needs to be kept in synch.
 * 
 * @author Costin Leau
 */
public class BlueprintParser {

	/** logger */
	private static final Log log = LogFactory.getLog(BlueprintParser.class);

	public static final String BEAN = "bean";
	public static final String COMPONENT_ID_ATTR = "component-id";
	public static final String CONSTRUCTOR_ARG = "argument";
	private static final String FACTORY_REF_ATTR = "factory-ref";
	private static final String LAZY_INIT_ATTR = "activation";
	private static final String LAZY_INIT_VALUE = "lazy";
	private static final String EAGER_INIT_VALUE = "eager";

	public static final String NAMESPACE_URI = "http://www.osgi.org/xmlns/blueprint/v1.0.0";
	public static final String DECLARED_SCOPE = "org.eclipse.gemini.blueprint.blueprint.xml.bean.declared.scope";

	private final ParseState parseState;

	private final Collection<String> usedNames;

	private ParserContext parserContext;
	private BlueprintDefaultsDefinition defaults;

	public BlueprintParser() {
		this(null, null);
	}

	/**
	 * Constructs a new <code>ComponentParser</code> instance. Used by certain reusable static methods.
	 * 
	 * @param parserContext
	 */
	private BlueprintParser(ParserContext parserContext) {
		this(null, null);
		this.parserContext = parserContext;
	}

	public BlueprintParser(ParseState parseState, Collection<String> usedNames) {
		this.parseState = (parseState != null ? parseState : new ParseState());
		this.usedNames = (usedNames != null ? usedNames : new LinkedHashSet<String>());
	}

	public BeanDefinitionHolder parseAsHolder(Element componentElement, ParserContext parserContext) {
		// save parser context
		this.parserContext = parserContext;
		this.defaults = new BlueprintDefaultsDefinition(componentElement.getOwnerDocument(), parserContext);

		// let Spring do its standard parsing
		BeanDefinitionHolder bdHolder = parseComponentDefinitionElement(componentElement, null);

		return bdHolder;
	}

	public BeanDefinition parse(Element componentElement, ParserContext parserContext) {
		return parseAsHolder(componentElement, parserContext).getBeanDefinition();
	}

	/**
	 * Parses the supplied <code>&lt;bean&gt;</code> element. May return <code>null</code> if there were errors during
	 * parse. Errors are reported to the {@link org.springframework.beans.factory.parsing.ProblemReporter}.
	 */
	private BeanDefinitionHolder parseComponentDefinitionElement(Element ele, BeanDefinition containingBean) {

		// extract bean name
		String id = ele.getAttribute(BeanDefinitionParserDelegate.ID_ATTRIBUTE);
		String nameAttr = ele.getAttribute(BeanDefinitionParserDelegate.NAME_ATTRIBUTE);

		List<String> aliases = new ArrayList<String>(4);
		if (StringUtils.hasLength(nameAttr)) {
			String[] nameArr =
					StringUtils.tokenizeToStringArray(nameAttr, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
			aliases.addAll(Arrays.asList(nameArr));
		}

		String beanName = id;

		if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
			beanName = (String) aliases.remove(0);
			if (log.isDebugEnabled()) {
				log.debug("No XML 'id' specified - using '" + beanName + "' as bean name and " + aliases
						+ " as aliases");
			}
		}

		if (containingBean == null) {

			if (checkNameUniqueness(beanName, aliases, usedNames)) {
				error("Bean name '" + beanName + "' is already used in this file", ele);
			}

			if (ParsingUtils.isReservedName(beanName, ele, parserContext)) {
				error("Blueprint reserved name '" + beanName + "' cannot be used", ele);
			}
		}

		AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
		if (beanDefinition != null) {
			if (!StringUtils.hasText(beanName)) {
				try {
					if (containingBean != null) {
						beanName =
								ParsingUtils.generateBlueprintBeanName(beanDefinition, parserContext.getRegistry(),
										true);
					} else {
						beanName =
								ParsingUtils.generateBlueprintBeanName(beanDefinition, parserContext.getRegistry(),
										false);
						// TODO: should we support 2.0 behaviour (see below):
						// 
						// Register an alias for the plain bean class name, if still possible,
						// if the generator returned the class name plus a suffix.
						// This is expected for Spring 1.2/2.0 backwards compatibility.
					}
					if (log.isDebugEnabled()) {
						log.debug("Neither XML 'id' nor 'name' specified - " + "using generated bean name [" + beanName
								+ "]");
					}
				} catch (Exception ex) {
					error(ex.getMessage(), ele, ex);
					return null;
				}
			}
			return new BeanDefinitionHolder(beanDefinition, beanName);
		}

		return null;
	}

	/**
	 * Parse the bean definition itself, without regard to name or aliases. May return <code>null</code> if problems
	 * occurred during the parse of the bean definition.
	 */
	private AbstractBeanDefinition parseBeanDefinitionElement(Element ele, String beanName,
			BeanDefinition containingBean) {

		this.parseState.push(new BeanEntry(beanName));

		String className = null;
		if (ele.hasAttribute(BeanDefinitionParserDelegate.CLASS_ATTRIBUTE)) {
			className = ele.getAttribute(BeanDefinitionParserDelegate.CLASS_ATTRIBUTE).trim();
		}

		try {
			AbstractBeanDefinition beanDefinition =
					BeanDefinitionReaderUtils.createBeanDefinition(null, className, parserContext.getReaderContext()
							.getBeanClassLoader());

			// some early validation
			String activation = ele.getAttribute(LAZY_INIT_ATTR);
			String scope = ele.getAttribute(BeanDefinitionParserDelegate.SCOPE_ATTRIBUTE);

			if (EAGER_INIT_VALUE.equals(activation) && BeanDefinition.SCOPE_PROTOTYPE.equals(scope)) {
				error("Prototype beans cannot be eagerly activated", ele);
			}

			// add marker to indicate that the scope was present
			if (StringUtils.hasText(scope)) {
				beanDefinition.setAttribute(DECLARED_SCOPE, Boolean.TRUE);
			}

			// parse attributes
			parseAttributes(ele, beanName, beanDefinition);

			// inner beans get a predefined scope in RFC 124
			if (containingBean != null) {
				beanDefinition.setLazyInit(true);
				beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
			}

			// parse description
			beanDefinition.setDescription(DomUtils.getChildElementValueByTagName(ele,
					BeanDefinitionParserDelegate.DESCRIPTION_ELEMENT));

			parseConstructorArgElements(ele, beanDefinition);
			parsePropertyElements(ele, beanDefinition);

			beanDefinition.setResource(parserContext.getReaderContext().getResource());
			beanDefinition.setSource(extractSource(ele));

			return beanDefinition;
		} catch (ClassNotFoundException ex) {
			error("Bean class [" + className + "] not found", ele, ex);
		} catch (NoClassDefFoundError err) {
			error("Class that bean class [" + className + "] depends on not found", ele, err);
		} catch (Throwable ex) {
			error("Unexpected failure during bean definition parsing", ele, ex);
		} finally {
			this.parseState.pop();
		}

		return null;
	}

	private AbstractBeanDefinition parseAttributes(Element ele, String beanName, AbstractBeanDefinition beanDefinition) {
		AbstractBeanDefinition bd =
				parserContext.getDelegate().parseBeanDefinitionAttributes(ele, beanName, null, beanDefinition);

		// handle lazy flag (initialize)
		String lazyInit = ele.getAttribute(LAZY_INIT_ATTR);
		// check whether the value is "lazy"
		if (StringUtils.hasText(lazyInit)) {
			if (lazyInit.equalsIgnoreCase(LAZY_INIT_VALUE)) {
				bd.setLazyInit(true);
			} else {
				bd.setLazyInit(false);
			}
		} else {
			bd.setLazyInit(getDefaults(ele).getDefaultInitialization());
		}

		// handle factory component
		String componentFactory = ele.getAttribute(FACTORY_REF_ATTR);
		if (StringUtils.hasText(componentFactory)) {
			bd.setFactoryBeanName(componentFactory);
		}

		// check whether the bean is a prototype with destroy method
		if (StringUtils.hasText(bd.getDestroyMethodName())
				&& BeanDefinition.SCOPE_PROTOTYPE.equalsIgnoreCase(bd.getScope())) {
			error("Blueprint prototype beans cannot define destroy methods", ele);
		}

		return bd;
	}

	/**
	 * Validate that the specified bean name and aliases have not been used already.
	 */
	private boolean checkNameUniqueness(String beanName, Collection<String> aliases, Collection<String> usedNames) {
		String foundName = null;

		if (StringUtils.hasText(beanName) && usedNames.contains(beanName)) {
			foundName = beanName;
		}
		if (foundName == null) {
			foundName = (String) CollectionUtils.findFirstMatch(usedNames, aliases);
		}

		usedNames.add(beanName);
		usedNames.addAll(aliases);

		return (foundName != null);
	}

	/**
	 * Parsers contructor arguments.
	 * 
	 * @param ele
	 * @param beanDefinition
	 * @param parserContext
	 */
	private void parseConstructorArgElements(Element ele, AbstractBeanDefinition beanDefinition) {

		NodeList nl = ele.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, CONSTRUCTOR_ARG)) {
				parseConstructorArgElement((Element) node, beanDefinition);
			}
		}
	}

	private void parseConstructorArgElement(Element ele, AbstractBeanDefinition beanDefinition) {

		String indexAttr = ele.getAttribute(BeanDefinitionParserDelegate.INDEX_ATTRIBUTE);
		String typeAttr = ele.getAttribute(BeanDefinitionParserDelegate.TYPE_ATTRIBUTE);

		boolean hasIndex = false;
		int index = -1;

		if (StringUtils.hasLength(indexAttr)) {
			hasIndex = true;
			try {
				index = Integer.parseInt(indexAttr);
			} catch (NumberFormatException ex) {
				error("Attribute 'index' of tag 'constructor-arg' must be an integer", ele);
			}

			if (index < 0) {
				error("'index' cannot be lower than 0", ele);
			}
		}

		try {
			this.parseState.push(hasIndex ? new ConstructorArgumentEntry(index) : new ConstructorArgumentEntry());

			ConstructorArgumentValues values = beanDefinition.getConstructorArgumentValues();
			// Blueprint failure (index duplication)
			Integer indexInt = Integer.valueOf(index);
			if (values.getIndexedArgumentValues().containsKey(indexInt)) {
				error("duplicate 'index' with value=[" + index + "] specified", ele);
			}

			Object value = parsePropertyValue(ele, beanDefinition, null);
			ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
			if (StringUtils.hasLength(typeAttr)) {
				valueHolder.setType(typeAttr);
			}
			valueHolder.setSource(extractSource(ele));

			if (hasIndex) {
				values.addIndexedArgumentValue(index, valueHolder);
			} else {
				values.addGenericArgumentValue(valueHolder);
			}
			// Blueprint failure (mixed index/non-indexed arguments)
			if (!values.getGenericArgumentValues().isEmpty() && !values.getIndexedArgumentValues().isEmpty()) {
				error("indexed and non-indexed constructor arguments are not supported by Blueprint; "
						+ "consider using the Spring namespace instead", ele);
			}
		} finally {
			this.parseState.pop();
		}
	}

	/**
	 * Parses property elements.
	 * 
	 * @param ele
	 * @param beanDefinition
	 * @param parserContext
	 */
	private void parsePropertyElements(Element ele, AbstractBeanDefinition beanDefinition) {

		NodeList nl = ele.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, BeanDefinitionParserDelegate.PROPERTY_ELEMENT)) {
				parsePropertyElement((Element) node, beanDefinition);
			}
		}
	}

	private void parsePropertyElement(Element ele, BeanDefinition bd) {
		String propertyName = ele.getAttribute(BeanDefinitionParserDelegate.NAME_ATTRIBUTE);
		if (!StringUtils.hasLength(propertyName)) {
			error("Tag 'property' must have a 'name' attribute", ele);
			return;
		}
		this.parseState.push(new PropertyEntry(propertyName));
		try {
			if (bd.getPropertyValues().contains(propertyName)) {
				error("Multiple 'property' definitions for property '" + propertyName + "'", ele);
				return;
			}
			Object val = parsePropertyValue(ele, bd, propertyName);
			PropertyValue pv = new PropertyValue(propertyName, val);
			pv.setSource(parserContext.extractSource(ele));
			bd.getPropertyValues().addPropertyValue(pv);
		} finally {
			this.parseState.pop();
		}
	}

	private Object parsePropertyValue(Element ele, BeanDefinition bd, String propertyName) {
		String elementName =
				(propertyName != null) ? "<property> element for property '" + propertyName + "'"
						: "<constructor-arg> element";

		// Should only have one child element: ref, value, list, etc.
		NodeList nl = ele.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element
					&& !DomUtils.nodeNameEquals(node, BeanDefinitionParserDelegate.DESCRIPTION_ELEMENT)) {
				// Child element is what we're looking for.
				if (subElement != null) {
					error(elementName + " must not contain more than one sub-element", ele);
				} else {
					subElement = (Element) node;
				}
			}
		}

		boolean hasRefAttribute = ele.hasAttribute(BeanDefinitionParserDelegate.REF_ATTRIBUTE);
		boolean hasValueAttribute = ele.hasAttribute(BeanDefinitionParserDelegate.VALUE_ATTRIBUTE);
		if ((hasRefAttribute && hasValueAttribute) || ((hasRefAttribute || hasValueAttribute) && subElement != null)) {
			error(elementName
					+ " is only allowed to contain either 'ref' attribute OR 'value' attribute OR sub-element", ele);
		}

		if (hasRefAttribute) {
			String refName = ele.getAttribute(BeanDefinitionParserDelegate.REF_ATTRIBUTE);
			if (!StringUtils.hasText(refName)) {
				error(elementName + " contains empty 'ref' attribute", ele);
			}
			RuntimeBeanReference ref = new RuntimeBeanReference(refName);
			ref.setSource(parserContext.extractSource(ele));
			return ref;
		} else if (hasValueAttribute) {
			TypedStringValue valueHolder =
					new TypedStringValue(ele.getAttribute(BeanDefinitionParserDelegate.VALUE_ATTRIBUTE));
			valueHolder.setSource(parserContext.extractSource(ele));
			return valueHolder;
		} else if (subElement != null) {
			return parsePropertySubElement(subElement, bd, null);
		} else {
			// Neither child element nor "ref" or "value" attribute found.
			error(elementName + " must specify a ref or value", ele);
			return null;
		}
	}

	public static Object parsePropertySubElement(ParserContext parserContext, Element ele, BeanDefinition bd) {
		return new BlueprintParser(parserContext).parsePropertySubElement(ele, bd, null);
	}

	public static Map<?, ?> parsePropertyMapElement(ParserContext parserContext, Element ele, BeanDefinition bd) {
		return new BlueprintParser(parserContext).parseMapElement(ele, bd);
	}

	public static Set<?> parsePropertySetElement(ParserContext parserContext, Element ele, BeanDefinition bd) {
		return new BlueprintParser(parserContext).parseSetElement(ele, bd);
	}

	/**
	 * Parse a value, ref or collection sub-element of a property or constructor-arg element. This method is called from
	 * several places to handle reusable elements such as idref, ref, null, value and so on.
	 * 
	 * In fact, this method is the main reason why the BeanDefinitionParserDelegate is not used in full since the
	 * element namespace becomes important as mixed rfc124/bean content can coexist.
	 * 
	 * @param ele subelement of property element; we don't know which yet
	 * @param defaultValueType the default type (class name) for any <code>&lt;value&gt;</code> tag that might be
	 * created
	 */
	private Object parsePropertySubElement(Element ele, BeanDefinition bd, String defaultValueType) {
		// skip other namespace
		String namespaceUri = ele.getNamespaceURI();

		// check Spring own namespace
		if (parserContext.getDelegate().isDefaultNamespace(namespaceUri)) {
			return parserContext.getDelegate().parsePropertySubElement(ele, bd);
		}
		// let the delegate handle other ns
		else if (!NAMESPACE_URI.equals(namespaceUri)) {
			return parserContext.getDelegate().parseCustomElement(ele);
		}

		// 
		else {
			if (DomUtils.nodeNameEquals(ele, BEAN)) {
				BeanDefinitionHolder bdHolder = parseComponentDefinitionElement(ele, bd);
				if (bdHolder != null) {
					bdHolder = ParsingUtils.decorateBeanDefinitionIfRequired(ele, bdHolder, parserContext);
				}
				return bdHolder;
			}

			if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.REF_ELEMENT)) {
				return parseRefElement(ele);
			} else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.IDREF_ELEMENT)) {
				return parseIdRefElement(ele);
			} else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.VALUE_ELEMENT)) {
				return parseValueElement(ele, defaultValueType);
			} else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.NULL_ELEMENT)) {
				// It's a distinguished null value. Let's wrap it in a TypedStringValue
				// object in order to preserve the source location.
				TypedStringValue nullHolder = new TypedStringValue(null);
				nullHolder.setSource(parserContext.extractSource(ele));
				return nullHolder;
			} else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.ARRAY_ELEMENT)) {
				return parseArrayElement(ele, bd);
			} else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.LIST_ELEMENT)) {
				return parseListElement(ele, bd);
			} else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.SET_ELEMENT)) {
				return parseSetElement(ele, bd);
			} else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.MAP_ELEMENT)) {
				return parseMapElement(ele, bd);
			} else if (DomUtils.nodeNameEquals(ele, BeanDefinitionParserDelegate.PROPS_ELEMENT)) {
				return parsePropsElement(ele);
			}

			// maybe it's a nested service/reference/ref-list/ref-set
			return parserContext.getDelegate().parseCustomElement(ele, bd);
		}
	}

	private Object parseRefElement(Element ele) {
		// A generic reference to any name of any component.
		String refName = ele.getAttribute(COMPONENT_ID_ATTR);
		if (!StringUtils.hasLength(refName)) {
			error("'" + COMPONENT_ID_ATTR + "' is required for <ref> element", ele);
			return null;
		}

		if (!StringUtils.hasText(refName)) {
			error("<ref> element contains empty target attribute", ele);
			return null;
		}
		RuntimeBeanReference ref = new InstanceEqualityRuntimeBeanReference(refName);
		ref.setSource(parserContext.extractSource(ele));
		return ref;
	}

	private Object parseIdRefElement(Element ele) {
		// A generic reference to any name of any bean/component.
		String refName = ele.getAttribute(COMPONENT_ID_ATTR);
		if (!StringUtils.hasLength(refName)) {
			error("'" + COMPONENT_ID_ATTR + "' is required for <idref> element", ele);
			return null;
		}
		if (!StringUtils.hasText(refName)) {
			error("<idref> element contains empty target attribute", ele);
			return null;
		}
		RuntimeBeanNameReference ref = new RuntimeBeanNameReference(refName);
		ref.setSource(parserContext.extractSource(ele));
		return ref;
	}

	/**
	 * Return a typed String value Object for the given value element.
	 * 
	 * @param ele element
	 * @param defaultTypeName type class name
	 * @return typed String value Object
	 */
	private Object parseValueElement(Element ele, String defaultTypeName) {
		// It's a literal value.
		String value = DomUtils.getTextValue(ele);
		String specifiedTypeName = ele.getAttribute(BeanDefinitionParserDelegate.TYPE_ATTRIBUTE);
		String typeName = specifiedTypeName;
		if (!StringUtils.hasText(typeName)) {
			typeName = defaultTypeName;
		}
		try {
			TypedStringValue typedValue = buildTypedStringValue(value, typeName);
			typedValue.setSource(extractSource(ele));
			typedValue.setSpecifiedTypeName(specifiedTypeName);
			return typedValue;
		} catch (ClassNotFoundException ex) {
			error("Type class [" + typeName + "] not found for <value> element", ele, ex);
			return value;
		}
	}

	/**
	 * Build a typed String value Object for the given raw value.
	 * 
	 * @see org.springframework.beans.factory.config.TypedStringValue
	 */
	private TypedStringValue buildTypedStringValue(String value, String targetTypeName) throws ClassNotFoundException {

		ClassLoader classLoader = parserContext.getReaderContext().getBeanClassLoader();
		TypedStringValue typedValue;
		if (!StringUtils.hasText(targetTypeName)) {
			typedValue = new TypedStringValue(value);
		} else if (classLoader != null) {
			Class<?> targetType = ClassUtils.forName(targetTypeName, classLoader);
			typedValue = new TypedStringValue(value, targetType);
		} else {
			typedValue = new TypedStringValue(value, targetTypeName);
		}
		return typedValue;
	}

	/**
	 * Parse an array element.
	 */
	public Object parseArrayElement(Element arrayEle, BeanDefinition bd) {
		String elementType = arrayEle.getAttribute(BeanDefinitionParserDelegate.VALUE_TYPE_ATTRIBUTE);
		NodeList nl = arrayEle.getChildNodes();
		ManagedArray target = new ManagedArray(elementType, nl.getLength());
		target.setSource(extractSource(arrayEle));
		target.setElementTypeName(elementType);
		target.setMergeEnabled(parseMergeAttribute(arrayEle));
		parseCollectionElements(nl, target, bd, elementType);
		return target;
	}

	/**
	 * Parse a list element.
	 */
	public List<?> parseListElement(Element collectionEle, BeanDefinition bd) {
		String defaultElementType = collectionEle.getAttribute(BeanDefinitionParserDelegate.VALUE_TYPE_ATTRIBUTE);
		NodeList nl = collectionEle.getChildNodes();
		ManagedList<Object> target = new ManagedList<Object>(nl.getLength());
		target.setSource(extractSource(collectionEle));
		target.setElementTypeName(defaultElementType);
		target.setMergeEnabled(parseMergeAttribute(collectionEle));
		parseCollectionElements(nl, target, bd, defaultElementType);
		return target;
	}

	/**
	 * Parse a set element.
	 */
	public Set<?> parseSetElement(Element collectionEle, BeanDefinition bd) {
		String defaultElementType = collectionEle.getAttribute(BeanDefinitionParserDelegate.VALUE_TYPE_ATTRIBUTE);
		NodeList nl = collectionEle.getChildNodes();
		ManagedSet<Object> target = new ManagedSet<Object>(nl.getLength());
		target.setSource(extractSource(collectionEle));
		target.setElementTypeName(defaultElementType);
		target.setMergeEnabled(parseMergeAttribute(collectionEle));
		parseCollectionElements(nl, target, bd, defaultElementType);
		return target;
	}

	protected void parseCollectionElements(NodeList elementNodes, Collection<Object> target, BeanDefinition bd,
			String defaultElementType) {

		for (int i = 0; i < elementNodes.getLength(); i++) {
			Node node = elementNodes.item(i);
			if (node instanceof Element
					&& !DomUtils.nodeNameEquals(node, BeanDefinitionParserDelegate.DESCRIPTION_ELEMENT)) {
				target.add(parsePropertySubElement((Element) node, bd, defaultElementType));
			}
		}
	}

	/**
	 * Parse a map element.
	 */
	public Map<?, ?> parseMapElement(Element mapEle, BeanDefinition bd) {
		String defaultKeyType = mapEle.getAttribute(BeanDefinitionParserDelegate.KEY_TYPE_ATTRIBUTE);
		String defaultValueType = mapEle.getAttribute(BeanDefinitionParserDelegate.VALUE_TYPE_ATTRIBUTE);

		List<Element> entryEles =
				DomUtils.getChildElementsByTagName(mapEle, BeanDefinitionParserDelegate.ENTRY_ELEMENT);
		ManagedMap<Object, Object> map = new ManagedMap<Object, Object>(entryEles.size());
		map.setSource(extractSource(mapEle));
		map.setKeyTypeName(defaultKeyType);
		map.setValueTypeName(defaultValueType);
		map.setMergeEnabled(parseMergeAttribute(mapEle));

		for (Element entryEle : entryEles) {
			// Should only have one value child element: ref, value, list, etc.
			// Optionally, there might be a key child element.
			NodeList entrySubNodes = entryEle.getChildNodes();
			Element keyEle = null;
			Element valueEle = null;
			for (int j = 0; j < entrySubNodes.getLength(); j++) {
				Node node = entrySubNodes.item(j);
				if (node instanceof Element) {
					Element candidateEle = (Element) node;
					if (DomUtils.nodeNameEquals(candidateEle, BeanDefinitionParserDelegate.KEY_ELEMENT)) {
						if (keyEle != null) {
							error("<entry> element is only allowed to contain one <key> sub-element", entryEle);
						} else {
							keyEle = candidateEle;
						}
					} else {
						// Child element is what we're looking for.
						if (valueEle != null) {
							error("<entry> element must not contain more than one value sub-element", entryEle);
						} else {
							valueEle = candidateEle;
						}
					}
				}
			}

			// Extract key from attribute or sub-element.
			Object key = null;
			boolean hasKeyAttribute = entryEle.hasAttribute(BeanDefinitionParserDelegate.KEY_ATTRIBUTE);
			boolean hasKeyRefAttribute = entryEle.hasAttribute(BeanDefinitionParserDelegate.KEY_REF_ATTRIBUTE);
			if ((hasKeyAttribute && hasKeyRefAttribute) || ((hasKeyAttribute || hasKeyRefAttribute)) && keyEle != null) {
				error("<entry> element is only allowed to contain either "
						+ "a 'key' attribute OR a 'key-ref' attribute OR a <key> sub-element", entryEle);
			}
			if (hasKeyAttribute) {
				key =
						buildTypedStringValueForMap(entryEle.getAttribute(BeanDefinitionParserDelegate.KEY_ATTRIBUTE),
								defaultKeyType, entryEle);
			} else if (hasKeyRefAttribute) {
				String refName = entryEle.getAttribute(BeanDefinitionParserDelegate.KEY_REF_ATTRIBUTE);
				if (!StringUtils.hasText(refName)) {
					error("<entry> element contains empty 'key-ref' attribute", entryEle);
				}
				RuntimeBeanReference ref = new RuntimeBeanReference(refName);
				ref.setSource(extractSource(entryEle));
				key = ref;
			} else if (keyEle != null) {
				key = parseKeyElement(keyEle, bd, defaultKeyType);
			} else {
				error("<entry> element must specify a key", entryEle);
			}

			// Extract value from attribute or sub-element.
			Object value = null;
			boolean hasValueAttribute = entryEle.hasAttribute(BeanDefinitionParserDelegate.VALUE_ATTRIBUTE);
			boolean hasValueRefAttribute = entryEle.hasAttribute(BeanDefinitionParserDelegate.VALUE_REF_ATTRIBUTE);
			if ((hasValueAttribute && hasValueRefAttribute) || ((hasValueAttribute || hasValueRefAttribute))
					&& valueEle != null) {
				error("<entry> element is only allowed to contain either "
						+ "'value' attribute OR 'value-ref' attribute OR <value> sub-element", entryEle);
			}
			if (hasValueAttribute) {
				value =
						buildTypedStringValueForMap(
								entryEle.getAttribute(BeanDefinitionParserDelegate.VALUE_ATTRIBUTE), defaultValueType,
								entryEle);
			} else if (hasValueRefAttribute) {
				String refName = entryEle.getAttribute(BeanDefinitionParserDelegate.VALUE_REF_ATTRIBUTE);
				if (!StringUtils.hasText(refName)) {
					error("<entry> element contains empty 'value-ref' attribute", entryEle);
				}
				RuntimeBeanReference ref = new RuntimeBeanReference(refName);
				ref.setSource(extractSource(entryEle));
				value = ref;
			} else if (valueEle != null) {
				value = parsePropertySubElement(valueEle, bd, defaultValueType);
			} else {
				error("<entry> element must specify a value", entryEle);
			}

			// Add final key and value to the Map.
			map.put(key, value);
		}

		return map;
	}

	/**
	 * Parse a props element.
	 */
	public Properties parsePropsElement(Element propsEle) {
		ManagedProperties props = new OrderedManagedProperties();
		props.setSource(extractSource(propsEle));
		props.setMergeEnabled(parseMergeAttribute(propsEle));

		List propEles = DomUtils.getChildElementsByTagName(propsEle, BeanDefinitionParserDelegate.PROP_ELEMENT);
		for (Iterator it = propEles.iterator(); it.hasNext();) {
			Element propEle = (Element) it.next();
			String key = propEle.getAttribute(BeanDefinitionParserDelegate.KEY_ATTRIBUTE);
			// Trim the text value to avoid unwanted whitespace
			// caused by typical XML formatting.
			String value = DomUtils.getTextValue(propEle).trim();

			TypedStringValue keyHolder = new TypedStringValue(key);
			keyHolder.setSource(extractSource(propEle));
			TypedStringValue valueHolder = new TypedStringValue(value);
			valueHolder.setSource(extractSource(propEle));
			props.put(keyHolder, valueHolder);
		}

		return props;
	}

	private boolean parseMergeAttribute(Element element) {
		return parserContext.getDelegate().parseMergeAttribute(element);
	}

	/**
	 * Build a typed String value Object for the given raw value.
	 * 
	 * @see org.springframework.beans.factory.config.TypedStringValue
	 */
	private Object buildTypedStringValueForMap(String value, String defaultTypeName, Element entryEle) {
		try {
			TypedStringValue typedValue = buildTypedStringValue(value, defaultTypeName);
			typedValue.setSource(extractSource(entryEle));
			return typedValue;
		} catch (ClassNotFoundException ex) {
			error("Type class [" + defaultTypeName + "] not found for Map key/value type", entryEle, ex);
			return value;
		}
	}

	/**
	 * Parse a key sub-element of a map element.
	 */
	private Object parseKeyElement(Element keyEle, BeanDefinition bd, String defaultKeyTypeName) {
		NodeList nl = keyEle.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				// Child element is what we're looking for.
				if (subElement != null) {
					error("<key> element must not contain more than one value sub-element", keyEle);
				} else {
					subElement = (Element) node;
				}
			}
		}
		return parsePropertySubElement(subElement, bd, defaultKeyTypeName);
	}

	// util methods (used as shortcuts)
	private Object extractSource(Element ele) {
		return parserContext.extractSource(ele);
	}

	/**
	 * Reports an error with the given message for the given source element.
	 */
	private void error(String message, Node source) {
		parserContext.getReaderContext().error(message, source, parseState.snapshot());
	}

	/**
	 * Reports an error with the given message for the given source element.
	 */
	private void error(String message, Node source, Throwable cause) {
		parserContext.getReaderContext().error(message, source, parseState.snapshot(), cause);
	}

	private BlueprintDefaultsDefinition getDefaults(Element ele) {
		if (defaults == null) {
			defaults = new BlueprintDefaultsDefinition(ele.getOwnerDocument(), parserContext);
		}
		return defaults;
	}
}