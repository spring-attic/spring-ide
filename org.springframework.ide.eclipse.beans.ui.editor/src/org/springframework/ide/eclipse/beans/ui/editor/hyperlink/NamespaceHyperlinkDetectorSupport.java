/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.DefaultBeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.DelegatingNamespaceHandlerResolver;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Support class for implementing custom {@link IHyperlinkDetector}s. Calculation of individual hyperlinks is done via
 * {@link IHyperlinkCalculator} strategy interfaces respectively.
 * <p>
 * Provides the {@link #registerHyperlinkCalculator} methods for registering a {@link IHyperlinkCalculator} to handle a
 * specific element.
 * @author Christian Dupuis
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class NamespaceHyperlinkDetectorSupport extends AbstractHyperlinkDetector {

	private static final Method FIND_PARSER_FOR_ELEMENT_METHOD;

	static {
		Method method = null;
		try {
			method = NamespaceHandlerSupport.class.getDeclaredMethod("findParserForElement", Element.class,
					ParserContext.class);
			method.setAccessible(true);
		}
		catch (Exception e) {
		}

		FIND_PARSER_FOR_ELEMENT_METHOD = method;
	}

	/**
	 * Stores the {@link IHyperlinkCalculator} keyed the return value of a call to
	 * {@link #createRegisteredName(String, String)}.
	 */
	private Map<String, IHyperlinkCalculator> calculators = new HashMap<String, IHyperlinkCalculator>();

	/**
	 * Calculates hyperlink for the given request by delegating the request to a located {@link IHyperlinkCalculator}
	 * returned by {@link #locateHyperlinkCalculator(String, String)}.
	 */
	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {

		String parentNodeName = null;
		String parentNamespaceUri = null;
		if (parentNode != null) {
			parentNodeName = parentNode.getLocalName();
			parentNamespaceUri = parentNode.getNamespaceURI();
		}

		IHyperlinkCalculator calculator = locateHyperlinkCalculator(parentNamespaceUri, parentNodeName,
				node.getLocalName(), name);
		if (calculator != null) {
			return calculator.createHyperlink(name, target, node, parentNode, document, textViewer, hyperlinkRegion,
					cursor);
		}
		return null;
	}

	/**
	 * Calculates multiple hyperlinks for the given request by delegating the request to a located
	 * {@link IHyperlinkCalculator} returned by {@link #locateHyperlinkCalculator(String, String)}.
	 */
	public IHyperlink[] createHyperlinks(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {

		String parentNodeName = null;
		String parentNamespaceUri = null;
		if (parentNode != null) {
			parentNodeName = parentNode.getLocalName();
			parentNamespaceUri = parentNode.getNamespaceURI();
		}

		IHyperlinkCalculator calculator = locateHyperlinkCalculator(parentNamespaceUri, parentNodeName,
				node.getLocalName(), name);

		if (calculator instanceof IMultiHyperlinkCalculator) {
			return ((IMultiHyperlinkCalculator) calculator).createHyperlinks(name, target, node, parentNode, document,
					textViewer, hyperlinkRegion, cursor);
		}
		else if (calculator != null) {
			return new IHyperlink[] { calculator.createHyperlink(name, target, node, parentNode, document, textViewer,
					hyperlinkRegion, cursor) };
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		IHyperlink[] hyperlinks = super.detectHyperlinks(textViewer, region, canShowMultipleHyperlinks);
		if (hyperlinks == null) {
			hyperlinks = new IHyperlink[0];
		}

		// Special support opening NamespaceHandlers
		IDocument document = textViewer.getDocument();
		Node currentNode = BeansEditorUtils.getNodeByOffset(document, region.getOffset());

		if ((canShowMultipleHyperlinks || hyperlinks.length == 0)
				&& BeansEditorUtils.isElementAtOffset(currentNode, region.getOffset())
				&& currentNode instanceof Element) {

			hyperlinks = createBeanDefinitionParserHyperlink(hyperlinks, document, currentNode);
		}

		return hyperlinks;
	}

	/**
	 * Empty implementation. To be overridden by subclasses.
	 */
	public void init() {
	}

	/**
	 * Checks if a {@link IHyperlinkCalculator} for the given attribute has been registered.
	 */
	public boolean isLinkableAttr(Attr attr) {

		Node parentNode = attr.getOwnerElement().getParentNode();
		String parentNodeName = null;
		String parentNamespaceUri = null;
		if (parentNode != null) {
			parentNodeName = parentNode.getLocalName();
			parentNamespaceUri = parentNode.getNamespaceURI();
		}

		return locateHyperlinkCalculator(parentNamespaceUri, parentNodeName, attr.getOwnerElement().getLocalName(),
				attr.getLocalName()) != null;
	}

	/**
	 * Locates {@link IHyperlink} instances of open up {@link BeanDefinitionParser}s for selected namespace elements. 
	 */
	private IHyperlink[] createBeanDefinitionParserHyperlink(IHyperlink[] hyperlinks, IDocument document,
			Node currentNode) {
		
		String namespaceUri = currentNode.getNamespaceURI();
		IFile file = BeansEditorUtils.getFile(document);
		IBeansConfig config = BeansCorePlugin.getModel().getConfig(file, true);
		
		// Only search for non-default namespace handlers
		if (config != null && namespaceUri != null && !namespaceUri.equals(NamespaceUtils.DEFAULT_NAMESPACE_URI)) {
			
			ClassLoader cl = BeansCorePlugin.getClassLoader();
			if (NamespaceUtils.useNamespacesFromClasspath(file.getProject())) {
				cl = JdtUtils.getClassLoader(file.getProject(), cl);
			}
			
			// Construct NamespaceHandlerResolver using the project's classpath if configured
			DelegatingNamespaceHandlerResolver resolver = new DelegatingNamespaceHandlerResolver(cl, config);
			NamespaceHandler handler = resolver.resolve(namespaceUri);
			if (handler instanceof NamespaceHandlerSupport) {
				
				XmlReaderContext readerContext = new XmlReaderContext((Resource) config.getAdapter(Resource.class),
						new NoOpProblemReporter(), null, null, new XmlBeanDefinitionReader(
								new DefaultBeanDefinitionRegistry()), resolver);
				
				Object parser = ReflectionUtils.invokeMethod(FIND_PARSER_FOR_ELEMENT_METHOD, handler,
						(Element) currentNode, new ParserContext(readerContext, new BeanDefinitionParserDelegate(
								readerContext)));

				if (parser != null) {
					IType type = JdtUtils.getJavaType(file.getProject(), parser.getClass().getName());
					if (type != null) {

						List<IHyperlink> links = new ArrayList<IHyperlink>(Arrays.asList(hyperlinks));
						links.add(new JavaElementHyperlink(
								new Region(((IndexedRegion) currentNode).getStartOffset() + 1, currentNode
										.getNodeName().length()), type));
						hyperlinks = links.toArray(new IHyperlink[links.size()]);
					}
				}
			}
		}
		return hyperlinks;
	}

	/**
	 * Locates a {@link IContentAssistCalculator} in the {@link #calculators} store for the given <code>nodeName</code>
	 * and <code>attributeName</code>.
	 */
	private IHyperlinkCalculator locateHyperlinkCalculator(String parentNamespaceUri, String parentNodeName,
			String nodeName, String attributeName) {
		String key = createRegisteredName(parentNamespaceUri, parentNodeName, nodeName, attributeName);
		if (this.calculators.containsKey(key)) {
			return this.calculators.get(key);
		}
		key = createRegisteredName(null, null, nodeName, attributeName);
		if (this.calculators.containsKey(key)) {
			return this.calculators.get(key);
		}
		key = createRegisteredName(null, null, null, attributeName);
		if (this.calculators.containsKey(key)) {
			return this.calculators.get(key);
		}
		return null;
	}

	/**
	 * Creates a name from the <code>nodeName</code> and <code>attributeName</code>.
	 * @param nodeName the local (non-namespace qualified) name of the element
	 * @param attributeName the local (non-namespace qualified) name of the attribute
	 */
	protected String createRegisteredName(String parentNamespaceUri, String parentNodeName, String nodeName,
			String attributeName) {
		StringBuilder builder = new StringBuilder();
		if (StringUtils.hasText(parentNamespaceUri)) {
			builder.append("/parentNamespaceUri=");
			builder.append(parentNamespaceUri);
		}
		else {
			builder.append("/parentNamespaceUri=");
			builder.append("*");
		}
		if (StringUtils.hasText(parentNodeName)) {
			builder.append("/parentNodeName=");
			builder.append(parentNodeName);
		}
		else {
			builder.append("/parentNodeName=");
			builder.append("*");
		}
		if (StringUtils.hasText(nodeName)) {
			builder.append("/nodeName=");
			builder.append(nodeName);
		}
		else {
			builder.append("/nodeName=");
			builder.append("*");
		}
		if (StringUtils.hasText(attributeName)) {
			builder.append("/attribute=");
			builder.append(attributeName);
		}
		return builder.toString();

	}

	/**
	 * Subclasses can call this to register the supplied {@link IHyperlinkCalculator} to handle the specified attribute.
	 * The attribute name is the local (non-namespace qualified) name.
	 */
	protected void registerHyperlinkCalculator(String attributeName, IHyperlinkCalculator calculator) {
		registerHyperlinkCalculator(null, attributeName, calculator);
	}

	/**
	 * Subclasses can call this to register the supplied {@link IHyperlinkCalculator} to handle the specified attribute
	 * <b>only</b> for a given element. The attribute name is the local (non-namespace qualified) name.
	 */
	protected void registerHyperlinkCalculator(String nodeName, String attributeName, IHyperlinkCalculator calculator) {
		registerHyperlinkCalculator(null, null, nodeName, attributeName, calculator);
	}

	/**
	 * Subclasses can call this to register the supplied {@link IHyperlinkCalculator} to handle the specified attribute
	 * <b>only</b> for a given element. The attribute name is the local (non-namespace qualified) name.
	 */
	protected void registerHyperlinkCalculator(String parentNamespaceUri, String parentNodeName, String nodeName,
			String attributeName, IHyperlinkCalculator calculator) {
		this.calculators.put(createRegisteredName(parentNamespaceUri, parentNodeName, nodeName, attributeName),
				calculator);
	}

	private static final class NoOpProblemReporter implements ProblemReporter {

		public void fatal(Problem problem) {
		}

		public void error(Problem problem) {
		}

		public void warning(Problem problem) {
		}
	}
}
