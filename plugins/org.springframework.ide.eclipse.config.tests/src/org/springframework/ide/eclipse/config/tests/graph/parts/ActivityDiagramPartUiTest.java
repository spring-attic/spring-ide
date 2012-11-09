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
package org.springframework.ide.eclipse.config.tests.graph.parts;

import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.springframework.ide.eclipse.config.core.schemas.BatchSchemaConstants;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.parts.ActivityDiagramPart;
import org.springframework.ide.eclipse.config.tests.AbstractConfigUiTestCase;
import org.springframework.ide.eclipse.config.tests.util.StsBotConfigEditor;
import org.springframework.ide.eclipse.config.tests.util.gef.EditPartMatcherFactory;
import org.springframework.ide.eclipse.config.tests.util.gef.StsBotGefEditor;

/**
 * @author Leo Dos Santos
 * @author Tomasz Zarna
 */
public class ActivityDiagramPartUiTest extends AbstractConfigUiTestCase {

	private RunningJobsCounter runningJobsListener;

	public void testDropInvalidPart() throws Exception {
		cEditor = openFileInEditor("src/batch-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				AbstractConfigGraphicalEditor page = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
				assertNotNull("Could not load batch-graph page.", page);
				cEditor.setActiveEditor(page);
			}
		});

		StsBotConfigEditor editor = getBot().activeConfigEditor();
		StsBotGefEditor gEditor = editor.toGefEditorFromUri(BatchSchemaConstants.URI);
		List<SWTBotGefEditPart> parts = gEditor.editParts(EditPartMatcherFactory
				.editPartOfType(ActivityDiagramPart.class));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart diagramPart = parts.get(0);
		assertEquals(2, diagramPart.children().size());

		gEditor.activateTool(BatchSchemaConstants.ELEM_SPLIT);
		diagramPart.click();

		gEditor.editParts(EditPartMatcherFactory.editPartOfType(ActivityDiagramPart.class));
		diagramPart = parts.get(0);
		assertEquals(2, diagramPart.children().size());

	}

	public void testDropValidPart() throws Exception {
		runningJobsListener = new RunningJobsCounter();
		Job.getJobManager().addJobChangeListener(runningJobsListener);
		cEditor = openFileInEditor("src/batch-config.xml");
		assertNotNull("Could not open a configuration editor.", cEditor);

		UIThreadRunnable.syncExec(new VoidResult() {
			public void run() {
				AbstractConfigGraphicalEditor page = cEditor.getGraphicalEditorForUri(BatchSchemaConstants.URI);
				assertNotNull("Could not load batch-graph page.", page);
				cEditor.setActiveEditor(page);
			}
		});
		waitForRunningJobsToFinish(runningJobsListener, 5000);

		StsBotConfigEditor editor = getBot().activeConfigEditor();
		StsBotGefEditor gEditor = editor.toGefEditorFromUri(BatchSchemaConstants.URI);
		List<SWTBotGefEditPart> parts = gEditor.editParts(EditPartMatcherFactory
				.editPartOfType(ActivityDiagramPart.class));
		assertFalse(parts.isEmpty());

		SWTBotGefEditPart diagramPart = parts.get(0);
		assertEquals(2, diagramPart.children().size());

		gEditor.activateTool(BatchSchemaConstants.ELEM_JOB);
		diagramPart.click();

		gEditor.editParts(EditPartMatcherFactory.editPartOfType(ActivityDiagramPart.class));
		diagramPart = parts.get(0);
		assertEquals(3, diagramPart.children().size());
	}

	private class RunningJobsCounter implements IJobChangeListener {

		int running = 0;

		private int getRunning() {
			return running;
		}

		public void sleeping(IJobChangeEvent event) {
		}

		public void scheduled(IJobChangeEvent event) {
		}

		public void running(IJobChangeEvent event) {
			running++;
		}

		public void done(IJobChangeEvent event) {
			running--;
		}

		public void awake(IJobChangeEvent event) {
		}

		public void aboutToRun(IJobChangeEvent event) {
		}

	}

	private void waitForRunningJobsToFinish(RunningJobsCounter jobsCounter, long waitLimit) {
		long start = System.currentTimeMillis();
		int running = 1;
		do {
			running = jobsCounter.getRunning();
			if (running > 0) {
				try {
					Thread.sleep(500);
				}
				catch (InterruptedException e) {
					return;
				}
			}
		} while (running > 0 || System.currentTimeMillis() - start > waitLimit);
	}

	@Override
	protected void tearDown() throws Exception {
		if (runningJobsListener != null) {
			Job.getJobManager().removeJobChangeListener(runningJobsListener);
		}
		super.tearDown();
	}

}
