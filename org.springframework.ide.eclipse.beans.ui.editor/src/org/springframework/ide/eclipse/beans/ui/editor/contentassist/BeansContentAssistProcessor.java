/*
 * Copyright 2002-2004 the original author or authors.
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
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
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.eclipse.wst.xml.ui.internal.templates.TemplateContextTypeIdsXML;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateCompletionProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateContextTypeIdsXML;
import org.springframework.ide.eclipse.core.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class BeansContentAssistProcessor
        extends XMLContentAssistProcessor implements IPropertyChangeListener {

    class VoidMethodSearchRequestor
            extends PropertyNameSearchRequestor {

        public VoidMethodSearchRequestor(ContentAssistRequest request) {
            super(request);
        }

        public void acceptSearchMatch(IMethod method) throws CoreException {
            String returnType = method.getReturnType();
            if (Flags.isPublic(method.getFlags()) && !Flags.isInterface(method.getFlags())
                    && "V".equals(returnType) && method.exists()
                    && ((IType) method.getParent()).isClass()) {
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
                            buf.append(parameterTypes[0] + " " + parameterNames[0]);
                            if (i < (parameterTypes.length - 1)) {
                                buf.append(", ");
                            }
                        }
                        buf.append(") void - ");
                        buf.append(method.getParent().getElementName());
                        displayText = buf.toString();
                    }
                    else {
                        displayText = replaceText + "() void - "
                                + method.getParent().getElementName();
                    }
                    Image image = imageProvider.getImageLabel(method, method.getFlags()
                            | JavaElementImageProvider.SMALL_ICONS);
                    CustomCompletionProposal proposal = new CustomCompletionProposal(replaceText,
                            request.getReplacementBeginPosition() + 1, request
                                    .getReplacementLength() - 2, replaceText.length(), image,
                            displayText, null, null, PropertyNameSearchRequestor.METHOD_RELEVANCE);
                    this.request.addProposal(proposal);
                    this.methods.put(method.getSignature(), proposal);
                }
            }
            catch (JavaModelException e) {
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
            if (Flags.isPublic(method.getFlags()) && !Flags.isInterface(method.getFlags())
                    && parameterCount == 1 && "V".equals(returnType) && method.exists()
                    && ((IType) method.getParent()).isClass()) {
                this.createMethodProposal(method);
            }
        }

        protected void createMethodProposal(IMethod method) {
            try {
                String[] parameterNames = method.getParameterNames();
                String[] parameterTypes = this.getParameterTypes(method);
                String key = method.getElementName() + method.getSignature();
                if (!this.methods.containsKey(key)) {
                    String replaceText = this.getPropertyNameFromMethodName(method);
                    String displayText = replaceText + " - " + method.getParent().getElementName()
                            + "." + method.getElementName() + "(" + parameterTypes[0] + " "
                            + parameterNames[0] + ")";
                    Image image = this.imageProvider.getImageLabel(method, method.getFlags()
                            | JavaElementImageProvider.SMALL_ICONS);
                    CustomCompletionProposal proposal = new CustomCompletionProposal(replaceText,
                            request.getReplacementBeginPosition() + 1, request
                                    .getReplacementLength() - 2, replaceText.length(), image,
                            displayText, null, null, PropertyNameSearchRequestor.METHOD_RELEVANCE);
                    this.request.addProposal(proposal);
                    this.methods.put(key, method);
                }
            }
            catch (JavaModelException e) {
                // do nothing
            }
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
    }

    final class TypeSearchRequestor
            extends SearchRequestor {

        public static final int CLASS_RELEVANCE = 90;

        public static final int INTERFACE_RELEVANCE = 90;

        public static final int PACKAGE_RELEVANCE = 10;

        private ContentAssistRequest request;

        private Map types;

        private boolean invertOrder = false;

        private JavaElementImageProvider imageProvider;

        public TypeSearchRequestor(ContentAssistRequest request, boolean invertOrder) {
            this.request = request;
            this.types = new HashMap();
            this.invertOrder = invertOrder;
            this.imageProvider = new JavaElementImageProvider();
        }

        public void acceptSearchMatch(SearchMatch match) throws CoreException {
            if (match.getElement() instanceof IType) {
                IType type = (IType) match.getElement();
                if (type.exists()) {
                    this.createTypeProposal(type);
                }
            }
            else if (match.getElement() instanceof IPackageFragment) {
                IPackageFragment packageFragment = (IPackageFragment) match.getElement();
                if (packageFragment.exists()) {
                    this.createPackageProposal(packageFragment);
                }
            }
        }

        private void createPackageProposal(IPackageFragment pkg) {
            String displayText = pkg.getElementName();
            if (!this.types.containsKey(displayText)) {
                Image image = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
                int relevance = TypeSearchRequestor.PACKAGE_RELEVANCE;
                if (this.invertOrder) {
                    relevance = TypeSearchRequestor.PACKAGE_RELEVANCE * -1;
                }

                CustomCompletionProposal proposal = new CustomCompletionProposal(displayText,
                        request.getReplacementBeginPosition() + 1,
                        request.getReplacementLength() - 2, displayText.length(), image,
                        displayText, null, null, relevance);
                this.request.addProposal(proposal);
                this.types.put(displayText, proposal);
            }
        }

        private void createTypeProposal(IType type) {
            try {
                String displayText = type.getElementName() + " - "
                        + type.getPackageFragment().getElementName();
                if (!this.types.containsKey(displayText)) {
                    String replaceText = type.getFullyQualifiedName();
                    Image image = null;
                    int relevance = -1;
                    if (type.isInterface()) {
                        relevance = TypeSearchRequestor.INTERFACE_RELEVANCE;
                        if (this.invertOrder) {
                            relevance = TypeSearchRequestor.INTERFACE_RELEVANCE * -1;
                        }
                    }
                    else {
                        relevance = TypeSearchRequestor.CLASS_RELEVANCE;
                        if (this.invertOrder) {
                            relevance = TypeSearchRequestor.CLASS_RELEVANCE * -1;
                        }
                    }

                    image = this.imageProvider.getImageLabel(type, type.getFlags()
                            | JavaElementImageProvider.SMALL_ICONS);

                    CustomCompletionProposal proposal = new CustomCompletionProposal(replaceText,
                            request.getReplacementBeginPosition() + 1, request
                                    .getReplacementLength() - 2, replaceText.length(), image,
                            displayText, null, null, relevance);
                    this.request.addProposal(proposal);
                    this.types.put(displayText, proposal);
                }
            }
            catch (JavaModelException e) {

            }
        }
    }

    private IEditorPart editor;

    private static final ImageDescriptorRegistry IMAGE_DESC_REGISTRY = JavaPlugin
            .getImageDescriptorRegistry();

    public BeansContentAssistProcessor(IEditorPart editor) {
        this.editor = editor;
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
            if ("bean".equals(node.getNodeName())) {
                if ("class".equals(attributeName)) {
                    addClassAttributeValueProposals(request, matchString);
                }
                else if ("init-method".equals(attributeName)
                        || "destroy-method".equals(attributeName)) {
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
                    if (factoryBean != null) {
                        String factoryBeanId = factoryBean.getNodeValue();
                        // TODO add factoryBean support for beans defined outside of the current
                        // xml file
                        Document doc = node.getOwnerDocument();
                        Element bean = doc.getElementById(factoryBeanId);
                        if (bean != null && bean instanceof Node) {
                            NamedNodeMap attr = ((Node) bean).getAttributes();
                            className = attr.getNamedItem("class").getNodeValue();
                        }
                    }
                    else {
                        className = attributes.getNamedItem("class").getNodeValue();
                    }
                    if (className != null) {
                        addFactoryMethodAttributeValueProposals(request, matchString, className);
                    }
                }
            }
            else if ("property".equals(node.getNodeName())) {
                Node parentNode = node.getParentNode();
                NamedNodeMap attributes = parentNode.getAttributes();
                if ("name".equals(attributeName) && attributes != null
                        && attributes.getNamedItem("class") != null) {
                    String className = attributes.getNamedItem("class").getNodeValue();
                    addPropertyNameAttributeValueProposals(request, matchString, className);
                }
            }
            if (request != null && request.getProposals() != null
                    && request.getProposals().size() == 0) {
                super.addAttributeValueProposals(request);
            }
            addTemplates(request, BeansTemplateContextTypeIdsXML.ALL);
        }
    }

    private void addPropertyNameAttributeValueProposals(ContentAssistRequest request,
            String prefix, String className) {
        if (editor.getEditorInput() instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) this.editor.getEditorInput()).getFile();
            IType type = BeansModelUtils.getJavaType(file.getProject(), className);
            if (type != null) {
                try {
                    Collection methods = Introspector.findWritableProperties(type, prefix);
                    if (methods != null && methods.size() > 0) {
                        PropertyNameSearchRequestor requestor = new PropertyNameSearchRequestor(
                                request);
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

    private void addFactoryMethodAttributeValueProposals(ContentAssistRequest request,
            String prefix, String className) {
        if (editor.getEditorInput() instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) this.editor.getEditorInput()).getFile();
            IType type = BeansModelUtils.getJavaType(file.getProject(), className);
            if (type != null) {
                try {
                    Collection methods = Introspector.findAllMethods(type, prefix, -1, true,
                            Introspector.STATIC_YES);
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

    private void addInitDestroyAttributeValueProposals(ContentAssistRequest request, String prefix,
            String className) {
        if (editor.getEditorInput() instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) this.editor.getEditorInput()).getFile();
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

    private void addClassAttributeValueProposals(ContentAssistRequest request, String prefix) {
        if (editor.getEditorInput() instanceof IFileEditorInput) {
            if (prefix != null && prefix.length() > 0) {
                String prefixTemp = prefix;
                if (!prefixTemp.endsWith("*")) {
                    prefixTemp = prefixTemp + "*";
                }
                IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
                try {
                    if (file.getProject().hasNature(JavaCore.NATURE_ID)) {
                        IJavaProject project = JavaCore.create(file.getProject());
                        IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
                                new IJavaElement[] { project }, true);
                        SearchPattern packagePattern = SearchPattern.createPattern(prefixTemp,
                                IJavaSearchConstants.PACKAGE, IJavaSearchConstants.DECLARATIONS,
                                SearchPattern.R_PATTERN_MATCH);
                        SearchPattern typePattern = SearchPattern.createPattern(prefixTemp,
                                IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS,
                                SearchPattern.R_PATTERN_MATCH);
                        SearchPattern pattern = SearchPattern.createOrPattern(packagePattern,
                                typePattern);
                        TypeSearchRequestor requestor = new TypeSearchRequestor(request,
                                StringUtils.isCapitalized(prefixTemp));
                        SearchEngine engine = new SearchEngine();

                        engine.search(pattern, new SearchParticipant[] { SearchEngine
                                .getDefaultSearchParticipant() }, scope, requestor, this
                                .getProgressMonitor());
                    }
                }
                catch (CoreException e) {
                    // do nothing
                }
            }
        }
        else {
            setErrorMessage("Prefix too short");
        }
    }

    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[] { '.' };
    }

    private IProgressMonitor getProgressMonitor() {
        if (this.editor != null
                && this.editor.getEditorSite() != null
                && this.editor.getEditorSite().getActionBars() != null
                && this.editor.getEditorSite().getActionBars().getStatusLineManager() != null
                && this.editor.getEditorSite().getActionBars().getStatusLineManager()
                        .getProgressMonitor() != null) {
            IStatusLineManager manager = this.editor.getEditorSite().getActionBars()
                    .getStatusLineManager();
            IProgressMonitor monitor = manager.getProgressMonitor();
            manager.setMessage("Processing completion proposals");
            return monitor;
        }
        else {
            return new NullProgressMonitor();
        }
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
        //if (!fTemplateContexts.contains(context)) {
            //fTemplateContexts.add(context);
            boolean useProposalList = !contentAssistRequest.shouldSeparate();

            if (getTemplateCompletionProcessor() != null) {
                getTemplateCompletionProcessor().setContextType(context);
                ICompletionProposal[] proposals = getTemplateCompletionProcessor().computeCompletionProposals(fTextViewer, contentAssistRequest.getReplacementBeginPosition());
                for (int i = 0; i < proposals.length; ++i) {
                    if (useProposalList)
                        contentAssistRequest.addProposal(proposals[i]);
                    else
                        contentAssistRequest.addMacro(proposals[i]);
                }
            }
        //}
    }
    
    private BeansTemplateCompletionProcessor fTemplateProcessor = null;
    private List fTemplateContexts = new ArrayList();
    private BeansTemplateCompletionProcessor getTemplateCompletionProcessor() {
        if (fTemplateProcessor == null) {
            fTemplateProcessor = new BeansTemplateCompletionProcessor();
        }
        return fTemplateProcessor;
    }
}
