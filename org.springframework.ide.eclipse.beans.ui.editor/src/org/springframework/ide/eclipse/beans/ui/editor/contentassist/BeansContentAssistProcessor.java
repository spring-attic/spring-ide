/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.BeansJavaDocUtils;
import org.springframework.ide.eclipse.beans.ui.editor.BeansLablelProvider;
import org.springframework.ide.eclipse.beans.ui.editor.BeansModelImageDescriptor;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateCompletionProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateContextTypeIds;
import org.springframework.ide.eclipse.core.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Main entry point for the xml editor's content assist.
 */
public class BeansContentAssistProcessor extends XMLContentAssistProcessor
		implements IPropertyChangeListener {

	private CompletionProposalComparator fComparator;

	class BeanReferenceSearchRequestor {

		public static final int LOCAL_BEAN_RELEVANCE = 20;

		public static final int EXTERNAL_BEAN_RELEVANCE = 10;

		protected ContentAssistRequest request;

		protected Map beans;

		public BeanReferenceSearchRequestor(ContentAssistRequest request) {
			this.request = request;
			this.beans = new HashMap();
		}

		public void acceptSearchMatch(Node beanNode, IFile file, String prefix) {
			NamedNodeMap attributes = beanNode.getAttributes();
			if (attributes.getNamedItem("id") != null
					&& attributes.getNamedItem("id").getNodeValue() != null
					&& attributes.getNamedItem("id").getNodeValue().startsWith(
							prefix)) {
				if (beanNode.getParentNode() != null
						&& "beans".equals(beanNode.getParentNode()
								.getNodeName())) {
					String replaceText = attributes.getNamedItem("id")
							.getNodeValue();
					String relFileName = file.getProjectRelativePath()
							.toString();
					String key = replaceText + relFileName;
					if (!this.beans.containsKey(key)) {

						StringBuffer buf = new StringBuffer();
						buf.append(replaceText);
						if (attributes.getNamedItem("class") != null) {
							String className = attributes.getNamedItem("class")
									.getNodeValue();
							buf.append(" [");
							buf.append(Signature.getSimpleName(className));
							buf.append("]");
						}
						Image image = BeansUIImages
								.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);

						int replacementLength = request.getReplacementLength();
						int replacementBegin = request
								.getReplacementBeginPosition();
						if (replacementLength != 0) {
							replacementLength = replacementLength - 2;
							replacementBegin++;
						}

						CustomCompletionProposal proposal = new CustomCompletionProposal(
								replaceText,
								replacementBegin,
								replacementLength,
								replaceText.length(),
								image,
								buf.toString(),
								null,
								BeansLablelProvider
										.createAdditionalProposalInfo(beanNode,
												file),
								BeanReferenceSearchRequestor.LOCAL_BEAN_RELEVANCE);

						this.request.addProposal(proposal);
						this.beans.put(key, proposal);
					}
				}
			}
		}

		public void acceptSearchMatch(IBean bean, IFile file, String prefix) {
			if (bean.getElementName() != null
					&& bean.getElementName().startsWith(prefix)) {

				String replaceText = bean.getElementName();
				String relFileName = bean.getElementResource()
						.getProjectRelativePath().toString();
				String key = replaceText + relFileName;
				if (!this.beans.containsKey(key)) {

					StringBuffer buf = new StringBuffer();
					buf.append(replaceText);
					if (bean.getClassName() != null) {
						String className = bean.getClassName();
						buf.append(" [");
						buf.append(Signature.getSimpleName(className));
						buf.append("]");
					}
					buf.append(" - ");
					buf.append(relFileName);
					Image image = BeansUIImages
							.getImage(BeansUIImages.IMG_OBJS_ROOT_BEAN);
					ImageDescriptor descriptor = new BeansModelImageDescriptor(
							image, getBeanFlags(bean));
					image = BeansUIPlugin.getImageDescriptorRegistry().get(
							descriptor);

					int replacementLength = request.getReplacementLength();
					int replacementBegin = request
							.getReplacementBeginPosition();
					if (replacementLength != 0) {
						replacementLength = replacementLength - 2;
						replacementBegin++;
					}

					CustomCompletionProposal proposal = new CustomCompletionProposal(
							replaceText,
							replacementBegin,
							replacementLength,
							replaceText.length(),
							image,
							buf.toString(),
							null,
							BeansLablelProvider
									.createAdditionalProposalInfo(bean),
							BeanReferenceSearchRequestor.EXTERNAL_BEAN_RELEVANCE);

					this.request.addProposal(proposal);
					this.beans.put(key, proposal);
				}
			}
		}

		private int getBeanFlags(IBean bean) {
			int flags = BeansModelImageDescriptor.FLAG_IS_EXTERNAL;
			if (!bean.isSingleton()) {
				flags |= BeansModelImageDescriptor.FLAG_IS_PROTOTYPE;
			}
			if (bean.isAbstract()) {
				flags |= BeansModelImageDescriptor.FLAG_IS_ABSTRACT;
			}
			if (bean.isLazyInit()) {
				flags |= BeansModelImageDescriptor.FLAG_IS_LAZY_INIT;
			}
			if (bean.isRootBean() && bean.getClassName() == null
					&& bean.getParentName() == null) {
				flags |= BeansModelImageDescriptor.FLAG_IS_ROOT_BEAN_WITHOUT_CLASS;
			}
			return flags;
		}

		public String getText(IFile file) {
			try {
				InputStream in = file.getContents();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				int read = in.read(buf);
				while (read > 0) {
					out.write(buf, 0, read);
					read = in.read(buf);
				}
				return out.toString();
			} catch (CoreException e) {
			} catch (IOException e) {
			}
			return "";
		}
	}

	class VoidMethodSearchRequestor extends PropertyNameSearchRequestor {

		public VoidMethodSearchRequestor(ContentAssistRequest request) {
			super(request);
		}

		public void acceptSearchMatch(IMethod method) throws CoreException {
			String returnType = method.getReturnType();
			if (Flags.isPublic(method.getFlags())
					&& !Flags.isInterface(method.getFlags())
					&& "V".equals(returnType) && method.exists()
					&& ((IType) method.getParent()).isClass()
					&& !method.isConstructor()) {
				this.createMethodProposal(method);
			}
		}

		protected void createMethodProposal(IMethod method) {
			try {
				String[] parameterNames = method.getParameterNames();
				String[] parameterTypes = this.getParameterTypes(method);
				String key = method.getElementName() + method.getSignature();
				if (!this.methods.containsKey(key)) {
					String replaceText = method.getElementName();
					String displayText = null;
					if (parameterTypes.length > 0 && parameterNames.length > 0) {
						StringBuffer buf = new StringBuffer();
						buf.append(replaceText + "(");
						for (int i = 0; i < parameterTypes.length; i++) {
							buf.append(parameterTypes[0] + " "
									+ parameterNames[0]);
							if (i < (parameterTypes.length - 1)) {
								buf.append(", ");
							}
						}
						buf.append(") void - ");
						buf.append(method.getParent().getElementName());
						displayText = buf.toString();
					} else {
						displayText = replaceText + "() void - "
								+ method.getParent().getElementName();
					}
					Image image = imageProvider.getImageLabel(method, method
							.getFlags()
							| JavaElementImageProvider.SMALL_ICONS);
					BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
					String javadoc = utils.getJavaDoc();

					int replacementLength = request.getReplacementLength();
					int replacementBegin = request
							.getReplacementBeginPosition();
					if (replacementLength != 0) {
						replacementLength = replacementLength - 2;
						replacementBegin++;
					}

					CustomCompletionProposal proposal = new CustomCompletionProposal(
							replaceText, replacementBegin, replacementLength,
							replaceText.length(), image, displayText, null,
							javadoc,
							PropertyNameSearchRequestor.METHOD_RELEVANCE);
					this.request.addProposal(proposal);
					this.methods.put(method.getSignature(), proposal);
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
	}

	class FactoryMethodSearchRequestor extends VoidMethodSearchRequestor {

		private String factoryClassName;

		public FactoryMethodSearchRequestor(ContentAssistRequest request,
				String className) {
			super(request);
			this.factoryClassName = className;
		}

		public void acceptSearchMatch(IMethod method) throws CoreException {
			String returnType = super.getReturnType(method);
			if (Flags.isPublic(method.getFlags())
					&& !Flags.isInterface(method.getFlags()) && method.exists()
					&& ((IType) method.getParent()).isClass()
					&& factoryClassName.equals(returnType)
					&& !method.isConstructor()) {
				this.createMethodProposal(method);
			}
		}

		protected void createMethodProposal(IMethod method) {
			try {
				String[] parameterNames = method.getParameterNames();
				String[] parameterTypes = super.getParameterTypes(method);
				String returnType = super.getReturnType(method);
				returnType = Signature.getSimpleName(returnType);
				String key = method.getElementName() + method.getSignature();
				if (!this.methods.containsKey(key)) {
					String replaceText = method.getElementName();
					String displayText = null;
					if (parameterTypes.length > 0 && parameterNames.length > 0) {
						StringBuffer buf = new StringBuffer();
						buf.append(replaceText + "(");
						for (int i = 0; i < parameterTypes.length; i++) {
							buf.append(parameterTypes[0] + " "
									+ parameterNames[0]);
							if (i < (parameterTypes.length - 1)) {
								buf.append(", ");
							}
						}
						buf.append(") ");
						buf.append(returnType);
						buf.append(" - ");
						buf.append(method.getParent().getElementName());
						displayText = buf.toString();
					} else {
						displayText = replaceText + "() " + returnType + " - "
								+ method.getParent().getElementName();
					}
					Image image = imageProvider.getImageLabel(method, method
							.getFlags()
							| JavaElementImageProvider.SMALL_ICONS);
					BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
					String javadoc = utils.getJavaDoc();

					int replacementLength = request.getReplacementLength();
					int replacementBegin = request
							.getReplacementBeginPosition();
					if (replacementLength != 0) {
						replacementLength = replacementLength - 2;
						replacementBegin++;
					}

					CustomCompletionProposal proposal = new CustomCompletionProposal(
							replaceText, replacementBegin, replacementLength,
							replaceText.length(), image, displayText, null,
							javadoc,
							PropertyNameSearchRequestor.METHOD_RELEVANCE);
					this.request.addProposal(proposal);
					this.methods.put(method.getSignature(), proposal);
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
	}

	class PropertyNameSearchRequestor {

		public static final int METHOD_RELEVANCE = 10;

		protected ContentAssistRequest request;

		protected Map methods;

		protected JavaElementImageProvider imageProvider;

		public PropertyNameSearchRequestor(ContentAssistRequest request) {
			this.request = request;
			this.methods = new HashMap();
			this.imageProvider = new JavaElementImageProvider();
		}

		public void acceptSearchMatch(IMethod method) throws CoreException {
			int parameterCount = method.getNumberOfParameters();
			String returnType = method.getReturnType();
			if (Flags.isPublic(method.getFlags())
					&& !Flags.isInterface(method.getFlags())
					&& parameterCount == 1 && "V".equals(returnType)
					&& method.exists()
					&& ((IType) method.getParent()).isClass()
					&& !method.isConstructor()) {
				this.createMethodProposal(method);
			}
		}

		protected void createMethodProposal(IMethod method) {
			try {
				String[] parameterNames = method.getParameterNames();
				String[] parameterTypes = this.getParameterTypes(method);
				String key = method.getElementName() + method.getSignature();
				if (!this.methods.containsKey(key)) {
					String replaceText = this
							.getPropertyNameFromMethodName(method);
					String displayText = replaceText + " - "
							+ method.getParent().getElementName() + "."
							+ method.getElementName() + "(" + parameterTypes[0]
							+ " " + parameterNames[0] + ")";
					Image image = this.imageProvider.getImageLabel(method,
							method.getFlags()
									| JavaElementImageProvider.SMALL_ICONS);
					BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
					String javadoc = utils.getJavaDoc();

					int replacementLength = request.getReplacementLength();
					int replacementBegin = request
							.getReplacementBeginPosition();
					if (replacementLength != 0) {
						replacementLength = replacementLength - 2;
						replacementBegin++;
					}

					CustomCompletionProposal proposal = new CustomCompletionProposal(
							replaceText, replacementBegin, replacementLength,
							replaceText.length(), image, displayText, null,
							javadoc,
							PropertyNameSearchRequestor.METHOD_RELEVANCE);
					this.request.addProposal(proposal);
					this.methods.put(key, method);
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}

		protected String getPropertyNameFromMethodName(IMethod method) {
			String replaceText = method.getElementName().substring(
					"set".length(), method.getElementName().length());
			if (replaceText != null) {
				char c = replaceText.charAt(0);
				replaceText = replaceText.substring(1, replaceText.length());
				replaceText = Character.toLowerCase(c) + replaceText;
			}
			return replaceText;
		}

		protected String[] getParameterTypes(IMethod method) {
			try {
				String[] parameterQualifiedTypes = Signature
						.getParameterTypes(method.getSignature());
				int length = parameterQualifiedTypes == null ? 0
						: parameterQualifiedTypes.length;
				String[] parameterPackages = new String[length];
				for (int i = 0; i < length; i++) {
					parameterQualifiedTypes[i] = parameterQualifiedTypes[i]
							.replace('/', '.');
					parameterPackages[i] = Signature
							.getSignatureSimpleName(parameterQualifiedTypes[i]);
				}

				return parameterPackages;
			} catch (IllegalArgumentException e) {
			} catch (JavaModelException e) {
			}
			return null;
		}

		protected String getReturnType(IMethod method) {
			try {
				String parameterQualifiedTypes = Signature.getReturnType(method
						.getSignature());
				IType type = (IType) method.getParent();
				String tempString = Signature
						.getSignatureSimpleName(parameterQualifiedTypes);
				String[][] parameterPackages = type.resolveType(tempString);
				if (parameterPackages != null) {
					return parameterPackages[0][0] + "."
							+ parameterPackages[0][1];
				}
			} catch (IllegalArgumentException e) {
			} catch (JavaModelException e) {
			}
			return null;
		}
	}

	public BeansContentAssistProcessor() {
		this.fComparator = new CompletionProposalComparator();
	}

	protected void addTagInsertionProposals(ContentAssistRequest request,
			int childPosition) {
		IDOMNode node = (IDOMNode) request.getNode();
		if (node != null && node.getParentNode() != null) {
			Node parentNode = node.getParentNode();
			if ("bean".equals(parentNode.getNodeName())) {
				this.addTemplates(request, BeansTemplateContextTypeIds.BEAN);
			} else if ("beans".equals(parentNode.getNodeName())) {
				this.addTemplates(request, BeansTemplateContextTypeIds.ALL);
			} else if ("property".equals(parentNode.getNodeName())) {
				this
						.addTemplates(request,
								BeansTemplateContextTypeIds.PROPERTY);
				this.addTemplates(request, BeansTemplateContextTypeIds.ALL);
			}
		}
		super.addTagInsertionProposals(request, childPosition);
	}

	protected void addTagCloseProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();

		// Find the attribute region and name for which this position should
		// have a value proposed
		IStructuredDocumentRegion open = node
				.getFirstStructuredDocumentRegion();
		ITextRegionList openRegions = open.getRegions();
		int i = openRegions.indexOf(request.getRegion());
		if (i < 0) {
			return;
		}
		ITextRegion nameRegion = null;
		while (i >= 0) {
			nameRegion = openRegions.get(i--);
			if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				break;
			}
		}

		String matchString = request.getMatchString();
		if (matchString == null) {
			matchString = "";
		}
		if (matchString.length() > 0
				&& (matchString.startsWith("\"") || matchString.startsWith("'"))) {
			matchString = matchString.substring(1);
		}

		// the name region is REQUIRED to do anything useful
		if (nameRegion != null && !matchString.endsWith("\"")) {
			String attributeName = open.getText(nameRegion);
			computeAttributeValueProposals(request, node, matchString,
					attributeName);
		}

		super.addTagCloseProposals(request);
	}

	protected void addAttributeValueProposals(ContentAssistRequest request) {
		IDOMNode node = (IDOMNode) request.getNode();

		// Find the attribute region and name for which this position should
		// have a value proposed
		IStructuredDocumentRegion open = node
				.getFirstStructuredDocumentRegion();
		ITextRegionList openRegions = open.getRegions();
		int i = openRegions.indexOf(request.getRegion());
		if (i < 0) {
			return;
		}
		ITextRegion nameRegion = null;
		while (i >= 0) {
			nameRegion = openRegions.get(i--);
			if (nameRegion.getType() == DOMRegionContext.XML_TAG_ATTRIBUTE_NAME) {
				break;
			}
		}

		String matchString = request.getMatchString();
		if (matchString == null) {
			matchString = "";
		}
		if (matchString.length() > 0
				&& (matchString.startsWith("\"") || matchString.startsWith("'"))) {
			matchString = matchString.substring(1);
		}

		// the name region is REQUIRED to do anything useful
		if (nameRegion != null) {
			String attributeName = open.getText(nameRegion);
			computeAttributeValueProposals(request, node, matchString,
					attributeName);
			if (request != null && request.getProposals() != null
					&& request.getProposals().size() == 0) {
				super.addAttributeValueProposals(request);
			}
		}
	}

	private void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName) {

		if ("bean".equals(node.getNodeName())) {
			if ("class".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			} else if ("init-method".equals(attributeName)
					|| "destroy-method".equals(attributeName)) {
				// TODO add support for parent bean
				NamedNodeMap attributes = node.getAttributes();
				String className = attributes.getNamedItem("class")
						.getNodeValue();
				if (className != null) {
					addInitDestroyAttributeValueProposals(request, matchString,
							className);
				}
			} else if ("factory-method".equals(attributeName)) {
				// TODO add support for parent bean
				NamedNodeMap attributes = node.getAttributes();
				Node factoryBean = attributes.getNamedItem("factory-bean");
				String className = null;
				String factoryClassName = null;
				if (factoryBean != null) {
					String factoryBeanId = factoryBean.getNodeValue();
					Document doc = node.getOwnerDocument();
					Element bean = doc.getElementById(factoryBeanId);
					if (bean != null && bean instanceof Node) {
						NamedNodeMap attr = ((Node) bean).getAttributes();
						className = attr.getNamedItem("class").getNodeValue();
					} else {
						if (getResource(request) instanceof IFile) {
							IFile file = (IFile) getResource(request);

							// assume this is an external reference
							Iterator beans = BeansEditorUtils
									.getBeansFromConfigSets(file).iterator();
							while (beans.hasNext()) {
								IBean modelBean = (IBean) beans.next();
								if (modelBean.getElementName().equals(
										factoryBeanId)) {
									className = modelBean.getClassName();
								}
							}
						}
					}
				} else {
					if (attributes.getNamedItem("class") != null) {
						className = attributes.getNamedItem("class")
								.getNodeValue();
					}
				}
				if (attributes.getNamedItem("class") != null) {
					factoryClassName = attributes.getNamedItem("class")
							.getNodeValue();
				}
				if (className != null && factoryClassName != null) {
					addFactoryMethodAttributeValueProposals(request,
							matchString, className, factoryClassName);
				}
			} else if ("parent".equals(attributeName)
					|| "depends-on".equals(attributeName)
					|| "factory-bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node
						.getOwnerDocument(), true);
			}
		} else if ("property".equals(node.getNodeName())) {
			// TODO add support for parent bean
			Node parentNode = node.getParentNode();
			NamedNodeMap attributes = parentNode.getAttributes();
			if ("name".equals(attributeName) && attributes != null
					&& attributes.getNamedItem("class") != null) {
				String className = attributes.getNamedItem("class")
						.getNodeValue();
				addPropertyNameAttributeValueProposals(request, matchString,
						className);
			}
		} else if ("ref".equals(node.getNodeName())) {
			if ("local".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node
						.getOwnerDocument(), false);
			} else if ("bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node
						.getOwnerDocument(), true);
			}
		}
	}

	private void addBeanReferenceProposals(ContentAssistRequest request,
			String prefix, Document document, boolean showExternal) {
		if (prefix == null) {
			prefix = "";
		}
		if (getResource(request) instanceof IFile) {
			IFile file = (IFile) getResource(request);
			if (document != null) {
				BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(
						request);
				NodeList beanNodes = document.getElementsByTagName("bean");
				for (int i = 0; i < beanNodes.getLength(); i++) {
					Node beanNode = beanNodes.item(i);
					requestor.acceptSearchMatch(beanNode, file, prefix);
				}
				if (showExternal) {
					List beans = BeansEditorUtils.getBeansFromConfigSets(file);
					for (int i = 0; i < beans.size(); i++) {
						IBean bean = (IBean) beans.get(i);
						requestor.acceptSearchMatch(bean, file, prefix);
					}
				}
			}
		}
	}

	private void addPropertyNameAttributeValueProposals(
			ContentAssistRequest request, String prefix, String className) {
		if (getResource(request) instanceof IFile) {
			IFile file = (IFile) getResource(request);
			IType type = BeansModelUtils.getJavaType(file.getProject(),
					className);
			if (type != null) {
				try {
					Collection methods = Introspector.findWritableProperties(
							type, prefix);
					if (methods != null && methods.size() > 0) {
						PropertyNameSearchRequestor requestor = new PropertyNameSearchRequestor(
								request);
						Iterator iterator = methods.iterator();
						while (iterator.hasNext()) {
							requestor.acceptSearchMatch((IMethod) iterator
									.next());
						}
					}
				} catch (JavaModelException e1) {
					// do nothing
				} catch (CoreException e) {
					// // do nothing
				}
			}
		}
	}

	private void addFactoryMethodAttributeValueProposals(
			ContentAssistRequest request, String prefix, String className,
			String factoryClassName) {
		if (getResource(request) instanceof IFile) {
			IFile file = (IFile) getResource(request);
			IType type = BeansModelUtils.getJavaType(file.getProject(),
					className);
			if (type != null) {
				try {
					Collection methods = Introspector.findAllMethods(type,
							prefix, -1, true, Introspector.STATIC_YES);
					if (methods != null && methods.size() > 0) {
						FactoryMethodSearchRequestor requestor = new FactoryMethodSearchRequestor(
								request, factoryClassName);
						Iterator iterator = methods.iterator();
						while (iterator.hasNext()) {
							requestor.acceptSearchMatch((IMethod) iterator
									.next());
						}
					}
				} catch (JavaModelException e1) {
					// do nothing
				} catch (CoreException e) {
					// // do nothing
				}
			}
		}
	}

	private void addInitDestroyAttributeValueProposals(
			ContentAssistRequest request, String prefix, String className) {
		if (getResource(request) instanceof IFile) {
			IFile file = (IFile) getResource(request);
			IType type = BeansModelUtils.getJavaType(file.getProject(),
					className);
			if (type != null) {
				try {
					Collection methods = Introspector
							.findAllNoParameterMethods(type, prefix);
					if (methods != null && methods.size() > 0) {
						VoidMethodSearchRequestor requestor = new VoidMethodSearchRequestor(
								request);
						Iterator iterator = methods.iterator();
						while (iterator.hasNext()) {
							requestor.acceptSearchMatch((IMethod) iterator
									.next());
						}
					}
				} catch (JavaModelException e1) {
					// do nothing
				} catch (CoreException e) {
					// // do nothing
				}
			}
		}
	}

	private void addClassAttributeValueProposals(ContentAssistRequest request,
			String prefix) {

		String contents = "public class _xxx {\n"
				+ "    public void main(String[] args) {\n" + "        ";
		String contents_end = "\n" + "    }\n" + "}";

		try {
			IFile file1 = (IFile) getResource(request);
			IJavaProject project1 = JavaCore.create(file1.getProject());
			IPackageFragment root = project1.getPackageFragments()[0];
			ICompilationUnit unit = root.getCompilationUnit("_xxx.java")
					.getWorkingCopy(
							CompilationUnitHelper.getInstance()
									.getWorkingCopyOwner(),
							CompilationUnitHelper.getInstance()
									.getProblemRequestor(),
							getProgressMonitor());
			String source = contents + prefix + contents_end;
			setContents(unit, source);

			CompletionProposalCollector collector = new CompletionProposalCollector(
					unit);
			unit.codeComplete(66 + prefix.length(), collector,
					DefaultWorkingCopyOwner.PRIMARY);

			IJavaCompletionProposal[] props = collector
					.getJavaCompletionProposals();

			ICompletionProposal[] proposals = order(props);

			int replacementLength = request.getReplacementLength();
			int replacementBegin = request.getReplacementBeginPosition();
			if (replacementLength != 0) {
				replacementLength = replacementLength - 2;
				replacementBegin++;
			}

			for (int i = 0; i < proposals.length; i++) {
				if (proposals[i] instanceof JavaCompletionProposal) {
					JavaCompletionProposal prop = (JavaCompletionProposal) proposals[i];
					BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
							prop.getReplacementString(), replacementBegin,
							replacementLength, prop.getReplacementString()
									.length(), prop.getImage(), prop
									.getDisplayString(), null, prop
									.getAdditionalProposalInfo(), prop
									.getRelevance());

					request.addProposal(proposal);
				} else if (proposals[i] instanceof LazyJavaTypeCompletionProposal) {
					LazyJavaTypeCompletionProposal prop = (LazyJavaTypeCompletionProposal) proposals[i];
					BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
							prop.getReplacementString(), replacementBegin,
							replacementLength, prop.getReplacementString()
									.length(), prop.getImage(), prop
									.getDisplayString(), null, prop
									.getAdditionalProposalInfo(), prop
									.getRelevance());

					request.addProposal(proposal);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}

	private IProgressMonitor getProgressMonitor() {
		/*
		 * if (this.editor != null && this.editor.getEditorSite() != null &&
		 * this.editor.getEditorSite().getActionBars() != null &&
		 * this.editor.getEditorSite().getActionBars() .getStatusLineManager() !=
		 * null && this.editor.getEditorSite().getActionBars()
		 * .getStatusLineManager().getProgressMonitor() != null) {
		 * IStatusLineManager manager = this.editor.getEditorSite()
		 * .getActionBars().getStatusLineManager(); IProgressMonitor monitor =
		 * manager.getProgressMonitor(); manager.setMessage("Processing
		 * completion proposals"); return monitor; } else {
		 */
		return new NullProgressMonitor();
		// }
	}

	/**
	 * Adds templates to the list of proposals
	 * 
	 * @param contentAssistRequest
	 * @param context
	 */
	private void addTemplates(ContentAssistRequest contentAssistRequest,
			String context) {
		if (contentAssistRequest == null)
			return;

		// if already adding template proposals for a certain context type, do
		// not add again
		// if (!fTemplateContexts.contains(context)) {
		// fTemplateContexts.add(context);
		boolean useProposalList = !contentAssistRequest.shouldSeparate();

		if (getTemplateCompletionProcessor() != null) {
			getTemplateCompletionProcessor().setContextType(context);
			ICompletionProposal[] proposals = getTemplateCompletionProcessor()
					.computeCompletionProposals(fTextViewer,
							contentAssistRequest.getReplacementBeginPosition());
			for (int i = 0; i < proposals.length; ++i) {
				if (useProposalList)
					contentAssistRequest.addProposal(proposals[i]);
				else
					contentAssistRequest.addMacro(proposals[i]);
			}
		}
		// }
	}

	private BeansTemplateCompletionProcessor fTemplateProcessor = null;

	private BeansTemplateCompletionProcessor getTemplateCompletionProcessor() {
		if (fTemplateProcessor == null) {
			fTemplateProcessor = new BeansTemplateCompletionProcessor();
		}
		return fTemplateProcessor;
	}

	/**
	 * Returns project request is in
	 * 
	 * @param request
	 * @return
	 */
	private IResource getResource(ContentAssistRequest request) {
		IResource resource = null;
		String baselocation = null;

		if (request != null) {
			IStructuredDocumentRegion region = request.getDocumentRegion();
			if (region != null) {
				IDocument document = region.getParentDocument();
				IStructuredModel model = null;
				try {
					model = StructuredModelManager.getModelManager()
							.getExistingModelForRead(document);
					if (model != null) {
						baselocation = model.getBaseLocation();
					}
				} finally {
					if (model != null)
						model.releaseFromRead();
				}
			}
		}

		if (baselocation != null) {
			// copied from JSPTranslationAdapter#getJavaProject
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IPath filePath = new Path(baselocation);
			if (filePath.segmentCount() > 0) {
				resource = root.getFile(filePath);
			}
		}
		return resource;
	}

	/**
	 * Set contents of the compilation unit to the translated jsp text.
	 * 
	 * @param the
	 *            ICompilationUnit on which to set the buffer contents
	 */
	private void setContents(ICompilationUnit cu, String source) {
		if (cu == null)
			return;

		synchronized (cu) {
			IBuffer buffer;
			try {

				buffer = cu.getBuffer();
			} catch (JavaModelException e) {
				e.printStackTrace();
				buffer = null;
			}

			if (buffer != null)
				buffer.setContents(source);
		}
	}

	/**
	 * Order the given proposals.
	 */
	private ICompletionProposal[] order(ICompletionProposal[] proposals) {
		Arrays.sort(proposals, fComparator);
		return proposals;
	}
}
