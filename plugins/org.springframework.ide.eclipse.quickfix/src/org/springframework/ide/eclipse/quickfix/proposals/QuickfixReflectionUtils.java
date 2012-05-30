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
package org.springframework.ide.eclipse.quickfix.proposals;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.quickfix.Activator;
import org.springsource.ide.eclipse.commons.core.StatusHandler;


/**
 * Util class for reflections needed for quickfix to work in both Eclipse 3.3
 * and 3.4
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class QuickfixReflectionUtils {

	public static boolean applyProposal(Object proposal, IDocument document) {
		Method method;
		try {
			method = proposal.getClass().getMethod("apply", IDocument.class);
			method.invoke(proposal, document);
			return true;
		}
		catch (SecurityException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (NoSuchMethodException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalArgumentException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalAccessException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		return false;
	}

	public static Object callProtectedMethod(Object target, String methodName) {
		Class<?> clazz = target.getClass();
		try {
			Method method = clazz.getDeclaredMethod(methodName);
			method.setAccessible(true);
			return method.invoke(target);
		}
		catch (SecurityException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (NoSuchMethodException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalArgumentException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalAccessException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}

		return null;
	}

	public static Object[] createChangeDescriptionArray(int length) {
		Class<?> clazz = getChangeMethodInnerClass("ChangeDescription");

		if (clazz == null) {
			return null;
		}

		Object array = Array.newInstance(clazz, length);
		return (Object[]) array;
	}

	public static Object createChangeMethodSignatureProposal(String label, ICompilationUnit targetCU,
			ClassInstanceCreation invocationNode, IMethodBinding methodBinding, Object[] changeDesc, int relevance,
			Image image) {
		Class<?> clazz = null;
		try {
			clazz = Class
					.forName("org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeMethodSignatureProposal");
		}
		catch (ClassNotFoundException e) {
			try {
				clazz = Class.forName("org.eclipse.jdt.internal.ui.text.correction.ChangeMethodSignatureProposal");
			}
			catch (ClassNotFoundException e1) {
				StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
				return null;
			}
		}

		Class<?> arrayClass = createChangeDescriptionArray(0).getClass();
		try {
			Constructor<?> constructor = clazz.getConstructor(String.class, ICompilationUnit.class, ASTNode.class,
					IMethodBinding.class, arrayClass, arrayClass, Integer.TYPE, Image.class);
			return constructor.newInstance(label, targetCU, invocationNode, methodBinding, changeDesc, null, relevance,
					image);

		}
		catch (SecurityException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (NoSuchMethodException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalArgumentException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InstantiationException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalAccessException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}

		return null;
	}

	public static Object createInsertDescription(ITypeBinding objectBinding, String name) {
		Class<?> clazz = getChangeMethodInnerClass("InsertDescription");
		if (clazz == null) {
			return null;
		}

		try {
			Constructor<?> constructor = clazz.getConstructor(ITypeBinding.class, String.class);
			return constructor.newInstance(objectBinding, name);
		}
		catch (SecurityException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (NoSuchMethodException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalArgumentException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InstantiationException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalAccessException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}

		return null;
	}

	public static Object createNewFieldProposal(String label, ICompilationUnit targetCU, SimpleName simpleName,
			ITypeBinding binding, int relevance, Image image) {
		Class<?> clazz = null;
		try {
			clazz = Class
					.forName("org.eclipse.jdt.internal.ui.text.correction.proposals.NewVariableCorrectionProposal");
		}
		catch (ClassNotFoundException e) {
			try {
				clazz = Class.forName("org.eclipse.jdt.internal.ui.text.correction.NewVariableCompletionProposal");
			}
			catch (ClassNotFoundException e1) {
				StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
				return null;
			}
		}

		try {
			int variableKind = clazz.getField("FIELD").getInt(null);
			Constructor<?> constructor = clazz.getConstructor(String.class, ICompilationUnit.class, Integer.TYPE,
					SimpleName.class, ITypeBinding.class, Integer.TYPE, Image.class);
			return constructor.newInstance(label, targetCU, variableKind, simpleName, binding, relevance, image);
		}
		catch (IllegalArgumentException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (SecurityException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalAccessException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (NoSuchFieldException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (NoSuchMethodException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InstantiationException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}

		return null;
	}

	public static Object createNewMethodProposal(String label, ICompilationUnit targetCU, ASTNode invocationNode,
			List<?> arguments, ITypeBinding binding, int relevance, Image image) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName("org.eclipse.jdt.internal.ui.text.correction.proposals.NewMethodCorrectionProposal");
		}
		catch (ClassNotFoundException e) {
			try {
				clazz = Class.forName("org.eclipse.jdt.internal.ui.text.correction.NewMethodCompletionProposal");
			}
			catch (ClassNotFoundException e1) {
				StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
				return null;
			}
		}

		try {
			Constructor<?> constructor = clazz.getConstructor(String.class, ICompilationUnit.class, ASTNode.class,
					List.class, ITypeBinding.class, Integer.TYPE, Image.class);
			return constructor.newInstance(label, targetCU, invocationNode, arguments, binding, relevance, image);

		}
		catch (SecurityException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (NoSuchMethodException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalArgumentException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InstantiationException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalAccessException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}

		return null;
	}

	public static Object createRemoveDescription() {
		Class<?> clazz = getChangeMethodInnerClass("RemoveDescription");
		if (clazz == null) {
			return null;
		}

		try {
			Constructor<?> constructor = clazz.getConstructor();
			return constructor.newInstance();
		}
		catch (SecurityException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (NoSuchMethodException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalArgumentException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InstantiationException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (IllegalAccessException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}
		catch (InvocationTargetException e) {
			StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
		}

		return null;
	}

	private static Class<?> getChangeMethodInnerClass(String innerClassName) {
		Class<?> clazz = null;
		String className = "org.eclipse.jdt.internal.ui.text.correction.proposals.ChangeMethodSignatureProposal";
		try {
			clazz = Class.forName(className);
		}
		catch (ClassNotFoundException e) {
			className = "org.eclipse.jdt.internal.ui.text.correction.ChangeMethodSignatureProposal";
			try {
				clazz = Class.forName(className);
			}
			catch (ClassNotFoundException e1) {
				StatusHandler.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "reflection error", e));
				return null;
			}
		}

		Class<?>[] innerClasses = clazz.getClasses();
		Class<?> innerClazz = null;
		String changeDescriptionClassName = className + "." + innerClassName;
		for (Class<?> innerClass : innerClasses) {
			String name = innerClass.getCanonicalName();
			if (name != null && name.equals(changeDescriptionClassName)) {
				innerClazz = innerClass;
				break;
			}
		}

		return innerClazz;
	}

	public static IPath getRelativePath(IPath basePath, IPath path) {
		try {
			Method method = IPath.class.getMethod("makeRelativeTo", IPath.class);
			return (IPath) method.invoke(path, basePath);
		}
		catch (SecurityException e) {
		}
		catch (NoSuchMethodException e) {
		}
		catch (IllegalArgumentException e) {
		}
		catch (IllegalAccessException e) {
		}
		catch (InvocationTargetException e) {
		}

		int commonLength = path.matchingFirstSegments(basePath);
		final int differenceLength = basePath.segmentCount() - commonLength;
		final int newSegmentLength = differenceLength + path.segmentCount() - commonLength;
		if (newSegmentLength == 0) {
			return Path.EMPTY;
		}
		String[] newSegments = new String[newSegmentLength];
		// add parent references for each segment different from the base
		Arrays.fill(newSegments, 0, differenceLength, ".."); //$NON-NLS-1$
		// append the segments of this path not in common with the base
		System.arraycopy(path.segments(), commonLength, newSegments, differenceLength, newSegmentLength
				- differenceLength);
		IPath result = new Path(null, newSegments[0]);
		for (int i = 1; i < newSegments.length; i++) {
			result = result.append(newSegments[i]);
		}

		return result;
	}
}
