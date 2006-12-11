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
package org.springframework.ide.eclipse.aop.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;
import org.springframework.ide.eclipse.core.SpringCore;

@SuppressWarnings("restriction")
public class BeansAopMarkerUtils {

	public static final String PROBLEM_MARKER = BeansAopPlugin.PLUGIN_ID
			+ ".aopmarker";

	public static final String BEFORE_ADVICE_MARKER = BeansAopPlugin.PLUGIN_ID
			+ ".beforeadvicemarker";

	public static final String AFTER_ADVICE_MARKER = BeansAopPlugin.PLUGIN_ID
			+ ".afteradvicemarker";

	public static final String AROUND_ADVICE_MARKER = BeansAopPlugin.PLUGIN_ID
			+ ".aroundadvicemarker";

	public static final String SOURCE_BEFORE_ADVICE_MARKER = BeansAopPlugin.PLUGIN_ID
			+ ".sourcebeforeadvicemarker";

	public static final String SOURCE_AFTER_ADVICE_MARKER = BeansAopPlugin.PLUGIN_ID
			+ ".sourceafteradvicemarker";

	public static final String SOURCE_AROUND_ADVICE_MARKER = BeansAopPlugin.PLUGIN_ID
			+ ".sourcearoundadvicemarker";

	public static final String ADIVCE_TYPE = "adivice_type";

	public static Map<ADVICE_TYPES, String> sourceMarkerMapping;

	public static Map<ADVICE_TYPES, String> targetMarkerMapping;

	static {
		sourceMarkerMapping = new HashMap<ADVICE_TYPES, String>();
		sourceMarkerMapping.put(ADVICE_TYPES.BEFORE,
				SOURCE_BEFORE_ADVICE_MARKER);
		sourceMarkerMapping.put(ADVICE_TYPES.AFTER, SOURCE_AFTER_ADVICE_MARKER);
		sourceMarkerMapping.put(ADVICE_TYPES.AFTER_RETURNING,
				SOURCE_AFTER_ADVICE_MARKER);
		sourceMarkerMapping.put(ADVICE_TYPES.AFTER_THROWING,
				SOURCE_AFTER_ADVICE_MARKER);
		sourceMarkerMapping.put(ADVICE_TYPES.AROUND,
				SOURCE_AROUND_ADVICE_MARKER);

		targetMarkerMapping = new HashMap<ADVICE_TYPES, String>();
		targetMarkerMapping.put(ADVICE_TYPES.BEFORE, BEFORE_ADVICE_MARKER);
		targetMarkerMapping.put(ADVICE_TYPES.AFTER, AFTER_ADVICE_MARKER);
		targetMarkerMapping.put(ADVICE_TYPES.AFTER_RETURNING,
				AFTER_ADVICE_MARKER);
		targetMarkerMapping.put(ADVICE_TYPES.AFTER_THROWING,
				AFTER_ADVICE_MARKER);
		targetMarkerMapping.put(ADVICE_TYPES.AROUND, AROUND_ADVICE_MARKER);
	}

	public static void createMarker(IAopReference reference) {
		createSourceMarker(reference, sourceMarkerMapping.get(reference
				.getAdviceType()));
		createTargetMarker(reference, targetMarkerMapping.get(reference
				.getAdviceType()));
	}

	public static void createTargetMarker(IAopReference reference,
			String markerId) {
		BeansAopUtils.createProblemMarker(reference.getTarget()
				.getResource(), "test", 1, BeansAopUtils
				.getLineNumber(reference.getTarget()), markerId);
	}

	public static void createSourceMarker(IAopReference reference,
			String markerId) {
		BeansAopUtils.createProblemMarker(reference.getResource(), "test",
				1, reference.getDefinition().getLineNumber(), markerId);
		BeansAopUtils.createProblemMarker(reference.getSource()
				.getResource(), "test", 1, BeansAopUtils
				.getLineNumber(reference.getSource()), markerId);
	}

	public static void deleteProblemMarkers(IProject resource) {
		if (resource != null && resource.isAccessible()) {
			try {
				resource.deleteMarkers(BeansAopMarkerUtils.PROBLEM_MARKER,
						true, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
	}

}
