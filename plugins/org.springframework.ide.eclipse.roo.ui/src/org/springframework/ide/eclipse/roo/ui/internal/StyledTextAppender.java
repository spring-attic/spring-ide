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
package org.springframework.ide.eclipse.roo.ui.internal;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.themes.IThemeManager;
import org.springframework.util.StringUtils;

/**
 * Appender implementation that appends text to a given {@link StyledText}
 * widget.
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Leo Dos Santos
 * @since 2.1.0
 */
public class StyledTextAppender {

	public static final String NL = System.getProperty("line.separator");

	private static final String ROO_PROMPT = "roo> ";

	private static final Pattern FILE_PATTERN = Pattern.compile("(.*?)((\\S*\\|+)*(\\bSRC_|\\bROOT|\\bSPRING_CONFIG_ROOT).*/.*\\.\\w*)( .*)?");

	// TODO e3.5 replace by SWT.UNDERLINE_LINK
	protected static final int SWT_UNDERLINE_LINK = 4;

	private final StyledText text;

	private boolean messageReceived = false;

	private String lastMessage = "";

	private static final Object LOCK = new Object();

	private boolean hasPresentation = false;

	public StyledTextAppender(final StyledText text) {
		this.text = text;
	}

	public boolean hasPrompt() {
		synchronized (LOCK) {
			String msg = text.getText();
			return msg.endsWith(ROO_PROMPT) || msg.endsWith(ROO_PROMPT + NL);
		}
	}

	private void initializePresentation() {
		if (RooUiColors.getColorRegistry() != null) {
			RooUiColors.getColorRegistry().addListener(PRESENTATION_LISTENER);
		}
		if (RooUiColors.getFontRegistry() != null) {
			RooUiColors.getFontRegistry().addListener(PRESENTATION_LISTENER);
		}
		
		text.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
		
		RooUiColors.applyShellBackground(text);
		RooUiColors.applyShellFont(text);
		
		hasPresentation = true;
	}
	
	public void dispose() {
		if (RooUiColors.getColorRegistry() != null) {
			RooUiColors.getColorRegistry().removeListener(PRESENTATION_LISTENER);
		}
		if (RooUiColors.getFontRegistry() != null) {
			RooUiColors.getFontRegistry().removeListener(PRESENTATION_LISTENER);
		}
	}

	public void append(final String message, final Integer level) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				synchronized (LOCK) {

					if (text.isDisposed() || message == null) {
						return;
					}

					// Set initial font and colours and add listener to
					// registries - only needs to be done once
					if (!hasPresentation) {
						initializePresentation();
					}

					// Clear out the text buffer before first Roo message will
					// be presented
					if (!messageReceived) {
						messageReceived = true;
						text.setText("");
					}

					boolean isPrompt = message.endsWith(ROO_PROMPT + NL);

					// Check that we that don't display messages that have just
					// been displayed
					if (lastMessage.equals(message) && !isPrompt) {
						return;
					}

					String trimmedMessage = message;
					if (isPrompt) {
						trimmedMessage = message.substring(0, message.length() - NL.length());
						// If we previously had a prompt as well add a line
						// break to move to next line
						if (lastMessage.equals(message)) {
							trimmedMessage = NL + trimmedMessage;
						}
					}

					// Remove leading line break
					if (trimmedMessage.startsWith(NL) && !lastMessage.endsWith(ROO_PROMPT + NL)) {
						trimmedMessage = trimmedMessage.substring(1);
					}

					// Keep track of last message
					lastMessage = message;

					text.setRedraw(false);
					int startIndex = text.getCharCount();
					text.append(trimmedMessage);
					int endIndex = text.getCharCount();

					// Install hyperlink if message has the appropriate
					// structure
					Matcher matcher = getHyperlinkPattern().matcher(StringUtils.replace(trimmedMessage, NL, "").replace('\\',
							'/'));
					if (matcher.matches()) {
						String prefix = matcher.group(1);
						String file = matcher.group(2);
						String appendix = matcher.group(5);

						StyleRange style = new StyleRange();
						style.start = startIndex;
						style.length = prefix.length();
						style.foreground = RooUiColors.getColor(level);
						text.setStyleRange(style);

						StyleRange link = new StyleRange();
						link.start = startIndex + prefix.length();
						link.length = file.length();
						link.underlineColor = null;
						link.underlineStyle = SWT_UNDERLINE_LINK;
						link.underline = true;
						setData(link, file);
						text.setStyleRange(link);
						
						if (appendix != null && appendix.length() > 0) {
							StyleRange ending = new StyleRange();
							ending.start = startIndex + prefix.length() + file.length();
							ending.length = appendix.length();
							ending.foreground = RooUiColors.getColor(level);
							text.setStyleRange(style);
						}
					}
					else {
						// Get a different shell coloring for the roo shell
						// prompt and path
						if (!isPrompt && trimmedMessage.length() > 0) {
							StyleRange style = new StyleRange();
							style.start = startIndex;
							style.length = endIndex - startIndex;
							style.foreground = RooUiColors.getColor(level);
							text.setStyleRange(style);
						}
						else {
							int length = trimmedMessage.length() - ROO_PROMPT.length();
							if (length > 0) {
								StyleRange style = new StyleRange();
								style.start = startIndex;
								style.length = length;
								style.foreground = RooUiColors.getColor(Level.WARNING.intValue());
								text.setStyleRange(style);
							}
							StyleRange style = new StyleRange();
							style.start = startIndex + length;
							style.length = endIndex - (startIndex + length);
							style.foreground = RooUiColors.getPromptColor();
							text.setStyleRange(style);
						}
					}
					text.setRedraw(true);
					text.setTopIndex(text.getLineCount() - 1);
				}
			}

		});
	}
	
	public Pattern getHyperlinkPattern() {
		return FILE_PATTERN;
	}

	// TODO e3.5 replace by object.data
	public static Object getData(TextStyle style) {
		Field field;
		try {
			field = TextStyle.class.getDeclaredField("data");
			if (field != null) {
				return field.get(style);
			}
		}
		catch (Exception e) {
			// ignore, not supported on Eclipse 3.4
		}
		return null;
	}

	// TODO e3.5 replace by object.data
	public static void setData(TextStyle style, Object data) {
		Field field;
		try {
			field = TextStyle.class.getDeclaredField("data");
			if (field != null) {
				field.set(style, data);
			}
		}
		catch (Exception e) {
			// ignore, not supported on Eclipse 3.4
		}
	}

	private IPropertyChangeListener PRESENTATION_LISTENER = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			if (IThemeManager.CHANGE_CURRENT_THEME.equals(event.getProperty())
					|| RooUiColors.COLOR_BACKGROUND_SHELL.equals(event.getProperty())) {
				RooUiColors.applyShellBackground(text);
			}
			if (IThemeManager.CHANGE_CURRENT_THEME.equals(event.getProperty())
					|| RooUiColors.FONT_SHELL.equals(event.getProperty())) {
				RooUiColors.applyShellFont(text);
			}
		}

	};

}
