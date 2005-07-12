package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.jdt.internal.ui.JavaElementAdapterFactory;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.refactoring.contentassist.CUPositionCompletionProcessor;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.core.internal.util.StringUtils;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.regions.DOMRegionContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLContentAssistProcessor;
import org.eclipse.wst.xml.ui.internal.contentassist.XMLRelevanceConstants;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImageHelper;
import org.eclipse.wst.xml.ui.internal.editor.XMLEditorPluginImages;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class BeansContentAssistProcessor
        extends XMLContentAssistProcessor implements IPropertyChangeListener {

    final class PropertyNameSearchRequestor
            extends SearchRequestor {

        public static final int METHOD_RELEVANCE = 10;

        private ContentAssistRequest request;

        private Map methods;

        private JavaElementImageProvider imageProvider;

        public PropertyNameSearchRequestor(ContentAssistRequest request) {
            this.request = request;
            this.methods = new HashMap();
            this.imageProvider = new JavaElementImageProvider();
        }

        public void acceptSearchMatch(SearchMatch match) throws CoreException {
            if (match.getElement() instanceof IMethod) {
                IMethod method = (IMethod) match.getElement();
                int parameterCount = method.getNumberOfParameters();
                String returnType = method.getReturnType();
                if (Flags.isPublic(method.getFlags()) && !Flags.isInterface(method.getFlags())
                        && parameterCount == 1 && "V".equals(returnType) && method.exists()
                        && ((IType) method.getParent()).isClass()) {
                    this.createMethodProposal(method);
                }
            }
        }

        private void createMethodProposal(IMethod method) {
            try {
                String[] parameterNames = method.getParameterNames();
                String[] parameterTypes = this.getParameterTypes(method);
                String displayText = method.getElementName() + "(" + parameterTypes[0] + " "
                        + parameterNames[0] + ") void - " + method.getParent().getElementName();
                if (!this.methods.containsKey(method.getSignature())) {
                    Image image = this.imageProvider.getImageLabel(method, method.getFlags()
                            | JavaElementImageProvider.SMALL_ICONS);
                    String replaceText = this.getPropertyNameFromMethodName(method);
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

        private String getPropertyNameFromMethodName(IMethod method) {
            String replaceText = method.getElementName().substring("set".length(),
                    method.getElementName().length());
            if (replaceText != null) {
                char c = replaceText.charAt(0);
                replaceText = replaceText.substring(1, replaceText.length());
                replaceText = Character.toLowerCase(c) + replaceText;
            }
            return replaceText;
        }

        private String[] getParameterTypes(IMethod method) {
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
        }
    }

    private void addPropertyNameAttributeValueProposals(ContentAssistRequest request,
            String prefix, String className) {
        if (editor.getEditorInput() instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) this.editor.getEditorInput()).getFile();
            IType type = BeansModelUtils.getJavaType(file.getProject(), className);
            if (type != null) {
                String prefixTemp = prefix;
                if (!prefixTemp.endsWith("*")) {
                    prefixTemp = prefixTemp + "*";
                }
                if (!prefixTemp.startsWith("set")) {
                    prefixTemp = "set" + prefixTemp;
                }
                try {
                    IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
                    SearchPattern methodPattern = SearchPattern.createPattern(prefixTemp,
                            IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS,
                            SearchPattern.R_PATTERN_MATCH);
                    PropertyNameSearchRequestor requestor = new PropertyNameSearchRequestor(request);
                    SearchEngine engine = new SearchEngine();

                    try {
                        engine.search(methodPattern, new SearchParticipant[] { SearchEngine
                                .getDefaultSearchParticipant() }, scope, requestor, this
                                .getProgressMonitor());
                    }
                    catch (CoreException e) {
                        // do nothing
                    }
                }
                catch (JavaModelException e1) {
                    // do nothing
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
                        TypeSearchRequestor requestor = new TypeSearchRequestor(request, this
                                .isUpperCaseSearch(prefixTemp));
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

    private boolean isUpperCaseSearch(String prefix) {
        if (prefix != null) {
            char c = prefix.charAt(0);
            return !Character.isUpperCase(c);
        }
        else {
            return false;
        }
    }
}
