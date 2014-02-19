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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.core.model.IRooInstall;
import org.springframework.ide.eclipse.roo.ui.RooUiActivator;
import org.springframework.roo.shell.eclipse.Bootstrap;
import org.springframework.roo.shell.eclipse.ProjectRefresher;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.core.CommandHistoryProvider;
import org.springsource.ide.eclipse.commons.core.Entry;
import org.springsource.ide.eclipse.commons.core.ICommandHistory;
import org.springsource.ide.eclipse.commons.frameworks.core.internal.commands.ICommandListener;
import org.springsource.ide.eclipse.commons.ui.CommandHistoryPopupList;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;


/**
 * A single shell instance.
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Kris De Volder
 * @since 2.5.0
 */
public class RooShellTab {

	private static final CommandHistoryPopupList.LabelProvider historyLabelProvider = new CommandHistoryPopupList.LabelProvider() {
		@Override
		public String getLabel(Entry entry) {
			return entry.getCommand();
		}
	};

	private static Map<String, String> PATH_MAPPING;

	private StyledTextAppender appender;

	private Bootstrap bootstrap;

	private Text command;

	private final ICommandHistory history = CommandHistoryProvider.getCommandHistory(RooUiActivator.PLUGIN_ID,
			RooCoreActivator.NATURE_ID);

	private String initialCommand;

	private boolean isReady = false;

	private String lastCompletionProposal = null;

	private StyledText text;

	protected IRooInstall install;

	protected final IProject project;

	protected final RooShellView shellView;

	/**
	 * If not null, the shell has been initialized.
	 */
	protected volatile IStatus initializationStatus;

	private enum State {
		CREATED, INITIALIZING, INITIALIZED
	};

	private volatile State state = State.CREATED;

	static {
		PATH_MAPPING = new HashMap<String, String>();
		PATH_MAPPING.put("SRC_MAIN_JAVA", "src/main/java");
		PATH_MAPPING.put("SRC_MAIN_RESOURCES", "src/main/resources");
		PATH_MAPPING.put("SRC_MAIN_WEBAPP", "src/main/webapp");
		PATH_MAPPING.put("SRC_TEST_JAVA", "src/test/java");
		PATH_MAPPING.put("SRC_TEST_RESOURCES", "src/test/resources");
		PATH_MAPPING.put("SPRING_CONFIG_ROOT", "src/main/resources/META-INF/spring");
		PATH_MAPPING.put("ROOT", "");
	}

	public RooShellTab(IProject project, String initialCommand, RooShellView shellView) {
		this.project = project;
		this.shellView = shellView;
		this.initialCommand = initialCommand;
	}

	public void addCommands(ICommandListener listener) {
		new WizardCommandJob(listener);
	}

	public CTabItem addTab(CTabFolder parent) {
		final Composite tabComposite = new Composite(parent, SWT.NONE);
		tabComposite.setFont(parent.getFont());
		GridLayout layout = new GridLayout(1, false);
		if (Platform.getOS().equals(Platform.OS_MACOSX) && Platform.getWS().equals(Platform.WS_CARBON)) {
			layout.marginLeft = -9;
			layout.marginRight = -9;
			layout.marginTop = -9;
			layout.marginBottom = -9;
			layout.horizontalSpacing = -10;
			layout.verticalSpacing = -10;
		}
		else {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;

		}
		tabComposite.setLayout(layout);
		tabComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		text = new StyledText(tabComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
		GridData data = new GridData(GridData.FILL_BOTH);
		text.setLayoutData(data);
		text.setFont(JFaceResources.getTextFont());
		text.setEditable(false);

		RooUiColors.applyShellBackground(text);
		RooUiColors.applyShellForeground(text);
		RooUiColors.applyShellFont(text);
		text.setText("Please stand by until the Roo Shell is completely loaded.\n\r");

		text.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {
				handleMouseUp(event);
			}
		});
		
		new Label(tabComposite, SWT.NONE);

		Composite commandComposite = new Composite(tabComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		commandComposite.setLayout(layout);
		commandComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(commandComposite, SWT.NONE);
		label.setText("roo> ");

		command = new Text(commandComposite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 5;
		command.setLayoutData(data);
		addTypeFieldAssistToText(command);

		command.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				processKeyEvent(text, e, command.getText());
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		CTabItem item = new CTabItem(parent, SWT.CLOSE);
		item.setText(project.getName());
		item.setImage(new WorkbenchLabelProvider().getImage(project));

		item.setControl(tabComposite);
		item.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				shellView.removeTab(project);
				tabComposite.dispose();
				removeTab();
			}
		});

		parent.showItem(item);
		parent.setSelection(parent.indexOf(item));

		setEnabled(false);

		setupBootstrap();

		return item;
	}

	public void addTypeFieldAssistToText(final Text text) {
		int bits = SWT.TOP | SWT.LEFT;
		ControlDecoration controlDecoration = new ControlDecoration(text, bits);
		controlDecoration.setMarginWidth(0);
		controlDecoration.setShowHover(true);
		controlDecoration.setShowOnlyOnFocus(true);
		FieldDecoration contentProposalImage = FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		controlDecoration.setImage(contentProposalImage.getImage());

		// Create the proposal provider
		RooShellProposalProvider proposalProvider = new RooShellProposalProvider(text);
		TextContentAdapter textContentAdapter = new TextContentAdapter();
		final RooContentProposalAdapter adapter = new RooContentProposalAdapter(text, textContentAdapter, proposalProvider,
				KeyStroke.getInstance(SWT.CTRL, SWT.SPACE), null);
		ILabelProvider labelProvider = new LabelProvider();
		adapter.setLabelProvider(labelProvider);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		adapter.setFilterStyle(ContentProposalAdapter.FILTER_NONE);
		adapter.addContentProposalListener(new IContentProposalListener() {

			public void proposalAccepted(IContentProposal proposal) {
				lastCompletionProposal = proposal.getContent();
			}
		});
		text.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				adapter.setPopupSize(new Point(text.getBounds().width, 200));
			}
		});
	}

	public StyledText getText() {
		return text;
	}

	public StyledTextAppender getStyledTextAppender() {
		return appender;
	}

	public boolean isReady() {
		return this.isReady;
	}

	public void removeTab() {
		if (bootstrap != null) {
			final Bootstrap shutdownBootstrap = bootstrap;
			Job shutdownJob = new Job("Shutdown Roo") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					shutdownBootstrap.shutdown();
					return Status.OK_STATUS;
				}
			};
			shutdownJob.schedule();
		}
		bootstrap = null;
	}

	public void executeCommand(String command) {
		new CommandJob(command);

		history.add(new Entry(command, project.getName()));
	}

	private Entry commandHistoryPopup(Control showBelow, Entry[] entries) {
		if (entries.length > 1) {
			CommandHistoryPopupList popup = new CommandHistoryPopupList(showBelow.getShell());
			popup.setLabelProvider(historyLabelProvider);
			popup.setItems(entries);
			return popup.open(showBelow.getDisplay().map(showBelow.getParent(), null, showBelow.getBounds()));
		}
		else if (entries.length == 0) {
			return null;
		}
		else {
			return entries[0];
		}
	}

	private void commandHistoryPopup(Text commandText) {
		List<Entry> entries = history.getRecentValid(ICommandHistory.DEFAULT_MAX_SIZE);
		List<Entry> filteredEntries = new ArrayList<Entry>();
		int counter = 0;

		// filter out empty commands and those that don't start with the given
		// string
		for (Entry entry : entries) {
			if (counter <= 20 && entry.getCommand() != null && entry.getCommand().length() > 0) {
				if (entry.getCommand().startsWith(commandText.getText())) {
					filteredEntries.add(entry);
					counter++;
				}
			}
		}

		Entry chosen = commandHistoryPopup(commandText, filteredEntries.toArray(new Entry[filteredEntries.size()]));
		if (chosen != null) {
			commandText.setText(chosen.getCommand());
			commandText.setSelection(chosen.getCommand().length());
		}
	}

	private void handleMouseUp(Event event) {
		int offset = text.getCaretOffset();
		StyleRange range = offset > 0 ? text.getStyleRangeAtOffset(offset - 1) : null;
		if (range != null) {
			Object data = StyledTextAppender.getData(range);
			if (data instanceof String) {
				String fileName = (String) data;
				int ix = fileName.indexOf('/');
				String newFileName = "";
				int pipe = fileName.indexOf('|');
				
				if (pipe > -1) {
					ix = fileName.indexOf('/', pipe);
					String folder = fileName.substring(pipe + 1, ix);
					newFileName = StringUtils.replace(fileName, folder, PATH_MAPPING.get(folder));
					newFileName = StringUtils.replace(newFileName, "|", "/");
				} else {
					String folder = fileName.substring(0, ix);
					newFileName = StringUtils.replace(fileName, folder, PATH_MAPPING.get(folder));					
				}
				
				IResource resource = project.findMember(newFileName);
				if (resource instanceof IFile) {
					SpringUIUtils.openInEditor((IFile) resource, -1);
				}
			}
		}
	}

	private void setEnabled(final boolean enable) {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (!text.isDisposed()) {
					text.setEnabled(enable);
				}
				if (!command.isDisposed()) {
					command.setEnabled(enable);
				}
				RooShellTab.this.isReady = enable;
			}
		});
	}

	private void setupBootstrap() {
		Job setupJob = new Job("Opening Roo Shell for project '" + project.getName() + "'") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				install = RooCoreActivator.getDefault().getInstallManager().getRooInstall(project);
				appender = new StyledTextAppender(text);

				if (install == null) {
					final Status status = new Status(
							IStatus.ERROR,
							RooUiActivator.PLUGIN_ID,
							"No valid Spring Roo installation configured. Use the 'Roo Support' preference pane to configure available Roo installations.");
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							appender.append(status.getMessage(), Level.SEVERE.intValue());
						}
					});
					initializationStatus = status;
					return Status.OK_STATUS;
				}

				final IStatus status = install.validate();
				if (!status.isOK()) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							appender.append(status.getMessage(), Level.SEVERE.intValue());
						}
					});
					initializationStatus = status;
					return Status.OK_STATUS;
				}

				String projectLocation = null;
				if (project.getLocation() != null) {
					projectLocation = project.getLocation().toOSString();
				}
				else if (project.getRawLocation() != null) {
					projectLocation = project.getRawLocation().toOSString();
				}

				try {
					ProjectRefresher refresher = new ProjectRefresher(project);
					bootstrap = new Bootstrap(projectLocation, install.getHome(), install.getVersion(), refresher);
					bootstrap.start(appender, project.getName());

					setEnabled(true);

					// refresh the project to make sure we are in sync with Roo
					refresher.refresh(null, false);

					if (initialCommand != null) {
						new CommandJob(initialCommand);
					}
					initializationStatus = Status.OK_STATUS;
				}
				catch (Throwable e) {
					SpringCore.log(e);
					initializationStatus = new Status(Status.ERROR, RooCoreActivator.PLUGIN_ID, e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}

		};
		setupJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				state = State.INITIALIZED;
			}
		});
		setupJob.setPriority(Job.INTERACTIVE);

		this.state = State.INITIALIZING;
		setupJob.schedule();
	}

	public IStatus waitForInitialization() {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask("Initializing Roo", IProgressMonitor.UNKNOWN);
						waitForInitialization(monitor);
					}
					finally {
						monitor.done();
					}
				}
			});
			return initializationStatus;
		}
		catch (InvocationTargetException e) {
			Status status = new Status(IStatus.ERROR, RooUiActivator.PLUGIN_ID,
					"Unexpected error during Roo initialization", e);
			RooUiActivator.getDefault().getLog().log(status);
			return status;
		}
		catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
	}

	public IStatus waitForInitialization(IProgressMonitor monitor) throws InterruptedException {
		if (state != State.INITIALIZING && state != State.INITIALIZED) {
			throw new IllegalStateException("Invoke addTab() first");
		}
		if (monitor.isCanceled()) {
			throw new InterruptedException();
		}
		while (initializationStatus == null) {
			try {
				Thread.sleep(200);
			}
			catch (InterruptedException e) {
				// ignore
			}
			if (monitor.isCanceled()) {
				throw new InterruptedException();
			}
		}
		return initializationStatus;
	}

	protected void processKeyEvent(final StyledText text, KeyEvent e, final String commandString) {
		Text sender = (Text) e.widget;
		if (e.character == SWT.CR || e.character == SWT.LF) {
			if (lastCompletionProposal == null || !lastCompletionProposal.equals(commandString)) {
				text.setTopIndex(text.getLineCount() - 1);
				executeCommand(commandString);
				command.setText("");
				e.doit = true;
			}
			else {
				lastCompletionProposal = null;
				e.doit = false;
			}
		}
		else if (e.keyCode == SWT.ARROW_UP) {
			commandHistoryPopup(sender);
			e.doit = false;
		}
		else if (e.keyCode == SWT.ARROW_DOWN) {
			commandHistoryPopup(sender);
			e.doit = false;
		}
	}

	private class CommandJob extends Job {

		private final String commandString;

		public CommandJob(String commandString) {
			super("Execute command '" + commandString + "' on project '" + project.getName() + "'");
			this.commandString = commandString;
			setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
			setPriority(Job.INTERACTIVE);
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			// Wait before we accept the command
			while (!RooShellTab.this.isReady()) {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
			}

			boolean isShutdown = false;
			try {

				final boolean[] done = { false };

				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						if (!appender.hasPrompt()) {
							appender.append(bootstrap.getShellPrompt() + StyledTextAppender.NL, Level.INFO.intValue());
						}
						appender.append(commandString + StyledTextAppender.NL, Level.ALL.intValue());
						done[0] = true;
					}
				});

				if (bootstrap != null) {
					// Wait for the command to be sent to the console
					while (!done[0]) {
						Thread.sleep(100);
					}

					bootstrap.execute(commandString);
					isShutdown = bootstrap.isShutdown();

				}

			}
			catch (Throwable ex) {
				return new Status(Status.ERROR, RooCoreActivator.PLUGIN_ID, ex.getMessage(), ex);
			}

			if (isShutdown) {
				new UiCommands(RooShellTab.this).exit();
			}

			return Status.OK_STATUS;
		}
	}

	private class WizardCommandJob extends Job {

		private final ICommandListener listener;

		public WizardCommandJob(ICommandListener listener) {
			super("");
			this.listener = listener;
			setPriority(Job.INTERACTIVE);
			setSystem(true);
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			// Wait before we accept the command
			while (!RooShellTab.this.isReady()) {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
			}

			bootstrap.addCommand(listener);

			return Status.OK_STATUS;
		}
	}

	private class RooShellContentProposal implements IContentProposal {

		private final String fContent;

		private final String fDescription;

		private final Image fImage;

		private final String fLabel;

		public RooShellContentProposal(String label, String content, String description, Image image) {
			fLabel = label;
			fContent = content;
			fDescription = description;
			fImage = image;
		}

		public String getContent() {
			return fContent;
		}

		public int getCursorPosition() {
			if (fContent != null) {
				return fContent.length();
			}
			return 0;
		}

		public String getDescription() {
			return fDescription;
		}

		@SuppressWarnings("unused")
		public Image getImage() {
			return fImage;
		}

		public String getLabel() {
			return fLabel;
		}

		@Override
		public String toString() {
			return fLabel;
		}
	}

	private class RooShellProposalProvider implements IContentProposalProvider {

		private final Text text;

		public RooShellProposalProvider(Text text) {
			this.text = text;
		}

		public IContentProposal[] getProposals(String contents, int position) {
			List<IContentProposal> proposals = new ArrayList<IContentProposal>();
			List<String> stringProposals = new ArrayList<String>();
			Integer pos = 0;
			String prefix = "";
			try {
				pos = bootstrap.complete(contents, position, stringProposals);
			}
			catch (Throwable e) {
				SpringCore.log(e);
			}

			if (pos > 0) {
				prefix = contents.substring(0, pos);
			}

			if (stringProposals.size() == 1) {
				text.setText(prefix + stringProposals.get(0));
				text.setSelection(text.getText().length());
				return new IContentProposal[0];
			}
			else {
				for (String stringProposal : stringProposals) {
					proposals.add(new RooShellContentProposal(prefix + stringProposal, prefix + stringProposal,
							findDescription(prefix + stringProposal), null));
				}
				return proposals.toArray(new IContentProposal[proposals.size()]);
			}
		}

		private String findDescription(String proposal) {
			int i = 0;
			String description = proposal;
			for (Map.Entry<String, String> entry : bootstrap.getCommandDescription().entrySet()) {
				if (proposal.startsWith(entry.getKey()) && entry.getKey().length() >= i) {
					description = entry.getValue();
					i = entry.getKey().length();
				}
			}
			System.out.println("description = "+description);
			return description;
		}
	}

	public Bootstrap getBootstrap() {
		return bootstrap;
	}

}
