package org.springframework.ide.eclipse.boot.wizard.starters;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.wizard.BootWizardActivator;
import org.springframework.ide.eclipse.boot.wizard.InitializrFactoryModel;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.utils.ProjectFilter;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.utils.SelectionUtils;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class AddStartersWizard extends Wizard implements IWorkbenchWizard {

	private InitializrFactoryModel<AddStartersModel> fmodel;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		try {
			IPreferenceStore preferenceStore = BootActivator.getDefault().getPreferenceStore();
			List<IProject> projects = SelectionUtils.getProjects(selection, ProjectFilter.anyProject);
			if (projects!=null && !projects.isEmpty()) {
				IProject project = projects.get(0);
				fmodel = new InitializrFactoryModel<>((url) -> {
					if (url!=null) {
						InitializrService initializr = InitializrService.create(BootActivator.getUrlConnectionFactory(), () -> url);
						SpringBootCore core = new SpringBootCore(initializr);
						return new AddStartersModel(
								project,
								core,
								preferenceStore
						);
					}
					return null;
				});
			}
		} catch (Exception e) {
			MessageDialog.openError(workbench.getActiveWorkbenchWindow().getShell(), "Error opening the wizard",
					ExceptionUtil.getMessage(e)+"\n\n"+
					"Note that this wizard uses a webservice and needs internet access.\n"+
					"A more detailed error message may be found in the Eclipse error log."
			);
			//Ensure exception is logged. (Eclipse UI may not log it!).
			BootWizardActivator.log(e);
			throw new Error(e);
		}
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(new DependencyPage(fmodel));
		addPage(new CompareGeneratedAndCurrentPage(fmodel));
	}

}
