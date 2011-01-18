/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model;

import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessor;

/**
 * @author Christian Dupuis
 * @since 2.2.5
 */
public interface IBeansConfigEventListener {
	
	/**
	 * Event indicating the start of internal reading of the {@link IBeansConfig}.
	 * @param config the {@link IBeansConfig} that starts reading
	 */
	void onReadStart(IBeansConfig config);
	
	/**
	 * Event indicating the end of internal reading of the {@link IBeansConfig}.
	 * @param config the {@link IBeansConfig} that ended reading
	 */
	void onReadEnd(IBeansConfig config);
	
	/**
	 * Event indicating that the {@link IBeansConfig}'s internal structure has been reset.
	 * @param config the {@link IBeansConfig} that was reset
	 */
	void onReset(IBeansConfig config);
	
	/**
	 * Event indicating that the {@link IBeansConfig} has detected a new {@link IBeansConfigPostProcessor.
	 * @param config the {@link IBeansConfig} that detected the post processor
	 * @param configPostProcessor the post processor instance
	 */
	void onPostProcessorDetected(IBeansConfig config, IBeansConfigPostProcessor configPostProcessor);

	/**
	 * Event indicating that the {@link IBeansConfig} has detected a removed {@link IBeansConfigPostProcessor.
	 * @param config the {@link IBeansConfig} that detected the post processor
	 * @param configPostProcessor the post processor instance
	 */
	void onPostProcessorRemoved(IBeansConfig config, IBeansConfigPostProcessor configPostProcessor);
}
