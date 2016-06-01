/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.jdt.processors.imports;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.text.correction.ContributedProcessorDescriptor;
import org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor;
import org.eclipse.jdt.internal.ui.text.correction.QuickFixProcessor;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.quickfix.Activator;

/**
 * Helper that adds/removes the JDT {@link QuickFixProcessor} from the
 * {@link JavaCorrectionProcessor} registry in JDT.
 * <p/>
 * This is NOT a utility class. It holds state as it keeps track of the removed
 * JDT quickfix processor, therefore only ONE helper should be created per STS
 * session.
 *
 */
public class JDTQuickFixProcessorHelper {

	private ContributedProcessorDescriptor jdtProcessorDescriptor = null;

	private static JDTQuickFixProcessorHelper instance;

	private JDTQuickFixProcessorHelper() {
		// Only one helper should ever exist in workbench
	}

	public static JDTQuickFixProcessorHelper getInstance() {
		if (instance == null) {
			instance = new JDTQuickFixProcessorHelper();
		}
		return instance;
	}

	/**
	 * Removes the JDT QuickFixProcessor from the
	 * {@link JavaCorrectionProcessor} registry
	 * @param cu
	 * @return true if JDT QuickFix Processor was removed on this call. False if
	 * no action or changes are performed on the JDT quickfix processor registry
	 * (e.g. JDT quickfix processor was already disabled from a previous run)
	 */
	public synchronized boolean removeJDTQuickFixProcessor(ICompilationUnit cu) {

		// The JDT processor was already removed and cached. No further action
		// to perform.
		if (this.jdtProcessorDescriptor != null) {
			return false;
		}

		Class<?> quickFixProcessor = getJavaCorrectionProcessor();
		if (quickFixProcessor != null) {
			try {

				Field correctionProcessor = quickFixProcessor.getDeclaredField("fgContributedCorrectionProcessors");

				if (correctionProcessor != null) {
					correctionProcessor.setAccessible(true);

					Object corrProcessorObj = correctionProcessor.get(null);
					if (corrProcessorObj instanceof ContributedProcessorDescriptor[]) {

						ContributedProcessorDescriptor[] descriptors = (ContributedProcessorDescriptor[]) corrProcessorObj;
						if (descriptors != null) {
							List<ContributedProcessorDescriptor> edited = new ArrayList<ContributedProcessorDescriptor>(
									descriptors.length);
							ContributedProcessorDescriptor foundJDTProcessorToRemove = null;
							for (ContributedProcessorDescriptor desc : descriptors) {
								IQuickFixProcessor curr = (IQuickFixProcessor) desc.getProcessor(cu,
										IQuickFixProcessor.class);
								if (!curr.getClass().getName()
										.equals("org.eclipse.jdt.internal.ui.text.correction.QuickFixProcessor")) {
									edited.add(desc);
								}
								else {
									foundJDTProcessorToRemove = desc;
								}
							}

							if (foundJDTProcessorToRemove != null) {
								this.jdtProcessorDescriptor = foundJDTProcessorToRemove;
								correctionProcessor.set(null, edited.toArray(new ContributedProcessorDescriptor[0]));
								return true;
							}
						}
					}
				}
			}

			catch (SecurityException e) {
				Activator.log(e);
			}
			catch (IllegalAccessException e) {
				Activator.log(e);
			}
			catch (IllegalArgumentException e) {
				Activator.log(e);
			}
			catch (NoSuchFieldException e) {
				Activator.log(e);
			}
		}

		return false;
	}

	/**
	 * Adds the JDT QuickFixProcessor back into the
	 * {@link JavaCorrectionProcessor} registry
	 * @param cu
	 * @return true if the processor was successfully restored. False if no
	 * action was performed on the registry
	 */
	public synchronized boolean addJDTQuickFixProcessor(ICompilationUnit cu) {

		// This restores the JDT Quickfix processor into JDT quickfix processor
		// registry.
		// No action needs to be peformed if there is no JDT processor to
		// restore
		if (jdtProcessorDescriptor == null) {
			return false;
		}

		Class<?> quickFixProcessor = getJavaCorrectionProcessor();
		if (quickFixProcessor != null) {
			try {

				Field correctionProcessor = quickFixProcessor.getDeclaredField("fgContributedCorrectionProcessors");

				if (correctionProcessor != null) {
					correctionProcessor.setAccessible(true);

					Object corrProcessorObj = correctionProcessor.get(null);
					if (corrProcessorObj instanceof ContributedProcessorDescriptor[]) {

						ContributedProcessorDescriptor[] descriptors = (ContributedProcessorDescriptor[]) corrProcessorObj;
						if (descriptors != null) {
							List<ContributedProcessorDescriptor> toEdit = Arrays.asList(descriptors);

							boolean exists = false;
							for (ContributedProcessorDescriptor desc : toEdit) {
								IQuickFixProcessor curr = (IQuickFixProcessor) desc.getProcessor(cu,
										IQuickFixProcessor.class);
								if (desc == jdtProcessorDescriptor || curr.getClass().getName()
										.equals("org.eclipse.jdt.internal.ui.text.correction.QuickFixProcessor")) {
									exists = true;
									break;
								}

							}

							if (!exists && jdtProcessorDescriptor != null) {
								toEdit.add(jdtProcessorDescriptor);
								correctionProcessor.set(null, toEdit.toArray(new ContributedProcessorDescriptor[0]));
								return true;
							}
						}
					}
				}
			}

			catch (SecurityException e) {
				Activator.log(e);
			}
			catch (IllegalAccessException e) {
				Activator.log(e);
			}
			catch (IllegalArgumentException e) {
				Activator.log(e);
			}
			catch (NoSuchFieldException e) {
				Activator.log(e);
			}
		}

		return false;
	}

	protected Class<?> getJavaCorrectionProcessor() {
		Class<?> processor = null;
		Bundle bundle = getBundle();

		if (bundle != null) {
			try {
				processor = bundle.loadClass("org.eclipse.jdt.internal.ui.text.correction.JavaCorrectionProcessor");
			}
			catch (Throwable e) {
				Activator.logInfo(
						"Unable to find the JDT 'JavaCorrectionProcessor' registry. The STS 'Add Imports' quickfix processor may not be available as access to the registry is required to enable this feature.");
			}
		}

		return processor;
	}

	/**
	 *
	 * @return true if JDT UI bundle is found. False otherwise
	 */
	protected Bundle getBundle() {
		Bundle bundle = null;
		try {
			bundle = Platform.getBundle("org.eclipse.jdt.ui");
		}
		catch (Throwable e) {
			Activator.log(e);
		}

		return bundle;
	}
}
