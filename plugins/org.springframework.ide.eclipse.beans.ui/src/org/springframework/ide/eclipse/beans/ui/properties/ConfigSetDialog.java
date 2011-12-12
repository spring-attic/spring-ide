/*******************************************************************************
 * Copyright (c) 2004, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig.Type;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IProfileAwareBeansComponent;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesConfigSet;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesProject;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.util.StringUtils;

/**
 * Dialog for creating a beans config set.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 * @author Terry Denney
 */
public class ConfigSetDialog extends Dialog {

	private static final String PREFIX = "ConfigSetDialog.";

	private static final String TITLE_NEW = PREFIX + "title.new";
	private static final String TITLE_EDIT = PREFIX + "title.edit";

	private static final String ERROR_INVALID_NAME = PREFIX
			+ "error.invalidName";
	private static final String ERROR_USED_NAME = PREFIX + "error.usedName";

	private static final String NAME_LABEL = PREFIX + "name.label";
	private static final String PROFILE_LABEL = PREFIX + "profiles.label";
	private static final String OVERRIDE_LABEL = PREFIX + "override.label";
	private static final String INCOMPLETE_LABEL = PREFIX + "incomplete.label";
	private static final String VIEWER_LABEL = PREFIX + "viewer.label";
	private static final String SELECT_ALL_LABEL = PREFIX + "select.all.label";
	private static final String DESELECT_ALL_LABEL = PREFIX + "deselect.all.label";
	

	private static final int LIST_VIEWER_WIDTH = 400;
	private static final int LIST_VIEWER_HEIGHT = 250;

	private CLabel messageLabel;
	
	private Text nameText;
	private Text profilesText; 
	
	private Button overrideButton;
	private Button incompleteButton;
	private CheckboxTableViewer configsViewer;
	private Label errorLabel;
	private Button okButton;

	private PropertiesProject project;
	private PropertiesConfigSet configSet;
	private String title;

	private enum Mode { NEW, EDIT };
	private Mode mode;

	public ConfigSetDialog(Shell parent, PropertiesProject project,
			String configSetName, IBeansConfigSet.Type type) {
		super(parent);
		this.project = project;
		if (configSetName == null) {
			configSet = new PropertiesConfigSet(project, (String) null, type);
			title = BeansUIPlugin.getResourceString(TITLE_NEW);
			mode = Mode.NEW;
		} else {
			configSet = (PropertiesConfigSet) project.getConfigSet(configSetName);
			title = BeansUIPlugin.getResourceString(TITLE_EDIT);
			mode = Mode.EDIT;
		}
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		// Create group composite for options
		Composite optionsGroup = new Composite(composite, SWT.NULL);
		optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout = new GridLayout();
		optionsGroup.setLayout(layout);

		messageLabel = new CLabel(optionsGroup, SWT.NONE);
		messageLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		// Create labeled name text field
		nameText = SpringUIUtils.createTextField(optionsGroup, BeansUIPlugin
				.getResourceString(NAME_LABEL), 0, 0, 50);
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateName();
			}
		});
		
		// Create labeled profiles text field
		profilesText = SpringUIUtils.createTextField(optionsGroup, BeansUIPlugin
				.getResourceString(PROFILE_LABEL), 0, 0, 50);
		profilesText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateProfiles();
			}
		});
		profilesText.setText(StringUtils.collectionToDelimitedString(configSet.getProfiles(), ", "));
		createProfilesContentAssist();
		
		Label options = new Label(optionsGroup, SWT.WRAP);
		options.setText("Options:");
		options.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Create labeled checkboxes
		overrideButton = SpringUIUtils.createCheckBox(optionsGroup,
				BeansUIPlugin.getResourceString(OVERRIDE_LABEL));
		overrideButton.setSelection(configSet
				.isAllowBeanDefinitionOverriding());
		incompleteButton = SpringUIUtils.createCheckBox(optionsGroup,
				BeansUIPlugin.getResourceString(INCOMPLETE_LABEL));
		incompleteButton.setSelection(configSet.isIncomplete());
		
		// Create config set list viewer
		Label viewerLabel = new Label(composite, SWT.NONE);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL);
		viewerLabel.setLayoutData(gd);
		viewerLabel.setText(BeansUIPlugin.getResourceString(VIEWER_LABEL));

		configsViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = LIST_VIEWER_WIDTH;
		gd.heightHint = LIST_VIEWER_HEIGHT;

		configsViewer.getTable().setLayoutData(gd);
		configsViewer.setContentProvider(new ConfigFilesContentProvider(
				createConfigList()));
		configsViewer.setLabelProvider(new PropertiesModelLabelProvider());
		configsViewer.setSorter(new ConfigFilesSorter());
		configsViewer.setInput(this); // activate content provider
		configsViewer.setCheckedElements(configSet.getConfigs().toArray());
		configsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				validateProfiles();
			}
		});
		
		// Create select all and deselect all buttons
		Composite buttonsGroup = new Composite(composite, SWT.NULL);
		buttonsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginTop = 0;
		layout.marginBottom = 10;
		buttonsGroup.setLayout(layout);
		buttonsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		SpringUIUtils.createButton(buttonsGroup, BeansUIPlugin
				.getResourceString(SELECT_ALL_LABEL), new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				configsViewer.setAllChecked(true);
			}
		});

		SpringUIUtils.createButton(buttonsGroup, BeansUIPlugin
				.getResourceString(DESELECT_ALL_LABEL), new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				configsViewer.setAllChecked(false);
			}
		});

		// Create error label
		errorLabel = new Label(composite, SWT.NONE);
		errorLabel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		errorLabel.setForeground(JFaceColors.getErrorText(parent.getDisplay()));
		errorLabel.setBackground(JFaceColors.getErrorBackground(parent
				.getDisplay()));
		applyDialogFont(composite);
		return composite;
	}

	private Set<String> getDefinedProfiles(IBeansComponent component, String filter, Set<String> existingProfiles) {
		Set<String> profiles = new HashSet<String>();
		if (component instanceof IProfileAwareBeansComponent) {
			Set<String> currentProfiles = ((IProfileAwareBeansComponent) component).getProfiles();
			for(String profile: currentProfiles) {
				if (profile.startsWith(filter) && !existingProfiles.contains(profile)) {
					profiles.add(profile);
				}
			}
		}
		
		Set<IBeansComponent> children = component.getComponents();
		for(IBeansComponent child: children) {
			profiles.addAll(getDefinedProfiles(child, filter, existingProfiles));
		}
		
		return profiles;
	}
	
	private class ProfileContentProposal implements IContentProposal {
		
		private final String profile;
		
		private final int offset;

		private final int cursorPosition;

		public ProfileContentProposal(String profile, int offset, int cursorPosition) {
			this.profile = profile;
			this.offset = offset;
			this.cursorPosition = cursorPosition;
		}
		
		public String getLabel() {
			return profile;
		}
		
		public String getDescription() {
			Set<IBeansConfig> configs = getDefiningConfigForProfile(profile);
			Set<String> configNames = new HashSet<String>();
			for(IBeansConfig config: configs) {
				configNames.add(config.getElementName());
			}
			
			return "Profile \"" + profile + "\" defined in " + StringUtils.collectionToDelimitedString(configNames, ", ");
		}
		
		public int getCursorPosition() {
			return offset + profile.length();
		}
		
		public String getContent() {
			return profile.substring(cursorPosition - offset, profile.length());
		}
		
		public String getProfile() {
			return profile;
		}
	}
	
	private void createProfilesContentAssist() {
		IContentProposalProvider proposalProvider = new IContentProposalProvider() {
			
			public IContentProposal[] getProposals(String contents, final int position) {
				String prefix = contents.substring(0, position);
				String filter = prefix;
				Set<String> existingProfiles = new HashSet<String>();
				
				int separatorIndex = -1;
				while(filter.contains(",")) {
					separatorIndex = prefix.indexOf(",");
					if (separatorIndex >= 0) {
						existingProfiles.add(prefix.substring(0, separatorIndex));
						filter = prefix.substring(separatorIndex + 1);
						while(filter.startsWith(" ")) {
							separatorIndex++;
							filter = filter.substring(1);
						}
					}
				}
				
				Set<IBeansConfig> configs = project.getConfigs();
				Set<String> profiles = new HashSet<String>();
				for(IBeansConfig config: configs) {
					Set<IBeansComponent> components = config.getComponents();
					for(IBeansComponent component: components) {
						profiles.addAll(getDefinedProfiles(component, filter, existingProfiles));
					}
				}
				
				List<IContentProposal> proposals = new ArrayList<IContentProposal>();
				for(final String profile: profiles) {
					proposals.add(new ProfileContentProposal(profile, separatorIndex + 1, position));
				}

				Collections.sort(proposals, new Comparator<IContentProposal>() {
					public int compare(IContentProposal o1, IContentProposal o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}
				});
				return proposals.toArray(new IContentProposal[proposals.size()]);
			}
		};
		try {
			KeyStroke keyStroke = KeyStroke.getInstance("Ctrl+Space");
			ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(profilesText, new TextContentAdapter(), proposalProvider, keyStroke, null);
			contentProposalAdapter.addContentProposalListener(new IContentProposalListener() {
				
				public void proposalAccepted(IContentProposal proposal) {
					if (proposal instanceof ProfileContentProposal) {
						ProfileContentProposal profileProposal = (ProfileContentProposal) proposal;
						Set<IBeansConfig> configs = getDefiningConfigForProfile(profileProposal.getProfile());
						for(IBeansConfig config: configs) {
							if (! configsViewer.getChecked(config)) {
								configsViewer.setChecked(config, true);
							}
						}
						
						validateProfiles();
					}
				}
			});
		} catch (ParseException e) {
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		// Create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		// Do this here because setting the text will set enablement on the
		// ok button
		nameText.setFocus();
		String name = configSet.getElementName();
		if (name != null && name.trim().length() != 0) {
			nameText.setText(name);
			okButton.setEnabled(true);
		} else {
			okButton.setEnabled(false);
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {

			// Remove old config set from project
			if (mode == Mode.EDIT) {
				project.removeConfigSet(configSet.getElementName());
			}

			// Update config set
			configSet.setElementName(nameText.getText());
			configSet.setAllowBeanDefinitionOverriding(overrideButton.getSelection());
			configSet.setIncomplete(incompleteButton.getSelection());
			if (profilesText.getText().length()> 0) {
				String[] profilesSpec = StringUtils.tokenizeToStringArray(profilesText.getText(), BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				Set<String> profiles = new LinkedHashSet<String>();
				for (String profile : profilesSpec) {
					profiles.add(profile);
				}
				configSet.setProfiles(profiles);
			}
			
			// Before removing all configs from this config set keep a copy of
			// the original list of configs in the config set
			Set<IBeansConfig> oldConfigs = new LinkedHashSet<IBeansConfig>(
					configSet.getConfigs());
			configSet.removeAllConfigs();

			// At first add the originally and still selected configs to the
			// config set to preserve their order
			List newConfigs = Arrays.asList(configsViewer.getCheckedElements());
			for (IBeansConfig config : oldConfigs) {
				if (newConfigs.contains(config)) {
					configSet.addConfig(config.getElementName());
				}
			}

			// Finally add the newly selected configs to the config set
			for (Object newConfig : newConfigs) {
				IBeansConfig config = (IBeansConfig) newConfig;
				String configName = config.getElementName();
				if (!configSet.hasConfig(configName)) {
					configSet.addConfig(configName);
				}
			}

			// Read updated or newly created config set
			project.addConfigSet(configSet);
		}
		super.buttonPressed(buttonId);
	}

	private List<IBeansConfig> createConfigList() {

		// Create new list with all config files from this config set
		List<IBeansConfig> configs = new ArrayList<IBeansConfig>(configSet.getConfigs());

		// Add additional configs from the selected and referenced projects
		addConfigsFromReferencedProjects(project, configs, new HashSet<IProject>(), false);

		return configs;
	}

	private void addConfigsFromReferencedProjects(IBeansProject beansProject, List<IBeansConfig> configs, Set<IProject> projects, boolean addProjectPath) {
		if (beansProject == null || projects.contains(beansProject.getProject())) {
			return;
		}
		
		projects.add(beansProject.getProject());
		IBeansModel model = BeansCorePlugin.getModel();
		try {
			for (IBeansConfig config : beansProject.getConfigs()) {
				String projectPath = ModelUtils.getResourcePath(config.getElementParent());
				if (projectPath != null) {

					// Create the full qualified path of the config
					// (with support for configs stored in JAR files)
					// as long as its not the initiating project
					String name = addProjectPath ? projectPath + "/" + config.getElementName() : config.getElementName();
					if (!configSet.hasConfig(name)) {
						configs.add(new BeansConfig(beansProject, name, Type.MANUAL));
					}
				}
			}
			
			// Recursively add configurations to project
			for (IProject proj : beansProject.getProject().getProject().getReferencedProjects()) {
				IBeansProject referencedProj = model.getProject(proj);
				addConfigsFromReferencedProjects(referencedProj, configs, projects, true);
			}
		} catch (CoreException e) {
			// We can't do anything here
		}
	}

	private Set<IBeansConfig> getDefiningConfigForProfile(String profile) {
		Set<IBeansConfig> configs = project.getConfigs();
		Set<IBeansConfig> definingConfigs = new HashSet<IBeansConfig>();
		for (IBeansConfig config : configs) {
			Set<IBeansComponent> components = config.getComponents();
			for(IBeansComponent component: components) {
				Set<String> existingProfiles = Collections.emptySet();
				if (getDefinedProfiles(component, "", existingProfiles).contains(profile)) {
					definingConfigs.add(config);
					break;
				}
			}
		}
		
		return definingConfigs;
	}
	
	private void validateProfiles() {
		if (configsViewer != null) {		
			String[] profiles = StringUtils.tokenizeToStringArray(profilesText.getText(), BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
			List configs = Arrays.asList(configsViewer.getCheckedElements());
			Set<String> unselectedProfiles = new HashSet<String>();
			
			for(String profile: profiles) {
				boolean configSelected = false;
				for (Object obj : configs) {
					IBeansConfig config = (IBeansConfig) obj;
					Set<IBeansComponent> components = config.getComponents();
					for(IBeansComponent component: components) {
						Set<String> existingProfiles = Collections.emptySet();
						if (getDefinedProfiles(component, "", existingProfiles).contains(profile)) {
							configSelected = true;
						}
					}
				}
				
				if (!configSelected) {
					unselectedProfiles.add(profile);
				}
			}
			
			if (unselectedProfiles.size() > 0) {
				messageLabel.setImage(BeansUIImages.getImage(BeansUIImages.IMG_OBJS_WARNING));
				messageLabel.setText("Profiles selected (" + StringUtils.arrayToDelimitedString(unselectedProfiles.toArray(), ", ") + ") are not defined in selected config files.");
				return;
			}

			for(String profile: profiles) {
				Set<IBeansConfig> definingConfigs = getDefiningConfigForProfile(profile);
				for(IBeansConfig definingConfig: definingConfigs) {
					if (! configs.contains(definingConfig)) {
						messageLabel.setImage(BeansUIImages.getImage(BeansUIImages.IMG_OBJS_WARNING));
						messageLabel.setText("Selected config files do not completely define profiles selected.");
						return;
					}
				}
			}
			
		}
		
		messageLabel.setImage(null);
		if (mode == Mode.NEW) {
			messageLabel.setText("Create a new config set.");
		} else {
			messageLabel.setText("Edit config set.");
		}
	}
	
	private void validateName() {
		boolean isEnabled = false;

		String name = nameText.getText();
		if (name == null || name.trim().length() == 0) {
			errorLabel.setText(BeansUIPlugin
					.getResourceString(ERROR_INVALID_NAME));
		} else if (mode == Mode.NEW
				|| !name.equals(configSet.getElementName())) {
			if (project.hasConfigSet(name)) {
				errorLabel.setText(BeansUIPlugin
						.getResourceString(ERROR_USED_NAME));
			} else {
				errorLabel.setText("");
				isEnabled = true;
			}
		} else {
			errorLabel.setText("");
			isEnabled = true;
		}

		okButton.setEnabled(isEnabled);
		errorLabel.getParent().update();
	}

	private static class ConfigFilesContentProvider implements
			IStructuredContentProvider {

		private List<IBeansConfig> configs;

		public ConfigFilesContentProvider(List<IBeansConfig> configs) {
			this.configs = configs;
		}

		public Object[] getElements(Object obj) {
			return configs.toArray();
		}

		public void inputChanged(Viewer viewer, Object oldInput,
				Object newInput) {
		}

		public void dispose() {
		}
	}

	private static class ConfigFilesSorter extends ViewerSorter {

		private enum Category {
			SUB_DIR, ROOT_DIR, OTHER
		};

		@Override
		public int category(Object element) {
			if (element instanceof IBeansConfig) {
				if (((IBeansConfig) element).getElementName()
						.indexOf('/') == -1) {
					return Category.ROOT_DIR.ordinal();
				}
				return Category.SUB_DIR.ordinal();
			}
			return Category.OTHER.ordinal();
		}
	}
}
