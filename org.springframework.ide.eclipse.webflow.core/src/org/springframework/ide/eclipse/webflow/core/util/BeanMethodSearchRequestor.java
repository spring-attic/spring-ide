package org.springframework.ide.eclipse.webflow.core.util;


import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.MethodSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.PublicMethodSearchRequestor;

/**
 * 
 */
@SuppressWarnings("restriction")
public class BeanMethodSearchRequestor extends PublicMethodSearchRequestor {

	/**
	 * 
	 */
	public static final int PROPERTY_RELEVANCE = 5;

	/**
	 * 
	 * 
	 * @param request 
	 */
	public BeanMethodSearchRequestor(ContentAssistRequest request) {
		super(request);
	}

	/**
	 * Check methods for the following signatur public Event
	 * execute(RequestContext context) throws Exception;.
	 * 
	 * @param prefix 
	 * @param method 
	 * 
	 * @throws CoreException 
	 */
	public void acceptSearchMatch(IMethod method, String prefix)
			throws CoreException {
		if (method.getElementName().toLowerCase().startsWith(
				prefix.toLowerCase())
				&& Flags.isPublic(method.getFlags())
				&& !Flags.isInterface(method.getFlags())
				&& method.exists()
				&& ((IType) method.getParent()).isClass()
				&& !method.isConstructor()) {

			if (method.getElementName().startsWith("set")) {
				String propertyName = method.getElementName().substring(3);
				Set<IMethod> methods = Introspector.findWritableProperties(
						method.getDeclaringType(), propertyName);
				if (methods.contains(method)) {
					createMethodProposal(method, PROPERTY_RELEVANCE);
				}
				else {
					createMethodProposal(method,
							MethodSearchRequestor.METHOD_RELEVANCE);
				}
			}
			else {
				createMethodProposal(method,
						MethodSearchRequestor.METHOD_RELEVANCE);
			}

		}
	}
}
