/*******************************************************************************
 * Copyright (c) 2006, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.outline.DelegatingLabelProvider;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Utility class for accepting bean matches and creating completion proposals for those bean matches.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeanReferenceSearchRequestor {

	public static final String LABEL_SEPARATOR = " -------------------------------------------- ";

	public static final int TYPE_MATCHING_RELEVANCE = 20;

	public static final int RELEVANCE = 10;

	protected Set<String> beans;

	protected IContentAssistProposalRecorder recorder;

	protected List<String> requiredTypes = null;

	private boolean insertedMatchingType = false;
	
	private boolean matchingTypeFound = false;

	public BeanReferenceSearchRequestor(IContentAssistProposalRecorder recorder) {
		this(recorder, new ArrayList<String>());
	}

	public BeanReferenceSearchRequestor(IContentAssistProposalRecorder recorder, List<String> requiredTypes) {
		this.recorder = recorder;
		this.beans = new HashSet<String>();
		this.requiredTypes = requiredTypes;
	}

	public void acceptSearchMatch(IBean bean, IFile file, String prefix) {
		if (bean.getElementName() != null && bean.getElementName().toLowerCase().startsWith(prefix.toLowerCase())) {
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

				Image image = null;
				if (Display.getCurrent() != null) {
					image = BeansUIPlugin.getLabelProvider().getImage(bean);
				}

				boolean matchesType = false;
				if (requiredTypes.size() > 0) {
					String className = BeansModelUtils.getBeanClass(bean, null);
					IType type = JdtUtils.getJavaType(file.getProject(), className);
					List<String> hierachyTypes = JdtUtils.getFlatListOfClassAndInterfaceNames(type, type);
					for (String cn : hierachyTypes) {
						if (this.requiredTypes.contains(cn)) {
							matchesType = true;
							break;
						}
					}
				}
				
				if (!insertedMatchingType && matchingTypeFound && !matchesType) {
					recorder.recordProposal(BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONTENT_ASSIST),
							TYPE_MATCHING_RELEVANCE - 1, LABEL_SEPARATOR, "");
					insertedMatchingType = true;
				}

				if (matchesType) {
					recorder.recordProposal(image, TYPE_MATCHING_RELEVANCE, displayText, replaceText, null);
					matchingTypeFound = true;
				}
				else {
					recorder.recordProposal(image, RELEVANCE, displayText, replaceText, bean);
				}
				

				beans.add(key);
			}
		}
	}

	public void acceptSearchMatch(String beanId, Node beanNode, IFile file, String prefix) {
		if (beanNode != null) {
			NamedNodeMap attributes = beanNode.getAttributes();
			if (beanId.toLowerCase().startsWith(prefix.toLowerCase())) {
				if (beanNode.getParentNode() != null) {
					String beanName = beanId;
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
						buf.append(" - ");
						buf.append(fileName);
						String displayText = buf.toString();
						Image image = null;
						if (Display.getCurrent() != null) {
							image = new DelegatingLabelProvider().getImage(beanNode);
						}
	
						String className = BeansEditorUtils.getClassNameForBean(beanNode);
						boolean matchesType = false;
						if (requiredTypes.size() > 0) {
							IType type = JdtUtils.getJavaType(file.getProject(), className);
							List<String> hierachyTypes = JdtUtils.getFlatListOfClassAndInterfaceNames(type, type);
							for (String cn : hierachyTypes) {
								if (this.requiredTypes.contains(cn)) {
									matchesType = true;
									break;
								}
							}
						}
	
						if (!insertedMatchingType && matchingTypeFound && !matchesType) {
							recorder.recordProposal(BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONTENT_ASSIST),
									TYPE_MATCHING_RELEVANCE - 1, LABEL_SEPARATOR, "");
							insertedMatchingType = true;
						}
	
						if (matchesType) {
							recorder.recordProposal(image, TYPE_MATCHING_RELEVANCE, displayText, replaceText, beanNode);
							matchingTypeFound = true;
						}
						else {
							recorder.recordProposal(image, RELEVANCE, displayText, replaceText, beanNode);
						}
	
						beans.add(key);
					}
				}
			}
		}
	}
}
