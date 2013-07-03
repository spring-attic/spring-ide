/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.computers;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaTypeCompletionProposal;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.SourceViewer;
import org.springframework.ide.eclipse.quickfix.Activator;
import org.springframework.ide.eclipse.quickfix.jdt.util.ProposalCalculatorUtil;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * Content assist proposal computer for showing user a list of allowable type
 * for a
 * @RequestMapping method parameter. See {@link RequestMapping}
 * 
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.6
 */
public class RequestMappingParamTypeProposalComputer extends JavaCompletionProposalComputer {

	// TODO: add javax.portlet.PortletRequest
	// TODO: add javax.portlet.ActionRequest
	// TODO: add javax.portlet.RenderRequest
	// TODO: add javax.portlet.PortletSession
	// TODO: add @RequestParam

	private static Class<?>[] PARAM_TYPE_CLASSES = new Class<?>[] { ServletRequest.class, HttpServletRequest.class,
			HttpSession.class, WebRequest.class, NativeWebRequest.class, Locale.class, InputStream.class, Reader.class,
			OutputStream.class, Writer.class, Map.class, Model.class, ModelMap.class, Errors.class,
			BindingResult.class, SessionStatus.class };

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor) {
		if (context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;

			// check if project is a spring project
			if (SpringCoreUtils.isSpringProject(javaContext.getProject().getProject())) {
				ICompilationUnit cu = javaContext.getCompilationUnit();
				if (ProposalCalculatorUtil.hasAnnotationOnType(cu, "Controller")) {
					ITextViewer viewer = javaContext.getViewer();

					if (viewer instanceof SourceViewer) {
						SourceViewer sourceViewer = (SourceViewer) viewer;
						int invocationOffset = context.getInvocationOffset();
						AssistContext assistContext = new AssistContext(cu, invocationOffset, 0);
						ASTNode node = assistContext.getCoveringNode();

						// cursor is at the beginning of an empty param list
						// [method(^)}
						if (node instanceof MethodDeclaration) {
							MethodDeclaration methodDecl = (MethodDeclaration) node;
							if (ProposalCalculatorUtil.hasAnnotation("RequestMapping", methodDecl)) {

								boolean found = true;
								int startIndex = invocationOffset;
								IDocument document = sourceViewer.getDocument();

								while (!found && startIndex > methodDecl.getStartPosition()) {
									try {
										char currChar = document.getChar(startIndex);
										if (Character.isWhitespace(currChar)) {
											startIndex--;
										}
										if ('(' == currChar) {
											found = true;
										}
									}
									catch (BadLocationException e) {
										StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
												e.getMessage(), e));
										break;
									}
								}

								if (found) {
									return getProposals(methodDecl, "", invocationOffset, null, javaContext);
								}
							}
						}

						else if (node instanceof SimpleName) {
							SimpleName name = (SimpleName) node;
							ASTNode parentNode = name.getParent();
							// cursor is at the start of a param at the end of
							// the
							// param
							// list [metohd(param, ^)]
							if (parentNode instanceof VariableDeclarationFragment) {
								parentNode = parentNode.getParent();
								if (parentNode instanceof VariableDeclarationStatement) {
									VariableDeclarationStatement varDeclStmt = (VariableDeclarationStatement) parentNode;
									Type varDeclType = varDeclStmt.getType();
									if (varDeclType instanceof SimpleType) {
										SimpleType sType = (SimpleType) varDeclType;
										parentNode = parentNode.getParent();
										if (parentNode instanceof Block) {
											Block block = (Block) parentNode;

											try {
												if (viewer.getDocument().getChar(block.getStartPosition()) != '{') {
													parentNode = parentNode.getParent();
													if (parentNode instanceof MethodDeclaration) {
														MethodDeclaration methodDecl = (MethodDeclaration) parentNode;
														return getProposals(methodDecl, sType.getName()
																.getFullyQualifiedName(), invocationOffset,
																varDeclStmt, javaContext);
													}
												}
											}
											catch (BadLocationException e) {
												StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
														.getMessage(), e));
											}
										}
									}
								}
							}

							// cursor is at the start of a param type
							else if (parentNode instanceof SimpleType) {
								SimpleType sType = (SimpleType) parentNode;
								parentNode = sType.getParent();
								if (parentNode instanceof SingleVariableDeclaration) {
									SingleVariableDeclaration varDecl = (SingleVariableDeclaration) parentNode;
									parentNode = varDecl.getParent();
									if (parentNode instanceof MethodDeclaration) {
										MethodDeclaration methodDecl = (MethodDeclaration) parentNode;
										return getProposals(methodDecl, sType.getName().getFullyQualifiedName(),
												invocationOffset, sType, javaContext);
									}
								}
							}
						}

						// param at the end of a method param list
						// [method(param,
						// w^)]
						else if (node instanceof Block) {
							Block block = (Block) node;
							ASTNode parentNode = block.getParent();
							if (parentNode instanceof MethodDeclaration) {
								MethodDeclaration methodDecl = (MethodDeclaration) parentNode;
								try {
									String blockContent = viewer.getDocument().get(block.getStartPosition(),
											block.getLength());
									if (blockContent.startsWith(",")) {
										blockContent = blockContent.substring(1);

										boolean isAnnotation = false;
										while (blockContent.length() > 0) {
											char currChar = blockContent.charAt(0);
											if (Character.isWhitespace(currChar)) {
												blockContent = blockContent.substring(1);
												isAnnotation = false;
											}
											else if (currChar == '@') {
												blockContent = blockContent.substring(1);
												isAnnotation = true;
											}
											else if (Character.isLetter(currChar)) {
												if (!isAnnotation) {
													break;
												}
												else {
													blockContent = blockContent.substring(1);
												}
											}
											else {
												break;
											}
										}

										// skip annotations
										if (blockContent.length() > 0) {
											if (blockContent.charAt(0) == '@') {
												blockContent = blockContent.substring(1);
											}
										}

										StringBuilder filter = new StringBuilder();
										while (blockContent.length() > 0) {
											char currChar = blockContent.charAt(0);
											if (Character.isLetter(currChar)) {
												filter.append(currChar);
												blockContent = blockContent.substring(1);
											}
											else {
												break;
											}
										}

										return getProposals(methodDecl, filter.toString(), invocationOffset, block,
												javaContext);
									}
								}
								catch (BadLocationException e) {
									StatusHandler
											.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
								}
							}
						}

					}
				}
			}
		}

		return Collections.emptyList();
	}

	private List<ICompletionProposal> getProposals(MethodDeclaration methodDecl, String filter, int offset,
			ASTNode toBeRemoved, JavaContentAssistInvocationContext context) {
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		for (Class<?> paramType : PARAM_TYPE_CLASSES) {
			if (paramType.getSimpleName().toLowerCase().startsWith(filter.toLowerCase())) {
				// proposals
				// .add(new
				// RequestMappingParamTypeCompletionProposal(methodDecl,
				// paramType, toBeRemoved, context));
				CompletionProposal proposal = CompletionProposal.create(CompletionProposal.TYPE_REF,
						context.getInvocationOffset());
				proposal.setCompletion(paramType.getCanonicalName().toCharArray());
				proposal.setDeclarationSignature(paramType.getPackage().getName().toCharArray());
				proposal.setFlags(paramType.getModifiers());
				proposal.setRelevance(Integer.MAX_VALUE);
				proposal.setReplaceRange(context.getInvocationOffset() - filter.length(), context.getInvocationOffset());
				proposal.setSignature(Signature.createTypeSignature(paramType.getCanonicalName(), true).toCharArray());
				LazyJavaTypeCompletionProposal typeProposal = new LazyJavaTypeCompletionProposal(proposal, context);
				typeProposal.setRelevance(Integer.MAX_VALUE);
				proposals.add(typeProposal);
			}
		}

		return proposals;
	}

}
