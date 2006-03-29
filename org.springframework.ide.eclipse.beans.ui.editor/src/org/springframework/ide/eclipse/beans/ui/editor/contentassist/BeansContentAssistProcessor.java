/*
 * Copyright 2002-2006 the original author or authors.
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

import java.util.ArrayList;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposal;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.text.java.CompletionProposalComparator;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.BeansJavaDocUtils;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateCompletionProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateContextTypeIds;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Main entry point for the Spring beans xml editor's content assist.
 */
public class BeansContentAssistProcessor
        extends XMLContentAssistProcessor implements IPropertyChangeListener {

    protected static class BeanReferenceSearchRequestor {

        public static final int EXTERNAL_BEAN_RELEVANCE = 10;

        public static final int LOCAL_BEAN_RELEVANCE = 20;

        protected Map beans;

        protected ContentAssistRequest request;

        public BeanReferenceSearchRequestor(ContentAssistRequest request) {
            this.request = request;
            this.beans = new HashMap();
        }

        public void acceptSearchMatch(IBean bean, IFile file, String prefix) {
            if (bean.getElementName() != null && bean.getElementName().startsWith(prefix)) {
                String beanName = bean.getElementName();
                String replaceText = beanName;
                String fileName = bean.getElementResource().getProjectRelativePath().toString();
                String key = beanName + fileName;
                if (!beans.containsKey(key)) {
                    StringBuffer buf = new StringBuffer();
                    buf.append(beanName);
                    if (bean.getClassName() != null) {
                        String className = bean.getClassName();
                        buf.append(" [");
                        buf.append(Signature.getSimpleName(className));
                        buf.append("]");
                    }
                    if (bean.getParentName() != null) {
                        buf.append(" <");
                        buf.append(bean.getParentName());
                        buf.append(">");
                    }
                    buf.append(" - ");
                    buf.append(fileName);
                    String displayText = buf.toString();

                    Image image = BeansModelImages.getImage(bean, BeansCorePlugin.getModel()
                            .getConfig(file));

                    BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
                            replaceText, request.getReplacementBeginPosition(), request
                                    .getReplacementLength(), replaceText.length(), image,
                            displayText, null, BeansEditorUtils.createAdditionalProposalInfo(bean),
                            BeanReferenceSearchRequestor.EXTERNAL_BEAN_RELEVANCE);

                    request.addProposal(proposal);
                    beans.put(key, proposal);
                }
            }
        }

        public void acceptSearchMatch(Node beanNode, IFile file, String prefix) {
            NamedNodeMap attributes = beanNode.getAttributes();
            Node idAttribute = attributes.getNamedItem("id");
            if (idAttribute != null && idAttribute.getNodeValue() != null
                    && idAttribute.getNodeValue().startsWith(prefix)) {
                if (beanNode.getParentNode() != null
                        && "beans".equals(beanNode.getParentNode().getNodeName())) {
                    String beanName = idAttribute.getNodeValue();
                    String replaceText = beanName;
                    String fileName = file.getProjectRelativePath().toString();
                    String key = beanName + fileName;
                    if (!beans.containsKey(key)) {
                        StringBuffer buf = new StringBuffer();
                        buf.append(beanName);
                        if (attributes.getNamedItem("class") != null) {
                            String className = attributes.getNamedItem("class").getNodeValue();
                            buf.append(" [");
                            buf.append(Signature.getSimpleName(className));
                            buf.append("]");
                        }
                        if (attributes.getNamedItem("parent") != null) {
                            String parentName = attributes.getNamedItem("parent").getNodeValue();
                            buf.append(" <");
                            buf.append(parentName);
                            buf.append(">");
                        }
                        String displayText = buf.toString();
                        Image image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN);

                        BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
                                replaceText, request.getReplacementBeginPosition(), request
                                        .getReplacementLength(), replaceText.length(), image,
                                displayText, null, BeansEditorUtils.createAdditionalProposalInfo(
                                        beanNode, file),
                                BeanReferenceSearchRequestor.LOCAL_BEAN_RELEVANCE);

                        request.addProposal(proposal);
                        beans.put(key, proposal);
                    }
                }
            }
        }
    }

    protected static class FactoryMethodSearchRequestor
            extends VoidMethodSearchRequestor {

        private String className;

        public FactoryMethodSearchRequestor(ContentAssistRequest request, String className) {
            super(request);
            this.className = className;
        }

        public void acceptSearchMatch(IMethod method) throws CoreException {
            String returnType = super.getReturnType(method);
            if (Flags.isPublic(method.getFlags()) && !Flags.isInterface(method.getFlags())
                    && method.exists() && ((IType) method.getParent()).isClass()
                    && className.equals(returnType) && !method.isConstructor()) {
                createMethodProposal(method);
            }
        }

        protected void createMethodProposal(IMethod method) {
            try {
                String[] parameterNames = method.getParameterNames();
                String[] parameterTypes = super.getParameterTypes(method);
                String returnType = super.getReturnType(method);
                returnType = Signature.getSimpleName(returnType);
                String key = method.getElementName() + method.getSignature();
                if (!methods.containsKey(key)) {
                    String methodName = method.getElementName();
                    String replaceText = methodName;
                    StringBuffer buf = new StringBuffer();
                    if (parameterTypes.length > 0 && parameterNames.length > 0) {
                        buf.append(replaceText + "(");
                        for (int i = 0; i < parameterTypes.length; i++) {
                            buf.append(parameterTypes[0]);
                            buf.append(' ');
                            buf.append(parameterNames[0]);
                            if (i < (parameterTypes.length - 1)) {
                                buf.append(", ");
                            }
                        }
                        buf.append(") ");
                        buf.append(returnType);
                        buf.append(" - ");
                        buf.append(method.getParent().getElementName());
                    }
                    else {
                        buf.append(replaceText);
                        buf.append("() ");
                        buf.append(returnType);
                        buf.append(" - ");
                        buf.append(method.getParent().getElementName());
                    }
                    String displayText = buf.toString();
                    Image image = imageProvider.getImageLabel(method, method.getFlags()
                            | JavaElementImageProvider.SMALL_ICONS);
                    BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
                    String javadoc = utils.getJavaDoc();

                    BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
                            replaceText, request.getReplacementBeginPosition(), request
                                    .getReplacementLength(), replaceText.length(), image,
                            displayText, null, javadoc,
                            PropertyNameSearchRequestor.METHOD_RELEVANCE);

                    request.addProposal(proposal);
                    methods.put(method.getSignature(), proposal);
                }
            }
            catch (JavaModelException e) {
                // do nothing
            }
        }
    }

    protected static class PropertyNameSearchRequestor {

        public static final int METHOD_RELEVANCE = 10;

        protected JavaElementImageProvider imageProvider;

        protected Map methods;

        protected ContentAssistRequest request;

        private String prefix;

        public PropertyNameSearchRequestor(ContentAssistRequest request, String prefix) {
            this.request = request;
            this.methods = new HashMap();
            this.imageProvider = new JavaElementImageProvider();
            this.prefix = prefix;
        }

        public void acceptSearchMatch(IMethod method, boolean external) throws CoreException {
            int parameterCount = method.getNumberOfParameters();
            String returnType = method.getReturnType();
            if (Flags.isPublic(method.getFlags()) && !Flags.isInterface(method.getFlags())
                    && parameterCount == 1 && "V".equals(returnType) && method.exists()
                    && ((IType) method.getParent()).isClass() && !method.isConstructor()) {
                createMethodProposal(method, external);
            }
        }

        protected void createMethodProposal(IMethod method, boolean external) {
            try {
                String[] parameterNames = method.getParameterNames();
                String[] parameterTypes = getParameterTypes(method);
                String key = method.getElementName() + method.getSignature();
                if (!methods.containsKey(key)) {
                    String propertyName = getPropertyNameFromMethodName(method);
                    String replaceText = prefix + propertyName;
                    StringBuffer buf = new StringBuffer();
                    buf.append(propertyName);
                    buf.append(" - ");
                    buf.append(method.getParent().getElementName());
                    buf.append('.');
                    buf.append(method.getElementName());
                    buf.append('(');
                    buf.append(parameterTypes[0]);
                    buf.append(' ');
                    buf.append(parameterNames[0]);
                    buf.append(')');
                    String displayText = buf.toString();
                    Image image = imageProvider.getImageLabel(method, method.getFlags()
                            | JavaElementImageProvider.SMALL_ICONS);
                    if (external) {
                        image = BeansModelImages.getDecoratedImage(image,
                                BeansModelImages.FLAG_EXTERNAL);
                    }
                    BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
                    String javadoc = utils.getJavaDoc();

                    BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
                            replaceText, request.getReplacementBeginPosition(), request
                                    .getReplacementLength(), replaceText.length(), image,
                            displayText, null, javadoc,
                            PropertyNameSearchRequestor.METHOD_RELEVANCE);

                    request.addProposal(proposal);
                    methods.put(key, method);
                }
            }
            catch (JavaModelException e) {
                // do nothing
            }
        }

        protected String[] getParameterTypes(IMethod method) {
            try {
                String[] parameterQualifiedTypes = Signature.getParameterTypes(method
                        .getSignature());
                int length = parameterQualifiedTypes == null ? 0 : parameterQualifiedTypes.length;
                String[] parameterPackages = new String[length];
                for (int i = 0; i < length; i++) {
                    parameterQualifiedTypes[i] = parameterQualifiedTypes[i].replace('/', '.');
                    parameterPackages[i] = Signature
                            .getSignatureSimpleName(parameterQualifiedTypes[i]);
                }

                return parameterPackages;
            }
            catch (IllegalArgumentException e) {
            }
            catch (JavaModelException e) {
            }
            return null;
        }

        protected String getPropertyNameFromMethodName(IMethod method) {
            String replaceText = method.getElementName().substring("set".length(),
                    method.getElementName().length());
            if (replaceText != null) {
                char c = replaceText.charAt(0);
                replaceText = replaceText.substring(1, replaceText.length());
                replaceText = Character.toLowerCase(c) + replaceText;
            }
            return replaceText;
        }

        protected String getReturnType(IMethod method) {
            try {
                String parameterQualifiedTypes = Signature.getReturnType(method.getSignature());
                IType type = (IType) method.getParent();
                String tempString = Signature.getSignatureSimpleName(parameterQualifiedTypes);
                String[][] parameterPackages = type.resolveType(tempString);
                if (parameterPackages != null) {
                    return parameterPackages[0][0] + "." + parameterPackages[0][1];
                }
            }
            catch (IllegalArgumentException e) {
            }
            catch (JavaModelException e) {
            }
            return null;
        }
    }

    protected static class VoidMethodSearchRequestor
            extends PropertyNameSearchRequestor {

        public VoidMethodSearchRequestor(ContentAssistRequest request) {
            super(request, "");
        }

        public void acceptSearchMatch(IMethod method) throws CoreException {
            String returnType = method.getReturnType();
            if (Flags.isPublic(method.getFlags()) && !Flags.isInterface(method.getFlags())
                    && "V".equals(returnType) && method.exists()
                    && ((IType) method.getParent()).isClass() && !method.isConstructor()) {
                createMethodProposal(method);
            }
        }

        protected void createMethodProposal(IMethod method) {
            try {
                String[] parameterNames = method.getParameterNames();
                String[] parameterTypes = getParameterTypes(method);
                String key = method.getElementName() + method.getSignature();
                if (!methods.containsKey(key)) {
                    String methodName = method.getElementName();
                    String replaceText = methodName;
                    StringBuffer buf = new StringBuffer();
                    if (parameterTypes.length > 0 && parameterNames.length > 0) {
                        buf.append(replaceText + "(");
                        for (int i = 0; i < parameterTypes.length; i++) {
                            buf.append(parameterTypes[0]);
                            buf.append(' ');
                            buf.append(parameterNames[0]);
                            if (i < (parameterTypes.length - 1)) {
                                buf.append(", ");
                            }
                        }
                        buf.append(") void - ");
                        buf.append(method.getParent().getElementName());
                    }
                    else {
                        buf.append(replaceText);
                        buf.append("() void - ");
                        buf.append(method.getParent().getElementName());
                    }
                    String displayText = buf.toString();
                    Image image = imageProvider.getImageLabel(method, method.getFlags()
                            | JavaElementImageProvider.SMALL_ICONS);
                    BeansJavaDocUtils utils = new BeansJavaDocUtils(method);
                    String javadoc = utils.getJavaDoc();

                    BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(
                            replaceText, request.getReplacementBeginPosition(), request
                                    .getReplacementLength(), replaceText.length(), image,
                            displayText, null, javadoc,
                            PropertyNameSearchRequestor.METHOD_RELEVANCE);

                    request.addProposal(proposal);
                    methods.put(method.getSignature(), proposal);
                }
            }
            catch (JavaModelException e) {
                // do nothing
            }
        }
    }

    private static final String CLASS_SOURCE_END = "\n" + "    }\n" + "}";

    private static final String CLASS_SOURCE_START = "public class _xxx {\n"
            + "    public void main(String[] args) {\n" + "        ";

    private CompletionProposalComparator comparator;

    private BeansTemplateCompletionProcessor templateProcessor = null;

    public BeansContentAssistProcessor() {
        comparator = new CompletionProposalComparator();
    }

    protected void addAttributeValueProposals(ContentAssistRequest request) {
        IDOMNode node = (IDOMNode) request.getNode();

        // Find the attribute region and name for which this position should
        // have a value proposed
        IStructuredDocumentRegion open = node.getFirstStructuredDocumentRegion();
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
            computeAttributeValueProposals(request, node, matchString, attributeName);
            // TODO remove as soon as WTP has fixed the bug; refs #237
            if (!"<".equals(attributeName)) {
                super.addAttributeValueProposals(request);
            }
        }
    }

    private void addBeanReferenceProposals(ContentAssistRequest request, String prefix,
            Document document, boolean showExternal) {
        if (prefix == null) {
            prefix = "";
        }
        if (getResource(request) instanceof IFile) {
            IFile file = (IFile) getResource(request);
            if (document != null) {
                BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(request);
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

    private void addClassAttributeValueProposals(ContentAssistRequest request, String prefix) {

        if (prefix == null || prefix.length() == 0) {
            setErrorMessage("Prefix too short for class content assist");
            return;
        }

        try {
            IFile file = (IFile) getResource(request);
            IJavaProject project = JavaCore.create(file.getProject());
            IPackageFragment root = project.getPackageFragments()[0];
            ICompilationUnit unit = root.getCompilationUnit("_xxx.java").getWorkingCopy(
                    CompilationUnitHelper.getInstance().getWorkingCopyOwner(),
                    CompilationUnitHelper.getInstance().getProblemRequestor(),
                    BeansEditorUtils.getProgressMonitor());
            String source = CLASS_SOURCE_START + prefix + CLASS_SOURCE_END;
            setContents(unit, source);

            BeansJavaCompletionProposalCollector collector = new BeansJavaCompletionProposalCollector(
                    unit);
            unit.codeComplete(CLASS_SOURCE_START.length() + prefix.length(), collector,
                    DefaultWorkingCopyOwner.PRIMARY);

            IJavaCompletionProposal[] props = collector.getJavaCompletionProposals();

            ICompletionProposal[] proposals = order(props);

            for (int i = 0; i < proposals.length; i++) {
                if (proposals[i] instanceof JavaCompletionProposal) {
                    JavaCompletionProposal prop = (JavaCompletionProposal) proposals[i];
                    BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(prop
                            .getReplacementString(), request.getReplacementBeginPosition(), request
                            .getReplacementLength(), prop.getReplacementString().length(), prop
                            .getImage(), prop.getDisplayString(), null, prop
                            .getAdditionalProposalInfo(), prop.getRelevance());

                    request.addProposal(proposal);
                }
                else if (proposals[i] instanceof LazyJavaTypeCompletionProposal) {
                    LazyJavaTypeCompletionProposal prop = (LazyJavaTypeCompletionProposal) proposals[i];
                    BeansJavaCompletionProposal proposal = new BeansJavaCompletionProposal(prop
                            .getReplacementString(), request.getReplacementBeginPosition(), request
                            .getReplacementLength(), prop.getReplacementString().length(), prop
                            .getImage(), prop.getDisplayString(), null, prop
                            .getAdditionalProposalInfo(), prop.getRelevance());

                    request.addProposal(proposal);
                }
            }
        }
        catch (Exception e) {
            // do nothing
        }
    }

    private void addFactoryMethodAttributeValueProposals(ContentAssistRequest request,
            String prefix, String className, String factoryClassName) {
        if (getResource(request) instanceof IFile) {
            IFile file = (IFile) getResource(request);
            IType type = BeansModelUtils.getJavaType(file.getProject(), className);
            if (type != null) {
                try {
                    Collection methods = Introspector.findAllMethods(type, prefix, -1, true,
                            Introspector.STATIC_YES);
                    if (methods != null && methods.size() > 0) {
                        FactoryMethodSearchRequestor requestor = new FactoryMethodSearchRequestor(
                                request, factoryClassName);
                        Iterator iterator = methods.iterator();
                        while (iterator.hasNext()) {
                            requestor.acceptSearchMatch((IMethod) iterator.next());
                        }
                    }
                }
                catch (JavaModelException e1) {
                    // do nothing
                }
                catch (CoreException e) {
                    // // do nothing
                }
            }
        }
    }

    private void addInitDestroyAttributeValueProposals(ContentAssistRequest request, String prefix,
            String className) {
        if (getResource(request) instanceof IFile) {
            IFile file = (IFile) getResource(request);
            IType type = BeansModelUtils.getJavaType(file.getProject(), className);
            if (type != null) {
                try {
                    Collection methods = Introspector.findAllNoParameterMethods(type, prefix);
                    if (methods != null && methods.size() > 0) {
                        VoidMethodSearchRequestor requestor = new VoidMethodSearchRequestor(request);
                        Iterator iterator = methods.iterator();
                        while (iterator.hasNext()) {
                            requestor.acceptSearchMatch((IMethod) iterator.next());
                        }
                    }
                }
                catch (JavaModelException e1) {
                    // do nothing
                }
                catch (CoreException e) {
                    // // do nothing
                }
            }
        }
    }

    private void addPropertyNameAttributeValueProposals(ContentAssistRequest request,
            String prefix, String oldPrefix, Node node, List classNames) {
		
		PropertyNameSearchRequestor requestor = new PropertyNameSearchRequestor(
				request, oldPrefix);
		if (prefix.lastIndexOf(".") >= 0) {
			int firstIndex = prefix.indexOf(".");
			String firstPrefix = prefix.substring(0, firstIndex);
			String lastPrefix = prefix.substring(firstIndex);
			if (".".equals(lastPrefix)) {
				lastPrefix = "";
			}
			else if (lastPrefix.startsWith(".")) {
				lastPrefix = lastPrefix.substring(1);
			}
			for (int i = 0; i < classNames.size(); i++) {
				IType type = (IType) classNames.get(i);
				try {
					Collection methods = Introspector.findReadableProperties(type,
							firstPrefix);
					if (methods != null && methods.size() == 1) {
	
						Iterator iterator = methods.iterator();
						while (iterator.hasNext()) {
							IMethod method = (IMethod) iterator.next();
							IType returnType = BeansEditorUtils.getTypeForMethodReturnType(method, type, (IFile) getResource(request));
							
							if (returnType != null) {
								List typesTemp = new ArrayList();
								typesTemp.add(returnType);
								
								String newPrefix = oldPrefix + firstPrefix + ".";;
								
								addPropertyNameAttributeValueProposals(request, lastPrefix, newPrefix, node, typesTemp);
							}
							return;
						}
					}
				} catch (JavaModelException e1) {
					// do nothing
				} catch (CoreException e) {
					// // do nothing
				}
			}
		}
		else {
			for (int i = 0; i < classNames.size(); i++) {
				IType type = (IType) classNames.get(i);
				try {
					Collection methods = Introspector.findWritableProperties(type,
							prefix);
					if (methods != null && methods.size() > 0) {
	
						Iterator iterator = methods.iterator();
						while (iterator.hasNext()) {
							requestor.acceptSearchMatch((IMethod) iterator.next(),
									false);
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

    protected void addTagCloseProposals(ContentAssistRequest request) {

        // add content assist proposals for incomplete tags
        this.addAttributeValueProposals(request);

        super.addTagCloseProposals(request);
    }

    protected void addTagInsertionProposals(ContentAssistRequest request, int childPosition) {
        IDOMNode node = (IDOMNode) request.getNode();
        if (node != null && node.getParentNode() != null) {
            Node parentNode = node.getParentNode();
            if ("bean".equals(parentNode.getNodeName())) {
                addTemplates(request, BeansTemplateContextTypeIds.BEAN);
            }
            else if ("beans".equals(parentNode.getNodeName())) {
                addTemplates(request, BeansTemplateContextTypeIds.ALL);
            }
            else if ("property".equals(parentNode.getNodeName())) {
                addTemplates(request, BeansTemplateContextTypeIds.PROPERTY);
                addTemplates(request, BeansTemplateContextTypeIds.ALL);
            }
        }
        super.addTagInsertionProposals(request, childPosition);
    }

    /**
     * Adds templates to the list of proposals
     * 
     * @param contentAssistRequest
     * @param context
     */
    private void addTemplates(ContentAssistRequest contentAssistRequest, String context) {
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
    }

    private void computeAttributeValueProposals(ContentAssistRequest request, IDOMNode node,
            String matchString, String attributeName) {

        if ("bean".equals(node.getNodeName())) {
            if ("class".equals(attributeName)) {
                addClassAttributeValueProposals(request, matchString);
            }
            else if ("init-method".equals(attributeName) || "destroy-method".equals(attributeName)) {
                // TODO add support for parent bean
                NamedNodeMap attributes = node.getAttributes();
                String className = attributes.getNamedItem("class").getNodeValue();
                if (className != null) {
                    addInitDestroyAttributeValueProposals(request, matchString, className);
                }
            }
            else if ("factory-method".equals(attributeName)) {
                NamedNodeMap attributes = node.getAttributes();
                Node factoryBean = attributes.getNamedItem("factory-bean");
                String className = null;
                String factoryClassName = null;
                if (factoryBean != null) {
                    List list = BeansEditorUtils.getClassNamesOfBean((IFile) getResource(request),
                            factoryBean);
                    className = (list.size() != 0 ? (String) list.get(0) : null);
                }
                else {
                    List list = BeansEditorUtils.getClassNamesOfBean((IFile) getResource(request),
                            node);
                    className = (list.size() != 0 ? (String) list.get(0) : null);
                }

                List list = BeansEditorUtils
                        .getClassNamesOfBean((IFile) getResource(request), node);
                factoryClassName = (list.size() != 0 ? (String) list.get(0) : null);

                if (className != null && factoryClassName != null) {
                    addFactoryMethodAttributeValueProposals(request, matchString, className,
                            factoryClassName);
                }

            }
            else if ("parent".equals(attributeName) || "depends-on".equals(attributeName)
                    || "factory-bean".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node.getOwnerDocument(), true);
            }
        }
        else if ("property".equals(node.getNodeName())) {
            Node parentNode = node.getParentNode();
            NamedNodeMap parentAttributes = parentNode.getAttributes();

            if ("name".equals(attributeName) && parentAttributes != null) {
                List classNames = BeansEditorUtils.getClassNamesOfBean(
                        (IFile) getResource(request), parentNode);
                addPropertyNameAttributeValueProposals(request, matchString, "", parentNode,
                        classNames);
            }
            else if ("ref".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node.getOwnerDocument(), true);
            }
        }
        else if ("ref".equals(node.getNodeName()) || "idref".equals(node.getNodeName())) {
            if ("local".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node.getOwnerDocument(), false);
            }
            else if ("bean".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node.getOwnerDocument(), true);
            }
        }
        else if ("constructor-arg".equals(node.getNodeName())) {
            if ("type".equals(attributeName)) {
                addClassAttributeValueProposals(request, matchString);
            }
            else if ("ref".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node.getOwnerDocument(), true);
            }
        }
        else if ("alias".equals(node.getNodeName())) {
            if ("name".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node.getOwnerDocument(), true);
            }
        }
        else if ("entry".equals(node.getNodeName())) {
            if ("key-ref".equals(attributeName) || "value-ref".equals(attributeName)) {
                addBeanReferenceProposals(request, matchString, node.getOwnerDocument(), true);
            }
        }
    }

    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '.', '=', '\"', '<' };
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
                    model = StructuredModelManager.getModelManager().getExistingModelForRead(
                            document);
                    if (model != null) {
                        baselocation = model.getBaseLocation();
                    }
                }
                finally {
                    if (model != null) {
                        model.releaseFromRead();
                    }
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

    private BeansTemplateCompletionProcessor getTemplateCompletionProcessor() {
        if (templateProcessor == null) {
            templateProcessor = new BeansTemplateCompletionProcessor();
        }
        return templateProcessor;
    }

    /**
     * Order the given proposals.
     */
    private ICompletionProposal[] order(ICompletionProposal[] proposals) {
        Arrays.sort(proposals, comparator);
        return proposals;
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
            }
            catch (JavaModelException e) {
                e.printStackTrace();
                buffer = null;
            }

            if (buffer != null)
                buffer.setContents(source);
        }
    }
}
