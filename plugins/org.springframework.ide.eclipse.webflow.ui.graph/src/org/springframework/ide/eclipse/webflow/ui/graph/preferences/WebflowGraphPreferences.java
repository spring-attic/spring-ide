/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.prefs.BackingStoreException;
import org.springframework.ide.eclipse.webflow.core.internal.model.ActionState;
import org.springframework.ide.eclipse.webflow.core.internal.model.DecisionState;
import org.springframework.ide.eclipse.webflow.core.internal.model.EndState;
import org.springframework.ide.eclipse.webflow.core.internal.model.SubflowState;
import org.springframework.ide.eclipse.webflow.core.internal.model.ViewState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.Activator;

/**
 * Utility class that handles preferences for the Web Flow Graph plugin.
 * <p>
 * Currently only handles color instances for certain
 * {@link IWebflowModelElement}.
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class WebflowGraphPreferences {

	private static class ColorFactory {
		private static Color getColor(final RGB rgb) {
			Display display1 = Display.getCurrent();
			if (display1 != null)
				return new Color(display1, rgb);
			final Display display2 = Display.getDefault();
			final Color result[] = new Color[1];
			display2.syncExec(new Runnable() {
				public void run() {
					synchronized (result) {
						result[0] = new Color(display2, rgb);
					}
				}
			});
			synchronized (result) {
				return result[0];
			}
		}
	}

	private static final Color DEFAULT_COLOR = ColorConstants.button;

	private static final Map<Class, Color> DEFAULT_COLORS;

	static {
		DEFAULT_COLORS = new HashMap<Class, Color>();
		DEFAULT_COLORS.put(ViewState.class, ColorFactory.getColor(new RGB(198,
				220, 235))); // #c6dceb
		DEFAULT_COLORS.put(ActionState.class, ColorFactory.getColor(new RGB(
				203, 235, 169))); // #cbeba9
		DEFAULT_COLORS.put(SubflowState.class, ColorFactory.getColor(new RGB(
				154, 212, 167))); // #9ad4a7
		DEFAULT_COLORS.put(DecisionState.class, ColorFactory.getColor(new RGB(
				255, 239, 169))); // #ffefa9
		DEFAULT_COLORS.put(EndState.class, ColorFactory.getColor(new RGB(220,
				220, 220))); // #dedede
	}

	private static Map<Class, Color> classToColorMapCache = new ConcurrentHashMap<Class, Color>();

	private static String DEFAULT_COLOR_STRING = StringConverter
			.asString(DEFAULT_COLOR.getRGB());

	protected static void clearColorCache() {
		classToColorMapCache.clear();
	}

	public static Color getColorForModelElement(IWebflowModelElement element) {
		if (classToColorMapCache.containsKey(element.getClass())) {
			return classToColorMapCache.get(element.getClass());
		}
		else {
			Color color = getColorFromPreferences(element.getClass());
			classToColorMapCache.put(element.getClass(), color);
			return color;
		}
	}

	private static Color getColorFromPreferences(Class element) {
		IScopeContext context = new InstanceScope();
		IEclipsePreferences node = context.getNode(Activator.PLUGIN_ID);

		String value = node.get(getPreferenceName(element.getName()),
				getDefaultColorStringForModelElementClass(element));
		RGB rgb = StringConverter.asRGB(value);

		return ColorFactory.getColor(rgb);
	}

	public static String getDefaultColorStringForModelElementClass(Class element) {
		if (DEFAULT_COLORS.containsKey(element)) {
			return StringConverter.asString(DEFAULT_COLORS.get(element)
					.getRGB());
		}
		else {
			return DEFAULT_COLOR_STRING;
		}
	}

	public static String getPreferenceName(String string) {
		return Activator.PLUGIN_ID + "." + string + ".color";
	}

	public static void setColorForModelElement(Color color,
			IWebflowModelElement element) {
		setColorToPreferences(element.getClass(), color.getRGB());
		classToColorMapCache.put(element.getClass(), color);
	}

	private static void setColorToPreferences(Class element, RGB rgb) {
		try {
			IScopeContext context = new InstanceScope();
			IEclipsePreferences node = context.getNode(Activator.PLUGIN_ID);

			node.put(getPreferenceName(element.getName()), StringConverter
					.asString(rgb));
			node.flush();
		}
		catch (BackingStoreException e) {
		}
	}
}
