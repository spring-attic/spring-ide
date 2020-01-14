package org.springframework.ide.eclipse.boot.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springframework.ide.eclipse.boot.wizard.starters.AddStartersModel;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class CompareGeneratedAndCurrentPage extends WizardPage {

	private final InitializrFactoryModel<AddStartersModel> factoryModel;
	private Composite contentsContainer;
	private Control compareViewer = null;

	public CompareGeneratedAndCurrentPage(InitializrFactoryModel<AddStartersModel> factoryModel) {
		super("Compare", "Compare Generated POM with the current POM", null);
		this.factoryModel = factoryModel;
	}

	@Override
	public void createControl(Composite parent) {
		contentsContainer = new Composite(parent, SWT.NONE);
		contentsContainer.setLayout(GridLayoutFactory.fillDefaults().create());
		setControl(contentsContainer);
	}

	private void setupCompareViewer() {
		try {
			ResourceNode left = new ResourceNode(factoryModel.getModel().getValue().getProject().getProject().getFile("pom.xml"));

			GeneratedInput right = new GeneratedInput("pom.xml", left.getImage(), factoryModel.getModel().getValue()
					.getProject().generatePom(factoryModel.getModel().getValue().dependencies.getCurrentSelection()));

			CompareConfiguration config = new CompareConfiguration();
			config.setLeftLabel(left.getName());
			config.setLeftImage(left.getImage());
			config.setRightLabel(right.getName());
			config.setRightImage(right.getImage());
			config.setLeftEditable(true);

			final CompareEditorInput input = new CompareEditorInput(config) {
				@Override
				protected Object prepareInput(IProgressMonitor arg0)
						throws InvocationTargetException, InterruptedException {
	//				setMessage(message);
					return new DiffNode(left, right);
				}
			};
			input.setTitle("Merge Local Deployment Manifest File");

			new Job("Comparing project pom with generated pom.") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						input.run(monitor);
						return Status.OK_STATUS;
					} catch (InvocationTargetException | InterruptedException e) {
						return ExceptionUtil.coreException(e).getStatus();
					}
				}

			}.schedule();

			compareViewer = input.createContents(contentsContainer);
			compareViewer.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
			contentsContainer.layout();
		} catch (Exception e) {
			Log.log(e);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			adjustCompareViewer();
		}
		super.setVisible(visible);
	}

	private void adjustCompareViewer() {
		if (compareViewer != null) {
			compareViewer.dispose();
		}
		setupCompareViewer();
	}

	private class GeneratedInput implements ITypedElement, IStreamContentAccessor {

		private String name;
		private Image image;
		private String content;

		public GeneratedInput(String name, Image image, String content) {
			super();
			this.name = name;
			this.image = image;
			this.content = content;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Image getImage() {
			return image;
		}

		@Override
		public String getType() {
			return "yml";
		}

		@Override
		public InputStream getContents() throws CoreException {
			return new ByteArrayInputStream(content.getBytes());
		}

	}


}
