/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa.client;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.prefs.BackingStoreException;

import org.springframework.uaa.client.TransmissionService;
import org.springframework.uaa.client.VersionHelper;
import org.springframework.uaa.client.internal.TransmissionAwareUaaServiceImpl;
import org.springframework.uaa.client.protobuf.UaaClient.FeatureUse;
import org.springframework.uaa.client.protobuf.UaaClient.Product;

/**
 * Extension to the UAA {@link TransmissionAwareUaaServiceImpl} that queues product and feature use 
 * usage records for {@link #BATCH_INTERVAL}
 * @author Christian Dupuis
 * @since 2.6.0
 */
public class QueueingUaaServiceExtension extends TransmissionAwareUaaServiceImpl {

	private static final long BATCH_INTERVAL = 1000L * 60L * 5L; // batch report all registrations every 5mins
	private static final String THREAD_NAME_TEMPLATE = "Queueing Thread (%s/%s.%s.%s)";
	
	/** The internal queue of {@link UsageRecord}s that will be stored until processed by {@link QueuedUsageRecordRunnable}. */
	private final Queue<UsageRecord> usageRecords = new ConcurrentLinkedQueue<UsageRecord>();

	private final Thread queuedUsageRecordsProcessingThread;
	private volatile boolean shouldExit = false;

	public QueueingUaaServiceExtension(TransmissionService transmssionService) {
		super(transmssionService);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				stopQueuedUsageRecordsProcessingThread();
			}
		});

		// Start up scheduling thread
		this.queuedUsageRecordsProcessingThread = new Thread(new QueuedUsageRecordRunnable(), getThreadName(THREAD_NAME_TEMPLATE));
		this.queuedUsageRecordsProcessingThread.setDaemon(true);
		this.queuedUsageRecordsProcessingThread.start();

	}
	
	/**
	 * {@inheritDoc}
	 */
	public void registerFeatureUsage(Product product, FeatureUse feature) {
		registerFeatureUsage(product, feature, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerFeatureUsage(Product product, FeatureUse feature, byte[] featureData) {
		queueFeatureUseUsageRecord(product, feature, featureData);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerProductUsage(Product product) {
		registerProductUsage(product, null, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerProductUsage(Product product, byte[] productData) {
		registerProductUsage(product, productData, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerProductUsage(Product product, byte[] productData, String project) {
		queueProductUsageRecord(product, productData, project);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerProductUsage(Product product, String project) {
		registerProductUsage(product, null, project);
	}

	/**
	 * Stops this instance.
	 */
	public void stop() {
		stopQueuedUsageRecordsProcessingThread();
	}

	private void queueFeatureUseUsageRecord(Product product, FeatureUse feature, byte[] featureData) {
		queueUsageRecord(new FeatureUseUsageRecord(feature, product, featureData));
	}

	private void queueProductUsageRecord(Product product, byte[] productData, String projectId) {
		queueUsageRecord(new ProductUsageRecord(product, projectId, productData));
	}

	private void queueUsageRecord(UsageRecord record) {
		usageRecords.offer(record);
	}

	private String getThreadName(String template) {
		Product uaaProduct = VersionHelper.getUaa();
		return String.format(template, uaaProduct.getName(), uaaProduct.getMajorVersion(),
				uaaProduct.getMinorVersion(), uaaProduct.getPatchVersion());
	}

	/**
	 * Stop the internal queue processing job.
	 */
	private void stopQueuedUsageRecordsProcessingThread() {
		shouldExit = true;
		if (queuedUsageRecordsProcessingThread != null) {
			queuedUsageRecordsProcessingThread.interrupt();
		}
		processQueuedUsageRecords();
	}
	
	/**
	 * Processes the {@link UsageRecord}s stored in {@link #usageRecords}.
	 */
	private void processQueuedUsageRecords() {
		
		// Work on a local copy
		UsageRecord[] records = usageRecords.toArray(new UsageRecord[0]);
		
		// Only process if we have reported usage records
		if (records != null && records.length > 0) {
			
			// Sync changes to the backend storage into the UAA infrastructure
			try { P.sync();	}
			catch (BackingStoreException e) {}
			
			// Process each usage record
			for (UsageRecord record : records) {
				if (record instanceof ProductUsageRecord) {
					super.registerProductUsage(record.getProduct(), ((ProductUsageRecord) record).getProductData(), 
							((ProductUsageRecord) record).getProjectId());
				}
				else if (record instanceof FeatureUseUsageRecord) {
					super.registerFeatureUsage(record.getProduct(), ((FeatureUseUsageRecord) record).getFeatureUse(), 
							((FeatureUseUsageRecord) record).getFeatureData());
				}
				
				// Remove processed records
				usageRecords.remove(record);
			}
			
			// Finally flush changes back into the Preferences API
			try { P.flush(); }
			catch (BackingStoreException e) {}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadChangesFromStorage() {
		// Prevent UaaServiceImpl to control sync and flush from Preferences API
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void persistChangesToStorage() {
		// Prevent UaaServiceImpl to control sync and flush from Preferences API		
	}

	/**
	 * {@link Runnable} that periodically calls {@link QueueingUaaServiceExtension#processQueuedUsageRecords()}. 
	 */
	private class QueuedUsageRecordRunnable implements Runnable {

		/**
		 * {@inheritDoc}
		 */
		public void run() {

			// Wait a defined period; enables testing
			try { Thread.sleep(BATCH_INTERVAL);	}
			catch (InterruptedException e) {}

			while (true) {
				// Test if this thread needs exiting
				if (shouldExit) {
					return;
				}
				
				// Initiate processing of usage records
				processQueuedUsageRecords();
				
				// Eventually wait until next run
				if (!Thread.interrupted() && !shouldExit) {
					try { Thread.sleep(BATCH_INTERVAL);	}
					catch (InterruptedException e) {
						return;
					}
				}
			}
		}
	}
	
}
