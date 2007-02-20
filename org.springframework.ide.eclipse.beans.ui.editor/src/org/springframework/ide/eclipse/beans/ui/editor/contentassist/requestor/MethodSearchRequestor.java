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

package org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;

@SuppressWarnings("restriction")
public abstract class MethodSearchRequestor {

    public static final int METHOD_RELEVANCE = 10;

    protected ContentAssistRequest request;
    protected JavaElementImageProvider imageProvider;
    protected Set<String> methods;

    public MethodSearchRequestor(ContentAssistRequest request) {
        this.request = request;
        this.methods = new HashSet<String>();
        this.imageProvider = new JavaElementImageProvider();
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

    protected String getReturnType(IMethod method, boolean classTypesOnly) {
        try {
            String qualifiedReturnType = Signature.getReturnType(method.getSignature());
            if (!classTypesOnly || qualifiedReturnType.startsWith("L")
            		|| qualifiedReturnType.startsWith("Q")) {
            	return Signature.getSignatureSimpleName(qualifiedReturnType.replace('/', '.'));
            }
        }
        catch (IllegalArgumentException e) {
        }
        catch (JavaModelException e) {
        }
        return null;
    }
}