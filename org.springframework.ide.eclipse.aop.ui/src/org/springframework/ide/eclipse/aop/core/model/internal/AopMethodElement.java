/*
 * Copyright 2002-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.aop.core.model.internal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceRange;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;

@SuppressWarnings("restriction")
public class AopMethodElement implements IMethod {

    private IResource file;

    private IJavaProject project;

    private IDOMNode node;

    private IDOMDocument document;

    private int lineNumber = -1;
    
    private SourceRange sourceRange;
    
    private IMethod aspectMethod;
    
    @SuppressWarnings("deprecation")
    public AopMethodElement(IResource file, IDOMNode node, IDOMDocument document, IMethod aspectMethod) {
        this.file = file;
        this.node = node;
        this.document = document;
        this.aspectMethod = aspectMethod;
        init();
    }

    private void init() {
        try {
            if (this.file.getProject().hasNature(JavaCore.NATURE_ID)) {
                this.project = JavaCore.create(this.file.getProject());
            }
        }
        catch (CoreException e) {
        }
        this.lineNumber = this.document.getStructuredDocument().getLineOfOffset(
                this.node.getStartOffset()) + 1;
        
        this.sourceRange = new SourceRange(this.node.getStartOffset(), this.node.getLength());
    }

    public boolean exists() {
        return true;
    }

    public IJavaElement getAncestor(int ancestorType) {
        return null;
    }

    public String getAttachedJavadoc(IProgressMonitor monitor)
            throws JavaModelException {
        return null;
    }

    public IResource getCorrespondingResource() throws JavaModelException {
        return this.file;
    }

    public String getElementName() {
        return this.file.getName();
    }

    public int getElementType() {
        return 0;
    }

    public String getHandleIdentifier() {
        return null;
    }

    public IJavaModel getJavaModel() {
        return this.project.getJavaModel();
    }

    public IJavaProject getJavaProject() {
        return this.project;
    }

    public IOpenable getOpenable() {
        return null;
    }

    public IJavaElement getParent() {
        return null;
    }

    public IPath getPath() {
        return this.file.getProjectRelativePath();
    }

    public IJavaElement getPrimaryElement() {
        return null;
    }

    public IResource getResource() {
        return this.file;
    }

    public ISchedulingRule getSchedulingRule() {
        return null;
    }

    public IResource getUnderlyingResource() throws JavaModelException {
        return this.file;
    }

    public boolean isReadOnly() {
        return false;
    }

    public boolean isStructureKnown() throws JavaModelException {
        return false;
    }

    public Object getAdapter(Class adapter) {
        return null;
    }

    public String[] getExceptionTypes() throws JavaModelException {
        return null;
    }

    public String getKey() {
        return null;
    }

    public int getNumberOfParameters() {
        return 0;
    }

    public String[] getParameterNames() throws JavaModelException {
        return null;
    }

    public String[] getParameterTypes() {
        return null;
    }

    public String[] getRawParameterNames() throws JavaModelException {
        return null;
    }

    public String getReturnType() throws JavaModelException {
        return null;
    }

    public String getSignature() throws JavaModelException {
        return null;
    }

    public ITypeParameter getTypeParameter(String name) {
        return null;
    }

    public String[] getTypeParameterSignatures() throws JavaModelException {
        return null;
    }

    public ITypeParameter[] getTypeParameters() throws JavaModelException {
        return null;
    }

    public boolean isConstructor() throws JavaModelException {
        return false;
    }

    public boolean isMainMethod() throws JavaModelException {
        return false;
    }

    public boolean isResolved() {
        return false;
    }

    public boolean isSimilar(IMethod method) {
        return false;
    }

    public String[] getCategories() throws JavaModelException {
        return null;
    }

    public IClassFile getClassFile() {
        return null;
    }

    public ICompilationUnit getCompilationUnit() {
        return null;
    }

    public IType getDeclaringType() {
        return null;
    }

    public int getFlags() throws JavaModelException {
        return 0;
    }

    public ISourceRange getJavadocRange() throws JavaModelException {
        return null;
    }

    public ISourceRange getNameRange() throws JavaModelException {
        return null;
    }

    public int getOccurrenceCount() {
        return 0;
    }

    public IType getType(String name, int occurrenceCount) {
        return null;
    }

    public boolean isBinary() {
        return false;
    }

    public String getSource() throws JavaModelException {
        // TODO Auto-generated method stub
        return null;
    }

    public ISourceRange getSourceRange() throws JavaModelException {
        return this.sourceRange;
    }

    public void copy(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException {
    }

    public void delete(boolean force, IProgressMonitor monitor) throws JavaModelException {
    }

    public void move(IJavaElement container, IJavaElement sibling, String rename, boolean replace, IProgressMonitor monitor) throws JavaModelException {
    }

    public void rename(String name, boolean replace, IProgressMonitor monitor) throws JavaModelException {
    }

    public IJavaElement[] getChildren() throws JavaModelException {
        return null;
    }

    public boolean hasChildren() throws JavaModelException {
        return false;
    }
    
    public int getLineNumber() {
        return this.lineNumber;
    }

    public IMethod getAspectMethod() {
        return this.aspectMethod;
    }
}
