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

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeansJavaCompletionProposal;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class BeanReferenceSearchRequestor {

    public static final int EXTERNAL_BEAN_RELEVANCE = 10;
    public static final int LOCAL_BEAN_RELEVANCE = 20;

    protected Set<String> beans;
    protected ContentAssistRequest request;

    public BeanReferenceSearchRequestor(ContentAssistRequest request) {
        this.request = request;
        this.beans = new HashSet<String>();
    }

    public void acceptSearchMatch(IBean bean, IFile file, String prefix) {
        if (bean.getElementName() != null && bean.getElementName().startsWith(prefix)) {
            String beanName = bean.getElementName();
            String replaceText = beanName;
            String fileName = bean.getElementResource().getProjectRelativePath().toString();
            String key = beanName + fileName;
            if (!beans.contains(key)) {
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
                beans.add(key);
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
                if (!beans.contains(key)) {
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
                    beans.add(key);
                }
            }
        }
    }
}