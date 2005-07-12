package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.core.internal.provisional.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.util.StringUtils;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Detects hyperlinks in XML tags. Includes detection in DOCTYPE and attribute values. Resolves
 * references to schemas, dtds, etc using the Common URI Resolver.
 * 
 */
public class BeansHyperLinkDetector implements IHyperlinkDetector {

    final class PropertyNameSearchRequestor
            extends SearchRequestor {

        private List methods;

        public PropertyNameSearchRequestor() {
            this.methods = new ArrayList();
        }

        public void acceptSearchMatch(SearchMatch match) throws CoreException {
            if (match.getElement() instanceof IMethod) {
                IMethod method = (IMethod) match.getElement();
                int parameterCount = method.getNumberOfParameters();
                String returnType = method.getReturnType();
                if (Flags.isPublic(method.getFlags()) && parameterCount == 1
                        && "V".equals(returnType) && method.exists()) {
                    this.methods.add(method);
                }
            }
        }

        public List getMethods() {
            return methods;
        }
    }

    private IEditorPart editor;

    public BeansHyperLinkDetector(IEditorPart editor) {
        this.editor = editor;
    }

    /**
     * Create the appropriate hyperlink
     * 
     * @param uriString
     * @param hyperlinkRegion
     * @return IHyperlink
     */
    private IHyperlink createHyperlink(Attr attr, IRegion hyperlinkRegion, IDocument document,
            Node node) {
        IHyperlink link = null;

        if (attr != null) {
            String name = attr.getName();
            String target = attr.getNodeValue();
            Node parentNode = attr.getOwnerElement();
            String parentName = null;
            if (parentNode != null) {
                parentName = parentNode.getNodeName();
            }
            if ("class".equals(name)) {
                IFile file = ((IFileEditorInput) this.editor.getEditorInput()).getFile();
                IType type = BeansModelUtils.getJavaType(file.getProject(), target);
                if (type != null) {
                    link = new JavaElementHyperlink(hyperlinkRegion, type);
                }
            }
            else if ("name".equals(name) && "property".equals(parentName)) {
                Node parentParentNode = parentNode.getParentNode();
                if ("bean".equals(parentParentNode.getNodeName())) {
                    NamedNodeMap attributes = parentParentNode.getAttributes();
                    if (attributes != null && attributes.getNamedItem("class") != null) {

                        String className = attributes.getNamedItem("class").getNodeValue();
                        IFile file = ((IFileEditorInput) this.editor.getEditorInput()).getFile();
                        IType type = BeansModelUtils.getJavaType(file.getProject(), className);
                        if (type != null) {
                            try {
                                IMethod method = this.findMethod(type, "set" + target, 1, true,
                                        false);
                                link = new JavaElementHyperlink(hyperlinkRegion, method);
                            }
                            catch (JavaModelException e) {
                            }
                        }
                        /*
                         * try { IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
                         * SearchPattern methodPattern = SearchPattern .createPattern("set" +
                         * target, IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS,
                         * SearchPattern.R_PREFIX_MATCH); PropertyNameSearchRequestor requestor =
                         * new PropertyNameSearchRequestor(); SearchEngine engine = new
                         * SearchEngine(); try { engine.search(methodPattern, new
                         * SearchParticipant[] { SearchEngine .getDefaultSearchParticipant() },
                         * scope, requestor, new NullProgressMonitor());
                         * 
                         * if (requestor.getMethods() != null && requestor.getMethods().size() > 0) {
                         * link = new JavaElementHyperlink(hyperlinkRegion, (IJavaElement[])
                         * requestor.getMethods() .toArray( new IJavaElement[requestor.getMethods()
                         * .size()])); }
                         *  } catch (CoreException e) { // do nothing } } catch (JavaModelException
                         * e1) { }
                         */
                    }
                }
            }
        }

        return link;
    }

    public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region,
            boolean canShowMultipleHyperlinks) {
        // for now, only capable of creating 1 hyperlink
        List hyperlinks = new ArrayList(0);

        if (region != null && textViewer != null) {
            IDocument document = textViewer.getDocument();
            Node currentNode = getCurrentNode(document, region.getOffset());
            if (currentNode != null) {
                if (currentNode.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
                    // nothing to do
                }
                else if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    // element nodes
                    Attr currentAttr = getCurrentAttrNode(currentNode, region.getOffset());
                    if (currentAttr != null && this.isLinkableAttr(currentAttr)) {
                        IRegion hyperlinkRegion = getHyperlinkRegion(currentAttr);
                        IHyperlink hyperlink = createHyperlink(currentAttr, hyperlinkRegion,
                                document, currentNode);
                        if (hyperlink != null) {
                            hyperlinks.add(hyperlink);
                        }
                    }
                    currentNode = currentAttr;
                }
                // try to create hyperlink from information gathered
                /*
                 * if (uriString != null && currentNode != null && isValidURI(uriString)) { IRegion
                 * hyperlinkRegion = getHyperlinkRegion(currentNode); IHyperlink hyperlink =
                 * createHyperlink(uriString, hyperlinkRegion, document, currentNode); if (hyperlink !=
                 * null) { hyperlinks.add(hyperlink); } }
                 */
            }
        }
        if (hyperlinks.size() == 0)
            return null;
        return (IHyperlink[]) hyperlinks.toArray(new IHyperlink[0]);
    }

    /**
     * Returns the attribute node within node at offset
     * 
     * @param node
     * @param offset
     * @return Attr
     */
    private Attr getCurrentAttrNode(Node node, int offset) {
        if ((node instanceof IndexedRegion) && ((IndexedRegion) node).contains(offset)
                && (node.hasAttributes())) {
            NamedNodeMap attrs = node.getAttributes();
            // go through each attribute in node and if attribute contains
            // offset, return that attribute
            for (int i = 0; i < attrs.getLength(); ++i) {
                // assumption that if parent node is of type IndexedRegion,
                // then its attributes will also be of type IndexedRegion
                IndexedRegion attRegion = (IndexedRegion) attrs.item(i);
                if (attRegion.contains(offset)) {
                    return (Attr) attrs.item(i);
                }
            }
        }
        return null;
    }

    /**
     * Returns the node the cursor is currently on in the document. null if no node is selected
     * 
     * @param offset
     * @return Node either element, doctype, text, or null
     */
    private Node getCurrentNode(IDocument document, int offset) {
        // get the current node at the offset (returns either: element,
        // doctype, text)
        IndexedRegion inode = null;
        IStructuredModel sModel = null;
        try {
            sModel = StructuredModelManager.getModelManager().getExistingModelForRead(document);
            inode = sModel.getIndexedRegion(offset);
            if (inode == null)
                inode = sModel.getIndexedRegion(offset - 1);
        }
        finally {
            if (sModel != null)
                sModel.releaseFromRead();
        }

        if (inode instanceof Node) {
            return (Node) inode;
        }
        return null;
    }

    private IRegion getHyperlinkRegion(Node node) {
        IRegion hyperRegion = null;

        if (node != null) {
            short nodeType = node.getNodeType();
            if (nodeType == Node.DOCUMENT_TYPE_NODE) {
                // handle doc type node
                IDOMNode docNode = (IDOMNode) node;
                hyperRegion = new Region(docNode.getStartOffset(), docNode.getEndOffset()
                        - docNode.getStartOffset());
            }
            else if (nodeType == Node.ATTRIBUTE_NODE) {
                // handle attribute nodes
                IDOMAttr att = (IDOMAttr) node;
                // do not include quotes in attribute value region
                int regOffset = att.getValueRegionStartOffset();
                int regLength = att.getValueRegionText().length();
                String attValue = att.getValueRegionText();
                if (StringUtils.isQuoted(attValue)) {
                    regOffset = ++regOffset;
                    regLength = regLength - 2;
                }
                hyperRegion = new Region(regOffset, regLength);
            }
        }
        return hyperRegion;
    }

    /**
     * Checks to see if the given attribute is openable. Attribute is openable if it is a namespace
     * declaration attribute or if the attribute value is of type URI.
     * 
     * @param attr
     *            cannot be null
     * @param cmElement
     *            CMElementDeclaration associated with the attribute (can be null)
     * @return true if this attribute is "openOn-able" false otherwise
     */
    private boolean isLinkableAttr(Attr attr) {
        String attrName = attr.getName();

        if ("class".equals(attrName)) {
            return true;
        }
        else if ("name".equals(attrName) && "property".equals(attr.getOwnerElement().getNodeName())) {
            return true;
        }

        return false;
    }

    /**
     * Finds a target methodName with specific number of arguments on the type hierarchy of given
     * type.
     * 
     * @param type
     *            The Java type object on which to retrieve the method
     * @param methodName
     *            Name of the method
     * @param argCount
     *            Number of arguments for the desired method
     * @param isPublic
     *            true if public method is requested
     * @param isStatic
     *            true if static method is requested
     */
    public IMethod findMethod(IType type, String methodName, int argCount, boolean isPublic,
            boolean isStatic) throws JavaModelException {
        while (type != null) {
            IMethod[] methods = type.getMethods();
            for (int i = 0; i < methods.length; i++) {
                IMethod method = methods[i];
                int flags = method.getFlags();
                if (Flags.isPublic(flags) == isPublic && Flags.isStatic(flags) == isStatic
                        && (argCount == -1 || method.getNumberOfParameters() == argCount)
                        && methodName.equalsIgnoreCase(method.getElementName())) {
                    return method;
                }
            }
            type = getSuperType(type);
        }
        return null;
    }

    /**
     * Returns super type of given type.
     */
    protected IType getSuperType(IType type) throws JavaModelException {
        String name = type.getSuperclassName();
        if (name != null) {
            if (type.isBinary()) {
                return type.getJavaProject().findType(name);
            }
            else {
                String[][] resolvedNames = type.resolveType(name);
                if (resolvedNames != null && resolvedNames.length > 0) {
                    String resolvedName = concatenate(resolvedNames[0][0], resolvedNames[0][1], ".");
                    return type.getJavaProject().findType(resolvedName);
                }
            }
        }
        return null;
    }

    /**
     * Returns concatenated text from given two texts delimited by given delimiter. Both texts can
     * be empty or <code>null</code>.
     */
    protected String concatenate(String text1, String text2, String delimiter) {
        StringBuffer buf = new StringBuffer();
        if (text1 != null && text1.length() > 0) {
            buf.append(text1);
        }
        if (text2 != null && text2.length() > 0) {
            if (buf.length() > 0) {
                buf.append(delimiter);
            }
            buf.append(text2);
        }
        return buf.toString();
    }
}
