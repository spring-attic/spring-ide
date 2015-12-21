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
package org.springframework.ide.eclipse.config.tests.util;

import static org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable.syncExec;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.hamcrest.Matchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.exceptions.QuickFixNotFoundException;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.matchers.AbstractMatcher;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.results.WidgetResult;
import org.eclipse.swtbot.swt.finder.utils.MessageFormat;
import org.eclipse.swtbot.swt.finder.utils.Position;
import org.eclipse.swtbot.swt.finder.waits.WaitForObjectCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.eclipse.ui.IEditorReference;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.springframework.ide.eclipse.config.tests.util.gef.StsBotGefEditor;
import org.springframework.ide.eclipse.config.ui.editors.SpringConfigEditor;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Steffen Pingel
 */
public class StsBotConfigEditor extends SWTBotEditor {

	private final SWTBotStyledText styledText;

	private final SpringConfigEditor configEditor;

	/** The widget inside the editor, this may not be a styledtext. */
	private Widget widget;

	public StsBotConfigEditor(IEditorReference editorReference, SWTWorkbenchBot bot) throws WidgetNotFoundException {
		super(editorReference, bot);
		configEditor = (SpringConfigEditor) editorReference.getEditor(false);
		this.styledText = new SWTBotStyledText(configEditor.getTextViewer().getTextWidget());
	}

	private void activateAutoCompleteShell() {
		invokeAction("ContentAssistProposal");
	}

	/**
	 * This activates the popup shell.
	 * 
	 * @return The shell.
	 */
	private Shell activatePopupShell() {
		System.err.println("Activating quickfix shell."); //$NON-NLS-1$
		try {
			final Shell mainWindow = syncExec(new WidgetResult<Shell>() {
				public Shell run() {
					return styledText.widget.getShell();
				}
			});
			final List<Shell> shells = bot.shells("", mainWindow);
			final Shell popup = syncExec(new WidgetResult<Shell>() {
				public Shell run() {
					for (Shell shell : shells) {
						if (shell.getChildren().length > 0 && shell.getChildren()[0] instanceof Table) {
							shell.setFocus();
							shell.forceActive();
							shell.forceFocus();
							return shell;
						}
					}
					return null;
				}
			});
			return popup;
		}
		catch (Exception e) {
			throw new QuickFixNotFoundException("Quickfix popup not found. Giving up.", e); //$NON-NLS-1$
		}
	}

	private void activateQuickFixShell() {
		invokeAction("QuickAssist");
	}

	private WaitForObjectCondition<SWTBotTable> autoCompleteAppears(Matcher<SWTBotTable> tableMatcher) {
		return new WaitForObjectCondition<SWTBotTable>(tableMatcher) {
			@Override
			protected List<SWTBotTable> findMatches() {
				try {
					activateAutoCompleteShell();
					SWTBotTable autoCompleteTable = getProposalTable();
					if (matcher.matches(autoCompleteTable)) {
						System.err.println("matched table, returning");
						return Arrays.asList(autoCompleteTable);
					}
				}
				catch (Throwable e) {
					makeProposalsDisappear();
				}
				return null;
			}

			public String getFailureMessage() {
				return "Could not find auto complete proposal using matcher " + matcher;
			}
		};
	}

	/**
	 * Auto completes the given proposal.
	 * 
	 * @param insertText the text to be inserted before activating the
	 * auto-complete.
	 * @param proposalText the auto-completion proposal to select from the list.
	 */
	public void autoCompleteProposal(String insertText, String proposalText) {
		typeText(insertText);
		WaitForObjectCondition<SWTBotTable> autoCompleteTable = autoCompleteAppears(tableWithRow(proposalText));
		waitUntil(autoCompleteTable);
		selectProposal(autoCompleteTable.get(0), proposalText);
	}

	/**
	 * Gets the background color of the widget.
	 * 
	 * @return the background color on the widget, or <code>null</code> if the
	 * widget is not an instance of {@link Control}.
	 * @since 1.3
	 * @see org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot#backgroundColor()
	 */
	public Color backgroundColor() {
		return styledText.backgroundColor();
	}

	/**
	 * Gets the context menu in the editor.
	 * 
	 * @param text the context menu item.
	 * @return the menu
	 * @throws WidgetNotFoundException if the menu with the specified text could
	 * not be found.
	 * @see org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot#contextMenu(java.lang.String)
	 */
	public SWTBotMenu contextMenu(String text) throws WidgetNotFoundException {
		return styledText.contextMenu(text);
	}

	/**
	 * Gets the current position of the cursor. The returned position will
	 * contain a 0-based line and column.
	 * 
	 * @return the position of the cursor.
	 * @see SWTBotStyledText#cursorPosition()
	 */
	public Position cursorPosition() {
		return styledText.cursorPosition();
	}

	/**
	 * Gets the foreground color of the widget.
	 * 
	 * @return the foreground color on the widget, or <code>null</code> if the
	 * widget is not an instance of {@link Control}.
	 * @since 1.3
	 * @see org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot#foregroundColor()
	 */
	public Color foregroundColor() {
		return styledText.foregroundColor();
	}

	/**
	 * Gets the auto completion proposal matching the given text..
	 * 
	 * @param insertText the proposal text to type before auto completing
	 * @return the list of proposals
	 * @throws TimeoutException if the autocomplete shell did not close in time.
	 * @since 1.2
	 */
	@SuppressWarnings("all")
	public List<String> getAutoCompleteProposals(String insertText) {
		typeText(insertText);
		WaitForObjectCondition<SWTBotTable> autoCompleteAppears = autoCompleteAppears(tableWithRowIgnoringCase(insertText));
		waitUntil(autoCompleteAppears);
		final SWTBotTable autoCompleteTable = autoCompleteAppears.get(0);
		List<String> proposals = getRows(autoCompleteTable);
		makeProposalsDisappear();
		return proposals;
	}

	/**
	 * @return the bullet on the current line.
	 * @see SWTBotStyledText#getBulletOnCurrentLine()
	 */
	public Bullet getBulletOnCurrentLine() {
		return styledText.getBulletOnCurrentLine();
	}

	/**
	 * @param line the line number, 0 based.
	 * @return the bullet on the given line.
	 * @see SWTBotStyledText#getBulletOnLine(int)
	 */
	public Bullet getBulletOnLine(int line) {
		return styledText.getBulletOnLine(line);
	}

	/**
	 * @return the editor reference for this view.
	 */
	public IEditorReference getEditorReference() {
		return partReference;
	}

	/**
	 * Gets the color of the background on the specified line.
	 * 
	 * @param line the line number, 0 based.
	 * @return the RGB of the line background color of the specified line.
	 * @since 1.3
	 * @see SWTBotStyledText#getLineBackground(int)
	 */
	public RGB getLineBackground(int line) {
		return styledText.getLineBackground(line);
	}

	/**
	 * Gets the number of lines in the {@link StyledText}.
	 * 
	 * @return the number of lines in the {@link StyledText}.
	 */
	public int getLineCount() {
		return styledText.getLineCount();
	}

	/**
	 * Gets all the lines in the editor.
	 * 
	 * @return the lines in the editor.
	 */
	public List<String> getLines() {
		return styledText.getLines();
	}

	/**
	 * Gets the quick fix table.
	 * 
	 * @param proposalShell the shell containing the quickfixes.
	 * @return the table containing the quickfix.
	 */
	private SWTBotTable getProposalTable() {
		System.err.println("Finding table containing proposals.");
		try {
			Table table = bot.widget(widgetOfType(Table.class), activatePopupShell());
			SWTBotTable swtBotTable = new SWTBotTable(table);
			System.err.println(MessageFormat.format("Found table containing proposals -- {0}", getRows(swtBotTable)));
			return swtBotTable;
		}
		catch (Exception e) {
			throw new QuickFixNotFoundException("Quickfix options not found. Giving up.", e); //$NON-NLS-1$
		}
	}

	/**
	 * Finds all the quickfixes in the quickfix list.
	 * 
	 * @return the list of all available quickfixes.
	 * @since 1.2
	 */
	public List<String> getQuickFixes() {
		List<String> proposals = null;
		WaitForObjectCondition<SWTBotTable> quickFixTableCondition = quickFixAppears(any(SWTBotTable.class));
		waitUntil(quickFixTableCondition);
		SWTBotTable quickFixTable = quickFixTableCondition.get(0);
		proposals = getRows(quickFixTable);
		return proposals;
	}

	/**
	 * Gets the quick fix item count.
	 * 
	 * @return the number of quickfix items in the quickfix proposals.
	 * @since 1.2
	 */
	public int getQuickfixListItemCount() {
		WaitForObjectCondition<SWTBotTable> quickFixTableCondition = quickFixAppears(any(SWTBotTable.class));
		waitUntil(quickFixTableCondition);
		SWTBotTable quickFixTable = quickFixTableCondition.get(0);
		return quickFixTable.rowCount();
	}

	private List<String> getRows(SWTBotTable table) {
		int rowCount = table.rowCount();
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < rowCount; i++) {
			result.add(table.cell(i, 0));
		}
		return result;
	}

	/**
	 * Gets the current selection.
	 * 
	 * @return The selected string.
	 */
	public String getSelection() {
		return styledText.getSelection();
	}

	/**
	 * Gets the style text.
	 * 
	 * @param line the line number, 0 based.
	 * @param column the column number, 0 based.
	 * @return the {@link StyleRange} at the specified location
	 * @see SWTBotStyledText#getStyle(int, int)
	 */
	public StyleRange getStyle(int line, int column) {
		return styledText.getStyle(line, column);
	}

	/**
	 * @return the styledText
	 */
	public SWTBotStyledText getStyledText() {
		return styledText;
	}

	/**
	 * @param line the line number, 0 based.
	 * @param column the column number, 0 based.
	 * @param length the length.
	 * @return the styles in the specified range.
	 * @see SWTBotStyledText#getStyles(int, int, int)
	 */
	public StyleRange[] getStyles(int line, int column, int length) {
		return styledText.getStyles(line, column, length);
	}

	/**
	 * Gets the text of this object's widget.
	 * 
	 * @return the text on the styledtext.
	 * @see org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot#getText()
	 */
	public String getText() {
		return styledText.getText();
	}

	/**
	 * @return the text on the current line, without the line delimiters.
	 * @see SWTBotStyledText#getTextOnCurrentLine()
	 */
	public String getTextOnCurrentLine() {
		return styledText.getTextOnCurrentLine();
	}

	/**
	 * @param line the line number, 0 based.
	 * @return the text on the given line number, without the line delimiters.
	 * @see SWTBotStyledText#getTextOnLine(int)
	 */
	public String getTextOnLine(int line) {
		return styledText.getTextOnLine(line);
	}

	/**
	 * Gets the tooltip of this object's widget.
	 * 
	 * @return the tooltip on the widget.
	 * @see org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot#getToolTipText()
	 * @since 1.3
	 */
	public String getToolTipText() {
		return styledText.getToolTipText();
	}

	/**
	 * The parent widget inside the partReference, that is the tabFolder for all
	 * controls within the view. If you want to look for a particular widget
	 * within the part, this is a good place to start searching for the widget.
	 * <p>
	 * <b>NOTE:</b> Clients must ensure that the view is active at the time of
	 * making this call. If the view is not active, then this method will throw
	 * a {@link WidgetNotFoundException}.
	 * </p>
	 * 
	 * @return the parent widget in the view.
	 * @see #findWidget(org.hamcrest.Matcher)
	 * @see #assertActive()
	 * @see #show()
	 */
	@Override
	public Widget getWidget() {
		show();
		if (widget == null) {
			widget = findWidget(any(Widget.class));
		}
		return widget;
	}

	/**
	 * @return <code>true</code> if the styledText has a bullet on the given
	 * line, <code>false</code> otherwise.
	 * @see SWTBotStyledText#hasBulletOnCurrentLine()
	 */
	public boolean hasBulletOnCurrentLine() {
		return styledText.hasBulletOnCurrentLine();
	}

	/**
	 * @param line the line number, 0 based.
	 * @return <code>true</code> if the styledText has a bullet on the given
	 * line, <code>false</code> otherwise.
	 * @see SWTBotStyledText#hasBulletOnLine(int)
	 */
	public boolean hasBulletOnLine(int line) {
		return styledText.hasBulletOnLine(line);
	}

	/**
	 * @param line the line number, 0 based.
	 * @param column the column number, 0 based.
	 * @param text the text to be inserted at the specified location
	 * @see SWTBotStyledText#insertText(int, int, java.lang.String)
	 */
	public void insertText(int line, int column, String text) {
		styledText.insertText(line, column, text);
	}

	/**
	 * @param text the text to be inserted at the location of the caret.
	 * @see SWTBotStyledText#insertText(java.lang.String)
	 */
	public void insertText(String text) {
		styledText.insertText(text);
	}

	private void invokeAction(final String actionId) {
		final IAction action = configEditor.getSourcePage().getAction(actionId);
		syncExec(new VoidResult() {
			public void run() {
				System.err.println(MessageFormat.format("Activating action with id {0}", actionId));
				action.run();
			}
		});
	}

	/**
	 * Gets if the object's widget is enabled.
	 * 
	 * @return <code>true</code> if the widget is enabled.
	 * @see org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBot#isEnabled()
	 */
	public boolean isEnabled() {
		return styledText.isEnabled();
	}

	private void makeProposalsDisappear() {
		// clear away all content assists for next retry.
		System.err.println("Making proposals disappear.");
		setFocus();
	}

	/**
	 * @param line the line number, 0 based.
	 * @param column the column number, 0 based.
	 * @see SWTBotStyledText#navigateTo(int, int)
	 */
	public void navigateTo(int line, int column) {
		styledText.navigateTo(line, column);
	}

	/**
	 * Sets the caret at the specified location.
	 * 
	 * @param position the position of the caret.
	 */
	public void navigateTo(Position position) {
		styledText.navigateTo(position);
	}

	/**
	 * Notifies of the keyboard event.
	 * <p>
	 * FIXME need some work for CTRL|SHIFT + 1 the 1 is to be sent as '!' in
	 * this case.
	 * </p>
	 * 
	 * @param modificationKeys the modification keys.
	 * @param c the character.
	 * @see Event#character
	 * @see Event#stateMask
	 * @deprecated use {@link #pressShortcut(int, char)} instead. This api will
	 * be removed.
	 */
	@Deprecated
	public void notifyKeyboardEvent(int modificationKeys, char c) {
		styledText.notifyKeyboardEvent(modificationKeys, c);
	}

	/**
	 * Notifies of keyboard event.
	 * 
	 * @param modificationKeys the modification key.
	 * @param c the character.
	 * @param keyCode any special keys (function keys, arrow or navigation keys
	 * etc.)
	 * @see Event#keyCode
	 * @see Event#character
	 * @see Event#stateMask
	 * @deprecated use {@link #pressShortcut(int, int, char)} instead. This api
	 * will be removed.
	 */
	@Deprecated
	public void notifyKeyboardEvent(int modificationKeys, char c, int keyCode) {
		styledText.notifyKeyboardEvent(modificationKeys, c, keyCode);
	}

	/**
	 * Presses the shortcut specified by the given keys.
	 * 
	 * @param modificationKeys the combination of {@link SWT#ALT} |
	 * {@link SWT#CTRL} | {@link SWT#SHIFT} | {@link SWT#COMMAND}.
	 * @param c the character.
	 * @see Keyboard#pressShortcut(KeyStroke...)
	 * @see Keystrokes#toKeys(int, char)
	 */
	public void pressShortcut(int modificationKeys, char c) {
		styledText.pressShortcut(modificationKeys, c);
	}

	/**
	 * Presses the shortcut specified by the given keys.
	 * 
	 * @param modificationKeys the combination of {@link SWT#ALT} |
	 * {@link SWT#CTRL} | {@link SWT#SHIFT} | {@link SWT#COMMAND}.
	 * @param keyCode the keyCode, these may be special keys like F1-F12, or
	 * navigation keys like HOME, PAGE_UP
	 * @param c the character
	 * @see Keystrokes#toKeys(int, char)
	 */
	public void pressShortcut(int modificationKeys, int keyCode, char c) {
		styledText.pressShortcut(modificationKeys, keyCode, c);
	}

	/**
	 * Presses the shortcut specified by the given keys.
	 * 
	 * @param keys the keys to press
	 * @see Keyboard#pressShortcut(KeyStroke...)
	 * @see Keystrokes
	 */
	public void pressShortcut(KeyStroke... keys) {
		styledText.pressShortcut(keys);
	}

	/**
	 * Applys a quick fix item at the given index.
	 * 
	 * @param quickFixIndex the index of the quickfix item to apply.
	 * @throws WidgetNotFoundException if the quickfix could not be found.
	 */
	public void quickfix(int quickFixIndex) {
		WaitForObjectCondition<SWTBotTable> quickFixTableCondition = quickFixAppears(any(SWTBotTable.class));
		waitUntil(quickFixTableCondition);
		SWTBotTable quickFixTable = quickFixTableCondition.get(0);
		selectProposal(quickFixTable, quickFixIndex);
	}

	/**
	 * Applys a quick fix item with the given name.
	 * 
	 * @param quickFixName the name of the quick fix to apply.
	 */
	public void quickfix(String quickFixName) {
		WaitForObjectCondition<SWTBotTable> quickFixTable = quickFixAppears(tableWithRow(quickFixName));
		waitUntil(quickFixTable);
		selectProposal(quickFixTable.get(0), quickFixName);
	}

	private WaitForObjectCondition<SWTBotTable> quickFixAppears(Matcher<SWTBotTable> tableMatcher) {
		return new WaitForObjectCondition<SWTBotTable>(tableMatcher) {
			@Override
			protected List<SWTBotTable> findMatches() {
				try {
					syncExec(new VoidResult() {
						public void run() {
							styledText.widget.getShell().forceActive();
							styledText.widget.getShell().forceFocus();
						}
					});
					activateQuickFixShell();
					SWTBotTable quickFixTable = getProposalTable();
					if (matcher.matches(quickFixTable)) {
						return Arrays.asList(quickFixTable);
					}
				}
				catch (Throwable e) {
					e.printStackTrace();
					makeProposalsDisappear();
				}
				return null;
			}

			public String getFailureMessage() {
				return "Could not find auto complete proposal using matcher " + matcher;
			}

		};
	}

	/**
	 * Selects the text on the current line.
	 * 
	 * @see SWTBotStyledText#selectCurrentLine()
	 * @since 1.1
	 */
	public void selectCurrentLine() {
		styledText.selectCurrentLine();
	}

	/**
	 * @param line the line number to select, 0 based.
	 * @see SWTBotStyledText#selectLine(int)
	 * @since 1.1
	 */
	public void selectLine(int line) {
		styledText.selectLine(line);
	}

	/**
	 * Applies the specified quickfix.
	 * 
	 * @param proposalTable the table containing the quickfix.
	 * @param proposalIndex the index of the quickfix.
	 */
	private void selectProposal(final SWTBotTable proposalTable, final int proposalIndex) {
		System.err.println(MessageFormat.format("Trying to select proposal with index {0}", proposalIndex)); //$NON-NLS-1$
		UIThreadRunnable.asyncExec(new VoidResult() {
			public void run() {
				Table table = proposalTable.widget;
				System.err.println(MessageFormat.format(
						"Selecting row [{0}] {1} in {2}", proposalIndex, table.getItem(proposalIndex).getText(), //$NON-NLS-1$
						table));
				table.setSelection(proposalIndex);
				Event event = new Event();
				event.type = SWT.Selection;
				event.widget = table;
				event.item = table.getItem(proposalIndex);
				table.notifyListeners(SWT.Selection, event);
				table.notifyListeners(SWT.DefaultSelection, event);
			}
		});
	}

	/**
	 * Attempst to applys the quick fix.
	 * <p>
	 * FIXME: this needs a lot of optimization.
	 * </p>
	 * 
	 * @param proposalTable the table containing the quickfix.
	 * @param proposalText the name of the quickfix to apply.
	 */
	private void selectProposal(SWTBotTable proposalTable, String proposalText) {
		System.err.println(MessageFormat.format("Trying to select proposal {0}", proposalText)); //$NON-NLS-1$
		if (proposalTable.containsItem(proposalText)) {
			selectProposal(proposalTable, proposalTable.indexOf(proposalText));
			return;
		}
		throw new QuickFixNotFoundException("Quickfix options not found. Giving up."); //$NON-NLS-1$
	}

	/**
	 * @param line the line number, 0 based.
	 * @param column the column number, 0 based.
	 * @param length the length of the selection.
	 * @see SWTBotStyledText#selectRange(int, int, int)
	 */
	public void selectRange(int line, int column, int length) {
		styledText.selectRange(line, column, length);
	}

	/**
	 * @see SWTBotStyledText#setFocus()
	 */
	@Override
	public void setFocus() {
		styledText.setFocus();
	}

	/**
	 * @param text the text to set.
	 * @see SWTBotStyledText#setText(java.lang.String)
	 */
	public void setText(String text) {
		styledText.setText(text);
	}

	private Matcher<SWTBotTable> tableWithRow(final String itemText) {
		return new AbstractMatcher<SWTBotTable>() {

			public void describeTo(Description description) {
				description.appendText("table with item (").appendText(itemText).appendText(")");
			}

			@Override
			protected boolean doMatch(Object item) {
				return ((SWTBotTable) item).containsItem(itemText);
			}
		};
	}

	private Matcher<SWTBotTable> tableWithRowIgnoringCase(final String itemText) {
		final String lowerCaseText = itemText.toLowerCase();
		return new AbstractMatcher<SWTBotTable>() {

			public void describeTo(Description description) {
				description.appendText("table with item (").appendText(itemText).appendText(")");
			}

			@Override
			protected boolean doMatch(Object item) {
				List<String> rows = getRows((SWTBotTable) item);
				for (String row : rows) {
					if (row.toLowerCase().startsWith(lowerCaseText)) {
						return true;
					}
				}
				return false;
			}
		};
	}

	public StsBotGefEditor toGefEditorFromUri(String uri) {
		return new StsBotGefEditor(getEditorReference(), this.bot);
	}

	/**
	 * @param line the line number, 0 based.
	 * @param column the column number, 0 based.
	 * @param text the text to be typed at the specified location
	 * @see SWTBotStyledText#typeText(int, int, java.lang.String)
	 * @since 1.0
	 */
	public void typeText(int line, int column, String text) {
		styledText.typeText(line, column, text);
	}

	/**
	 * @param text the text to be typed at the location of the caret. *
	 * @see SWTBotStyledText#typeText(java.lang.String)
	 * @since 1.0
	 */
	public void typeText(String text) {
		styledText.typeText(text);
	}

	/**
	 * @param text the text to be typed at the location of the caret.
	 * @param interval the interval between consecutive key strokes.
	 * @see SWTBotStyledText#typeText(java.lang.String, int)
	 * @since 1.0
	 */
	public void typeText(String text, int interval) {
		styledText.typeText(text, interval);
	}

	private void waitUntil(WaitForObjectCondition<SWTBotTable> table) {
		bot.waitUntil(table, 20000);
	}

}
